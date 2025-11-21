package com.grupo_rho.ui;

import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.service.RecitalService;

import java.util.List;
import java.util.Scanner;

public class ConsoleHelper {

    private final Scanner scanner;

    public ConsoleHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    // ---- salida ----

    public void println(String msg) {
        System.out.println(msg);
    }

    public void print(String msg) {
        System.out.print(msg);
    }

    // ---- entrada ----

    public String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }

    public int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine();
            try {
                return Integer.parseInt(linea.trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número válido.");
            }
        }
    }

    public Cancion elegirCancion(RecitalService service) {
        List<Cancion> canciones = service.getCanciones();
        if (canciones.isEmpty()) {
            System.out.println("No hay canciones cargadas.");
            return null;
        }

        System.out.println("Elegí una canción:");
        for (int i = 0; i < canciones.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, canciones.get(i).getTitulo());
        }

        int opcion = leerEntero("Número de canción: ");
        if (opcion < 1 || opcion > canciones.size()) {
            System.out.println("Número inválido.");
            return null;
        }
        return canciones.get(opcion - 1);
    }

    public ArtistaExterno elegirArtistaExterno(RecitalService service) {
        List<ArtistaExterno> externos = service.getArtistasExternosPool();
        if (externos.isEmpty()) {
            System.out.println("No hay artistas externos cargados.");
            return null;
        }

        System.out.println("Elegí un artista externo:");
        for (int i = 0; i < externos.size(); i++) {
            ArtistaExterno a = externos.get(i);
            System.out.printf("%d) %s (roles: %s, maxCanciones: %d, costoBase: %.2f)%n",
                    i + 1, a.getNombre(), a.getRolesHistoricos(),
                    a.getMaxCanciones(), a.getCostoBase());
        }

        int opcion = leerEntero("Número de artista: ");
        if (opcion < 1 || opcion > externos.size()) {
            System.out.println("Número inválido.");
            return null;
        }
        return externos.get(opcion - 1);
    }

    public RolTipo elegirRolTipo(String mensaje) {
        RolTipo[] valores = RolTipo.values();
        System.out.println("Roles disponibles:");
        for (int i = 0; i < valores.length; i++) {
            System.out.printf("%d) %s%n", i + 1, valores[i]);
        }
        int opcion = leerEntero(mensaje);
        if (opcion < 1 || opcion > valores.length) {
            System.out.println("Opción inválida.");
            return null;
        }
        return valores[opcion - 1];
    }
}
