package com.grupo_rho.app.io.estado;

import java.util.List;

public record CancionEstadoDTO(
        String titulo,
        List<RolEstadoDTO> roles
) {}
