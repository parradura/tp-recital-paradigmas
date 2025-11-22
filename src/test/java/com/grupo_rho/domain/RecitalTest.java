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

    @Test
    void getRolesFaltantesTotalesSumaTodasLasCanciones() {
        Cancion c1 = new Cancion(
                "C1",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.BAJO)
                )
        );
        Cancion c2 = new Cancion(
                "C2",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.BATERIA)
                )
        );
        Recital recital = new Recital(
                "Test faltantes",
                List.of(c1, c2),
                List.of(),
                List.of(),
                TipoRecital.ROCK
        );

        Map<RolTipo, Integer> faltantes = recital.getRolesFaltantesTotales();
        assertEquals(2, faltantes.get(RolTipo.VOZ_PRINCIPAL));
        assertEquals(1, faltantes.get(RolTipo.BAJO));
        assertEquals(1, faltantes.get(RolTipo.BATERIA));
    }

    @Test
    void getArtistasContratadosDevuelveExternosSinDuplicar() {
        ArtistaBase brian = new ArtistaBase("Brian", Set.of(RolTipo.GUITARRA_ELECTRICA), Set.of("Queen"));
        ArtistaExterno george = new ArtistaExterno(
                "George",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Wham!"),
                1000.0,
                3,
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

        c1.asignarArtista(c1.getRolesRequeridos().get(0), george);
        c2.asignarArtista(c2.getRolesRequeridos().get(0), george);

        Recital recital = new Recital(
                "Test contratados",
                List.of(c1, c2),
                List.of(brian),
                List.of(george),
                TipoRecital.POP
        );

        List<ArtistaExterno> contratados = recital.getArtistasContratados();
        assertEquals(1, contratados.size());
        assertEquals("George", contratados.get(0).getNombre());
    }

    @Test
    void costoTotalRecitalIncluyeDescuentoPorBandasYArtistaEstrella() {
        // Setup: un artista estrella con preferencia matching y bastante costo
        ArtistaBase brian = new ArtistaBase("Brian", Set.of(RolTipo.GUITARRA_ELECTRICA), Set.of("Queen"));

        ArtistaExterno estrella = new ArtistaExterno(
                "Estrella Rock",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("RockBand"),
                2000.0,
                3,
                TipoRecital.ROCK
        );

        // Dos canciones donde la estrella canta
        Cancion c1 = new Cancion(
                "C1",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.GUITARRA_ELECTRICA)
                )
        );
        Cancion c2 = new Cancion(
                "C2",
                List.of(
                        new RolRequerido(RolTipo.VOZ_PRINCIPAL),
                        new RolRequerido(RolTipo.GUITARRA_ELECTRICA)
                )
        );

        c1.asignarArtista(c1.getRolesRequeridos().get(0), estrella);
        c1.asignarArtista(c1.getRolesRequeridos().get(1), brian);
        estrella.registrarAsignacionEnCancion();

        c2.asignarArtista(c2.getRolesRequeridos().get(0), estrella);
        c2.asignarArtista(c2.getRolesRequeridos().get(1), brian);
        estrella.registrarAsignacionEnCancion();

        Recital recital = new Recital(
                "Test estrella",
                List.of(c1, c2),
                List.of(brian),
                List.of(estrella),
                TipoRecital.ROCK
        );

        // Costo sin estrella: 2 canciones × 2000 cada una = 4000 (sin bandas compartidas)
        // Descuento estrella (suponiendo 25%) = 1000
        // Esperamos: 3000
        double costo = recital.getCostoTotalRecital();
        assertEquals(3000.0, costo, 0.0001);
    }
}
