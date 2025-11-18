package com.grupo_rho.domain;

import lombok.ToString;

import java.util.Set;

/**
 * Representa un artista que pertenece a la discográfica (base).
 * No tienen costo y tienen disponibilidad "ilimitada".
 */
@ToString(callSuper = true)
public class ArtistaBase extends Artista {

    public ArtistaBase(String nombre,
                       Set<RolTipo> rolesHistoricos,
                       Set<String> historialBandas) {
        super(nombre, rolesHistoricos, historialBandas);
    }

    @Override
    public double getCostoFinal(Iterable<ArtistaBase> artistasBase) {
        return 0.0;
    }

    @Override
    public int getMaxCanciones() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean esExterno() {
        return false;
    }
}
