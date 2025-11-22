package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;

public class EntrenarArtistaCommand extends AbstractCommand {

    public EntrenarArtistaCommand(RecitalService service, UiContext ui) {
        super(service, ui);
    }

    @Override
    public String getDescription() {
        return "Entrenar artista externo";
    }

    @Override
    public void execute() {
        println("== Entrenar artista externo ==");

        ArtistaExterno artista = selector().elegirArtistaExterno();
        if (artista == null) {
            return;
        }

        if (!recitalService.puedeEntrenarse(artista)) {
            println("El artista '" + artista.getNombre() +
                    "' no se puede entrenar porque ya está asignado " +
                    "en al menos una canción del recital.");
            return;
        }

        printer().imprimirResumenArtistaExterno(artista, recitalService.getRecital());

        RolTipo rol = selector().elegirRolTipo("Elegí el rol en el que querés entrenarlo: ");
        if (rol == null) return;

        recitalService.entrenarArtista(artista, rol);
        println("Artista entrenado correctamente en el rol " + rol + ".");
        printer().imprimirResumenArtistaExterno(artista, recitalService.getRecital());
    }
}
