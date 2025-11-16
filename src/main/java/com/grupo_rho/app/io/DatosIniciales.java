package com.grupo_rho.app.io;

import com.grupo_rho.domain.ArtistaBase;
import com.grupo_rho.domain.ArtistaExterno;
import com.grupo_rho.domain.Cancion;
import lombok.Getter;

import java.util.List;

@Getter
public class DatosIniciales {

    private final List<ArtistaBase> artistasBase;
    private final List<ArtistaExterno> artistasExternos;
    private final List<Cancion> canciones;

    public DatosIniciales(List<ArtistaBase> artistasBase,
                          List<ArtistaExterno> artistasExternos,
                          List<Cancion> canciones) {
        this.artistasBase = artistasBase;
        this.artistasExternos = artistasExternos;
        this.canciones = canciones;
    }
}
