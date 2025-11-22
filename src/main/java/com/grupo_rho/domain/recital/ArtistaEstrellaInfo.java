package com.grupo_rho.domain.recital;

import com.grupo_rho.domain.artista.ArtistaExterno;

public record ArtistaEstrellaInfo(
        ArtistaExterno artista,
        double totalFacturado,
        double descuento
) {}