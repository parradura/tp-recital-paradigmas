package com.grupo_rho.integration.prolog;

import com.grupo_rho.domain.artista.RolTipo;
import org.jpl7.*;

import java.lang.Integer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrologEntrenamientoClient {

    public PrologEntrenamientoClient(String rutaRelativaPl) {
        // Construimos la ruta absoluta al archivo .pl
        String absPath = Path.of(rutaRelativaPl).toAbsolutePath()
                .toString()
                .replace("\\", "/");

        // consult('ruta')
        Query q = new Query(
                "consult",
                new Term[]{new Atom(absPath)}
        );

        if (!q.hasSolution()) {
            throw new IllegalStateException("No se pudo consultar el archivo Prolog: " + absPath);
        }
    }

    /**
     * Llama al predicado Prolog entrenamientos_minimos/2.
     *
     * @param faltantesPorRol map de RolTipo -> lista de faltantes por canción
     * @return número total de entrenamientos mínimos
     */
    public int calcularEntrenamientosMinimos(Map<RolTipo, List<Integer>> faltantesPorRol) {
        // Construimos una lista de listas de enteros para Prolog.
        // Cada sub-lista es FaltantesRol para un rol.
        List<Term> listasPorRol = new ArrayList<>();

        for (List<Integer> faltantes : faltantesPorRol.values()) {
            int[] arr = faltantes.stream().mapToInt(Integer::intValue).toArray();
            Term listaFaltantes = Util.intArrayToList(arr); // [n1,n2,...]
            listasPorRol.add(listaFaltantes);
        }

        // Lista de listas: [[...], [...], ...]
        Term listaDeListas = Util.termArrayToList(listasPorRol.toArray(new Term[0]));

        Variable totalVar = new Variable("Total");
        Query q = new Query(
                "entrenamientos_minimos",
                new Term[]{listaDeListas, totalVar}
        );

        Map<String, Term> sol = q.oneSolution();
        return sol.get("Total").intValue();
    }
}
