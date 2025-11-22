package com.grupo_rho.service;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;
import com.grupo_rho.util.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlanificacionServiceTest {

    @Test
    void contratarParaCancionAsignaBasesYElMasBaratoPorRol() {
        Recital recital = TestDataFactory.recitalBasicoRockConUnaCancion();
        PlanificacionService service = new PlanificacionService(recital);

        Cancion c = recital.getCanciones().get(0); // Somebody to Love

        service.contratarParaCancion(c);

        assertTrue(c.estaCompleta());
        // Base son gratis, invitadoBarato debe estar en VOZ_PRINCIPAL
        assertTrue(
                c.getArtistasAsignados().stream()
                        .anyMatch(a -> a.getNombre().equals("Invitado Queen Barato"))
        );
    }

    @Test
    void contratarParaCancionLanzaExcepcionSiNoHayArtistasParaUnRol() {
        // Canción requiere un rol exótico sin nadie que lo pueda tocar
        Cancion c = new Cancion(
                "Raro",
                List.of(new RolRequerido(RolTipo.COROS))
        );
        // No hay coros en ningún artista externo ni base
        ArtistaBase base = new ArtistaBase("Base", Set.of(RolTipo.BAJO), Set.of("Band"));
        ArtistaExterno ext = new ArtistaExterno(
                "Ext",
                Set.of(RolTipo.BAJO),
                Set.of("Band"),
                1000.0,
                1,
                null
        );
        Recital recital = new Recital(
                "Test sin coros",
                List.of(c),
                List.of(base),
                List.of(ext),
                TipoRecital.ROCK
        );

        PlanificacionService service = new PlanificacionService(recital);

        NoHayArtistasDisponiblesException ex = assertThrows(
                NoHayArtistasDisponiblesException.class,
                () -> service.contratarParaCancion(c)
        );
        assertEquals(c, ex.getCancion());
        assertEquals(RolTipo.COROS, ex.getRolFaltante());
        // Y la canción sigue sin asignaciones
        assertTrue(c.getArtistasAsignados().isEmpty());
    }

    @Test
    void contratarParaTodoElRecitalNoTocaCancionesYaCompletas() {
        // C1 ya completa; C2 incompleta y necesita externos
        ArtistaBase brian = TestDataFactory.brianMay();
        ArtistaBase john = TestDataFactory.johnDeacon();

        ArtistaExterno george = new ArtistaExterno(
                "George",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Wham!"),
                1000.0,
                3,
                null
        );

        Cancion c1 = new Cancion(
                "Ya Completa",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.BAJO)
                )
        );

        // La completamos "a mano"
        c1.asignarArtista(c1.getRolesRequeridos().get(0), george);
        c1.asignarArtista(c1.getRolesRequeridos().get(1), john);
        george.registrarAsignacionEnCancion();

        // C2 todavía falta voz
        Cancion c2 = new Cancion(
                "Faltante",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.BAJO)
                )
        );
        c2.asignarArtista(c2.getRolesRequeridos().get(1), john);

        Recital recital = new Recital(
                "Test global",
                List.of(c1, c2),
                List.of(brian, john),
                List.of(george),
                TipoRecital.POP
        );

        PlanificacionService service = new PlanificacionService(recital);
        service.contratarParaTodoElRecital();

        // c1 debe seguir completa con el mismo artista en VOZ
        assertTrue(c1.estaCompleta());
        assertTrue(
                c1.getArtistasAsignados().stream()
                        .anyMatch(a -> a.getNombre().equals("George"))
        );

        // c2 ahora debe ser completa y haber usado George nuevamente (si tenía cupo)
        assertTrue(c2.estaCompleta());
    }

    @Test
    void entrenarArtistaNoPermitidoSiYaTieneCancionesAsignadas() {
        Recital recital = TestDataFactory.recitalBasicoRockConUnaCancion();
        PlanificacionService service = new PlanificacionService(recital);

        Cancion c = recital.getCanciones().get(0);
        var invitado = recital.getArtistasExternosPool().stream()
                .filter(a -> a.getNombre().equals("Invitado Queen Barato"))
                .findFirst()
                .orElseThrow();

        // Lo asignamos primero a VOZ
        RolRequerido rolVoz = c.getRolesRequeridos().stream()
                .filter(r -> r.getTipoRol() == RolTipo.VOZ_PRINCIPAL)
                .findFirst()
                .orElseThrow();

        c.asignarArtista(rolVoz, invitado);
        invitado.registrarAsignacionEnCancion();

        // Ahora no debería poder entrenarse
        assertFalse(service.puedeEntrenarse(invitado));
        assertThrows(
                ArtistaNoEntrenableException.class,
                () -> service.entrenarArtista(invitado, RolTipo.PIANO)
        );
    }

    @Test
    void contratarParaTodoElRecitalUsaBacktrackingYRespetaMaxCanciones() {
        // Caso sintético pequeño donde un artista tiene maxCanciones limitado
        ArtistaBase brian = TestDataFactory.brianMay();

        ArtistaExterno caroLimitado = new ArtistaExterno(
                "Caro Limitado",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Band"),
                1000.0,
                1,
                null
        );
        ArtistaExterno barato = new ArtistaExterno(
                "Barato",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Band"),
                800.0,
                1,
                null
        );

        Cancion c1 = new Cancion(
                "C1",
                List.of(new RolRequerido(RolTipo.VOZ_PRINCIPAL))
        );
        Cancion c2 = new Cancion(
                "C2",
                List.of(new RolRequerido(RolTipo.VOZ_PRINCIPAL))
        );

        Recital recital = new Recital(
                "Test backtracking",
                List.of(c1, c2),
                List.of(brian),
                List.of(caroLimitado, barato),
                TipoRecital.ROCK
        );

        PlanificacionService service = new PlanificacionService(recital);
        service.contratarParaTodoElRecital();

        // CaroLimitado solo puede tomar 1 canción, el resto debe ir con "Barato"
        long vecesCaro = recital.getCanciones().stream()
                .flatMap(c -> c.getArtistasAsignados().stream())
                .filter(a -> a.getNombre().equals("Caro Limitado"))
                .count();

        long vecesBarato = recital.getCanciones().stream()
                .flatMap(c -> c.getArtistasAsignados().stream())
                .filter(a -> a.getNombre().equals("Barato"))
                .count();

        assertEquals(1, vecesCaro);
        assertEquals(1, vecesBarato);
    }

    @Test
    void contratarParaTodoElRecitalLanzaExcepcionSiNoHayNingunaSolucion() {
        // Ningún artista puede tocar el rol requerido
        Cancion c1 = new Cancion(
                "Imposible",
                List.of(new RolRequerido(RolTipo.PIANO))
        );
        ArtistaExterno ext = new ArtistaExterno(
                "SoloBajo",
                Set.of(RolTipo.BAJO),
                Set.of("Band"),
                500.0,
                1,
                null
        );
        Recital recital = new Recital(
                "Sin solucion",
                List.of(c1),
                List.of(),
                List.of(ext),
                TipoRecital.ROCK
        );

        PlanificacionService service = new PlanificacionService(recital);

        assertThrows(
                NoHayArtistasDisponiblesException.class,
                service::contratarParaTodoElRecital
        );
        assertFalse(c1.estaCompleta());
    }
}
