package com.grupo_rho.domain;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import org.junit.jupiter.api.Test;

import com.grupo_rho.domain.recital.TipoRecital;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CancionTest {

    @Test
    void detectaRolesFaltantesYComplecion() {
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        RolRequerido guitarra = new RolRequerido(RolTipo.GUITARRA_ELECTRICA);

        Cancion cancion = new Cancion("Test Song", List.of(voz, guitarra));

        assertFalse(cancion.estaCompleta());
        assertEquals(2, cancion.getRolesFaltantes().size());

        ArtistaBase cantante = new ArtistaBase(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Banda")
        );

        ArtistaBase guitarrista = new ArtistaBase(
                "Guitarrista",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Banda")
        );

        cancion.asignarArtista(voz, cantante);
        cancion.asignarArtista(guitarra, guitarrista);

        assertTrue(cancion.estaCompleta());
        assertTrue(cancion.getRolesFaltantes().isEmpty());
    }

    @Test
    void asignarArtistaALRolQueNoPerteneceALaCancionLanzaExcepcion() {
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        Cancion cancion = new Cancion("Test Song", List.of(voz));

        RolRequerido bateria = new RolRequerido(RolTipo.BATERIA);
        ArtistaBase baterista = new ArtistaBase(
                "Baterista",
                Set.of(RolTipo.BATERIA),
                Set.of("Banda")
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cancion.asignarArtista(bateria, baterista)
        );

        assertTrue(ex.getMessage().contains("no pertenece a la canción"));
        assertFalse(voz.estaCubierto());
    }

    @Test
    void noPermiteQueUnArtistaTengaMasDeUnRolEnLaMismaCancion() {
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        RolRequerido coros = new RolRequerido(RolTipo.COROS);

        Cancion cancion = new Cancion("Test Song", List.of(voz, coros));

        ArtistaBase cantante = new ArtistaBase(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL, RolTipo.COROS),
                Set.of("Banda")
        );

        cancion.asignarArtista(voz, cantante);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cancion.asignarArtista(coros, cantante)
        );

        assertTrue(ex.getMessage().contains("ya está asignado a la canción"));
    }

    @Test
    void costoTotalSumaCostosFinalesDeLosArtistasAsignados() {
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        RolRequerido guitarra = new RolRequerido(RolTipo.GUITARRA_ELECTRICA);

        Cancion cancion = new Cancion("Test Song", List.of(voz, guitarra));

        ArtistaBase baseGuitarra = new ArtistaBase(
                "Guitarrista Base",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );

        ArtistaExterno cantanteExterno = new ArtistaExterno(
                "Cantante Externo",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.COUNTRY
        );

        cancion.asignarArtista(voz, cantanteExterno);
        cancion.asignarArtista(guitarra, baseGuitarra);

        double costoTotal = cancion.getCostoTotal(List.of(baseGuitarra));
        assertEquals(1000.0, costoTotal, 0.001);
    }

    @Test
    void getArtistasAsignadosDevuelveListaInmutable() {
        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        Cancion cancion = new Cancion("Test Song", List.of(voz));

        ArtistaBase cantante = new ArtistaBase(
                "Cantante",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Banda")
        );

        cancion.asignarArtista(voz, cantante);

        var artistas = cancion.getArtistasAsignados();
        assertEquals(1, artistas.size());
        assertThrows(UnsupportedOperationException.class, () -> artistas.add(cantante));
    }
}
