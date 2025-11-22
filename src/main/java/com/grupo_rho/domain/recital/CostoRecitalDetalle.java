package com.grupo_rho.domain.recital;

import com.grupo_rho.domain.artista.ArtistaExterno;

public record CostoRecitalDetalle(
        double totalSinEntrenamiento,
        double aumentoPorEntrenamientos,
        double descuentoPorBandas,
        double descuentoArtistaEstrella,
        double totalFinal,
        ArtistaExterno artistaEstrella
) { }