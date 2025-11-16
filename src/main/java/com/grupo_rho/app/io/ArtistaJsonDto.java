package com.grupo_rho.app.io;

import java.util.List;

public record ArtistaJsonDto(
        String nombre,
        List<String> roles,
        List<String> bandas,
        double costo,
        int maxCanciones
) {
}
