package com.grupo_rho.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolRequeridoTest {

    @Test
    void asignarValidaQueArtistaPuedaTocarElRol() {
        ArtistaBase guitarrista = new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        RolRequerido rol = new RolRequerido(RolTipo.GUITARRA_ELECTRICA);
        rol.asignar(guitarrista);

        assertTrue(rol.estaCubierto());
        assertEquals(guitarrista, rol.getArtistaAsignado());
    }

    @Test
    void asignarLanzaErrorSiArtistaNoPuedeTocarElRol() {
        ArtistaBase baterista = new ArtistaBase(
                "Roger Taylor",
                Set.of(RolTipo.BATERIA),
                Set.of("Queen")
        );

        RolRequerido rol = new RolRequerido(RolTipo.GUITARRA_ELECTRICA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> rol.asignar(baterista)
        );

        assertTrue(ex.getMessage().contains("no puede tocar el rol"));
        assertFalse(rol.estaCubierto());
    }

    @Test
    void desasignarLiberaElRol() {
        ArtistaBase bajista = new ArtistaBase(
                "John Deacon",
                Set.of(RolTipo.BAJO),
                Set.of("Queen")
        );

        RolRequerido rol = new RolRequerido(RolTipo.BAJO);
        rol.asignar(bajista);
        assertTrue(rol.estaCubierto());

        rol.desasignar();
        assertFalse(rol.estaCubierto());
        assertNull(rol.getArtistaAsignado());
    }
}
