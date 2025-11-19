package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.estado.EstadoRecitalInfo;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.command.Command;

import java.util.List;

public class CargarEstadoCommand implements Command {

    private final RecitalService recitalService;
    private final EstadoRecitalRepository estadoRepo;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;

    public CargarEstadoCommand(RecitalService service,
                               EstadoRecitalRepository estadoRepo,
                               ConsoleHelper console,
                               PrettyPrinter printer) {
        this.recitalService = service;
        this.estadoRepo = estadoRepo;
        this.console = console;
        this.printer = printer;
    }

    @Override
    public String getDescription() {
        return "Cargar estado del recital";
    }

    @Override
    public void execute() {
        console.println("== Cargar estado de recital guardado ==");
        List<EstadoRecitalInfo> estados;
        try {
            estados = estadoRepo.listarEstados();
        } catch (Exception e) {
            console.println("No se pudieron listar los estados: " + e.getMessage());
            return;
        }

        if (estados.isEmpty()) {
            console.println("No hay estados de recital guardados.");
            return;
        }

        printer.imprimirEstadosRecital(estados);
        int opcion = console.leerEntero("Elegí un estado: ");

        if (opcion < 1 || opcion > estados.size()) {
            console.println("Opción inválida. Operación cancelada.");
            return;
        }

        EstadoRecitalInfo seleccionado = estados.get(opcion - 1);

        try {
            Recital cargado = estadoRepo.cargarEstado(seleccionado.nombreLogico());
            recitalService.setRecital(cargado);
            console.println("Estado '" + seleccionado.nombreLogico() + "' cargado correctamente.");
        } catch (Exception e) {
            console.println("No se pudo cargar el estado: " + e.getMessage());
        }
    }
}
