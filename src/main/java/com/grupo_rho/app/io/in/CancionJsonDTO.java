package com.grupo_rho.app.io.in;

import java.util.List;

public record CancionJsonDTO(
        String titulo,
        List<String> rolesRequeridos
) {}
