package com.grupo_rho.domain;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecitalTest {

    private Recital crearRecitalSimple() {
        ArtistaBase brian = new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                Set.of("Queen")
        );
        ArtistaBase roger = new ArtistaBase(
                "Roger Taylor",
                Set.of(RolTipo.BATERIA),
                Set.of("Queen")
        );
        ArtistaBase john = new ArtistaBase(
                "John Deacon",
                Set.of(RolTipo.BAJO),
                Set.of("Queen")
        );

        RolRequerido voz = new RolRequerido(RolTipo.VOZ_PRINCIPAL);
        RolRequerido guitarra = new RolRequerido(RolTipo.GUITARRA_ELECTRICA);
        RolRequerido bajo = new RolRequerido(RolTipo.BAJO);
        RolRequerido bateria = new RolRequerido(RolTipo.BATERIA);

        Cancion somebody = new Cancion(
                "Somebody to Love",
                List.of(voz, guitarra, bajo, bateria)
        );

        return new Recital(
                "Recital Test",
                List.of(somebody),
                List.of(brian, roger, john),
                List.of(),
                TipoRecital.ROCK
        );
    }

    @Test
    void getRolesFaltantesTotalesCuentaRolesPorTipo() {
        Recital recital = crearRecitalSimple();
        Map<RolTipo, Integer> faltantes = recital.getRolesFaltantesTotales();

        assertEquals(1, faltantes.get(RolTipo.VOZ_PRINCIPAL));
        assertEquals(1, faltantes.get(RolTipo.GUITARRA_ELECTRICA));
        assertEquals(1, faltantes.get(RolTipo.BAJO));
        assertEquals(1, faltantes.get(RolTipo.BATERIA));
    }

    @Test
    void getArtistasContratadosDevuelveSoloExternosYListaInmutable() {
        Recital recital = crearRecitalSimple();
        Cancion cancion = recital.getCanciones().get(0);

        ArtistaBase baseGuitarra = recital.getArtistasBase().get(0);
        ArtistaExterno cantanteExterno = new ArtistaExterno(
                "Cantante Externo",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                500.0,
                2,
                TipoRecital.COUNTRY
        );

        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            if (rol.getTipoRol() == RolTipo.VOZ_PRINCIPAL) {
                rol.asignar(cantanteExterno);
            } else if (rol.getTipoRol() == RolTipo.GUITARRA_ELECTRICA) {
                rol.asignar(baseGuitarra);
            }
        }

        List<ArtistaExterno> contratados = recital.getArtistasContratados();
        assertEquals(1, contratados.size());
        assertTrue(contratados.get(0).esExterno());
        assertEquals("Cantante Externo", contratados.get(0).getNombre());

        assertThrows(UnsupportedOperationException.class, () -> contratados.add(cantanteExterno));
    }

    @Test
    void getEstadoCancionesIndicaCompletaOIncompleta() {
        Recital recital = crearRecitalSimple();
        Cancion cancion = recital.getCanciones().get(0);

        Map<String, String> estadoInicial = recital.getEstadoCanciones();
        assertEquals("INCOMPLETA", estadoInicial.get(cancion.getTitulo()));

        // completamos solo con artistas base (no importa costo)
        ArtistaBase brian = recital.getArtistasBase().get(0);
        ArtistaBase roger = recital.getArtistasBase().get(1);
        ArtistaBase john = recital.getArtistasBase().get(2);

        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            switch (rol.getTipoRol()) {
                case GUITARRA_ELECTRICA -> rol.asignar(brian);
                case BATERIA -> rol.asignar(roger);
                case BAJO -> rol.asignar(john);
                default -> { /* VOZ_PRINCIPAL queda faltante */ }
            }
        }

        // Todavía incompleta porque falta VOZ_PRINCIPAL
        Map<String, String> estadoMitad = recital.getEstadoCanciones();
        assertEquals("INCOMPLETA", estadoMitad.get(cancion.getTitulo()));

        // ahora agregamos un externo para la voz
        ArtistaExterno cantante = new ArtistaExterno(
                "Cantante Externo",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.ROCK
        );
        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            if (rol.getTipoRol() == RolTipo.VOZ_PRINCIPAL) {
                rol.asignar(cantante);
            }
        }

        Map<String, String> estadoFinal = recital.getEstadoCanciones();
        assertEquals("COMPLETA", estadoFinal.get(cancion.getTitulo()));
    }

    @Test
    void costoTotalRecitalSumaCostosDeTodasLasCanciones() {
        Recital recital = crearRecitalSimple();
        Cancion cancion = recital.getCanciones().get(0);

        ArtistaExterno cantanteExterno = new ArtistaExterno(
                "Cantante Externo",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Otra Banda"),
                1000.0,
                3,
                TipoRecital.COUNTRY
        );

        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            if (rol.getTipoRol() == RolTipo.VOZ_PRINCIPAL) {
                rol.asignar(cantanteExterno);
            }
        }

        double costoCancion = cancion.getCostoTotal(recital.getArtistasBase());
        double costoRecital = recital.getCostoTotalRecital();

        assertEquals(costoCancion, costoRecital, 0.001);
        assertEquals(1000.0, costoRecital, 0.001);
    }
}
