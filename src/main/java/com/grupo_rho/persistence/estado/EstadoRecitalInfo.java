package com.grupo_rho.persistence.estado;

/**
 * DTO liviano para listar estados disponibles al usuario.
 */
public record EstadoRecitalInfo(
        String nombreLogico,
        String nombreRecital,
        String tipoRecital,
        int cantidadCanciones,
        int cancionesCompletas,
        double costoTotal
) {}
