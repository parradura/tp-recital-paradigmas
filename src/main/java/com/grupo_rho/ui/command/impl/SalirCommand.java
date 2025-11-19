package com.grupo_rho.ui.command.impl;

import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.command.Command;
import com.grupo_rho.ui.command.ExitMenuException;

public class SalirCommand implements Command {

    private final RecitalService recitalService;
    private final EstadoRecitalRepository estadoRepo;
    private final ConsoleHelper console;

    public SalirCommand(RecitalService service,
                        EstadoRecitalRepository estadoRepo,
                        ConsoleHelper console) {
        this.recitalService = service;
        this.estadoRepo = estadoRepo;
        this.console = console;
    }

    @Override
    public String getDescription() {
        return "Salir";
    }

    @Override
    public void execute() {
        String resp = console.leerLinea("¿Querés guardar el estado antes de salir? (s/n): ")
                .trim()
                .toLowerCase();
        if (resp.equals("s")) {
            String nombre = console.leerLinea("Nombre para el estado (sin extensión): ")
                    .trim();
            if (!nombre.isEmpty()) {
                try {
                    estadoRepo.guardarEstado(recitalService.getRecital(), nombre);
                    System.out.println("Estado guardado como '" + nombre + "'.");
                } catch (Exception e) {
                    System.out.println("No se pudo guardar el estado: " + e.getMessage());
                }
            } else {
                System.out.println("Nombre vacío. No se guardó el estado.");
            }
        }

        System.out.println("Saliendo del sistema. ¡Gracias!");
        throw new ExitMenuException();
    }
}
