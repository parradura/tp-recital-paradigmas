package com.grupo_rho.domain;

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

    public Recital(String nombre,
                   List<Cancion> canciones,
                   List<ArtistaBase> artistasBase,
                   List<ArtistaExterno> artistasExternosPool) {
        this.nombre = nombre;
        this.canciones = new ArrayList<>(canciones);
        this.artistasBase = new ArrayList<>(artistasBase);
        this.artistasExternosPool = new ArrayList<>(artistasExternosPool);
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

    public List<RolRequerido> getRolesFaltantesCancion(Cancion c) {
        return c.getRolesFaltantes();
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

    public List<Artista> getArtistasContratados() {
        List<Artista> contratados = new ArrayList<>();
        for (Cancion c : canciones) {
            for (Artista a : c.getArtistasAsignados()) {
                if (a.esExterno() && !contratados.contains(a)) {
                    contratados.add(a);
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
        return total;
    }
}
