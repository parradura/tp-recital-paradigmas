package com.grupo_rho.ui;

import com.grupo_rho.domain.artista.Artista;
import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.estado.EstadoRecitalInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrettyPrinter {

    private final ConsoleHelper console;

    public PrettyPrinter(ConsoleHelper console) {
        this.console = console;
    }

    // =========================================================
    // RECITAL
    // =========================================================

    public void imprimirResumenRecital(Recital recital) {
        console.println("");
        console.println("══════════════════════════════════════════════");
        console.println("   🎵 RESUMEN DEL RECITAL");
        console.println("══════════════════════════════════════════════");
        console.println("Nombre      : " + recital.getNombre());
        console.println("Tipo        : " + recital.getTipoRecital());
        console.println("");

        List<Cancion> canciones = recital.getCanciones();
        long completas = canciones.stream().filter(Cancion::estaCompleta).count();
        long incompletas = canciones.size() - completas;

        console.println("Canciones   : " + canciones.size() +
                "  (✅ completas: " + completas +
                " | ⚠ incompletas: " + incompletas + ")");

        double costoTotal = recital.getCostoTotalRecital();
        console.println(String.format("Costo total : %.2f", costoTotal));

        Map<RolTipo, Integer> faltantes = recital.getRolesFaltantesTotales();
        if (faltantes.isEmpty()) {
            console.println("Roles faltantes en el recital: ninguno ✅");
        } else {
            console.println("Roles faltantes en el recital:");
            faltantes.forEach((rol, cant) ->
                    console.println("  - " + rol + ": " + cant));
        }

        console.println("");
        imprimirArtistasBase(recital);
        console.println("");
        imprimirArtistasContratados(recital.getArtistasContratados());
        console.println("══════════════════════════════════════════════");
        console.println("");
    }

    private void imprimirArtistasBase(Recital recital) {
        List<ArtistaBase> base = recital.getArtistasBase();
        console.println("👥 Artistas base (" + base.size() + "):");
        if (base.isEmpty()) {
            console.println("  (no hay artistas base configurados)");
            return;
        }
        for (ArtistaBase a : base) {
            String roles = a.getRolesHistoricos().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            console.println("  - " + a.getNombre() + " | roles: [" + roles + "]");
        }
    }

    public void imprimirEstadosRecital(List<EstadoRecitalInfo> estados) {
        System.out.println("--------------------------------------------------");
        System.out.println("Estados de recital guardados:");
        for (int i = 0; i < estados.size(); i++) {
            EstadoRecitalInfo info = estados.get(i);
            System.out.printf(
                    "%2d) %-15s | Recital: %-30s | Tipo: %-6s | Canciones: %2d (completas: %2d) | Costo total: %.2f%n",
                    i + 1,
                    info.nombreLogico(),
                    info.nombreRecital(),
                    info.tipoRecital(),
                    info.cantidadCanciones(),
                    info.cancionesCompletas(),
                    info.costoTotal()
            );
        }
        System.out.println("--------------------------------------------------");
    }

    // =========================================================
    // CANCIONES
    // =========================================================

    public void imprimirListadoCanciones(Recital recital) {
        List<Cancion> canciones = recital.getCanciones();
        console.println("");
        console.println("══════════════════════════════════════════════");
        console.println("   🎶 LISTADO DE CANCIONES");
        console.println("══════════════════════════════════════════════");

        if (canciones.isEmpty()) {
            console.println("(No hay canciones cargadas)");
            return;
        }

        console.println(String.format("%-3s %-35s %-12s %-10s %-20s",
                "#", "Título", "Estado", "Costo", "Roles faltantes"));
        console.println("─".repeat(90));

        int idx = 1;
        for (Cancion c : canciones) {
            String estado = c.estaCompleta() ? "COMPLETA" : "INCOMPLETA";
            double costo = c.getCostoTotal(recital.getArtistasBase());
            List<RolRequerido> faltantes = c.getRolesFaltantes();

            String faltantesStr;
            if (faltantes.isEmpty()) {
                faltantesStr = "-";
            } else {
                Map<RolTipo, Long> agrupados = faltantes.stream()
                        .collect(Collectors.groupingBy(RolRequerido::getTipoRol, Collectors.counting()));
                faltantesStr = agrupados.entrySet().stream()
                        .map(e -> e.getKey() + "(" + e.getValue() + ")")
                        .collect(Collectors.joining(", "));
            }

            console.println(String.format("%-3d %-35s %-12s %-10.2f %-20s",
                    idx++, c.getTitulo(), estado, costo, faltantesStr));
        }

        console.println("══════════════════════════════════════════════");
        console.println("");
    }

    public void imprimirDetalleCancion(Cancion cancion, Recital recital) {
        console.println("");
        console.println("══════════════════════════════════════════════");
        console.println("   🎼 DETALLE DE CANCIÓN");
        console.println("══════════════════════════════════════════════");
        console.println("Título       : " + cancion.getTitulo());
        console.println("Estado       : " + (cancion.estaCompleta() ? "COMPLETA ✅" : "INCOMPLETA ⚠"));
        console.println(String.format("Costo actual : %.2f",
                cancion.getCostoTotal(recital.getArtistasBase())));
        console.println("");

        console.println(String.format("%-15s %-25s %-10s %-10s",
                "Rol", "Artista asignado", "Tipo", "Base/Ext"));
        console.println("─".repeat(70));

        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            Artista artista = rol.getArtistaAsignado();
            String nombreArtista;
            String tipoArtista;
            String etiqueta;

            if (artista == null) {
                nombreArtista = "(sin asignar)";
                tipoArtista = "-";
                etiqueta = "-";
            } else {
                nombreArtista = artista.getNombre();
                tipoArtista = artista.getClass().getSimpleName();
                etiqueta = artista.esExterno() ? "EXTERNO" : "BASE";
            }

            console.println(String.format("%-15s %-25s %-10s %-10s",
                    rol.getTipoRol(),
                    nombreArtista,
                    tipoArtista,
                    etiqueta));
        }

        console.println("══════════════════════════════════════════════");
        console.println("");
    }

    public void listadoCancionesNumerado(List<Cancion> canciones, List<ArtistaBase> base) {
        for (int i = 0; i < canciones.size(); i++) {
            Cancion c = canciones.get(i);
            double costo = c.getCostoTotal(base);
            String estado = c.estaCompleta() ? "COMPLETA" : "INCOMPLETA";
            System.out.printf(
                    "%d) %-30s  [%-10s]  Costo: %.2f%n",
                    i + 1,
                    c.getTitulo(),
                    estado,
                    costo
            );
        }
    }

    // =========================================================
    // ARTISTAS EXTERNOS
    // =========================================================

    public void imprimirArtistasContratados(List<ArtistaExterno> contratados) {
        System.out.println("--------------------------------------------------");
        if (contratados.isEmpty()) {
            System.out.println("Todavía no hay artistas externos contratados.");
        } else {
            System.out.println("Artistas externos contratados:");
            for (ArtistaExterno a : contratados) {
                System.out.printf(
                        "- %-20s | roles: %-35s | canciones: %d/%d | costo base: %.2f%n",
                        a.getNombre(),
                        a.getRolesHistoricos(),
                        a.getCancionesAsignadasEnRecital(),
                        a.getMaxCanciones(),
                        a.getCostoBase()
                );
            }
        }
        System.out.println("--------------------------------------------------");
    }

    public void imprimirResumenArtistaExterno(ArtistaExterno a, Recital recital) {
        String roles = a.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        double costoUnitario = a.getCostoFinal(recital.getArtistasBase());
        int cancionesAsignadas = a.getCancionesAsignadasEnRecital();
        double costoTotalAprox = costoUnitario * cancionesAsignadas;

        console.println("  - " + a.getNombre());
        console.println("      Roles          : " + roles);
        console.println("      Max canciones  : " + a.getMaxCanciones());
        console.println("      Asignadas      : " + cancionesAsignadas);
        console.println(String.format("      Costo base     : %.2f", a.getCostoBase()));
        console.println(String.format("      Costo unit. c/desc.: %.2f", costoUnitario));
        console.println(String.format("      Costo total aprox  : %.2f", costoTotalAprox));
        if (a.getTipoRecitalPreferido() != null) {
            console.println("      Prefiere recital : " + a.getTipoRecitalPreferido());
        }
    }

    public void listadoArtistasExternosNumerado(List<ArtistaExterno> externos) {
        for (int i = 0; i < externos.size(); i++) {
            ArtistaExterno a = externos.get(i);
            System.out.printf(
                    "%d) %-20s | roles: %-40s | max: %d | asignadas: %d | costo base: %.2f%n",
                    i + 1,
                    a.getNombre(),
                    a.getRolesHistoricos(),
                    a.getMaxCanciones(),
                    a.getCancionesAsignadasEnRecital(),
                    a.getCostoBase()
            );
        }
    }

    // =========================================================
    // ROLES
    // =========================================================

    public void listadoRolesNumerado(RolTipo[] roles) {
        for (int i = 0; i < roles.length; i++) {
            System.out.printf("%d) %s%n", i + 1, roles[i]);
        }
    }

    public void imprimirRolesFaltantes(String contexto, Map<RolTipo, Integer> faltantes) {
        System.out.println("--------------------------------------------------");
        System.out.println("Roles faltantes para " + contexto + ":");

        if (faltantes == null || faltantes.isEmpty()) {
            System.out.println("No hay roles faltantes.");
        } else {
            for (Map.Entry<RolTipo, Integer> entry : faltantes.entrySet()) {
                System.out.printf("- %-20s : %d%n",
                        entry.getKey(),
                        entry.getValue());
            }
        }

        System.out.println("--------------------------------------------------");
    }
}
