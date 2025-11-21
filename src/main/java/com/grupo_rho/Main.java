package com.grupo_rho;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.persistence.ConfigRecitalRepository;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.json.JsonConfigRecitalRepository;
import com.grupo_rho.persistence.json.JsonEstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.Menu;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.SelectionHelper;
import com.grupo_rho.ui.command.impl.*;

import java.util.Scanner;

public class Main {
    private static final String DATA_DIR = "data";
    private static final String ESTADOS_DIR = DATA_DIR + "/estados";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Sistema Discográfica - Recital Especial ===");

        Scanner scanner = new Scanner(System.in);
        ConsoleHelper console = new ConsoleHelper(scanner);
        ConfigRecitalRepository configRepo = new JsonConfigRecitalRepository(DATA_DIR);
        EstadoRecitalRepository estadoRepo = new JsonEstadoRecitalRepository(ESTADOS_DIR);
        PrettyPrinter printer = new PrettyPrinter(console);


        Recital recitalInicial;
        try {
            recitalInicial = configRepo.cargarRecitalInicial();
            System.out.println("Datos iniciales cargados desde JSON.");
        } catch (Exception e) {
            System.out.println("No se pudieron cargar los datos iniciales: " + e.getMessage());
            return;
        }

        RecitalService recitalService = new RecitalService(recitalInicial);
        SelectionHelper selector = new SelectionHelper(recitalService, console, printer);

        Menu menu = new Menu(scanner);
        menu.agregarComando(1, new ListarCancionesCommand(recitalService, printer));
        menu.agregarComando(2, new MostrarRolesFaltantesCancionCommand(recitalService, console, printer, selector));
        menu.agregarComando(3, new MostrarRolesFaltantesRecitalCommand(recitalService, console, printer));
        menu.agregarComando(4, new ContratarCancionCommand(recitalService, console, printer, selector));
        menu.agregarComando(5, new ContratarTodoRecitalCommand(recitalService, console));
        menu.agregarComando(6, new EntrenarArtistaCommand(recitalService, console, printer, selector));
        menu.agregarComando(7, new ListarArtistasContratadosCommand(recitalService, console, printer));
        menu.agregarComando(8, new GuardarEstadoCommand(recitalService, estadoRepo, console));
        menu.agregarComando(9, new CargarEstadoCommand(recitalService, estadoRepo, console, printer));
        menu.agregarComando(0, new SalirCommand(recitalService, estadoRepo, console));
        menu.iniciar();
    }
}