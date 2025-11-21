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

    public List<ArtistaExterno> getCandidatosParaRol(RolTipo rol) {
        List<ArtistaExterno> candidatos = new ArrayList<>();
        for (ArtistaExterno externo : artistasExternosPool) {
            if (externo.puedeTocar(rol) && externo.puedeTomarOtraCancion()) {
                candidatos.add(externo);
            }
        }
        return candidatos;
    }

    public Map<String, String> getEstadoCanciones() {
        Map<String, String> estado = new LinkedHashMap<>();
        for (Cancion c : canciones) {
            String valor = c.estaCompleta() ? "COMPLETA" : "INCOMPLETA";
            estado.put(c.getTitulo(), valor);
        }
        return estado;
    }

    public double getCostoTotalRecital() {
        double total = 0.0;
        for (Cancion c : canciones) {
            total += c.getCostoTotal(artistasBase);
        }

        total -= descuentoArtistaEstrella();
        return total;
    }

    /**
     * Bonus: un único artista estrella con descuento adicional si su tipo preferido
     * coincide con el tipo del recital.
     *
     * Implementación simple: se toma el artista contratado cuyo total acumulado
     * en el recital sea máximo, siempre que su tipo preferido coincida.
     * A ese total se le aplica 25% de descuento.
     */
    private double descuentoArtistaEstrella(){
        List<ArtistaExterno> externos = getArtistasContratados();
        double maxTotalArtistaEstrella = 0.0;
        for (ArtistaExterno externo : externos) {
            TipoRecital preferido = externo.getTipoRecitalPreferido();
            if (preferido == null || preferido != this.tipoRecital) {
                continue;
            }

            double totalExterno = 0.0;

            for (Cancion cancion : canciones) {
                if (cancion.getArtistasAsignados().contains(externo)) {
                    totalExterno += externo.getCostoFinal(artistasBase);
                }
            }

            if (totalExterno > maxTotalArtistaEstrella) {
                maxTotalArtistaEstrella = totalExterno;
            }
        }

        return maxTotalArtistaEstrella * 0.25;
    }
}
