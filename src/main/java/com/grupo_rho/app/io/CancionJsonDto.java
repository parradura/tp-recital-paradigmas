package com.grupo_rho.app.io;

import java.util.List;

public record CancionJsonDto(
        String titulo,
        List<String> rolesRequeridos
) {
}
