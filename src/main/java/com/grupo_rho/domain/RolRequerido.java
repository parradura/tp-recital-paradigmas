package com.grupo_rho.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RolRequerido {

    private RolTipo tipoRol;
    private Artista artistaAsignado;

    public RolRequerido(RolTipo tipoRol) {
        this.tipoRol = tipoRol;
    }

    public boolean estaCubierto() {
        return artistaAsignado != null;
    }

    public void asignar(Artista artista) {
        if (artista == null) {
            throw new IllegalArgumentException("El artista no puede ser null");
        }
        if (!artista.puedeTocar(this.tipoRol)) {
            throw new IllegalArgumentException("El artista " + artista.getNombre()
                    + " no puede tocar el rol " + tipoRol);
        }
        this.artistaAsignado = artista;
    }

    public void desasignar() {
        this.artistaAsignado = null;
    }
}
