package com.grupo_rho.dto;

import com.grupo_rho.domain.artista.ArtistaBase;
import com.grupo_rho.domain.artista.ArtistaExterno;
import com.grupo_rho.domain.cancion.Cancion;
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
