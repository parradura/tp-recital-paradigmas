package com.grupo_rho.domain.exception;

import com.grupo_rho.domain.artista.ArtistaExterno;

public class ArtistaNoEntrenableException extends DomainException {

    public ArtistaNoEntrenableException(ArtistaExterno artista, String motivo) {
        super(String.format(
                "No se puede entrenar al artista '%s': %s",
                artista.getNombre(), motivo
        ));
    }
}
