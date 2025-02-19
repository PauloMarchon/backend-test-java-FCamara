package com.paulomarchon.parking.veiculo.payload;

public record AtualizarVeiculoRequest(
        String marca,
        String modelo,
        String cor,
        String placa,
        String tipoVeiculo
) {
}
