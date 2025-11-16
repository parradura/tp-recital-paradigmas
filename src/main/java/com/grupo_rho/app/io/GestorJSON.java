package com.grupo_rho.app.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo_rho.domain.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GestorJSON {

    private final ObjectMapper mapper = new ObjectMapper();

    public DatosIniciales cargarDatos(String pathArtistas,
                                      String pathRecital,
                                      String pathArtistasBase) throws IOException {
        // 1) Leer artistas.json
        List<ArtistaJsonDto> artistasDto = mapper.readValue(
                new File(pathArtistas),
                new TypeReference<List<ArtistaJsonDto>>() {}
        );

        // 2) Leer artistas-discografica.json (nombres de artistas base)
        List<String> nombresBase = mapper.readValue(
                new File(pathArtistasBase),
                new TypeReference<List<String>>() {}
        );
        Set<String> nombresBaseSet = new HashSet<>(nombresBase);

        List<ArtistaBase> artistasBase = new ArrayList<>();
        List<ArtistaExterno> artistasExternos = new ArrayList<>();

        for (ArtistaJsonDto dto : artistasDto) {
            Set<RolTipo> roles = dto.roles().stream()
                    .map(this::mapRolTipo)
                    .collect(Collectors.toSet());

            Set<String> bandas = new HashSet<>(dto.bandas());

            if (nombresBaseSet.contains(dto.nombre())) {
                // Artista del sello (base)
                artistasBase.add(new ArtistaBase(dto.nombre(), roles, bandas));
            } else {
                // Artista externo
                artistasExternos.add(new ArtistaExterno(
                        dto.nombre(),
                        roles,
                        bandas,
                        dto.costo(),
                        dto.maxCanciones()
                ));
            }
        }

        // 3) Leer recital.json
        List<CancionJsonDto> cancionesDto = mapper.readValue(
                new File(pathRecital),
                new TypeReference<List<CancionJsonDto>>() {}
        );

        List<Cancion> canciones = new ArrayList<>();
        for (CancionJsonDto cDto : cancionesDto) {
            List<RolRequerido> rolesRequeridos = new ArrayList<>();
            for (String rolStr : cDto.rolesRequeridos()) {
                RolTipo tipo = mapRolTipo(rolStr);
                rolesRequeridos.add(new RolRequerido(tipo));
            }
            canciones.add(new Cancion(cDto.titulo(), rolesRequeridos));
        }

        return new DatosIniciales(artistasBase, artistasExternos, canciones);
    }

    /**
     * Mapea el string del JSON (ej. "voz principal") a tu enum RolTipo.
     * Tolera mayúsculas/minúsculas y acentos básicos.
     */
    private RolTipo mapRolTipo(String nombreRol) {
        String normalizado = nombreRol
                .toLowerCase(Locale.ROOT)
                .trim();

        // simplificación rápida de acentos comunes
        normalizado = normalizado
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");

        return switch (normalizado) {
            case "voz principal" -> RolTipo.VOZ_PRINCIPAL;
            case "guitarra electrica" -> RolTipo.GUITARRA_ELECTRICA;
            case "bajo" -> RolTipo.BAJO;
            case "bateria" -> RolTipo.BATERIA;
            case "piano" -> RolTipo.PIANO;
            case "coros" -> RolTipo.COROS;
            default -> throw new IllegalArgumentException("Rol desconocido en JSON: " + nombreRol);
        };
    }
}
