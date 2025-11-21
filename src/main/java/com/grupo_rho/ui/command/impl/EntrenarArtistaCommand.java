package com.grupo_rho.ui.command.impl;


import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.SelectionHelper;
import com.grupo_rho.ui.command.Command;

public class EntrenarArtistaCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;
    private final SelectionHelper selector;

    public EntrenarArtistaCommand(RecitalService service,
                                  ConsoleHelper console,
                                  PrettyPrinter printer,
                                  SelectionHelper selector) {
        this.recitalService = service;
        this.console = console;
        this.printer = printer;
        this.selector = selector;
    }

    @Override
    public String getDescription() {
        return "Entrenar artista externo";
    }

    @Override
    public void execute() {
        console.println("== Entrenar artista externo ==");

        ArtistaExterno artista = selector.elegirArtistaExterno();
        if (artista == null) {
            return;
        }

        if (!recitalService.puedeEntrenarse(artista)) {
            console.println("El artista '" + artista.getNombre() +
                    "' no se puede entrenar porque ya está asignado " +
                    "en al menos una canción del recital.");
            return;
        }

        printer.imprimirResumenArtistaExterno(artista, recitalService.getRecital());

        RolTipo rol = selector.elegirRolTipo("Elegí el rol en el que querés entrenarlo: ");
        if (rol == null) return;

        recitalService.entrenarArtista(artista, rol);
        console.println("Artista entrenado correctamente en el rol " + rol + ".");
        printer.imprimirResumenArtistaExterno(artista, recitalService.getRecital());
    }
}
