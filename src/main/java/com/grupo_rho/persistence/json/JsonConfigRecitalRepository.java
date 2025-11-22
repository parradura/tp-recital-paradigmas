package com.grupo_rho.persistence.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo_rho.domain.recital.*;
import com.grupo_rho.domain.artista.*;
import com.grupo_rho.domain.cancion.*;
import com.grupo_rho.dto.ArtistaJsonDTO;
import com.grupo_rho.dto.CancionJsonDTO;
import com.grupo_rho.dto.RecitalJsonDTO;
import com.grupo_rho.persistence.ConfigRecitalRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de ConfigRecitalRepository que lee
 * artistas.json, recital.json y artistas-discografica.json usando Jackson.
 */
public class JsonConfigRecitalRepository implements ConfigRecitalRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String dataDir;

    public JsonConfigRecitalRepository(String dataDir) {
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir no puede ser null");
    }

    @Override
    public Recital cargarRecitalInicial() throws IOException {
        Path pathArtistas = Path.of(dataDir, "artistas.json");
        Path pathRecital = Path.of(dataDir, "recital.json");
        Path pathArtistasBase = Path.of(dataDir, "artistas-discografica.json");

        // 1) Leer artistas.json
        List<ArtistaJsonDTO> artistasDto = mapper.readValue(
                pathArtistas.toFile(),
                new TypeReference<List<ArtistaJsonDTO>>() {}
        );

        // 2) Leer artistas-discografica.json
        List<String> nombresBase = mapper.readValue(
                pathArtistasBase.toFile(),
                new TypeReference<List<String>>() {}
        );
        Set<String> nombresBaseSet = new HashSet<>(nombresBase);

        List<ArtistaBase> artistasBase = new ArrayList<>();
        List<ArtistaExterno> artistasExternos = new ArrayList<>();

        for (ArtistaJsonDTO dto : artistasDto) {
            if (dto.nombre() == null || dto.nombre().isBlank()) {
                throw new IllegalArgumentException("Hay un artista en artistas.json sin nombre");
            }

            Set<RolTipo> roles = dto.roles().stream()
                    .map(this::mapRolTipoDesdeArtistasJson)
                    .collect(Collectors.toSet());

            Set<String> bandas = new HashSet<>(dto.bandas() != null ? dto.bandas() : List.of());

            TipoRecital preferido = mapTipoRecitalNullable(dto.tipoRecitalPreferido());

            if (nombresBaseSet.contains(dto.nombre())) {
                artistasBase.add(new ArtistaBase(dto.nombre(), roles, bandas));
            } else {
                artistasExternos.add(new ArtistaExterno(
                        dto.nombre(),
                        roles,
                        bandas,
                        dto.costo(),
                        dto.maxCanciones(),
                        preferido
                ));
            }
        }

        // 3) Leer recital.json
        RecitalJsonDTO recitalDto = mapper.readValue(
                pathRecital.toFile(),
                RecitalJsonDTO.class
        );

        if (recitalDto.nombre() == null || recitalDto.nombre().isBlank()) {
            throw new IllegalArgumentException("El recital.json no tiene nombre de recital");
        }
        if (recitalDto.tipoRecital() == null || recitalDto.tipoRecital().isBlank()) {
            throw new IllegalArgumentException("El recital.json no tiene tipoRecital");
        }

        TipoRecital tipoRecital = TipoRecital.valueOf(recitalDto.tipoRecital());

        List<Cancion> canciones = new ArrayList<>();
        for (CancionJsonDTO cDto : recitalDto.canciones()) {
            List<RolRequerido> rolesRequeridos = new ArrayList<>();
            for (String rolStr : cDto.rolesRequeridos()) {
                RolTipo tipoRol = RolTipo.valueOf(rolStr);
                rolesRequeridos.add(new RolRequerido(tipoRol));
            }
            canciones.add(new Cancion(cDto.titulo(), rolesRequeridos));
        }

        // 4) Construir el Recital de dominio
        return new Recital(
                recitalDto.nombre(),
                canciones,
                artistasBase,
                artistasExternos,
                tipoRecital
        );
    }

    // -----------------------------------------------------
    // Helpers de mapeo
    // -----------------------------------------------------

    /**
     * Mapea el string del JSON de artistas (roles) al enum RolTipo.
     * Permite:
     *  - nombres de enum ("VOZ_PRINCIPAL")
     *  - nombres “humanos” ("voz principal", "voz principal ", etc.)
     */
    private RolTipo mapRolTipoDesdeArtistasJson(String nombreRol) {
        if (nombreRol == null) {
            throw new IllegalArgumentException("Rol null en artistas.json");
        }

        // Primero intento mapear directamente a nombre de enum por si ya viene normalizado
        String mayus = nombreRol.trim().toUpperCase(Locale.ROOT);
        try {
            return RolTipo.valueOf(mayus);
        } catch (IllegalArgumentException ignored) {
            // si falla, seguimos con la normalización "humana"
        }

        String normalizado = nombreRol
                .toLowerCase(Locale.ROOT)
                .trim();

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
            default -> throw new IllegalArgumentException("Rol desconocido en artistas.json: " + nombreRol);
        };
    }

    private TipoRecital mapTipoRecitalNullable(String tipoRecitalStr) {
        if (tipoRecitalStr == null || tipoRecitalStr.isBlank()) {
            return null; // artista sin preferencia -> no recibe descuento de “estrella”
        }
        String mayus = tipoRecitalStr.trim().toUpperCase(Locale.ROOT);
        return TipoRecital.valueOf(mayus);
    }
}
