package com.paulomarchon.parking.veiculo;

public record VeiculoDto(
        String marca,
        String modelo,
        String cor,
        String placa,
        TipoVeiculo tipoVeiculo
) {

    static VeiculoDto from(Veiculo veiculo) {
        return new VeiculoDto(
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getCor(),
                veiculo.getPlaca().getPlaca(),
                veiculo.getTipoVeiculo()
        );
    }
}
