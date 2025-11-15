package com.grupo_rho.domain;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@ToString
public class Cancion {

    private String titulo;
    private List<RolRequerido> rolesRequeridos;

    public Cancion(String titulo, List<RolRequerido> rolesRequeridos) {
        this.titulo = titulo;
        this.rolesRequeridos = new ArrayList<>(rolesRequeridos);
    }

    public List<RolRequerido> getRolesFaltantes() {
        List<RolRequerido> faltantes = new ArrayList<>();
        for (RolRequerido rol : rolesRequeridos) {
            if (!rol.estaCubierto()) {
                faltantes.add(rol);
            }
        }
        return faltantes;
    }

    public boolean estaCompleta() {
        return getRolesFaltantes().isEmpty();
    }

    public double getCostoTotal(Iterable<ArtistaBase> artistasBase) {
        double total = 0.0;
        for (RolRequerido rol : rolesRequeridos) {
            Artista artista = rol.getArtistaAsignado();
            if (artista instanceof ArtistaExterno externo) {
                total += externo.getCostoFinal(artistasBase);
            }
        }
        return total;
    }

    public boolean asignarArtista(RolRequerido rol, Artista artista) {
        if (rol == null || artista == null) return false;
        if (!rolesRequeridos.contains(rol)) return false;
        if (!artista.puedeTocar(rol.getTipoRol())) return false;

        rol.asignar(artista);
        return true;
    }

    public List<Artista> getArtistasAsignados() {
        List<Artista> resultado = new ArrayList<>();
        for (RolRequerido rol : rolesRequeridos) {
            if (rol.estaCubierto()) {
                resultado.add(rol.getArtistaAsignado());
            }
        }
        return Collections.unmodifiableList(resultado);
    }
}
