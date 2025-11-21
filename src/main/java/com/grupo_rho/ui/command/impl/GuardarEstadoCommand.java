package com.grupo_rho.ui.command.impl;

import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.command.Command;

public class GuardarEstadoCommand implements Command {

    private final RecitalService recitalService;
    private final EstadoRecitalRepository estadoRepo;
    private final ConsoleHelper console;

    public GuardarEstadoCommand(RecitalService service,
                                EstadoRecitalRepository estadoRepo,
                                ConsoleHelper console) {
        this.recitalService = service;
        this.estadoRepo = estadoRepo;
        this.console = console;
    }

    @Override
    public String getDescription() {
        return "Guardar estado del recital";
    }

    @Override
    public void execute() {
        console.println("== Guardar estado del recital ==");
        String nombre = console.leerLinea("Ingresá un nombre lógico para este estado: ").trim();

        if (nombre.isEmpty()) {
            console.println("Nombre vacío. Operación cancelada.");
            return;
        }

        try {
            estadoRepo.guardarEstado(recitalService.getRecital(), nombre);
            console.println("Estado del recital guardado como '" + nombre + "'.");
        } catch (Exception e) {
            console.println("No se pudo guardar el estado del recital: " + e.getMessage());
        }
    }
}
