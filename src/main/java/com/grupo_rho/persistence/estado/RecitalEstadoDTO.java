package com.grupo_rho.persistence.estado;

import java.util.List;

public record RecitalEstadoDTO(
        String nombreRecital,
        String tipoRecital,
        List<ArtistaBaseEstadoDTO> artistasBase,
        List<ArtistaExternoEstadoDTO> artistasExternos,
        List<CancionEstadoDTO> canciones,
        double costoTotal,
        int cantidadCanciones,
        int cancionesCompletas
) {}
