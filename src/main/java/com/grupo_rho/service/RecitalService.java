package com.grupo_rho.service;

import com.grupo_rho.domain.recital.*;
import com.grupo_rho.domain.artista.*;
import com.grupo_rho.domain.cancion.*;
import lombok.Getter;

import java.util.ArrayList;
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
}
