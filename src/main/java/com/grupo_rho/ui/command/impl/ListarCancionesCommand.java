package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.cancion.*;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.command.Command;

public class ListarCancionesCommand implements Command {

    private final RecitalService recitalService;
    private final PrettyPrinter printer;

    public ListarCancionesCommand(RecitalService recitalService, PrettyPrinter printer) {
        this.recitalService = recitalService;
        this.printer = printer;
    }

    @Override
    public String getDescription() {
        return "Listar canciones y estado";
    }

    @Override
    public void execute() {
        printer.imprimirResumenRecital(recitalService.getRecital());
        printer.imprimirListadoCanciones(recitalService.getRecital());
    }
}
