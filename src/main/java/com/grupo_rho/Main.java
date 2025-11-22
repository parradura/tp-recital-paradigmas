package com.grupo_rho;

import com.grupo_rho.domain.recital.Recital;
import com.grupo_rho.integration.prolog.PrologEntrenamientoClient;
import com.grupo_rho.persistence.ConfigRecitalRepository;
import com.grupo_rho.persistence.EstadoRecitalRepository;
import com.grupo_rho.persistence.json.JsonConfigRecitalRepository;
import com.grupo_rho.persistence.json.JsonEstadoRecitalRepository;
import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.*;
import com.grupo_rho.ui.command.commands.*;

import java.util.Scanner;

public class Main {
    private static final String DATA_DIR = "data";
    private static final String ESTADOS_DIR = DATA_DIR + "/estados";
    private static final String PROLOG_FILE = "src/main/resources/prolog/entrenamientos.pl";

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
        PrologEntrenamientoClient prologClient = new PrologEntrenamientoClient(PROLOG_FILE);
        UiContext ui = new UiContext(console, printer, selector);


        Menu menu = new Menu(scanner);
        menu.agregarComando(1, new ListarCancionesCommand(recitalService, ui));
        menu.agregarComando(2, new MostrarRolesFaltantesCancionCommand(recitalService, ui));
        menu.agregarComando(3, new MostrarRolesFaltantesRecitalCommand(recitalService, ui));
        menu.agregarComando(4, new ContratarCancionCommand(recitalService, ui));
        menu.agregarComando(5, new ContratarTodoRecitalCommand(recitalService, ui));
        menu.agregarComando(6, new EntrenarArtistaCommand(recitalService, ui));
        menu.agregarComando(7, new ListarArtistasContratadosCommand(recitalService, ui));
        menu.agregarComando(8, new GuardarEstadoCommand(recitalService, ui, estadoRepo));
        menu.agregarComando(9, new CargarEstadoCommand(recitalService, ui, estadoRepo));
        menu.agregarComando(10, new CalcularEntrenamientosMinimosCommand(recitalService, ui, prologClient));
        menu.agregarComando(11, new QuitarArtistaCommand(recitalService, ui));
        menu.agregarComando(0, new SalirCommand(recitalService, ui, estadoRepo));
        menu.iniciar();
    }
}