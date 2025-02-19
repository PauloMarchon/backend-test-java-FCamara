package com.paulomarchon.parking.veiculo;

import com.paulomarchon.parking.exception.RecursoDuplicadoException;
import com.paulomarchon.parking.exception.RecursoNaoEncontradoException;
import com.paulomarchon.parking.veiculo.payload.AtualizarVeiculoRequest;
import com.paulomarchon.parking.veiculo.payload.CadastroVeiculoRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public List<VeiculoDto> buscarTodosVeiculos() {
        return veiculoRepository.findAll()
                .stream()
                .map(VeiculoDto::from)
                .collect(Collectors.toList());
    }

    public VeiculoDto buscarVeiculoPorId(Integer veiculoId) {
        return veiculoRepository.findById(veiculoId)
                .map(VeiculoDto::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Veiculo nao encontrado!"
                ));
    }

    public VeiculoDto buscarVeiculoPorPlaca(Placa placa) {
        return veiculoRepository.findByPlaca(placa)
                .map(VeiculoDto::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Veiculo nao encontrado!"
                ));

    }

    public VeiculoDto cadastrarVeiculo(CadastroVeiculoRequest cadastroVeiculoRequest) {
        Placa placa = Placa.from(cadastroVeiculoRequest.placa());

        if (veiculoRepository.existsByPlaca(placa))
            throw new RecursoDuplicadoException("Placa ja cadastrada!");

        TipoVeiculo tipoVeiculo = TipoVeiculo.valueOf(cadastroVeiculoRequest.tipoVeiculo());

        Veiculo veiculo = new Veiculo(
                cadastroVeiculoRequest.marca(),
                cadastroVeiculoRequest.modelo(),
                cadastroVeiculoRequest.cor(),
                placa,
                tipoVeiculo
        );

        veiculoRepository.save(veiculo);

        return VeiculoDto.from(veiculo);
    }

    public void atualizarVeiculo(Integer veiculoId, AtualizarVeiculoRequest atualizarVeiculoRequest) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Veiculo nao encontrado!"
                ));

        boolean alteracao = false;

        if (atualizarVeiculoRequest.marca() != null && !atualizarVeiculoRequest.marca().equals(veiculo.getMarca())) {
            veiculo.setMarca(atualizarVeiculoRequest.marca());
            alteracao = true;
        }

        if (atualizarVeiculoRequest.modelo() != null && !atualizarVeiculoRequest.modelo().equals(veiculo.getModelo())) {
            veiculo.setModelo(atualizarVeiculoRequest.modelo());
            alteracao = true;
        }

        if (atualizarVeiculoRequest.cor() != null && !atualizarVeiculoRequest.cor().equals(veiculo.getCor())) {
            veiculo.setCor(atualizarVeiculoRequest.cor());
            alteracao = true;
        }

        if (atualizarVeiculoRequest.placa() != null && !atualizarVeiculoRequest.placa().equals(veiculo.getPlaca().getPlaca())) {
            Placa placa = Placa.from(atualizarVeiculoRequest.placa());
            if (veiculoRepository.existsByPlaca(placa)) {
                throw new RecursoDuplicadoException("Placa ja cadastrada!");
            }
            veiculo.setPlaca(placa);
            alteracao = true;
        }

        if (atualizarVeiculoRequest.tipoVeiculo() != null && !atualizarVeiculoRequest.tipoVeiculo().equals(veiculo.getTipoVeiculo().name())) {
            TipoVeiculo tipoVeiculo = TipoVeiculo.valueOf(atualizarVeiculoRequest.tipoVeiculo());
            veiculo.setTipoVeiculo(tipoVeiculo);
            alteracao = true;
        }

        if (!alteracao)
            throw new RuntimeException("Nenhuma alteracao encontrada!");

        veiculoRepository.save(veiculo);
    }

    public void removerVeiculo(Integer veiculoId) {
        if (!veiculoRepository.existsById(veiculoId))
            throw new RecursoNaoEncontradoException("Veiculo nao encontrado!");

        veiculoRepository.deleteById(veiculoId);
    }
}
