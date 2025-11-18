package com.grupo_rho.app.io.in;

import java.util.List;

public record RecitalJsonDTO(
        String nombre,
        String tipoRecital,
        List<CancionJsonDTO> canciones
) {}