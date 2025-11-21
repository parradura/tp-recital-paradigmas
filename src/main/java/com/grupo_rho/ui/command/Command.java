package com.grupo_rho.ui.command;

/**
 * Interfaz del patrón Command.
 * Representa una acción ejecutable en el menú.
 */
public interface Command {
    /**
     * Ejecuta la lógica del comando.
     */
    void execute();

    /**
     * Retorna la descripción que se mostrará en el menú.
     * @return String con la descripción.
     */
    String getDescription();
}
