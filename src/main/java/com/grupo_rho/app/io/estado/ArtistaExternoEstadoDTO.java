package com.grupo_rho.app.io.estado;

import java.util.Set;

public record ArtistaExternoEstadoDTO(
        String nombre,
        Set<String> rolesHistoricos,   // enum names
        Set<String> bandas,
        double costoBase,
        int maxCanciones,
        int cancionesAsignadasEnRecital,
        String tipoRecitalPreferido
) {}
