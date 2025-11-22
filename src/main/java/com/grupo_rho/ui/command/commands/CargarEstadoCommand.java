package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.estado.EstadoRecitalInfo;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

import java.util.List;

public class CargarEstadoCommand extends AbstractCommand {

    private final EstadoRecitalRepository estadoRepo;

    public CargarEstadoCommand(RecitalService service, 
                               UiContext ui,
                               EstadoRecitalRepository estadoRepo
    ) {
        super(service, ui);
        this.estadoRepo = estadoRepo;
    }

    @Override
    public String getDescription() {
        return "Cargar estado del recital";
    }

    @Override
    public void execute() {
        println("== Cargar estado de recital guardado ==");
        List<EstadoRecitalInfo> estados;
        try {
            estados = estadoRepo.listarEstados();
        } catch (Exception e) {
            println("No se pudieron listar los estados: " + e.getMessage());
            return;
        }

        if (estados.isEmpty()) {
            println("No hay estados de recital guardados.");
            return;
        }

        printer().imprimirEstadosRecital(estados);
        int opcion = console().leerEntero("Elegí un estado: ");

        if (opcion < 1 || opcion > estados.size()) {
            println("Opción inválida. Operación cancelada.");
            return;
        }

        EstadoRecitalInfo seleccionado = estados.get(opcion - 1);

        try {
            Recital cargado = estadoRepo.cargarEstado(seleccionado.nombreLogico());
            recitalService.setRecital(cargado);
            println("Estado '" + seleccionado.nombreLogico() + "' cargado correctamente.");
        } catch (Exception e) {
            println("No se pudo cargar el estado: " + e.getMessage());
        }
    }
}
