package com.grupo_rho.service;

import com.grupo_rho.domain.recital.*;
import com.grupo_rho.domain.artista.*;
import com.grupo_rho.domain.cancion.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecitalService {

    @Getter
    private Recital recital;
    private PlanificacionService planificador;

    public RecitalService(Recital recital) {
        setRecital(recital);
    }

    public void setRecital(Recital recital) {
        this.recital = recital;
        this.planificador = new PlanificacionService(recital);
    }

    public List<Cancion> getCanciones() {
        return recital.getCanciones();
    }

    public Map<RolTipo, Integer> getRolesFaltantesTotales() {
        return recital.getRolesFaltantesTotales();
    }

    public Map<RolTipo, Integer> getRolesFaltantesCancion(Cancion c) {
        return recital.getRolesFaltantesCancion(c);
    }

    public void contratarParaCancion(Cancion c) {
        planificador.contratarParaCancion(c);
    }

    public void contratarParaTodoElRecital() {
        planificador.contratarParaTodoElRecital();
    }

    public boolean puedeEntrenarse(ArtistaExterno artista) {
        return planificador.puedeEntrenarse(artista);
    }

    public void entrenarArtista(ArtistaExterno artista, RolTipo rol) {
        planificador.entrenarArtista(artista, rol);
    }

    public List<ArtistaBase> getArtistasBase() {
        return recital.getArtistasBase();
    }

    public List<ArtistaExterno> getArtistasExternosPool() {
        return recital.getArtistasExternosPool();
    }

    public List<ArtistaExterno> getArtistasContratados() {
        return recital.getArtistasContratados();
    }

    public double getCostoTotalRecital() {
        return recital.getCostoTotalRecital();
    }

    public List<ArtistaExterno> encontrarArtistasEntrenablesParaRol(RolTipo rol) {
        List<ArtistaExterno> resultado = new ArrayList<>();

        for (ArtistaExterno ext : recital.getArtistasExternosPool()) {
            if (ext.getCancionesAsignadasEnRecital() > 0) continue;
            if (ext.getRolesHistoricos().contains(rol)) continue;
            if (ext.getMaxCanciones() <= 0) continue;

            resultado.add(ext);
        }

        return resultado;
    }

    /**
     * Construye la estructura que Prolog necesita:
     * Para cada RolTipo utilizado en el recital, una lista de faltantes por canción.
     *
     * Ejemplo:
     *  VOZ_PRINCIPAL -> [2, 0, 1, 3]
     *  BATERIA       -> [0, 1, 0, 0]
     *
     * La interpretación es:
     *  - En la canción 1 faltan 2 voces principales.
     *  - En la canción 2 no falta ninguna, etc.
     *
     * NO se tienen en cuenta los artistas externos ya contratados.
     * Se calcula como si sólo existieran los artistas base y futuros artistas
     * sin experiencia a entrenar.
     */
    public Map<RolTipo, List<Integer>> construirFaltantesParaProlog() {
        Map<RolTipo, List<Integer>> resultado = new LinkedHashMap<>();

        List<Cancion> canciones = recital.getCanciones();
        List<ArtistaBase> artistasBase = recital.getArtistasBase();

        for (RolTipo rol : RolTipo.values()) {
            List<Integer> faltantesPorCancion = new ArrayList<>();
            long demandaTotalRol = 0;

            for (Cancion c : canciones) {
                // Demanda de este rol en esta canción (cantidad de slots de ese tipo)
                long demanda = c.getRolesRequeridos().stream()
                        .filter(rr -> rr.getTipoRol() == rol)
                        .count();

                demandaTotalRol += demanda;

                // Cuántos artistas base pueden tocar este rol
                long baseDisponibles = artistasBase.stream()
                        .filter(base -> base.puedeTocar(rol))
                        .count();

                long faltan = demanda - baseDisponibles;
                if (faltan < 0) {
                    faltan = 0;
                }

                faltantesPorCancion.add((int) faltan);
            }

            if (demandaTotalRol > 0) {
                resultado.put(rol, faltantesPorCancion);
            }
        }

        return resultado;
    }

    /**
     * BONUS “Arrepentimiento”: quita por completo a un artista externo del recital.
     * - Lo desasigna de todos los roles de todas las canciones.
     * - Actualiza el contador de canciones asignadas del artista.
     * - No lo saca del pool de candidatos: sigue estando disponible para futuras contrataciones.
     */
    public void quitarArtistaDelRecital(ArtistaExterno artista) {
        // Si no tiene nada asignado, no hay nada que hacer
        if (artista.getCancionesAsignadasEnRecital() == 0) {
            return;
        }

        for (Cancion c : recital.getCanciones()) {
            for (RolRequerido rol : c.getRolesRequeridos()) {
                Artista asignado = rol.getArtistaAsignado();
                if (asignado != null && asignado.equals(artista)) {
                    artista.cancelarAsignacionEnCancion();
                    rol.desasignar();
                }
            }
        }
    }
}
