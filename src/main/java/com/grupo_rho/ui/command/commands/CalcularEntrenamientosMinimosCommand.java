package com.grupo_rho.ui.command.commands;

import com.grupo_rho.domain.artista.RolTipo;
import com.grupo_rho.integration.prolog.PrologEntrenamientoClient;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.UiContext;
import com.grupo_rho.ui.command.AbstractCommand;
import org.jpl7.JPLException;

import java.util.List;
import java.util.Map;

public class CalcularEntrenamientosMinimosCommand extends AbstractCommand {

    private final PrologEntrenamientoClient prologClient;

    public CalcularEntrenamientosMinimosCommand(RecitalService service,
                                                UiContext ui,
                                                PrologEntrenamientoClient prologClient
) {
        super(service, ui);
        this.prologClient = prologClient;
    }

    @Override
    public String getDescription() {
        return "Calcular entrenamientos mínimos (Prolog)";
    }

    @Override
    public void execute() {
        Map<RolTipo, List<Integer>> faltantesPorRol = recitalService.construirFaltantesParaProlog();

        if (faltantesPorRol.isEmpty()) {
            println("El recital no requiere ningún rol. Entrenamientos mínimos: 0.");
            return;
        }

        println("Roles considerados (faltantes por canción usando solo artistas base):");
        faltantesPorRol.forEach((rol, lista) ->
                println("  " + rol + " -> " + lista)
        );
        println("");

        try {
            int entrenamientos = prologClient.calcularEntrenamientosMinimos(faltantesPorRol);
            println("Entrenamientos mínimos necesarios (según Prolog): " + entrenamientos);
        } catch (JPLException e) {
            println("Error al llamar a Prolog: " + e.getMessage());
        } catch (Exception e) {
            println("Error inesperado en cálculo de entrenamientos: " + e.getMessage());
        }
    }
}
