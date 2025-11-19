package com.grupo_rho.persistence.estado;

import java.util.List;

public record CancionEstadoDTO(
        String titulo,
        List<RolEstadoDTO> roles
) {}
