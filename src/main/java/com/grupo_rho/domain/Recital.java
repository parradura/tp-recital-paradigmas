package com.grupo_rho.domain;

import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@Getter
@ToString
public class Recital {

    private String nombre;
    private List<Cancion> canciones;
    private List<ArtistaBase> artistasBase;
    private List<ArtistaExterno> artistasExternosPool;

    public Recital(String nombre,
                   List<Cancion> canciones,
                   List<ArtistaBase> artistasBase,
                   List<ArtistaExterno> artistasExternosPool) {
        this.nombre = nombre;
        this.canciones = new ArrayList<>(canciones);
        this.artistasBase = new ArrayList<>(artistasBase);
        this.artistasExternosPool = new ArrayList<>(artistasExternosPool);
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

    /**
     * Contrata artistas externos para cubrir todos los roles faltantes de una canción,
     * eligiendo siempre el artista más barato posible para cada rol (greedy).
     *
     * Si no hay artistas disponibles para algún rol:
     * - se hace rollback de las asignaciones hechas en esta canción
     * - se lanza NoHayArtistasDisponiblesException
     */
    public void contratarParaCancion(Cancion c) {
        List<RolRequerido> faltantes = new ArrayList<>(c.getRolesFaltantes());
        if (faltantes.isEmpty()) {
            return;
        }

        List<RolRequerido> asignadosEnEstaOperacion = new ArrayList<>();
        Map<ArtistaExterno, Integer> usosNuevosPorArtista = new HashMap<>();

        for (RolRequerido rol : faltantes) {
            ArtistaExterno mejor = buscarMasBaratoParaRol(rol.getTipoRol());
            if (mejor == null) {
                deshacerAsignaciones(asignadosEnEstaOperacion, usosNuevosPorArtista);
                throw new NoHayArtistasDisponiblesException(c, rol.getTipoRol());
            }

            boolean ok = c.asignarArtista(rol, mejor);
            if (!ok) {
                deshacerAsignaciones(asignadosEnEstaOperacion, usosNuevosPorArtista);
                throw new RuntimeException("No se pudo asignar artista al rol " + rol.getTipoRol());
            }

            mejor.registrarAsignacionEnCancion();
            usosNuevosPorArtista.merge(mejor, 1, Integer::sum);
            asignadosEnEstaOperacion.add(rol);
        }
    }

    private ArtistaExterno buscarMasBaratoParaRol(RolTipo rol) {
        ArtistaExterno mejor = null;
        double mejorCosto = Double.MAX_VALUE;

        for (ArtistaExterno externo : artistasExternosPool) {
            if (externo.puedeTocar(rol) && externo.puedeTomarOtraCancion()) {
                double costo = externo.getCostoFinal(artistasBase);
                if (costo < mejorCosto) {
                    mejorCosto = costo;
                    mejor = externo;
                }
            }
        }

        return mejor;
    }

    private void deshacerAsignaciones(List<RolRequerido> rolesAsignados,
                                      Map<ArtistaExterno, Integer> usosNuevosPorArtista) {
        for (RolRequerido rol : rolesAsignados) {
            rol.desasignar();
        }
        for (Map.Entry<ArtistaExterno, Integer> entry : usosNuevosPorArtista.entrySet()) {
            ArtistaExterno artista = entry.getKey();
            int usos = entry.getValue();
            for (int i = 0; i < usos; i++) {
                artista.cancelarAsignacionEnCancion();
            }
        }
    }

    /**
     * Intenta contratar artistas externos para todas las canciones del recital.
     * Si alguna canción no se puede cubrir, lanza NoHayArtistasDisponiblesException
     */
    public void contratarParaTodoElRecital() {
        for (Cancion c : canciones) {
            if (!c.estaCompleta()) {
                contratarParaCancion(c);
            }
        }
    }

    /**
     * Entrena a un artista externo en un rol nuevo.
     * Regla: no se puede entrenar si ya está contratado para alguna canción.
     */
    public void entrenarArtista(ArtistaExterno a, RolTipo rol) {
        if (estaAsignadoEnAlgunaCancion(a)) {
            throw new ArtistaNoEntrenableException(
                    a,
                    "ya está asignado en al menos una canción del recital"
            );
        }
        a.entrenar(rol);
    }

    private boolean estaAsignadoEnAlgunaCancion(ArtistaExterno artista) {
        for (Cancion c : canciones) {
            for (RolRequerido rol : c.getRolesRequeridos()) {
                Artista asignado = rol.getArtistaAsignado();
                if (artista.equals(asignado)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ArtistaExterno> getArtistasContratados() {
        List<ArtistaExterno> contratados = new ArrayList<>();
        for (Cancion c : canciones) {
            for (Artista a : c.getArtistasAsignados()) {
                if (a instanceof ArtistaExterno externo && !contratados.contains(externo)) {
                    contratados.add(externo);
                }
            }
        }
        return contratados;
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
