package com.grupo_rho.app.io.estado;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.grupo_rho.domain.*;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GestorEstadoRecital {

    private final ObjectMapper mapper;

    public GestorEstadoRecital() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // --------- GUARDAR ESTADO ---------

    public void guardarEstado(Recital recital, String path) throws IOException {
        double costoTotal = recital.getCostoTotalRecital();
        List<Cancion> canciones = recital.getCanciones();
        int cantidadCanciones = canciones.size();
        int completas = (int) canciones.stream().filter(Cancion::estaCompleta).count();

        List<ArtistaBaseEstadoDTO> baseDTO = recital.getArtistasBase().stream()
                .map(this::toDtoBase)
                .collect(Collectors.toList());

        List<ArtistaExternoEstadoDTO> externosDTO = recital.getArtistasExternosPool().stream()
                .map(this::toDtoExterno)
                .collect(Collectors.toList());

        List<CancionEstadoDTO> cancionesDTO = canciones.stream()
                .map(this::toDtoCancion)
                .collect(Collectors.toList());

        RecitalEstadoDTO dto = new RecitalEstadoDTO(
                recital.getNombre(),
                recital.getTipoRecital().name(),
                baseDTO,
                externosDTO,
                cancionesDTO,
                costoTotal,
                cantidadCanciones,
                completas
        );

        mapper.writeValue(new File(path), dto);
    }

    private ArtistaBaseEstadoDTO toDtoBase(ArtistaBase base) {
        Set<String> roles = base.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new ArtistaBaseEstadoDTO(base.getNombre(), roles, base.getHistorialBandas());
    }

    private ArtistaExternoEstadoDTO toDtoExterno(ArtistaExterno ext) {
        Set<String> roles = ext.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String tipoPref = ext.getTipoRecitalPreferido() != null
                ? ext.getTipoRecitalPreferido().name()
                : null;

        return new ArtistaExternoEstadoDTO(
                ext.getNombre(),
                roles,
                ext.getHistorialBandas(),
                ext.getCostoBase(),
                ext.getMaxCanciones(),
                ext.getCancionesAsignadasEnRecital(),
                tipoPref
        );
    }

    private CancionEstadoDTO toDtoCancion(Cancion c) {
        List<RolEstadoDTO> roles = new ArrayList<>();
        for (RolRequerido rr : c.getRolesRequeridos()) {
            String tipo = rr.getTipoRol().name();
            String artistaNombre = rr.getArtistaAsignado() != null
                    ? rr.getArtistaAsignado().getNombre()
                    : null;
            roles.add(new RolEstadoDTO(tipo, artistaNombre));
        }
        return new CancionEstadoDTO(c.getTitulo(), roles);
    }

    // --------- CARGAR ESTADO ---------

    public Recital cargarEstado(String path) throws IOException {
        RecitalEstadoDTO dto = mapper.readValue(new File(path), RecitalEstadoDTO.class);

        // 1) Reconstruir tipoRecital
        TipoRecital tipoRecital = TipoRecital.valueOf(dto.tipoRecital());

        // 2) Reconstruir artistas base
        List<ArtistaBase> bases = new ArrayList<>();
        for (ArtistaBaseEstadoDTO b : dto.artistasBase()) {
            Set<RolTipo> roles = b.roles().stream()
                    .map(RolTipo::valueOf)
                    .collect(Collectors.toSet());
            bases.add(new ArtistaBase(b.nombre(), roles, b.bandas()));
        }

        // 3) Reconstruir artistas externos
        List<ArtistaExterno> externos = new ArrayList<>();
        for (ArtistaExternoEstadoDTO e : dto.artistasExternos()) {
            Set<RolTipo> roles = e.rolesHistoricos().stream()
                    .map(RolTipo::valueOf)
                    .collect(Collectors.toSet());

            TipoRecital preferido = null;
            if (e.tipoRecitalPreferido() != null) {
                preferido = TipoRecital.valueOf(e.tipoRecitalPreferido());
            }

            ArtistaExterno ext = new ArtistaExterno(
                    e.nombre(),
                    roles,
                    e.bandas(),
                    e.costoBase(),
                    e.maxCanciones(),
                    preferido
            );
            // reponer cantidad de canciones asignadas
            for (int i = 0; i < e.cancionesAsignadasEnRecital(); i++) {
                ext.registrarAsignacionEnCancion();
            }
            externos.add(ext);
        }

        // Mapa nombre -> artista (base o externo) para reconstruir asignaciones
        Map<String, Artista> artistasPorNombre = new HashMap<>();
        bases.forEach(b -> artistasPorNombre.put(b.getNombre(), b));
        externos.forEach(e -> artistasPorNombre.put(e.getNombre(), e));

        // 4) Reconstruir canciones y roles asignados
        List<Cancion> canciones = new ArrayList<>();
        for (CancionEstadoDTO cDto : dto.canciones()) {
            List<RolRequerido> roles = new ArrayList<>();
            for (RolEstadoDTO rDto : cDto.roles()) {
                RolTipo tipo = RolTipo.valueOf(rDto.tipoRol());
                RolRequerido rr = new RolRequerido(tipo);
                if (rDto.artistaNombre() != null) {
                    Artista artista = artistasPorNombre.get(rDto.artistaNombre());
                    if (artista == null) {
                        throw new IllegalStateException(
                                "En el estado guardado, el artista '" + rDto.artistaNombre() +
                                        "' no existe en la lista de artistas."
                        );
                    }
                    rr.asignar(artista);
                }
                roles.add(rr);
            }
            canciones.add(new Cancion(cDto.titulo(), roles));
        }

        return new Recital(
                dto.nombreRecital(),
                canciones,
                bases,
                externos,
                tipoRecital
        );
    }
}
