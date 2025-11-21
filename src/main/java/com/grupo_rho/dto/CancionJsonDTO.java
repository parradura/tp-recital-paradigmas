package com.grupo_rho.dto;

import java.util.List;

public record CancionJsonDTO(
        String titulo,
        List<String> rolesRequeridos
) {}
