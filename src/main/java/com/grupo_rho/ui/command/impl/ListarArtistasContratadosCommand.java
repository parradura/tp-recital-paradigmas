package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.command.Command;

import java.util.List;

public class ListarArtistasContratadosCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;

    public ListarArtistasContratadosCommand(RecitalService service,
                                            ConsoleHelper console,
                                            PrettyPrinter printer) {
        this.recitalService = service;
        this.console = console;
        this.printer = printer;
    }

    @Override
    public String getDescription() {
        return "Listar artistas contratados";
    }

    @Override
    public void execute() {
        console.println("== Listar artistas externos contratados ==");
        List<ArtistaExterno> contratados = recitalService.getArtistasContratados();
        printer.imprimirArtistasContratados(contratados);
    }
}
