package com.grupo_rho.ui;

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
}
