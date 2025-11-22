package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

import java.util.List;

public class ListarArtistasContratadosCommand extends AbstractCommand {

    public ListarArtistasContratadosCommand(RecitalService service,
                                            UiContext ui) {
        super(service, ui);
    }

    @Override
    public String getDescription() {
        return "Listar artistas contratados";
    }

    @Override
    public void execute() {
        println("== Listar artistas externos contratados ==");
        List<ArtistaExterno> contratados = recitalService.getArtistasContratados();
        printer().imprimirArtistasContratados(contratados, recitalService.getRecital());
    }
}
