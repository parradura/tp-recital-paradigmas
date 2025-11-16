package com.grupo_rho.domain.exception;

import com.grupo_rho.domain.Cancion;
import com.grupo_rho.domain.RolTipo;
import lombok.Getter;

@Getter
public class NoHayArtistasDisponiblesException extends RuntimeException {

    private final Cancion cancion;
    private final RolTipo rolFaltante;

    public NoHayArtistasDisponiblesException(Cancion cancion, RolTipo rolFaltante) {
        super("No hay artistas disponibles para el rol " + rolFaltante +
                " en la canción '" + cancion.getTitulo() + "'");
        this.cancion = cancion;
        this.rolFaltante = rolFaltante;
    }
}
