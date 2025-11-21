package com.grupo_rho.service;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.domain.recital.Recital;

import java.util.*;

/**
 * Responsabilidad: decidir cómo asignar artistas externos a canciones
 * y aplicar entrenamientos según las reglas del dominio.
 */
public class PlanificacionService {

    private final Recital recital;

    public PlanificacionService(Recital recital) {
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
        asignarArtistasBase(c);

        List<RolRequerido> faltantes = new ArrayList<>(c.getRolesFaltantes());
        if (faltantes.isEmpty()) {
            return;
        }

        List<RolRequerido> asignadosEnEstaOperacion = new ArrayList<>();
        Map<ArtistaExterno, Integer> usosNuevosPorArtista = new HashMap<>();

        for (RolRequerido rol : faltantes) {
            ArtistaExterno mejor = buscarMasBaratoParaRol(rol.getTipoRol(), c);
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
     * Valida si el artista puede entrenarse en un nuevo rol
     */
    public boolean puedeEntrenarse(ArtistaExterno artista) {
        return artista.getCancionesAsignadasEnRecital() == 0;
    }

    /**
     * Entrena a un artista externo en un rol nuevo.
     */
    public void entrenarArtista(ArtistaExterno artista, RolTipo rol) {
        if (!puedeEntrenarse(artista)) {
            throw new ArtistaNoEntrenableException(
                    artista,
                    "ya está asignado en al menos una canción del recital"
            );
        }
        artista.entrenar(rol);
    }

    private void asignarArtistasBase(Cancion c) {
        List<RolRequerido> faltantes = new ArrayList<>(c.getRolesFaltantes());

        var yaAsignados = new java.util.HashSet<>(c.getArtistasAsignados());

        for (RolRequerido rol : faltantes) {
            for (ArtistaBase base : recital.getArtistasBase()) {

                if (yaAsignados.contains(base)) continue;

                if (base.puedeTocar(rol.getTipoRol())) {
                    c.asignarArtista(rol, base);

                    yaAsignados.add(base);
                    break;
                }
            }
        }
    }

    private ArtistaExterno buscarMasBaratoParaRol(RolTipo rol, Cancion cancion) {
        ArtistaExterno mejor = null;
        double mejorCosto = Double.MAX_VALUE;

        var yaAsignados = new HashSet<>(cancion.getArtistasAsignados());

        for (ArtistaExterno externo : recital.getArtistasExternosPool()) {
            if (!externo.puedeTocar(rol)) continue;
            if (!externo.puedeTomarOtraCancion()) continue;
            if (yaAsignados.contains(externo)) continue;

            double costo = externo.getCostoFinal(recital.getArtistasBase());
            if (costo < mejorCosto) {
                mejorCosto = costo;
                mejor = externo;
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
