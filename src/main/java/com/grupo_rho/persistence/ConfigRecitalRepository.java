package com.grupo_rho.persistence;

import com.grupo_rho.domain.recital.Recital;

import java.io.IOException;

public interface ConfigRecitalRepository {

    /**
     * Carga la configuración inicial del recital
     * (artistas, canciones, tipo de recital, etc.)
     * desde el origen de datos configurado.
     */
    Recital cargarRecitalInicial() throws IOException;
}
