package com.grupo_rho.app.io;

import com.grupo_rho.domain.ArtistaBase;
import com.grupo_rho.domain.ArtistaExterno;
import com.grupo_rho.domain.Cancion;
import com.grupo_rho.domain.recital.TipoRecital;
import lombok.Getter;

import java.util.List;

@Getter
public class DatosIniciales {

    private final String nombreRecital;
    private final TipoRecital tipoRecital;
    private final List<ArtistaBase> artistasBase;
    private final List<ArtistaExterno> artistasExternos;
    private final List<Cancion> canciones;

    public DatosIniciales(String nombreRecital,
                          TipoRecital tipoRecital,
                          List<ArtistaBase> artistasBase,
                          List<ArtistaExterno> artistasExternos,
                          List<Cancion> canciones) {
        this.nombreRecital = nombreRecital;
        this.tipoRecital = tipoRecital;
        this.artistasBase = artistasBase;
        this.artistasExternos = artistasExternos;
        this.canciones = canciones;
    }
}
