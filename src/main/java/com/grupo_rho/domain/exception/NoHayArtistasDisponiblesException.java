package com.grupo_rho.domain.exception;

import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.artista.RolTipo;
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
