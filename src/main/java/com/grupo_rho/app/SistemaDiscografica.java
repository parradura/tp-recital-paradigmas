package com.grupo_rho.app;

import com.grupo_rho.domain.*;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;

import java.util.*;

public class SistemaDiscografica {

    private final Scanner scanner = new Scanner(System.in);
    private Recital recital;
    private PlanificadorContrataciones planificador;

    public static void main(String[] args) {
        new SistemaDiscografica().run();
    }

    private void run() {
        System.out.println("=== Sistema Discográfica - Recital Especial ===");

        // TODO: Cargar desde JSON
        cargarDatosDemo();

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Elegí una opción: ");

            try {
                switch (opcion) {
                    case 1 -> listarCanciones();
                    case 2 -> mostrarRolesFaltantesDeCancion();
                    case 3 -> mostrarRolesFaltantesDelRecital();
                    case 4 -> contratarParaCancion();
                    case 5 -> contratarParaTodoElRecital();
                    case 6 -> entrenarArtista();
                    case 7 -> listarArtistasContratados();
                    case 0 -> {
                        System.out.println("Saliendo del sistema. ¡Gracias!");
                        salir = true;
                    }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (NoHayArtistasDisponiblesException |
                     ArtistaNoEntrenableException e) {
                System.out.println("[ERROR DE DOMINIO] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[ERROR] Ocurrió un error inesperado: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        }
    }

    private void mostrarMenu() {
        System.out.println("--------------------------------------------------");
        System.out.println("1) Listar canciones y estado");
        System.out.println("2) Ver roles faltantes de una canción");
        System.out.println("3) Ver roles faltantes de todo el recital");
        System.out.println("4) Contratar artistas para una canción");
        System.out.println("5) Contratar artistas para todo el recital");
        System.out.println("6) Entrenar artista externo");
        System.out.println("7) Listar artistas contratados");
        System.out.println("0) Salir");
        System.out.println("--------------------------------------------------");
    }

    // ------------------------------------------------------------
    // Operaciones de menú
    // ------------------------------------------------------------

    private void listarCanciones() {
        System.out.println("Canciones del recital '" + recital.getNombre() + "':");
        int i = 1;
        for (Cancion c : recital.getCanciones()) {
            String estado = c.estaCompleta() ? "COMPLETA" : "INCOMPLETA";
            double costo = c.getCostoTotal(recital.getArtistasBase());
            System.out.printf("%d) %s - Estado: %s - Costo: %.2f%n",
                    i++, c.getTitulo(), estado, costo);
        }
        System.out.printf("Costo total del recital: %.2f%n", recital.getCostoTotalRecital());
    }

    private void mostrarRolesFaltantesDeCancion() {
        Cancion cancion = elegirCancion();
        if (cancion == null) return;

        List<RolRequerido> faltantes = recital.getRolesFaltantesCancion(cancion);
        if (faltantes.isEmpty()) {
            System.out.println("La canción ya tiene todos los roles cubiertos.");
        } else {
            System.out.println("Roles faltantes para '" + cancion.getTitulo() + "':");
            for (RolRequerido rol : faltantes) {
                System.out.println("- " + rol.getTipoRol());
            }
        }
    }

    private void mostrarRolesFaltantesDelRecital() {
        Map<RolTipo, Integer> faltantes = recital.getRolesFaltantesTotales();
        if (faltantes.isEmpty()) {
            System.out.println("El recital no tiene roles faltantes.");
        } else {
            System.out.println("Roles faltantes en todo el recital:");
            for (Map.Entry<RolTipo, Integer> entry : faltantes.entrySet()) {
                System.out.printf("- %s: %d%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private void contratarParaCancion() {
        Cancion cancion = elegirCancion();
        if (cancion == null) return;

        planificador.contratarParaCancion(cancion);
        System.out.println("Contratación realizada para la canción '" + cancion.getTitulo() + "'.");
    }

    private void contratarParaTodoElRecital() {
        planificador.contratarParaTodoElRecital();
        System.out.println("Contratación realizada para todas las canciones posibles.");
    }

    private void entrenarArtista() {
        ArtistaExterno artista = elegirArtistaExterno();
        if (artista == null) return;

        System.out.println("Artista seleccionado: " + artista.getNombre());
        System.out.println("Roles actuales: " + artista.getRolesHistoricos());

        RolTipo rol = elegirRolTipo("Elegí el rol en el que querés entrenarlo: ");
        if (rol == null) return;

        planificador.entrenarArtista(artista, rol);
        System.out.println("Artista entrenado correctamente en el rol " + rol);
    }

    private void listarArtistasContratados() {
        List<Artista> contratados = recital.getArtistasContratados();
        if (contratados.isEmpty()) {
            System.out.println("Todavía no hay artistas externos contratados.");
        } else {
            System.out.println("Artistas externos contratados:");
            for (Artista a : contratados) {
                if (a instanceof ArtistaExterno externo) {
                    System.out.printf("- %s (canciones asignadas: %d, costo base: %.2f)%n",
                            a.getNombre(), externo.getCancionesAsignadasEnRecital(), externo.getCostoBase());
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Helpers de selección
    // ------------------------------------------------------------

    private Cancion elegirCancion() {
        List<Cancion> canciones = recital.getCanciones();
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

    private ArtistaExterno elegirArtistaExterno() {
        List<ArtistaExterno> externos = recital.getArtistasExternosPool();
        if (externos.isEmpty()) {
            System.out.println("No hay artistas externos cargados.");
            return null;
        }

        System.out.println("Elegí un artista externo:");
        for (int i = 0; i < externos.size(); i++) {
            ArtistaExterno a = externos.get(i);
            System.out.printf("%d) %s (roles: %s, maxCanciones: %d, costoBase: %.2f)%n",
                    i + 1, a.getNombre(), a.getRolesHistoricos(), a.getMaxCanciones(), a.getCostoBase());
        }

        int opcion = leerEntero("Número de artista: ");
        if (opcion < 1 || opcion > externos.size()) {
            System.out.println("Número inválido.");
            return null;
        }
        return externos.get(opcion - 1);
    }

    private RolTipo elegirRolTipo(String mensaje) {
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

    // ------------------------------------------------------------
    // Lectura segura de enteros
    // ------------------------------------------------------------

    private int leerEntero(String mensaje) {
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

    // ------------------------------------------------------------
    // Datos de demo (después se reemplazan con lectura JSON)
    // ------------------------------------------------------------

    private void cargarDatosDemo() {
        System.out.println("Cargando datos de demo...");

        // Artistas base (Brian May, Roger Taylor, John Deacon)
        Set<String> queen = Set.of("Queen");

        ArtistaBase brian = new ArtistaBase(
                "Brian May",
                Set.of(RolTipo.GUITARRA_ELECTRICA),
                queen
        );
        ArtistaBase roger = new ArtistaBase(
                "Roger Taylor",
                Set.of(RolTipo.BATERIA),
                queen
        );
        ArtistaBase john = new ArtistaBase(
                "John Deacon",
                Set.of(RolTipo.BAJO),
                queen
        );

        List<ArtistaBase> base = List.of(brian, roger, john);

        // Artistas externos (George Michael, Elton John, David Bowie, etc.)
        ArtistaExterno george = new ArtistaExterno(
                "George Michael",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Wham!", "George Michael"),
                1000.0,
                3
        );

        ArtistaExterno elton = new ArtistaExterno(
                "Elton John",
                Set.of(RolTipo.VOZ_PRINCIPAL, RolTipo.PIANO),
                Set.of("Elton John Band"),
                1200.0,
                2
        );

        ArtistaExterno bowie = new ArtistaExterno(
                "David Bowie",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Tin Machine", "David Bowie"),
                1500.0,
                2
        );

        ArtistaExterno annie = new ArtistaExterno(
                "Annie Lennox",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Eurythmics"),
                900.0,
                2
        );

        ArtistaExterno lisa = new ArtistaExterno(
                "Lisa Stansfield",
                Set.of(RolTipo.VOZ_PRINCIPAL),
                Set.of("Lisa Stansfield"),
                800.0,
                2
        );

        List<ArtistaExterno> externos = new ArrayList<>();
        externos.add(george);
        externos.add(elton);
        externos.add(bowie);
        externos.add(annie);
        externos.add(lisa);

        // Canciones del ejemplo
        Cancion somebodyToLove = new Cancion(
                "Somebody to Love",
                crearRolesRequeridos(
                        RolTipo.VOZ_PRINCIPAL,
                        RolTipo.GUITARRA_ELECTRICA,
                        RolTipo.BAJO,
                        RolTipo.BATERIA,
                        RolTipo.PIANO
                )
        );

        Cancion weWillRockYou = new Cancion(
                "We Will Rock You",
                crearRolesRequeridos(
                        RolTipo.VOZ_PRINCIPAL,
                        RolTipo.GUITARRA_ELECTRICA,
                        RolTipo.BAJO,
                        RolTipo.BATERIA
                )
        );

        Cancion theseAreTheDays = new Cancion(
                "These Are the Days of Our Lives",
                crearRolesRequeridos(
                        RolTipo.VOZ_PRINCIPAL,
                        RolTipo.GUITARRA_ELECTRICA,
                        RolTipo.BAJO,
                        RolTipo.BATERIA
                )
        );

        // Under Pressure con dos voces principales
        List<RolRequerido> rolesUnderPressure = new ArrayList<>();
        rolesUnderPressure.add(new RolRequerido(RolTipo.VOZ_PRINCIPAL));
        rolesUnderPressure.add(new RolRequerido(RolTipo.VOZ_PRINCIPAL));
        rolesUnderPressure.add(new RolRequerido(RolTipo.GUITARRA_ELECTRICA));
        rolesUnderPressure.add(new RolRequerido(RolTipo.BAJO));
        rolesUnderPressure.add(new RolRequerido(RolTipo.BATERIA));

        Cancion underPressure = new Cancion("Under Pressure", rolesUnderPressure);

        List<Cancion> canciones = List.of(
                somebodyToLove,
                weWillRockYou,
                theseAreTheDays,
                underPressure
        );

        this.recital = new Recital("Recital Homenaje a Queen", canciones, base, externos);
        this.planificador = new PlanificadorContrataciones(this.recital);

        System.out.println("Datos de demo cargados.");
    }

    private List<RolRequerido> crearRolesRequeridos(RolTipo... tipos) {
        List<RolRequerido> lista = new ArrayList<>();
        for (RolTipo t : tipos) {
            lista.add(new RolRequerido(t));
        }
        return lista;
    }
}
