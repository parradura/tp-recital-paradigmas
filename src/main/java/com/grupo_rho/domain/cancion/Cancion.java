package com.grupo_rho.domain.cancion;

import com.grupo_rho.domain.artista.Artista;
import com.grupo_rho.domain.artista.ArtistaBase;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
public class Cancion {

    @Getter
    private final String titulo;
    private final List<RolRequerido> rolesRequeridos;

    public Cancion(String titulo, List<RolRequerido> rolesRequeridos) {
        this.titulo = titulo;
        this.rolesRequeridos = new ArrayList<>(rolesRequeridos);
    }

    public List<RolRequerido> getRolesRequeridos() {
        return Collections.unmodifiableList(rolesRequeridos);
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
            if (artista != null) {
                total += artista.getCostoFinal(artistasBase);
            }
        }
        return total;
    }

    public void asignarArtista(RolRequerido rol, Artista artista) {
        if (rol == null || artista == null) {
            throw new IllegalArgumentException("Rol y artista no pueden ser null");
        }
        if (!rolesRequeridos.contains(rol)) {
            throw new IllegalArgumentException("El rol no pertenece a la canción " + titulo);
        }
        for (RolRequerido otroRol : rolesRequeridos) {
            if (otroRol.getArtistaAsignado() == artista) {
                throw new IllegalArgumentException("El artista " + artista.getNombre()
                        + " ya está asignado a la canción " + titulo);
            }
        }
        if (!artista.puedeTocar(rol.getTipoRol())) {
            throw new IllegalArgumentException("El artista " + artista.getNombre()
                    + " no puede tocar el rol " + rol.getTipoRol());
        }

        rol.asignar(artista);
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
