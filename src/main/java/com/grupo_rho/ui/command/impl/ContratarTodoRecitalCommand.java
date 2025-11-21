package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.command.Command;

public class ContratarTodoRecitalCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;

    public ContratarTodoRecitalCommand(RecitalService service, ConsoleHelper console) {
        this.recitalService = service;
        this.console = console;
    }

    @Override
    public String getDescription() {
        return "Contratar artistas para todo el recital";
    }

    @Override
    public void execute() {
        try {
            recitalService.contratarParaTodoElRecital();
            console.println("Contratación realizada para todas las canciones posibles.");
        } catch (NoHayArtistasDisponiblesException e) {
            console.println("[ERROR DE DOMINIO] " + e.getMessage());
            console.println("Tip: podés usar la opción de contratar por canción para entrenar artistas si hace falta.");
        }
    }
}

