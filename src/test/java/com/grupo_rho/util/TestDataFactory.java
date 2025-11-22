package com.grupo_rho.util;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;

import java.util.List;
import java.util.Set;

public class TestDataFactory {

    public static ArtistaBase brianMay() {
        return new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA, RolTipo.COROS),
                Set.of("Queen")
        );
    }

    public static ArtistaBase rogerTaylor() {
        return new ArtistaBase(
                "Roger Taylor",
                Set.of(RolTipo.BATERIA, RolTipo.COROS),
                Set.of("Queen")
        );
    }

    public static ArtistaBase johnDeacon() {
        return new ArtistaBase(
                "John Deacon",
                Set.of(RolTipo.BAJO),
                Set.of("Queen")
        );
    }

    public static ArtistaExterno invitadoQueenBarato() {
        return new ArtistaExterno(
                "Invitado Queen Barato",
                Set.of(RolTipo.VOZ_PRINCIPAL, RolTipo.COROS),
                Set.of("Queen"),
                700.0,
                1,
                null
        );
    }

    public static ArtistaExterno eltonJohn() {
        return new ArtistaExterno(
                "Elton John",
                Set.of(RolTipo.VOZ_PRINCIPAL, RolTipo.PIANO),
                Set.of("Elton John Band"),
                1200.0,
                2,
                TipoRecital.POP
        );
    }

    public static ArtistaExterno davidBowieRock() {
        return new ArtistaExterno(
                "David Bowie",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Tin Machine", "David Bowie"),
                1500.0,
                2,
                TipoRecital.ROCK
        );
    }

    public static ArtistaExterno multiRolistaCaro() {
        return new ArtistaExterno(
                "MultiRolista Caro",
                Set.of(
                        RolTipo.VOZ_PRINCIPAL,
                        RolTipo.GUITARRA_ELECTRICA,
                        RolTipo.BAJO,
                        RolTipo.BATERIA,
                        RolTipo.PIANO,
                        RolTipo.COROS
                ),
                Set.of("Session Musicians"),
                3000.0,
                4,
                null
        );
    }

    public static Cancion somebodyToLove() {
        return new Cancion(
                "Somebody to Love",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.GUITARRA_ELECTRICA),
                        new RolRequerido(RolTipo.BAJO),
                        new RolRequerido(RolTipo.BATERIA),
                        new RolRequerido(RolTipo.PIANO)
                )
        );
    }

    public static Recital recitalBasicoRockConUnaCancion() {
        var brian = brianMay();
        var roger = rogerTaylor();
        var john = johnDeacon();

        var invitado = invitadoQueenBarato();
        var elton = eltonJohn();
        var multi = multiRolistaCaro();

        var cancion = somebodyToLove();

        return new Recital(
                "Recital Test Rock",
                List.of(cancion),
                List.of(brian, roger, john),
                List.of(invitado, elton, multi),
                TipoRecital.ROCK
        );
    }
}
