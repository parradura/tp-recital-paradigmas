package com.grupo_rho.domain;

import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;

import java.util.*;

/**
 * Responsabilidad: decidir cómo asignar artistas externos a canciones
 * y aplicar entrenamientos según las reglas del dominio.
 */
public class PlanificadorContrataciones {

    private final Recital recital;

    public PlanificadorContrataciones(Recital recital) {
        this.recital = Objects.requireNonNull(recital);
    }

    /**
     * Contrata artistas externos para cubrir todos los roles faltantes de una canción,
     * eligiendo siempre el artista más barato posible para cada rol.
     * Si no hay artistas disponibles para algún rol:
     *  - rollback de las asignaciones hechas en esta canción
     *  - lanza NoHayArtistasDisponiblesException
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

            c.asignarArtista(rol, mejor);

            mejor.registrarAsignacionEnCancion();
            usosNuevosPorArtista.merge(mejor, 1, Integer::sum);
            asignadosEnEstaOperacion.add(rol);
        }
    }

    /**
     * Intenta contratar artistas externos para todas las canciones del recital.
     * Si alguna canción no se puede cubrir, deja las demás como estén y propaga
     * la excepción.
     */
    public void contratarParaTodoElRecital() {
        for (Cancion c : recital.getCanciones()) {
            if (!c.estaCompleta()) {
                contratarParaCancion(c);
            }
        }
    }

    /**
     * Entrena a un artista externo en un rol nuevo.
     * Regla: no se puede entrenar si ya está contratado para alguna canción.
     */
    public void entrenarArtista(ArtistaExterno artista, RolTipo rol) {
        if (artista.getCancionesAsignadasEnRecital() > 0) {
            throw new ArtistaNoEntrenableException(
                    artista,
                    "ya está asignado en al menos una canción del recital"
            );
        }
        artista.entrenar(rol);
    }

    private ArtistaExterno buscarMasBaratoParaRol(RolTipo rol) {
        ArtistaExterno mejor = null;
        double mejorCosto = Double.MAX_VALUE;

        for (ArtistaExterno externo : recital.getArtistasExternosPool()) {
            if (externo.puedeTocar(rol) && externo.puedeTomarOtraCancion()) {
                double costo = externo.getCostoFinal(recital.getArtistasBase());
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
}
