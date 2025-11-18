package com.grupo_rho.app;

import com.grupo_rho.domain.*;
import com.grupo_rho.domain.exception.ArtistaNoEntrenableException;
import com.grupo_rho.domain.exception.NoHayArtistasDisponiblesException;
import com.grupo_rho.app.io.DatosIniciales;
import com.grupo_rho.app.io.GestorJSON;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.domain.recital.TipoRecital;
import com.grupo_rho.app.io.estado.GestorEstadoRecital;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SistemaDiscografica {

    private static final String DATA_DIR = "data";
    private static final String OUTPUT_DIR = "data/output";
    private final Scanner scanner = new Scanner(System.in);
    private Recital recital;
    private PlanificadorContrataciones planificador;

    private final GestorJSON gestorJSON = new GestorJSON();
    private final GestorEstadoRecital gestorEstado = new GestorEstadoRecital();

    public static void main(String[] args) {
        new SistemaDiscografica().run();
    }

    private void run() {
        System.out.println("=== Sistema Discográfica - Recital Especial ===");

        try {
            cargarDatosDesdeJson();
        } catch (Exception e) {
            System.out.println("No se pudieron cargar los datos desde JSON (" + e.getMessage() + ").");
            //e.printStackTrace();
        }

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
                    case 8 -> guardarEstadoInteractivo();
                    case 9 -> cargarEstadoInteractivo();
                    case 0 -> {
                        System.out.print("¿Querés guardar el estado del recital antes de salir? (s/n): ");
                        String resp = leerLinea().trim().toLowerCase();

                        if (resp.equals("s")) {
                            guardarEstadoInteractivo();
                        } else {
                            System.out.println("No se guardó el estado del recital.");
                        }

                        System.out.println("Saliendo del sistema. ¡Gracias!");
                        salir = true;
                    }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (NoHayArtistasDisponiblesException e) {
                System.out.println("[ERROR DE DOMINIO] " + e.getMessage());
                manejarFaltaDeArtistas(e);
            } catch (ArtistaNoEntrenableException e) {
                System.out.println("[ERROR DE DOMINIO] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[ERROR] Ocurrió un error inesperado: " + e.getMessage());
                //e.printStackTrace();
            }

            System.out.println();
        }
    }


    private String leerLinea() {
        return scanner.nextLine();
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
        System.out.println("8) Guardar estado del recital con nombre");
        System.out.println("9) Cargar estado del recital desde archivo");
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
        List<ArtistaExterno> contratados = recital.getArtistasContratados();
        if (contratados.isEmpty()) {
            System.out.println("Todavía no hay artistas externos contratados.");
        } else {
            System.out.println("Artistas externos contratados:");
            for (ArtistaExterno a : contratados) {
                System.out.printf("- %s (canciones asignadas: %d, costo base: %.2f)%n",
                        a.getNombre(), a.getCancionesAsignadasEnRecital(), a.getCostoBase());
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
    // Carga de datos
    // ------------------------------------------------------------

    private void cargarDatosDesdeJson() throws IOException {
        System.out.println("Cargando datos desde JSON...");

        Path pathArtistas = Path.of(DATA_DIR, "artistas.json");
        Path pathRecital = Path.of(DATA_DIR, "recital.json");
        Path pathArtistasBase = Path.of(DATA_DIR, "artistas-discografica.json");

        DatosIniciales datos = gestorJSON.cargarDatos(
                pathArtistas.toString(),
                pathRecital.toString(),
                pathArtistasBase.toString()
        );

        this.recital = new Recital(
                datos.getNombreRecital(),
                datos.getCanciones(),
                datos.getArtistasBase(),
                datos.getArtistasExternos(),
                datos.getTipoRecital()
        );
        this.planificador = new PlanificadorContrataciones(this.recital);

        System.out.println("Datos cargados correctamente desde JSON.");
    }

    private List<ArtistaExterno> encontrarArtistasEntrenablesParaRol(RolTipo rol) {
        List<ArtistaExterno> resultado = new ArrayList<>();

        for (ArtistaExterno ext : recital.getArtistasExternosPool()) {
            if (ext.getCancionesAsignadasEnRecital() > 0) continue;
            if (ext.getRolesHistoricos().contains(rol)) continue;
            if (ext.getMaxCanciones() <= 0) continue;

            resultado.add(ext);
        }

        return resultado;
    }

    private void manejarFaltaDeArtistas(NoHayArtistasDisponiblesException e) {
        Cancion cancion = e.getCancion();
        RolTipo rol = e.getRolFaltante();

        System.out.println();
        System.out.println("No se pudo cubrir el rol " + rol +
                " en la canción '" + cancion.getTitulo() + "'.");
        System.out.println("Vamos a buscar si hay artistas externos que puedan entrenarse para este rol.");
        System.out.println();

        var entrenables = encontrarArtistasEntrenablesParaRol(rol);

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

        System.out.println("¿Querés entrenar a alguno de estos artistas para el rol " + rol + "? (s/n)");
        String respuesta = leerLinea().trim().toLowerCase();

        if (!respuesta.equals("s")) {
            System.out.println("No se entrenó a ningún artista. La operación de contratación quedó incompleta.");
            return;
        }

        int indice = leerEntero("Ingresá el número de artista a entrenar: ");
        if (indice < 1 || indice > entrenables.size()) {
            System.out.println("Número inválido. No se entrenó a ningún artista.");
            return;
        }

        ArtistaExterno seleccionado = entrenables.get(indice - 1);

        try {
            planificador.entrenarArtista(seleccionado, rol);
            System.out.println("Se entrenó a " + seleccionado.getNombre() +
                    " en el rol " + rol + ".");
        } catch (ArtistaNoEntrenableException ex) {
            System.out.println("[ERROR DE DOMINIO] " + ex.getMessage());
            return;
        }

        System.out.println("¿Querés reintentar la contratación de la canción '" +
                cancion.getTitulo() + "' ahora que " + seleccionado.getNombre() +
                " sabe el rol " + rol + "? (s/n)");
        String reintento = leerLinea().trim().toLowerCase();

        if (reintento.equals("s")) {
            try {
                planificador.contratarParaCancion(cancion);
                System.out.println("Contratación realizada para la canción '" + cancion.getTitulo() + "'.");
            } catch (NoHayArtistasDisponiblesException ex2) {
                System.out.println("[ERROR DE DOMINIO] " + ex2.getMessage());
                manejarFaltaDeArtistas(ex2);
            }
        } else {
            System.out.println("Podés reintentar la contratación más adelante desde el menú.");
        }
    }

    private void asegurarDirectorioData() {
        java.io.File dir = new java.io.File(DATA_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.out.println("Advertencia: no se pudo crear el directorio de datos '" + DATA_DIR + "'.");
            }
        }
    }

    private List<File> listarArchivosEstado() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        File[] archivos = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (archivos == null || archivos.length == 0) {
            return List.of();
        }

        Arrays.sort(archivos, Comparator.comparing(File::getName));
        return Arrays.asList(archivos);
    }

    private void guardarEstadoInteractivo() {
        System.out.print("Ingresá un nombre de archivo para guardar el estado (sin extensión): ");
        String nombre = scanner.nextLine().trim();

        if (nombre.isEmpty()) {
            System.out.println("Nombre vacío. Operación cancelada.");
            return;
        }

        asegurarDirectorioData();
        String path = OUTPUT_DIR + "/" + nombre + ".json";

        try {
            gestorEstado.guardarEstado(recital, path);
            System.out.println("Estado del recital guardado en '" + path + "'.");
        } catch (Exception e) {
            System.out.println("No se pudo guardar el estado del recital: " + e.getMessage());
        }
    }

    private void cargarEstadoInteractivo() {
        List<File> estados = listarArchivosEstado();

        if (estados.isEmpty()) {
            System.out.println("No hay estados de recital guardados en la carpeta '" + OUTPUT_DIR + "'.");
            return;
        }

        System.out.println("Estados de recital guardados disponibles:");
        for (int i = 0; i < estados.size(); i++) {
            File f = estados.get(i);
            System.out.printf("%d) %s%n", i + 1, f.getName());
        }

        int opcion = leerEntero("Elegí el número de estado que querés cargar: ");
        if (opcion < 1 || opcion > estados.size()) {
            System.out.println("Número inválido. Operación cancelada.");
            return;
        }

        File seleccionado = estados.get(opcion - 1);
        String path = seleccionado.getPath();

        try {
            Recital cargado = gestorEstado.cargarEstado(path);
            this.recital = cargado;
            this.planificador = new PlanificadorContrataciones(this.recital);

            System.out.println("Estado del recital cargado desde '" + path + "'.");
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("El archivo no tiene un estado válido de recital: " + e.getMessage());
        }
    }
}
