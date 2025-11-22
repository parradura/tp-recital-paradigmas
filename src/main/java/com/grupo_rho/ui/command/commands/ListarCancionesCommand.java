package com.grupo_rho.ui.command.commands;

import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class ListarCancionesCommand extends AbstractCommand {

    public ListarCancionesCommand(RecitalService recitalService, UiContext ui) {
        super(recitalService, ui);
    }

    @Override
    public String getDescription() {
        return "Listar canciones y estado";
    }

    @Override
    public void execute() {
        printer().imprimirResumenRecital(recitalService.getRecital());
        printer().imprimirListadoCanciones(recitalService.getRecital());
    }
}
