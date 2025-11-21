package com.grupo_rho.dto;

import java.util.List;

public record RecitalJsonDTO(
        String nombre,
        String tipoRecital,
        List<CancionJsonDTO> canciones
) {}