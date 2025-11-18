package com.grupo_rho.app.io.in;

import java.util.List;


public record ArtistaJsonDTO(
        String nombre,
        List<String> roles,
        List<String> bandas,
        double costo,
        int maxCanciones,
        String tipoRecitalPreferido
) {
}
