package com.paulomarchon.parking.veiculo.payload;

import jakarta.validation.constraints.NotBlank;

public record CadastroVeiculoRequest(
         @NotBlank String marca,
         @NotBlank String modelo,
         @NotBlank String cor,
         @NotBlank String placa,
         @NotBlank String tipoVeiculo
) {
}
