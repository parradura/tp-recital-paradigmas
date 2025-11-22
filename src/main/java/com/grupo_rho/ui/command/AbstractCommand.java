package com.grupo_rho.ui.command;

import com.grupo_rho.service.RecitalService;
import com.grupo_rho.ui.ConsoleHelper;
import com.grupo_rho.ui.PrettyPrinter;
import com.grupo_rho.ui.SelectionHelper;
import com.grupo_rho.ui.UiContext;

public abstract class AbstractCommand implements Command {

    protected final RecitalService recitalService;
    protected final UiContext ui;

    protected AbstractCommand(RecitalService recitalService, UiContext ui) {
        this.recitalService = recitalService;
        this.ui = ui;
    }

    protected void println(String msg) {
        ui.console().println(msg);
    }

    protected void print(String msg) {
        ui.console().print(msg);
    }

    protected PrettyPrinter printer() {
        return ui.printer();
    }

    protected ConsoleHelper console() {
        return ui.console();
    }

    protected SelectionHelper selector() {
        return ui.selector();
    }
}
