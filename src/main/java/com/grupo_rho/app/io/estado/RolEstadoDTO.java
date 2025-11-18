package com.grupo_rho.app.io.estado;

public record RolEstadoDTO(
        String tipoRol,        // enum name: "VOZ_PRINCIPAL" etc
        String artistaNombre   // puede ser null si está sin cubrir
) {}
