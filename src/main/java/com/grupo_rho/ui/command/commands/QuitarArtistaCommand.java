package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class QuitarArtistaCommand extends AbstractCommand {

    public QuitarArtistaCommand(RecitalService recitalService, UiContext ui) {
        super(recitalService, ui);
    }

    @Override
    public String getDescription() {
        return "Quitar artista contratado (arrepentimiento)";
    }

    @Override
    public void execute() {
        println("== Quitar artista contratado del recital ==");

        ArtistaExterno artista = ui.selector().elegirArtistaContratado();
        if (artista == null) {
            return;
        }

        printer().imprimirResumenArtistaExterno(artista, recitalService.getRecital());

        String resp = ui.console().leerLinea(
                "¿Seguro que querés quitar a '" + artista.getNombre() +
                        "' de todas las canciones del recital? (s/n): "
        ).trim().toLowerCase();

        if (!resp.equals("s")) {
            println("Operación cancelada. No se quitó al artista.");
            return;
        }

        recitalService.quitarArtistaDelRecital(artista);

        println("Se quitó a '" + artista.getNombre() +
                "' de todas las canciones. Algunos roles pueden haber quedado incompletos.");
    }
}
