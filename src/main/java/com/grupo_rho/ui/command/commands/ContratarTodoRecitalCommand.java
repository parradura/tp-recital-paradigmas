package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class ContratarTodoRecitalCommand extends AbstractCommand {

    public ContratarTodoRecitalCommand(RecitalService service, UiContext ui) {
        super(service, ui);
    }

    @Override
    public String getDescription() {
        return "Contratar artistas para todo el recital";
    }

    @Override
    public void execute() {
        try {
            recitalService.contratarParaTodoElRecital();
            println("Contratación realizada para todas las canciones posibles.");
        } catch (NoHayArtistasDisponiblesException e) {
            println("[ERROR DE DOMINIO] " + e.getMessage());
            println("Tip: podés usar la opción de contratar por canción para entrenar artistas si hace falta.");
        }
    }
}

