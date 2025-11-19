package com.grupo_rho.ui;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.service.RecitalService;

import java.util.List;

public class SelectionHelper {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;

    public SelectionHelper(RecitalService recitalService,
                           ConsoleHelper console,
                           PrettyPrinter printer) {
        this.recitalService = recitalService;
        this.console = console;
        this.printer = printer;
    }

    public Cancion elegirCancion() {
        List<Cancion> canciones = recitalService.getCanciones();
        if (canciones.isEmpty()) {
            console.println("No hay canciones cargadas.");
            return null;
        }

        console.println("Elegí una canción:");
        printer.listadoCancionesNumerado(canciones, recitalService.getArtistasBase());

        int opcion = console.leerEntero("Número de canción: ");
        if (opcion < 1 || opcion > canciones.size()) {
            console.println("Número inválido.");
            return null;
        }
        return canciones.get(opcion - 1);
    }

    public ArtistaExterno elegirArtistaExterno() {
        List<ArtistaExterno> externos = recitalService.getArtistasExternosPool();
        if (externos.isEmpty()) {
            console.println("No hay artistas externos cargados.");
            return null;
        }

        console.println("Elegí un artista externo:");
        printer.listadoArtistasExternosNumerado(externos);

        int opcion = console.leerEntero("Número de artista: ");
        if (opcion < 1 || opcion > externos.size()) {
            console.println("Número inválido.");
            return null;
        }
        return externos.get(opcion - 1);
    }

    public RolTipo elegirRolTipo(String mensaje) {
        RolTipo[] valores = RolTipo.values();

        console.println("Roles disponibles:");
        printer.listadoRolesNumerado(valores);

        int opcion = console.leerEntero(mensaje);
        if (opcion < 1 || opcion > valores.length) {
            console.println("Opción inválida.");
            return null;
        }
        return valores[opcion - 1];
    }
}
