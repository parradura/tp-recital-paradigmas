package com.grupo_rho.domain.recital;

import com.grupo_rho.domain.artista.Artista;
import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@ToString
public class Recital {
    private static final double FACTOR_DESCUENTO_ESTRELLA = 0.25;
    @Getter
    private final String nombre;
    private final List<Cancion> canciones;
    private final List<ArtistaBase> artistasBase;
    private final List<ArtistaExterno> artistasExternosPool;
    @Getter
    private final TipoRecital tipoRecital;

    public Recital(String nombre,
                   List<Cancion> canciones,
                   List<ArtistaBase> artistasBase,
                   List<ArtistaExterno> artistasExternosPool,
                   TipoRecital tipoRecital) {
        this.nombre = nombre;
        this.canciones = new ArrayList<>(canciones);
        this.artistasBase = new ArrayList<>(artistasBase);
        this.artistasExternosPool = new ArrayList<>(artistasExternosPool);
        this.tipoRecital = tipoRecital;
    }

    public List<ArtistaBase> getArtistasBase() {
        return Collections.unmodifiableList(artistasBase);
    }

    public List<ArtistaExterno> getArtistasExternosPool() {
        return Collections.unmodifiableList(artistasExternosPool);
    }

    public List<Cancion> getCanciones() {
        return Collections.unmodifiableList(canciones);
    }

    public Map<RolTipo, Integer> getRolesFaltantesCancion(Cancion c) {
        Map<RolTipo, Integer> acumulado = new HashMap<>();

        for (RolRequerido rol : c.getRolesFaltantes()) {
            acumulado.merge(rol.getTipoRol(), 1, Integer::sum);
        }

        return Collections.unmodifiableMap(acumulado);
    }

    public Map<RolTipo, Integer> getRolesFaltantesTotales() {
        Map<RolTipo, Integer> acumulado = new HashMap<>();

        for (Cancion c : canciones) {
            for (RolRequerido rol : c.getRolesFaltantes()) {
                acumulado.merge(rol.getTipoRol(), 1, Integer::sum);
            }
        }

        return Collections.unmodifiableMap(acumulado);
    }

    public List<ArtistaExterno> getArtistasContratados() {
        List<ArtistaExterno> contratados = new ArrayList<>();
        for (Cancion c : canciones) {
            for (Artista a : c.getArtistasAsignados()) {
                if (a.esExterno() && !contratados.contains(a)) {
                    contratados.add((ArtistaExterno) a);
                }
            }
        }
        return Collections.unmodifiableList(contratados);
    }

    /**
     * Calcula un detalle completo de costos del recital (basado en el estado actual del recital):
     * - total sin entrenamientos
     * - aumento por entrenamientos
     * - descuento por compartir bandas con artistas base
     * - descuento por artista estrella
     * - total final
     */
    public CostoRecitalDetalle calcularCostoDetallado() {
        double totalSinEntrenamiento = 0.0;
        double totalTrasEntrenamiento = 0.0;
        double totalTrasBandas = 0.0;

        Map<ArtistaExterno, Double> totalPorExternoDespuesBandas = new HashMap<>();

        for (Cancion c : canciones) {
            for (RolRequerido rol : c.getRolesRequeridos()) {
                Artista artista = rol.getArtistaAsignado();
                if (!(artista instanceof ArtistaExterno ext)) {
                    continue;
                }

                ArtistaExterno.CostosUnitarios costos =
                        ext.calcularCostosUnitariosPorCancion(artistasBase);

                totalSinEntrenamiento   += costos.costoOriginal();
                totalTrasEntrenamiento  += costos.costoTrasEntrenamiento();
                totalTrasBandas         += costos.costoTrasBandas();

                totalPorExternoDespuesBandas.merge(
                        ext,
                        costos.costoTrasBandas(),
                        Double::sum
                );
            }
        }

        double aumentoPorEntrenamientos = totalTrasEntrenamiento - totalSinEntrenamiento;
        double descuentoPorBandas = totalTrasEntrenamiento - totalTrasBandas;

        ArtistaEstrellaInfo estrellaInfo =
                calcularArtistaEstrella(totalPorExternoDespuesBandas);

        double descuentoEstrella = estrellaInfo.descuento();
        double totalFinal = totalTrasBandas - descuentoEstrella;

        return new CostoRecitalDetalle(
                totalSinEntrenamiento,
                aumentoPorEntrenamientos,
                descuentoPorBandas,
                descuentoEstrella,
                totalFinal,
                estrellaInfo.artista()
        );
    }

    /**
     * Determina el artista estrella (si lo hay) y su descuento:
     * - Debe tener tipoRecitalPreferido != null
     * - Debe coincidir con el tipoRecital del recital
     * - Se elige el que MÁS factura (después de descuentos por bandas)
     * - El descuento es el 25% de lo que factura ese artista
     */
    private ArtistaEstrellaInfo calcularArtistaEstrella(
            Map<ArtistaExterno, Double> totalPorExternoDespuesBandas
    ) {
        ArtistaExterno estrella = null;
        double totalEstrella = 0.0;

        for (Map.Entry<ArtistaExterno, Double> entry : totalPorExternoDespuesBandas.entrySet()) {
            ArtistaExterno ext = entry.getKey();
            double totalExterno = entry.getValue();

            if (ext.getTipoRecitalPreferido() == null) {
                continue;
            }
            if (ext.getTipoRecitalPreferido() != this.tipoRecital) {
                continue;
            }

            if (totalExterno > totalEstrella) {
                totalEstrella = totalExterno;
                estrella = ext;
            }
        }

        double descuento = (estrella != null) ? totalEstrella * FACTOR_DESCUENTO_ESTRELLA : 0.0;
        return new ArtistaEstrellaInfo(estrella, totalEstrella, descuento);
    }


    /**
     * Costo total del recital tal como lo usa el algoritmo de backtracking.
     * Es SIEMPRE consistente con calcularCostoDetallado().
     */
    public double getCostoTotalRecital() {
        return calcularCostoDetallado().totalFinal();
    }
}
