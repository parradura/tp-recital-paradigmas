package com.grupo_rho.persistence.estado;

import java.util.Set;

public record ArtistaBaseEstadoDTO(
        String nombre,
        Set<String> roles,
        Set<String> bandas
) {}
