package com.grupo_rho.domain;

import com.grupo_rho.domain.recital.TipoRecital;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa un artista que necesita ser contratado.
 * Tiene un costo, un límite de canciones y puede ser entrenado.
 */
@Getter
@ToString(callSuper = true)
public class ArtistaExterno extends Artista {

    private double costoBase;
    private final int maxCanciones;
    private int cancionesAsignadasEnRecital;
    @Getter(AccessLevel.NONE)
    private final Set<RolTipo> rolesEntrenados;
    private final TipoRecital tipoRecitalPreferido;

    public ArtistaExterno(String nombre,
                          Set<RolTipo> rolesHistoricos,
                          Set<String> historialBandas,
                          double costoBase,
                          int maxCanciones,
                          TipoRecital tipoRecitalPreferido) {
        super(nombre, rolesHistoricos, historialBandas);
        this.costoBase = costoBase;
        this.maxCanciones = maxCanciones;
        this.cancionesAsignadasEnRecital = 0;
        this.rolesEntrenados = new HashSet<>();
        this.tipoRecitalPreferido = tipoRecitalPreferido;
    }

    public Set<RolTipo> getRolesEntrenados() {
        return Collections.unmodifiableSet(rolesEntrenados);
    }

    public boolean puedeTomarOtraCancion() {
        return cancionesAsignadasEnRecital < maxCanciones;
    }

    public void registrarAsignacionEnCancion() {
        if (!puedeTomarOtraCancion()) {
            throw new IllegalStateException("Artista " + nombre + " ya alcanzó el máximo de canciones");
        }
        this.cancionesAsignadasEnRecital++;
    }

    public void cancelarAsignacionEnCancion() {
        if (cancionesAsignadasEnRecital > 0) {
            this.cancionesAsignadasEnRecital--;
        }
    }

    public void entrenar(RolTipo nuevoRol) {
        if (!rolesHistoricos.contains(nuevoRol)) {
            rolesHistoricos.add(nuevoRol);
            rolesEntrenados.add(nuevoRol);
            this.costoBase = this.costoBase * 1.5;
        }
    }

    /**
     * Calcula el costo final del artista, aplicando descuentos por bandas
     * compartidas e incrementos por entrenamiento.
     * @param artistasBase La lista de artistas base del recital.
     * @return El costo final calculado.
     */
    @Override
    public double getCostoFinal(Iterable<ArtistaBase> artistasBase) {
        double costo = this.costoBase;
        boolean comparteBandaConBase = false;

        for (ArtistaBase base : artistasBase) {
            if (this.comparteBandaCon(base)) {
                comparteBandaConBase = true;
                break;
            }
        }

        if (comparteBandaConBase) {
            costo = costo * 0.5;
        }

        return costo;
    }

    @Override
    public int getMaxCanciones() {
        return maxCanciones;
    }

    @Override
    public boolean esExterno() {
        return true;
    }
}
