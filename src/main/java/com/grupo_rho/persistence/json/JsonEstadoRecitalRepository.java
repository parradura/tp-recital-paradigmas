package com.grupo_rho.persistence.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.grupo_rho.domain.recital.*;
import com.grupo_rho.domain.artista.*;
import com.grupo_rho.domain.cancion.*;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.estado.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de EstadoRecitalRepository basada en archivos JSON.
 * Guarda y carga estados completos de Recital.
 *
 * Convención de nombres de archivo:
 *   - Nombre lógico: "queen_full"
 *   - Archivo en disco: "queen_full.estado.json"
 */
public class JsonEstadoRecitalRepository implements EstadoRecitalRepository {

    private static final String ESTADO_SUFFIX = ".estado.json";

    private final ObjectMapper mapper;
    private final String dataDir;

    public JsonEstadoRecitalRepository(String dataDir) {
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir no puede ser null");
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void guardarEstado(Recital recital, String nombreLogico) throws IOException {
        if (nombreLogico == null || nombreLogico.isBlank()) {
            throw new IllegalArgumentException("nombreLogico vacío al guardar estado");
        }

        asegurarDirectorioData();

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

        Path path = Path.of(dataDir, nombreLogico + ESTADO_SUFFIX);
        mapper.writeValue(path.toFile(), dto);
    }

    @Override
    public Recital cargarEstado(String nombreLogico) throws IOException {
        if (nombreLogico == null || nombreLogico.isBlank()) {
            throw new IllegalArgumentException("nombreLogico vacío al cargar estado");
        }

        Path path = Path.of(dataDir, nombreLogico + ESTADO_SUFFIX);
        File file = path.toFile();
        if (!file.exists()) {
            throw new IOException("No existe el archivo de estado: " + file.getAbsolutePath());
        }

        RecitalEstadoDTO dto = mapper.readValue(file, RecitalEstadoDTO.class);

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

            TipoRecital preferido = e.tipoRecitalPreferido() != null
                    ? TipoRecital.valueOf(e.tipoRecitalPreferido())
                    : null;

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

    @Override
    public List<EstadoRecitalInfo> listarEstados() throws IOException {
        File dir = new File(dataDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        File[] archivos = dir.listFiles((d, name) -> name.toLowerCase().endsWith(ESTADO_SUFFIX));
        if (archivos == null || archivos.length == 0) {
            return List.of();
        }

        Arrays.sort(archivos, Comparator.comparing(File::getName));

        List<EstadoRecitalInfo> infoList = new ArrayList<>();
        for (File archivo : archivos) {
            try {
                RecitalEstadoDTO dto = mapper.readValue(archivo, RecitalEstadoDTO.class);
                String fileName = archivo.getName();
                String nombreLogico = fileName.substring(0, fileName.length() - ESTADO_SUFFIX.length());

                infoList.add(new EstadoRecitalInfo(
                        nombreLogico,
                        dto.nombreRecital(),
                        dto.tipoRecital(),
                        dto.cantidadCanciones(),
                        dto.cancionesCompletas(),
                        dto.costoTotal()
                ));
            } catch (Exception e) {
                // Podemos loguear o simplemente saltear archivos corruptos
                System.out.println("Advertencia: no se pudo leer el estado de " + archivo.getName() +
                        " (" + e.getMessage() + ")");
            }
        }

        return infoList;
    }

    private void asegurarDirectorioData() {
        File dir = new File(dataDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta de datos: " + dataDir);
        }
    }

    // ---------- helpers de mapeo a DTO ----------

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
        String preferido = ext.getTipoRecitalPreferido() != null
                ? ext.getTipoRecitalPreferido().name()
                : null;
        return new ArtistaExternoEstadoDTO(
                ext.getNombre(),
                roles,
                ext.getHistorialBandas(),
                ext.getCostoBase(),
                ext.getMaxCanciones(),
                ext.getCancionesAsignadasEnRecital(),
                preferido
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
}
