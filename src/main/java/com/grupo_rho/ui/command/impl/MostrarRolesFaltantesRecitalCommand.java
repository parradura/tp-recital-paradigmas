package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.command.Command;

import java.util.Map;

public class MostrarRolesFaltantesRecitalCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;

    public MostrarRolesFaltantesRecitalCommand(RecitalService recitalService,
                                               ConsoleHelper console,
                                               PrettyPrinter printer) {
        this.recitalService = recitalService;
        this.console = console;
        this.printer = printer;
    }

    @Override
    public String getDescription() {
        return "Ver roles faltantes de todo el recital";
    }

    @Override
    public void execute() {
        console.println("== Roles faltantes en todo el recital ==");
        Map<RolTipo, Integer> faltantes = recitalService.getRolesFaltantesTotales();
        printer.imprimirRolesFaltantes("todo el recital", faltantes);
    }
}
