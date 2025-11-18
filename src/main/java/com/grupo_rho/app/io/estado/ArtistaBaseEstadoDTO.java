package com.grupo_rho.app.io.estado;

import java.util.Set;

public record ArtistaBaseEstadoDTO(
        String nombre,
        Set<String> roles,
        Set<String> bandas
) {}
