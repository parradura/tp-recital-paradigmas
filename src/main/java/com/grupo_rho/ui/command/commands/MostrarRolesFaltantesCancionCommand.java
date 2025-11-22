package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.cancion.*;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class MostrarRolesFaltantesCancionCommand extends AbstractCommand {

    public MostrarRolesFaltantesCancionCommand(RecitalService recitalService,
                                               UiContext ui) {
        super(recitalService, ui);
    }

    @Override
    public String getDescription() {
        return "Ver roles faltantes de una canción";
    }

    @Override
    public void execute() {
        println("== Ver roles faltantes de una canción ==");
        Cancion cancion = selector().elegirCancion();
        if (cancion == null) {
            return;
        }
        var faltantesAgrupados = recitalService.getRolesFaltantesCancion(cancion);

        printer().imprimirRolesFaltantes(
                "la canción '" + cancion.getTitulo() + "'",
                faltantesAgrupados
        );
    }
}
