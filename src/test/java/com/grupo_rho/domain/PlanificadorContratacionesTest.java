package com.grupo_rho.domain;

import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlanificadorContratacionesTest {

    @Test
    void contratarParaCancionEligeArtistaMasBarato() {
        // Base (solo para descuentos, no se usa en contratación)
        ArtistaBase base = new ArtistaBase(
                "Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        // Canción con solo VOZ_PRINCIPAL
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        Cancion cancion = new Cancion("Solo Voz", List.of(voz));

        // Externos: uno caro y uno barato
        ArtistaExterno caro = new ArtistaExterno(
                "Cantante Caro",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                2000.0,
                5,
                TipoRecital.ROCK
        );
        ArtistaExterno barato = new ArtistaExterno(
                "Cantante Barato",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                500.0,
                5,
                TipoRecital.COUNTRY
        );

        Recital recital = new Recital(
                "Recital Test",
                List.of(cancion),
                List.of(base),
                List.of(caro, barato),
                TipoRecital.ROCK
        );

        PlanificadorContrataciones planificador = new PlanificadorContrataciones(recital);

        planificador.contratarParaCancion(cancion);

        Artista asignado = voz.getArtistaAsignado();
        assertNotNull(asignado);
        assertEquals("Cantante Barato", asignado.getNombre());
        assertEquals(1, ((ArtistaExterno) asignado).getCancionesAsignadasEnRecital());
    }

    @Test
    void contratarParaCancionLanzaExcepcionSiNoHayArtistasDisponiblesParaUnRol() {
        ArtistaBase base = new ArtistaBase(
                "Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        Cancion cancion = new Cancion("Solo Voz", List.of(voz));

        // Externo que NO puede cantar voz
        ArtistaExterno soloGuitarra = new ArtistaExterno(
                "Guitarrista Externo",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.COUNTRY
        );

        Recital recital = new Recital(
                "Recital Test",
                List.of(cancion),
                List.of(base),
                List.of(soloGuitarra),
                TipoRecital.ROCK
        );

        PlanificadorContrataciones planificador = new PlanificadorContrataciones(recital);

        assertThrows(
                NoHayArtistasDisponiblesException.class,
                () -> planificador.contratarParaCancion(cancion)
        );

        // No debería quedar nadie asignado ni contador aumentado
        assertFalse(voz.estaCubierto());
        assertEquals(0, soloGuitarra.getCancionesAsignadasEnRecital());
    }

    @Test
    void contratarParaTodoElRecitalRespetaMaximoDeCancionesPorArtista() {
        ArtistaBase base = new ArtistaBase(
                "Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        // Dos canciones, las dos necesitan VOZ_PRINCIPAL
        Cancion c1 = new Cancion("C1", List.of(new RolRequerido(RolTipo.VOZ_PRINCIPAL)));
        Cancion c2 = new Cancion("C2", List.of(new RolRequerido(RolTipo.VOZ_PRINCIPAL)));

        // Un cantante barato pero con maxCanciones = 1, y uno caro con max=2
        ArtistaExterno barato = new ArtistaExterno(
                "Barato",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra"),
                500.0,
                1,
                TipoRecital.POP
        );
        ArtistaExterno caro = new ArtistaExterno(
                "Caro",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra"),
                2000.0,
                2,
                TipoRecital.ROCK
        );

        Recital recital = new Recital(
                "Recital Test",
                List.of(c1, c2),
                List.of(base),
                List.of(barato, caro),
                TipoRecital.ROCK
        );

        PlanificadorContrataciones planificador = new PlanificadorContrataciones(recital);
        planificador.contratarParaTodoElRecital();

        // c1 debería tener al barato, c2 al caro (por límite de canciones)
        Artista asignadoC1 = c1.getRolesRequeridos().getFirst().getArtistaAsignado();
        Artista asignadoC2 = c2.getRolesRequeridos().getFirst().getArtistaAsignado();

        assertEquals("Barato", asignadoC1.getNombre());
        assertEquals("Caro", asignadoC2.getNombre());

        assertEquals(1, barato.getCancionesAsignadasEnRecital());
        assertEquals(1, caro.getCancionesAsignadasEnRecital());
    }

    @Test
    void entrenarArtistaLanzaExcepcionSiYaEstaAsignadoEnAlgunaCancion() {
        ArtistaBase base = new ArtistaBase(
                "Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        Cancion cancion = new Cancion("Test Song", List.of(voz));

        ArtistaExterno externo = new ArtistaExterno(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.POP
        );

        Recital recital = new Recital(
                "Recital Test",
                List.of(cancion),
                List.of(base),
                List.of(externo),
                TipoRecital.POP
        );

        PlanificadorContrataciones planificador = new PlanificadorContrataciones(recital);

        // simulamos que ya está asignado
        voz.asignar(externo);
        externo.registrarAsignacionEnCancion();

        assertThrows(
                ArtistaNoEntrenableException.class,
                () -> planificador.entrenarArtista(externo, RolTipo.PIANO)
        );
    }

    @Test
    void entrenarArtistaFuncionaSiNoEstaAsignadoEnNingunaCancion() {
        ArtistaBase base = new ArtistaBase(
                "Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        Cancion cancion = new Cancion(
                "Test Song",
                List.of(new RolRequerido(RolTipo.VOZ_PRINCIPAL))
        );

        ArtistaExterno externo = new ArtistaExterno(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.COUNTRY
        );

        Recital recital = new Recital(
                "Recital Test",
                List.of(cancion),
                List.of(base),
                List.of(externo),
                TipoRecital.COUNTRY
        );

        PlanificadorContrataciones planificador = new PlanificadorContrataciones(recital);

        planificador.entrenarArtista(externo, RolTipo.PIANO);

        assertTrue(externo.getRolesHistoricos().contains(RolTipo.PIANO));
        assertTrue(externo.getRolesEntrenados().contains(RolTipo.PIANO));
    }
}
