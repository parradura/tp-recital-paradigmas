package com.grupo_rho.app.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo_rho.app.io.in.ArtistaJsonDTO;
import com.grupo_rho.app.io.in.CancionJsonDTO;
import com.grupo_rho.app.io.in.RecitalJsonDTO;
import com.grupo_rho.domain.*;
import com.grupo_rho.domain.recital.TipoRecital;

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
        List<ArtistaJsonDTO> artistasDto = mapper.readValue(
                new File(pathArtistas),
                new TypeReference<List<ArtistaJsonDTO>>() {
                }
        );

        // 2) Leer artistas-discografica.json (nombres de artistas base)
        List<String> nombresBase = mapper.readValue(
                new File(pathArtistasBase),
                new TypeReference<List<String>>() {
                }
        );
        Set<String> nombresBaseSet = new HashSet<>(nombresBase);

        List<ArtistaBase> artistasBase = new ArrayList<>();
        List<ArtistaExterno> artistasExternos = new ArrayList<>();

        for (ArtistaJsonDTO dto : artistasDto) {
            Set<RolTipo> roles = dto.roles().stream()
                    .map(RolTipo::valueOf)
                    .collect(Collectors.toSet());

            Set<String> bandas = new HashSet<>(dto.bandas());
            String tipoPrefStr = dto.tipoRecitalPreferido();
            TipoRecital tipoRecitalPreferido = null;

            if (tipoPrefStr != null && !tipoPrefStr.isBlank()) {
                try {
                    tipoRecitalPreferido = TipoRecital.valueOf(tipoPrefStr.trim());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            "tipoRecitalPreferido inválido para artista '" + dto.nombre() +
                                    "': '" + tipoPrefStr + "'. Valores válidos: " +
                                    Arrays.toString(TipoRecital.values()),
                            ex
                    );
                }
            }

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
                        dto.maxCanciones(),
                        tipoRecitalPreferido
                ));
            }
        }

        // 3) Leer recital.json
        RecitalJsonDTO recitalDto = mapper.readValue(
                new File(pathRecital),
                RecitalJsonDTO.class
        );

        String nombreRecital = recitalDto.nombre();
        TipoRecital tipoRecital = TipoRecital.valueOf(recitalDto.tipoRecital());

        List<Cancion> canciones = new ArrayList<>();
        for (CancionJsonDTO cDto : recitalDto.canciones()) {
            List<RolRequerido> rolesRequeridos = new ArrayList<>();
            for (String rolStr : cDto.rolesRequeridos()) {
                RolTipo tipo = RolTipo.valueOf(rolStr);
                rolesRequeridos.add(new RolRequerido(tipo));
            }
            canciones.add(new Cancion(cDto.titulo(), rolesRequeridos));
        }

        return new DatosIniciales(
                nombreRecital,
                tipoRecital,
                artistasBase,
                artistasExternos,
                canciones
        );
    }
}
