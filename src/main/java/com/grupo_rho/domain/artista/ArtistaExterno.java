package com.grupo_rho.domain.artista;

import com.grupo_rho.domain.recital.TipoRecital;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un artista que necesita ser contratado.
 * Tiene un costo, un límite de canciones y puede ser entrenado.
 */
@Getter
@ToString(callSuper = true)
public class ArtistaExterno extends Artista {
    private static final double FACTOR_AUMENTO_ENTRENAMIENTO = 1.5;
    private static final double FACTOR_DESCUENTO_BANDA = 0.5;
    private double costoBase;
    private final int maxCanciones;
    private int cancionesAsignadasEnRecital;
    @Getter(AccessLevel.NONE)
    private final Set<RolTipo> rolesEntrenados;
    private final TipoRecital tipoRecitalPreferido;

    public ArtistaExterno(String nombre,
                          Set<RolTipo> rolesHistoricos,
                          Set<String> historialBandas,
                          double costoBase,
                          int maxCanciones,
                          TipoRecital tipoRecitalPreferido) {
        super(nombre, rolesHistoricos, historialBandas);
        this.costoBase = costoBase;
        this.maxCanciones = maxCanciones;
        this.cancionesAsignadasEnRecital = 0;
        this.rolesEntrenados = new HashSet<>();
        this.tipoRecitalPreferido = tipoRecitalPreferido;
    }

    public record CostosUnitarios(double costoOriginal,
                                  double costoTrasEntrenamiento,
                                  double costoTrasBandas) {}

    public CostosUnitarios calcularCostosUnitariosPorCancion(List<ArtistaBase> artistasBase) {
        int entrenamientos = this.getRolesEntrenados().size();
        double factorEntrenamiento = Math.pow(FACTOR_AUMENTO_ENTRENAMIENTO, entrenamientos);

        // Costo base original
        double costoOriginal = this.getCostoBase() / factorEntrenamiento;

        // Costo base post entrenamientos
        double costoTrasEntrenamiento = this.getCostoBase();

        // Costo base post entrenamientos con descuento por bandas
        double costoTrasBandas = this.getCostoFinal(artistasBase);

        return new CostosUnitarios(
                costoOriginal,
                costoTrasEntrenamiento,
                costoTrasBandas
        );
    }

    public Set<RolTipo> getRolesEntrenados() {
        return Collections.unmodifiableSet(rolesEntrenados);
    }

    public boolean puedeTomarOtraCancion() {
        return cancionesAsignadasEnRecital < maxCanciones;
    }

    public void registrarAsignacionEnCancion() {
        if (!puedeTomarOtraCancion()) {
            throw new IllegalStateException("Artista " + nombre + " ya alcanzó el máximo de canciones");
        }
        this.cancionesAsignadasEnRecital++;
    }

    public void cancelarAsignacionEnCancion() {
        if (cancionesAsignadasEnRecital > 0) {
            this.cancionesAsignadasEnRecital--;
        }
    }

    public void entrenar(RolTipo nuevoRol) {
        if (!rolesHistoricos.contains(nuevoRol)) {
            rolesHistoricos.add(nuevoRol);
            rolesEntrenados.add(nuevoRol);
            this.costoBase = this.costoBase * FACTOR_AUMENTO_ENTRENAMIENTO;
        }
    }

    /**
     * Calcula el costo final del artista, aplicando descuentos por bandas
     * compartidas e incrementos por entrenamiento.
     * @param artistasBase La lista de artistas base del recital.
     * @return El costo final calculado.
     */
    @Override
    public double getCostoFinal(Iterable<ArtistaBase> artistasBase) {
        double costo = this.costoBase;
        boolean comparteBandaConBase = false;

        for (ArtistaBase base : artistasBase) {
            if (this.comparteBandaCon(base)) {
                comparteBandaConBase = true;
                break;
            }
        }

        if (comparteBandaConBase) {
            costo = costo * FACTOR_DESCUENTO_BANDA;
        }

        return costo;
    }

    @Override
    public int getMaxCanciones() {
        return maxCanciones;
    }

    @Override
    public boolean esExterno() {
        return true;
    }
}
