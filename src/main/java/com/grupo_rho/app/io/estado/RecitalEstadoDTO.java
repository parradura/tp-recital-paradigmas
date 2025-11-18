package com.grupo_rho.app.io.estado;

import java.util.List;

public record RecitalEstadoDTO(
        String nombreRecital,
        String tipoRecital,
        List<ArtistaBaseEstadoDTO> artistasBase,
        List<ArtistaExternoEstadoDTO> artistasExternos,
        List<CancionEstadoDTO> canciones,
        double costoTotalRecital,
        int cantidadCanciones,
        int cantidadCancionesCompletas
) {}
