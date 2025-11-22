package com.grupo_rho.service;

import com.grupo_rho.domain.artista.Artista;
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
     * Slot de backtracking: un rol específico de una canción específica
     * que todavía falta cubrir.
     */
    private record RolPendiente(Cancion cancion, RolRequerido rol) {}

    /**
     * Estructura para guardar la mejor solución encontrada.
     */
    private static class MejorSolucion {
        double costoMinimo = Double.MAX_VALUE;
        Map<RolRequerido, ArtistaExterno> asignacion = new HashMap<>();

        boolean tieneSolucion() {
            return costoMinimo < Double.MAX_VALUE;
        }

        void guardarSiEsMejor(double costo, List<RolPendiente> slots) {
            if (costo >= costoMinimo) {
                return;
            }
            this.costoMinimo = costo;
            this.asignacion.clear();

            for (RolPendiente slot : slots) {
                Artista asignado = slot.rol().getArtistaAsignado();
                if (asignado instanceof ArtistaExterno ext) {
                    this.asignacion.put(slot.rol(), ext);
                }
            }
        }
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
        if (faltantes.isEmpty()) return;

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
     * Optimiza globalmente la contratación para todas las canciones del recital,
     * buscando la combinación de asignaciones con menor costo.
     */
    public void contratarParaTodoElRecital() {
        for (Cancion c : recital.getCanciones()) {
            if (!c.estaCompleta()) {
                asignarArtistasBase(c);
            }
        }

        List<RolPendiente> slots = obtenerSlotsVacios();
        if (slots.isEmpty()) {
            return;
        }

        MejorSolucion mejorSolucion = new MejorSolucion();
        backtracking(0, slots, mejorSolucion);

        if (!mejorSolucion.tieneSolucion()) {
            // Tomamos cualquier slot como referencia del problema
            RolPendiente first = slots.getFirst();
            throw new NoHayArtistasDisponiblesException(first.cancion(), first.rol().getTipoRol());
        }

        aplicarMejorSolucion(mejorSolucion);
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

    /**
     * Usa artistas base para cubrir todos los roles posibles de la canción.
     * No afecta a artistas externos.
     */
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

    /**
     * Backtracking sobre la lista de slots:
     *   - En cada slot elegimos un artista externo candidato.
     *   - Cuando cubrimos todos, medimos el costo total del recital.
     *   - Si es mejor, guardamos esa configuración.
     */
    private void backtracking(int indice, List<RolPendiente> slots, MejorSolucion mejorSolucion) {
        // Caso base: cubrimos todos los roles pendientes
        if (indice == slots.size()) {
            double costoTotal = recital.getCostoTotalRecital();
            mejorSolucion.guardarSiEsMejor(costoTotal, slots);
            return;
        }

        // Paso recursivo
        RolPendiente slotActual = slots.get(indice);
        RolRequerido rol = slotActual.rol();
        Cancion cancion = slotActual.cancion();

        List<ArtistaExterno> candidatos = obtenerCandidatosValidos(rol.getTipoRol(), cancion);

        // Probar primero los más baratos ayuda a encontrar antes una buena solución
        candidatos.sort(Comparator.comparingDouble(a -> a.getCostoFinal(recital.getArtistasBase())));

        for (ArtistaExterno artista : candidatos) {
            // asignamos artista a ese rol
            rol.asignar(artista);
            artista.registrarAsignacionEnCancion();

            // siguiente slot
            backtracking(indice + 1, slots, mejorSolucion);

            // deshacemos para seguir probando otras combinaciones
            artista.cancelarAsignacionEnCancion();
            rol.desasignar();
        }
        // Si ningún candidato sirve, esta rama simplemente no llega al caso base.
    }

    /**
     * Aplica la mejor solución encontrada (mapa de RolRequerido -> ArtistaExterno)
     * al estado real del recital.
     */
    private void aplicarMejorSolucion(MejorSolucion solucion) {
        for (Map.Entry<RolRequerido, ArtistaExterno> entry : solucion.asignacion.entrySet()) {
            RolRequerido rol = entry.getKey();
            ArtistaExterno artista = entry.getValue();

            rol.asignar(artista);
            artista.registrarAsignacionEnCancion();
        }
    }

    /**
     * Construye la lista de (Cancion, RolRequerido) que todavía no están cubiertos
     * luego de haber usado artistas base.
     */
    private List<RolPendiente> obtenerSlotsVacios() {
        List<RolPendiente> slots = new ArrayList<>();
        for (Cancion c : recital.getCanciones()) {
            for (RolRequerido rol : c.getRolesFaltantes()) {
                slots.add(new RolPendiente(c, rol));
            }
        }
        return slots;
    }

    /**
     * Artistas externos que pueden tocar el rol, tienen cupo,
     * y no están ya en esa canción (regla: un rol por canción por artista).
     */
    private List<ArtistaExterno> obtenerCandidatosValidos(RolTipo rol, Cancion cancion) {
        List<ArtistaExterno> lista = new ArrayList<>();
        List<Artista> yaAsignados = cancion.getArtistasAsignados();

        for (ArtistaExterno a : recital.getArtistasExternosPool()) {
            if (!a.puedeTocar(rol)) continue;
            if (!a.puedeTomarOtraCancion()) continue;
            if (yaAsignados.contains(a)) continue; // no puede tener otro rol en la misma canción

            lista.add(a);
        }
        return lista;
    }
}
