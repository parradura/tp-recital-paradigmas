package com.grupo_rho.ui.command.impl;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.SelectionHelper;
import com.grupo_rho.ui.command.Command;

import java.util.List;

public class ContratarCancionCommand implements Command {

    private final RecitalService recitalService;
    private final ConsoleHelper console;
    private final PrettyPrinter printer;
    private final SelectionHelper selector;

    public ContratarCancionCommand(RecitalService recitalService,
                                   ConsoleHelper console,
                                   PrettyPrinter printer,
                                   SelectionHelper selector) {
        this.recitalService = recitalService;
        this.console = console;
        this.printer = printer;
        this.selector = selector;
    }

    @Override
    public String getDescription() {
        return "Contratar artistas para una canción";
    }

    @Override
    public void execute() {
        console.println("== Contratar artistas para una canción ==");
        Cancion cancion = selector.elegirCancion();
        if (cancion == null) {
            return;
        }

        printer.imprimirDetalleCancion(cancion, recitalService.getRecital());

        try {
            recitalService.contratarParaCancion(cancion);
            console.println("Contratación realizada para la canción '" + cancion.getTitulo() + "'.");
            printer.imprimirDetalleCancion(cancion, recitalService.getRecital());
        } catch (NoHayArtistasDisponiblesException e) {
            manejarFaltaDeArtistas(e);
        }
    }

    private void manejarFaltaDeArtistas(NoHayArtistasDisponiblesException e) {
        Cancion cancion = e.getCancion();
        RolTipo rol = e.getRolFaltante();

        System.out.println();
        System.out.println("No se pudo cubrir el rol " + rol +
                " en la canción '" + cancion.getTitulo() + "'.");
        System.out.println("Vamos a buscar si hay artistas externos que puedan entrenarse para este rol.");
        System.out.println();

        List<ArtistaExterno> entrenables = recitalService.encontrarArtistasEntrenablesParaRol(rol);

        if (entrenables.isEmpty()) {
            System.out.println("No hay artistas externos disponibles que puedan ser entrenados para este rol.");
            System.out.println("Sugerencia: revisá tus archivos JSON o agregá más artistas al plantel.");
            return;
        }

        System.out.println("Artistas entrenables para el rol " + rol + ":");
        for (int i = 0; i < entrenables.size(); i++) {
            ArtistaExterno a = entrenables.get(i);
            double costoActual = a.getCostoBase();
            double costoEntrenado = costoActual * 1.5;

            System.out.printf(
                    "%d) %s | roles actuales: %s | maxCanciones: %d | asignadas: %d | costo actual: %.2f | costo si se entrena: %.2f%n",
                    i + 1,
                    a.getNombre(),
                    a.getRolesHistoricos(),
                    a.getMaxCanciones(),
                    a.getCancionesAsignadasEnRecital(),
                    costoActual,
                    costoEntrenado
            );
        }
        System.out.println();

        String respuesta = console.leerLinea("¿Querés entrenar a alguno de estos artistas para el rol " + rol + "? (s/n): ")
                .trim()
                .toLowerCase();

        if (!respuesta.equals("s")) {
            System.out.println("No se entrenó a ningún artista. La operación de contratación quedó incompleta.");
            return;
        }

        int indice = console.leerEntero("Ingresá el número de artista a entrenar: ");
        if (indice < 1 || indice > entrenables.size()) {
            System.out.println("Número inválido. No se entrenó a ningún artista.");
            return;
        }

        ArtistaExterno seleccionado = entrenables.get(indice - 1);

        try {
            recitalService.entrenarArtista(seleccionado, rol);
            System.out.println("Se entrenó a " + seleccionado.getNombre() +
                    " en el rol " + rol + ".");
        } catch (ArtistaNoEntrenableException ex) {
            System.out.println("[ERROR DE DOMINIO] " + ex.getMessage());
            return;
        }

        String reintento = console.leerLinea(
                "¿Querés reintentar la contratación de la canción '" +
                        cancion.getTitulo() + "' ahora que " + seleccionado.getNombre() +
                        " sabe el rol " + rol + "? (s/n): "
        ).trim().toLowerCase();

        if (reintento.equals("s")) {
            try {
                recitalService.contratarParaCancion(cancion);
                System.out.println("Contratación realizada para la canción '" + cancion.getTitulo() + "'.");
            } catch (NoHayArtistasDisponiblesException ex2) {
                System.out.println("[ERROR DE DOMINIO] " + ex2.getMessage());
                // Podrías recursivamente ofrecer otro entrenamiento; por ahora, sólo informamos.
            }
        } else {
            System.out.println("Podés reintentar la contratación más adelante desde el menú.");
        }
    }
}
