package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.cancion.*;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.SelectionHelper;
import com.grupo_rho.ui.command.Command;

import java.util.List;

public class MostrarRolesFaltantesCancionCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;
    private final SelectionHelper selector;

    public MostrarRolesFaltantesCancionCommand(RecitalService recitalService,
                                               ConsoleHelper console,
                                               PrettyPrinter printer,
                                               SelectionHelper selector) {
        this.recitalService = recitalService;
        this.console = console;
        this.printer = printer;
        this.selector = selector;
    }

    @Override
    public String getDescription() {
        return "Ver roles faltantes de una canción";
    }

    @Override
    public void execute() {
        console.println("== Ver roles faltantes de una canción ==");
        Cancion cancion = selector.elegirCancion();
        if (cancion == null) {
            return;
        }
        var faltantesAgrupados = recitalService.getRolesFaltantesCancion(cancion);

        printer.imprimirRolesFaltantes(
                "la canción '" + cancion.getTitulo() + "'",
                faltantesAgrupados
        );
    }
}
