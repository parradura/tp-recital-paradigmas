package com.grupo_rho.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Clase abstracta que representa a un artista.
 * Contiene la información y comportamiento común.
 */
@Getter
public abstract class Artista {
    @EqualsAndHashCode.Include
    @ToString.Include
    protected String nombre;

    protected Set<RolTipo> rolesHistoricos;
    protected Set<String> historialBandas;

    protected Artista(String nombre,
                      Set<RolTipo> rolesHistoricos,
                      Set<String> historialBandas) {
        this.nombre = Objects.requireNonNull(nombre, "nombre obligatorio");
        this.rolesHistoricos = rolesHistoricos != null
                ? new HashSet<>(rolesHistoricos)
                : new HashSet<>();
        this.historialBandas = historialBandas != null
                ? new HashSet<>(historialBandas)
                : new HashSet<>();
    }

    public boolean puedeTocar(RolTipo rol) {
        return rolesHistoricos.contains(rol);
    }

    public boolean comparteBandaCon(Artista otro) {
        for (String banda : this.historialBandas) {
            if (otro.historialBandas.contains(banda)) {
                return true;
            }
        }
        return false;
    }

    public abstract double getCostoFinal(Iterable<ArtistaBase> artistasBase);
    public abstract int getMaxCanciones();
}
