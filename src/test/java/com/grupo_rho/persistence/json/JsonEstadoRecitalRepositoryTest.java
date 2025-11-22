package com.grupo_rho.persistence.json;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.estado.EstadoRecitalInfo;
import com.grupo_rho.service.PlanificacionService;
import com.grupo_rho.util.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonEstadoRecitalRepositoryTest {

    @Test
    void guardarYLuegoCargarEstadoMantieneAsignacionesYTotales() throws Exception {
        // Arrange: recital con una canción ya contratada
        Recital recital = TestDataFactory.recitalBasicoRockConUnaCancion();
        PlanificacionService planificacion = new PlanificacionService(recital);
        planificacion.contratarParaTodoElRecital();

        Path tempDir = Files.createTempDirectory("estado-recital-test");
        EstadoRecitalRepository repo = new JsonEstadoRecitalRepository(tempDir.toString());

        // Act: guardar y cargar
        repo.guardarEstado(recital, "estado1");
        Recital cargado = repo.cargarEstado("estado1");

        // Assert: mismo nombre, tipo, cantidad de canciones
        assertEquals(recital.getNombre(), cargado.getNombre());
        assertEquals(recital.getTipoRecital(), cargado.getTipoRecital());
        assertEquals(recital.getCanciones().size(), cargado.getCanciones().size());

        // Comprobar que las canciones están completas en ambos
        assertEquals(
                recital.getCanciones().stream().filter(c -> c.estaCompleta()).count(),
                cargado.getCanciones().stream().filter(c -> c.estaCompleta()).count()
        );

        // Al menos un artista externo debería estar contratado en ambos
        assertFalse(recital.getArtistasContratados().isEmpty());
        assertEquals(
                recital.getArtistasContratados().size(),
                cargado.getArtistasContratados().size()
        );
    }

    @Test
    void listarEstadosDevuelveMetadatosCorrectos() throws Exception {
        Recital recital = TestDataFactory.recitalBasicoRockConUnaCancion();

        Path tempDir = Files.createTempDirectory("estado-recital-test2");
        EstadoRecitalRepository repo = new JsonEstadoRecitalRepository(tempDir.toString());

        repo.guardarEstado(recital, "estadoA");
        repo.guardarEstado(recital, "estadoB");

        List<EstadoRecitalInfo> estados = repo.listarEstados();
        assertEquals(2, estados.size());

        var nombres = estados.stream()
                .map(EstadoRecitalInfo::nombreLogico)
                .toList();

        assertTrue(nombres.contains("estadoA"));
        assertTrue(nombres.contains("estadoB"));

        // Cada info debería tener datos coherentes
        EstadoRecitalInfo infoA = estados.get(0);
        assertNotNull(infoA.nombreRecital());
        assertNotNull(infoA.tipoRecital());
        assertTrue(infoA.cantidadCanciones() > 0);
    }

    @Test
    void cargarEstadoInexistenteLanzaError() throws Exception {
        Path tempDir = Files.createTempDirectory("estado-recital-test3");
        EstadoRecitalRepository repo = new JsonEstadoRecitalRepository(tempDir.toString());

        assertThrows(
                java.io.IOException.class,
                () -> repo.cargarEstado("no_existe")
        );
    }
}
