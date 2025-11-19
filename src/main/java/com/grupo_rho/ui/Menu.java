package com.grupo_rho.ui;

import com.grupo_rho.ui.command.Command;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Clase Invoker que maneja las opciones y la interacción con el usuario.
 * Es agnóstica de la lógica de negocio, solo sabe ejecutar comandos.
 */
public class Menu {

    private final Map<Integer, Command> comandos = new LinkedHashMap<>();
    private final Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void agregarComando(int opcion, Command comando) {
        comandos.put(opcion, comando);
    }

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            mostrarOpciones();
            System.out.print("Seleccione una opción: ");
            try {
                String entrada = scanner.nextLine();
                int opcion = Integer.parseInt(entrada);

                if (opcion == 0) {
                    continuar = false;
                    System.out.println("Saliendo del sistema...");
                } else if (comandos.containsKey(opcion)) {
                    System.out.println("\n--- " + comandos.get(opcion).getDescription() + " ---");
                    comandos.get(opcion).execute();
                    System.out.println("------------------------------------------------");
                } else {
                    System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un número válido.");
            } catch (Exception e) {
                System.err.println("Error inesperado: " + e.getMessage());
                //e.printStackTrace();
            }
        }
    }

    private void mostrarOpciones() {
        System.out.println("\n=== SISTEMA DE GESTIÓN DE RECITALES ===");
        comandos.forEach((key, cmd) ->
                System.out.println(key + ". " + cmd.getDescription())
        );
        System.out.println("=======================================");
    }
}