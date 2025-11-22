package com.grupo_rho.ui;

import com.grupo_rho.domain.artista.Artista;
import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.cancion.Cancion;
import com.grupo_rho.domain.cancion.RolRequerido;
import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.domain.recital.CostoRecitalDetalle;
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
        console.println("Nombre : " + recital.getNombre());
        console.println("Tipo   : " + recital.getTipoRecital());

        List<Cancion> canciones = recital.getCanciones();
        long completas   = canciones.stream().filter(Cancion::estaCompleta).count();
        long incompletas = canciones.size() - completas;

        console.println(String.format(
                "Canciones: %d  (✅ completas: %d | ⚠ incompletas: %d)",
                canciones.size(), completas, incompletas
        ));

        // Costos detallados
        CostoRecitalDetalle costos = recital.calcularCostoDetallado();

        console.println("");
        console.println("💰 Costos (resumen):");
        console.println(String.format(
                "  Base sin entrenam.: %8.2f | +Entren.: %8.2f | -Bandas: %8.2f",
                costos.totalSinEntrenamiento(),
                costos.aumentoPorEntrenamientos(),
                costos.descuentoPorBandas()
        ));
        console.println(String.format(
                "  -Artista estrella  : %8.2f | TOTAL FINAL: %8.2f",
                costos.descuentoArtistaEstrella(),
                costos.totalFinal()
        ));

        if (costos.artistaEstrella() != null) {
            console.println("⭐ Artista estrella invitado: " + costos.artistaEstrella().getNombre());
        } else {
            console.println("⭐ Artista estrella invitado: (no aplica)");
        }

        console.println("══════════════════════════════════════════════");
        console.println("");
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


    public void imprimirBloqueCancion(Cancion c, Recital recital, int indiceOpcional) {
        String prefijo = (indiceOpcional > 0) ? (indiceOpcional + ") ") : "- ";

        console.println(prefijo + c.getTitulo());
        console.println("   Estado : " + (c.estaCompleta() ? "COMPLETA ✅" : "INCOMPLETA ⚠"));
        console.println(String.format("   Costo  : %.2f", c.getCostoTotal(recital.getArtistasBase())));

        // Roles y artistas
        for (RolRequerido rol : c.getRolesRequeridos()) {
            Artista artista = rol.getArtistaAsignado();
            String nombreArtista = (artista != null) ? artista.getNombre() : "(sin asignar)";
            String tipoArtista = (artista != null && artista.esExterno()) ? "EXTERNO" : "BASE";
            console.println(
                    String.format("   - %-15s -> %-25s (%s)",
                            rol.getTipoRol(),
                            nombreArtista,
                            tipoArtista)
            );
        }
        console.println("");
    }

    /**
     * Listado compacto de canciones: 1 línea por canción.
     */
    public void imprimirListadoCanciones(Recital recital) {
        List<Cancion> canciones = recital.getCanciones();
        console.println("");
        console.println("══════════════════════════════════════════════");
        console.println("   🎶 LISTADO DE CANCIONES");
        console.println("══════════════════════════════════════════════");

        if (canciones.isEmpty()) {
            console.println("(No hay canciones cargadas)");
            console.println("══════════════════════════════════════════════");
            return;
        }

        console.println(String.format(
                "%-3s %-30s %-11s %-10s %-30s",
                "#", "Título", "Estado", "Costo", "Roles faltantes"
        ));
        console.println("─".repeat(90));

        int idx = 1;
        for (Cancion c : canciones) {
            imprimirCancionEnLista(idx++, c, recital);
        }

        console.println("══════════════════════════════════════════════");
        console.println("");
    }

    /**
     * Un ítem de canción para un listado (1 línea).
     */
    public void imprimirCancionEnLista(int index, Cancion c, Recital recital) {
        String estado = c.estaCompleta() ? "COMPLETA" : "INCOMPLETA";
        double costo  = c.getCostoTotal(recital.getArtistasBase());
        String faltantesStr = resumirRolesFaltantes(c);

        console.println(String.format(
                "%-3d %-30s %-11s %-10.2f %-30s",
                index,
                recortar(c.getTitulo(), 30),
                estado,
                costo,
                faltantesStr
        ));
    }

    /**
     * Vista detallada de una canción puntual.
     */
    public void imprimirDetalleCancion(Cancion cancion, Recital recital) {
        console.println("");
        console.println("══════════════════════════════════════════════");
        console.println("   🎼 DETALLE DE CANCIÓN");
        console.println("══════════════════════════════════════════════");
        console.println("Título : " + cancion.getTitulo());
        console.println("Estado : " + (cancion.estaCompleta() ? "COMPLETA ✅" : "INCOMPLETA ⚠"));
        console.println(String.format("Costo  : %.2f", cancion.getCostoTotal(recital.getArtistasBase())));
        console.println("");

        console.println(String.format(
                "%-15s %-22s %-10s %-8s",
                "Rol", "Artista asignado", "Tipo", "Base/Ext"
        ));
        console.println("─".repeat(70));

        for (RolRequerido rol : cancion.getRolesRequeridos()) {
            imprimirRolAsignadoEnDetalle(rol);
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
    // ARTISTAS BASE
    // =========================================================

    public void imprimirArtistasBase(Recital recital) {
        List<ArtistaBase> base = recital.getArtistasBase();
        console.println("👥 Artistas base (" + base.size() + "):");
        if (base.isEmpty()) {
            console.println("  (no hay artistas base configurados)");
            return;
        }

        console.println(String.format(
                "%-3s %-20s %-40s",
                "#", "Nombre", "Roles"
        ));
        console.println("─".repeat(70));

        for (int i = 0; i < base.size(); i++) {
            imprimirArtistaBaseEnLista(i + 1, base.get(i));
        }
        console.println("");
    }

    /**
     * Un ítem de artista base para un listado.
     */
    public void imprimirArtistaBaseEnLista(int index, ArtistaBase a) {
        String roles = a.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        console.println(String.format(
                "%-3d %-20s %-40s",
                index,
                recortar(a.getNombre(), 20),
                recortar(roles, 40)
        ));
    }

    // =========================================================
    // ARTISTAS EXTERNOS
    // =========================================================

    /**
     * Listado compacto de artistas externos contratados.
     */
    public void imprimirArtistasContratados(List<ArtistaExterno> contratados, Recital recital) {
        console.println("══════════════════════════════════════════════");
        console.println("   👤 ARTISTAS EXTERNOS CONTRATADOS");
        console.println("══════════════════════════════════════════════");

        if (contratados.isEmpty()) {
            console.println("No hay artistas externos contratados.");
            console.println("══════════════════════════════════════════════");
            return;
        }

        console.println(String.format(
                "%-3s %-18s %-25s %-17s %-11s %-11s %-11s %-8s",
                "#", "Nombre", "Roles", "Entrenamientos", "Base orig", "Base act.", "Base final", "Asig/max"
        ));
        console.println("─".repeat(110));

        for (int i = 0; i < contratados.size(); i++) {
            imprimirArtistaExternoEnLista(i + 1, contratados.get(i), recital);
        }

        console.println("══════════════════════════════════════════════");
        console.println("");
    }

    /**
     * Un ítem de artista externo para un listado.
     */
    public void imprimirArtistaExternoEnLista(int index, ArtistaExterno a, Recital recital) {
        String roles = a.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        int entrenamientos       = a.getRolesEntrenados().size();
        double factorEntrenamiento = Math.pow(1.5, entrenamientos);
        double baseOriginal      = a.getCostoBase() / factorEntrenamiento;
        double baseActual        = a.getCostoBase();
        double unitConDesc       = a.getCostoFinal(recital.getArtistasBase());
        String asigMax           = a.getCancionesAsignadasEnRecital() + "/" + a.getMaxCanciones();

        console.println(String.format(
                "%-3d %-18s %-25s %-17d %-11.2f %-11.2f %-11.2f %-8s",
                index,
                recortar(a.getNombre(), 18),
                recortar(roles, 25),
                entrenamientos,
                baseOriginal,
                baseActual,
                unitConDesc,
                asigMax
        ));
    }

    /**
     * Detalle multi-línea de un artista externo puntual.
     */
    public void imprimirResumenArtistaExterno(ArtistaExterno a, Recital recital) {
        String roles = a.getRolesHistoricos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        int entrenamientos       = a.getRolesEntrenados().size();
        double factorEntrenamiento = Math.pow(1.5, entrenamientos);
        double baseOriginal      = a.getCostoBase() / factorEntrenamiento;
        double baseActual        = a.getCostoBase();
        double unitConDesc       = a.getCostoFinal(recital.getArtistasBase());
        int cancionesAsignadas   = a.getCancionesAsignadasEnRecital();
        double costoTotalAprox   = unitConDesc * cancionesAsignadas;

        console.println("");
        console.println("Detalle de artista externo:");
        console.println("  Nombre           : " + a.getNombre());
        console.println("  Roles            : " + roles);
        console.println("  Entrenamientos   : " + entrenamientos);
        console.println(String.format("  Base original    : %.2f", baseOriginal));
        console.println(String.format("  Base actual      : %.2f", baseActual));
        console.println(String.format("  Unit. c/desc.    : %.2f", unitConDesc));
        console.println("  Asignadas / max  : " + cancionesAsignadas + "/" + a.getMaxCanciones());
        console.println(String.format("  Costo total aprox: %.2f", costoTotalAprox));
        if (a.getTipoRecitalPreferido() != null) {
            console.println("  Prefiere recital : " + a.getTipoRecitalPreferido());
        }
        console.println("");
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

    private void imprimirRolAsignadoEnDetalle(RolRequerido rol) {
        Artista artista = rol.getArtistaAsignado();

        String nombreArtista;
        String tipoArtista;
        String etiqueta;

        if (artista == null) {
            nombreArtista = "(sin asignar)";
            tipoArtista   = "-";
            etiqueta      = "-";
        } else {
            nombreArtista = artista.getNombre();
            tipoArtista   = artista.getClass().getSimpleName();
            etiqueta      = artista.esExterno() ? "EXT" : "BASE";
        }

        console.println(String.format(
                "%-15s %-22s %-10s %-8s",
                rol.getTipoRol(),
                recortar(nombreArtista, 22),
                tipoArtista,
                etiqueta
        ));
    }

    private String resumirRolesFaltantes(Cancion c) {
        List<RolRequerido> faltantes = c.getRolesFaltantes();
        if (faltantes.isEmpty()) return "-";

        Map<RolTipo, Long> agrupados = faltantes.stream()
                .collect(Collectors.groupingBy(RolRequerido::getTipoRol, Collectors.counting()));

        return agrupados.entrySet().stream()
                .map(e -> e.getKey() + "(" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

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

    // =========================================================
    // UTILS
    // =========================================================

    private String recortar(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }
}
