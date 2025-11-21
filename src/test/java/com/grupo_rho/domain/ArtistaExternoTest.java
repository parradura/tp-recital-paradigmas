package com.grupo_rho.domain;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import org.junit.jupiter.api.Test;

import com.grupo_rho.domain.recital.TipoRecital;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistaExternoTest {

    @Test
    void puedeTomarOtraCancionRespetaMaximoYRegistroAsignaciones() {
        ArtistaExterno externo = new ArtistaExterno(
                "George Michael",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Wham!", "George Michael"),
                1000.0,
                2,
                TipoRecital.POP
        );

        assertTrue(externo.puedeTomarOtraCancion());
        assertEquals(0, externo.getCancionesAsignadasEnRecital());

        externo.registrarAsignacionEnCancion();
        assertTrue(externo.puedeTomarOtraCancion());
        assertEquals(1, externo.getCancionesAsignadasEnRecital());

        externo.registrarAsignacionEnCancion();
        assertFalse(externo.puedeTomarOtraCancion());
        assertEquals(2, externo.getCancionesAsignadasEnRecital());

        // No debería poder registrar una tercera
        assertThrows(IllegalStateException.class, externo::registrarAsignacionEnCancion);
    }

    @Test
    void cancelarAsignacionNoPermiteValoresNegativos() {
        ArtistaExterno externo = new ArtistaExterno(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Banda"),
                500.0,
                3,
                TipoRecital.COUNTRY
        );

        externo.registrarAsignacionEnCancion();
        externo.registrarAsignacionEnCancion();
        assertEquals(2, externo.getCancionesAsignadasEnRecital());

        externo.cancelarAsignacionEnCancion();
        assertEquals(1, externo.getCancionesAsignadasEnRecital());

        externo.cancelarAsignacionEnCancion();
        assertEquals(0, externo.getCancionesAsignadasEnRecital());

        // no debería romper si llamo de más
        externo.cancelarAsignacionEnCancion();
        assertEquals(0, externo.getCancionesAsignadasEnRecital());
    }

    @Test
    void entrenarAgregaRolYAumentaCostoSoloUnaVezPorRol() {
        ArtistaExterno externo = new ArtistaExterno(
                "David Bowie",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Tin Machine", "David Bowie"),
                1000.0,
                2,
                TipoRecital.ROCK
        );

        double costoOriginal = externo.getCostoBase();
        externo.entrenar(RolTipo.PIANO);

        assertTrue(externo.getRolesHistoricos().contains(RolTipo.PIANO));
        assertTrue(externo.getRolesEntrenados().contains(RolTipo.PIANO));
        assertEquals(costoOriginal * 1.5, externo.getCostoBase(), 0.001);

        // entrenar de nuevo el mismo rol NO debe volver a multiplicar
        externo.entrenar(RolTipo.PIANO);
        assertEquals(costoOriginal * 1.5, externo.getCostoBase(), 0.001);
    }

    @Test
    void getRolesEntrenadosEsInmutable() {
        ArtistaExterno externo = new ArtistaExterno(
                "Annie Lennox",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Eurythmics"),
                900.0,
                2,
                TipoRecital.POP
        );

        externo.entrenar(RolTipo.COROS);

        var rolesEntrenados = externo.getRolesEntrenados();
        assertTrue(rolesEntrenados.contains(RolTipo.COROS));
        assertThrows(UnsupportedOperationException.class,
                () -> rolesEntrenados.add(RolTipo.PIANO));
    }

    @Test
    void getCostoFinalAplicaDescuentoPorBandasCompartidas() {
        ArtistaBase brian = new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        ArtistaExterno externo = new ArtistaExterno(
                "Colaborador Queen",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Queen"),
                1000.0,
                3,
                TipoRecital.COUNTRY
        );

        double costoConDescuento = externo.getCostoFinal(Set.of(brian));
        assertEquals(500.0, costoConDescuento, 0.001);
    }

    @Test
    void getCostoFinalSinBandasCompartidasNoTieneDescuento() {
        ArtistaBase brian = new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        ArtistaExterno externo = new ArtistaExterno(
                "Otro Artista",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.POP
        );

        double costo = externo.getCostoFinal(Set.of(brian));
        assertEquals(1000.0, costo, 0.001);
    }
}
