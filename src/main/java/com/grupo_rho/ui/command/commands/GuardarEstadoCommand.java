package com.grupo_rho.ui.command.commands;

import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class GuardarEstadoCommand extends AbstractCommand {

    private final EstadoRecitalRepository estadoRepo;

    public GuardarEstadoCommand(RecitalService service,
                                UiContext ui,
                                EstadoRecitalRepository estadoRepo
    ) {
        super(service, ui);
        this.estadoRepo = estadoRepo;
    }

    @Override
    public String getDescription() {
        return "Guardar estado del recital";
    }

    @Override
    public void execute() {
        println("== Guardar estado del recital ==");
        String nombre = console().leerLinea("Ingresá un nombre lógico para este estado: ").trim();

        if (nombre.isEmpty()) {
            println("Nombre vacío. Operación cancelada.");
            return;
        }

        try {
            estadoRepo.guardarEstado(recitalService.getRecital(), nombre);
            println("Estado del recital guardado como '" + nombre + "'.");
        } catch (Exception e) {
            println("No se pudo guardar el estado del recital: " + e.getMessage());
        }
    }
}
