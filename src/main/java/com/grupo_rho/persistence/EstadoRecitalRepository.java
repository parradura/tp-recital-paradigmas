package com.grupo_rho.persistence;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.estado.EstadoRecitalInfo;

import java.io.IOException;
import java.util.List;

/**
 * Abstracción para guardar y cargar estados de un recital.
 * Usado para los bonus de "guardar estado" y "cargar estado previo".
 */
public interface EstadoRecitalRepository {

    /**
     * Guarda el estado del recital con un nombre lógico (sin extensión).
     * La implementación decide cómo mapearlo a un archivo físico.
     */
    void guardarEstado(Recital recital, String nombreLogico) throws IOException;

    /**
     * Carga un recital a partir de un nombre lógico (sin extensión).
     */
    Recital cargarEstado(String nombreLogico) throws IOException;

    /**
     * Lista los estados disponibles, con información de resumen
     * (nombre lógico, nombre de recital, tipo, costo, etc.).
     */
    List<EstadoRecitalInfo> listarEstados() throws IOException;
}
