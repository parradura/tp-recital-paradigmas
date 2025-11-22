package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

import java.util.Map;

public class MostrarRolesFaltantesRecitalCommand extends AbstractCommand {

    public MostrarRolesFaltantesRecitalCommand(RecitalService recitalService,
                                               UiContext ui) {
        super(recitalService, ui);
    }

    @Override
    public String getDescription() {
        return "Ver roles faltantes de todo el recital";
    }

    @Override
    public void execute() {
        println("== Roles faltantes en todo el recital ==");
        Map<RolTipo, Integer> faltantes = recitalService.getRolesFaltantesTotales();
        printer().imprimirRolesFaltantes("todo el recital", faltantes);
    }
}
