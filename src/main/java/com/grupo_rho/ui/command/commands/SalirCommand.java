package com.grupo_rho.ui.command.commands;

import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;
import com.grupo_rho.ui.command.ExitMenuException;

public class SalirCommand extends AbstractCommand {

    private final EstadoRecitalRepository estadoRepo;

    public SalirCommand(RecitalService recitalService,
                        UiContext ui,
                        EstadoRecitalRepository estadoRepo
    ) {
        super(recitalService, ui);
        this.estadoRepo = estadoRepo;
    }

    @Override
    public String getDescription() {
        return "Salir";
    }

    @Override
    public void execute() {
        String resp = console().leerLinea("¿Querés guardar el estado antes de salir? (s/n): ")
                .trim()
                .toLowerCase();
        if (resp.equals("s")) {
            String nombre = console().leerLinea("Nombre para el estado (sin extensión): ")
                    .trim();
            if (!nombre.isEmpty()) {
                try {
                    estadoRepo.guardarEstado(recitalService.getRecital(), nombre);
                    println("Estado guardado como '" + nombre + "'.");
                } catch (Exception e) {
                    println("No se pudo guardar el estado: " + e.getMessage());
                }
            } else {
                println("Nombre vacío. No se guardó el estado.");
            }
        }

        println("Saliendo del sistema. ¡Gracias!");
        throw new ExitMenuException();
    }
}
