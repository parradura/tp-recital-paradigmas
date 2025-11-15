package com.grupo_rho.domain.exception;

import com.grupo_rho.domain.Cancion;
import com.grupo_rho.domain.RolTipo;

import lombok.Getter;

@Getter
public class NoHayArtistasDisponiblesException extends DomainException {

    private final Cancion cancion;
    private final RolTipo rolFaltante;

    public NoHayArtistasDisponiblesException(Cancion cancion, RolTipo rolFaltante) {
        super(String.format(
                "No hay artistas disponibles para cubrir el rol %s en la canción '%s'",
                rolFaltante, cancion.getTitulo()
        ));
        this.cancion = cancion;
        this.rolFaltante = rolFaltante;
    }
}
