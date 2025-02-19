package com.paulomarchon.parking.veiculo;

import com.paulomarchon.parking.exception.RecursoDuplicadoException;
import com.paulomarchon.parking.exception.RecursoNaoEncontradoException;
import com.paulomarchon.parking.veiculo.payload.AtualizarVeiculoRequest;
import com.paulomarchon.parking.veiculo.payload.CadastroVeiculoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class VeiculoServiceTest {
    @Mock
    private VeiculoRepository veiculoRepository;

    private VeiculoService veiculoService;

    @BeforeEach
    void setUp() {
        veiculoService = new VeiculoService(veiculoRepository);
    }

    @Test
    @DisplayName("Deve retornar uma lista de todos os veiculos cadastrados")
    void deveRetornarTodosVeiculos() {
        veiculoService.buscarTodosVeiculos();

        verify(veiculoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar VeiculoDto quando o veiculo for encontrado por ID")
    void buscarVeiculoPorId_DeveRetornarVeiculoDto_QuandoVeiculoEncontrado() {
        //Given
        Integer id = 1;
        Veiculo veiculo = new Veiculo(id, "marca", "modelo", "cor", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        //When
        VeiculoDto resultado = veiculoService.buscarVeiculoPorId(id);

        //Then
        assertThat(VeiculoDto.from(veiculo)).isEqualTo(resultado);
        verify(veiculoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar excecao quando o veiculo nao for encontrado por ID")
    void buscarVeiculoPorId_DeveLancarException_QuandoVeiculoNaoEncontrado() {
        //Given
        Integer id = 1;
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        //When - Then
        assertThatThrownBy(() -> veiculoService.buscarVeiculoPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Veiculo nao encontrado!"));

        verify(veiculoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar VeiculoDto quando o veiculo for encontrado por placa")
    void buscarVeiculoPorPlaca_DeveRetornarVeiculoDto_QuandoVeiculoEncontrado() {
        //Given
        Placa placa = Placa.from("ABC1234");
        Veiculo veiculo = new Veiculo(1, "marca", "modelo", "cor", placa, TipoVeiculo.CARRO);
        when(veiculoRepository.findByPlaca(placa)).thenReturn(Optional.of(veiculo));

        //When
        VeiculoDto resultado = veiculoService.buscarVeiculoPorPlaca(placa);

        //Then
        assertThat(VeiculoDto.from(veiculo)).isEqualTo(resultado);
        verify(veiculoRepository, times(1)).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve lançar excecao quando o veiculo nao for encontrado por placa")
    void buscarVeiculoPorPlaca_DeveLancarException_QuandoVeiculoNaoEncontrado() {
        //Given
        Placa placa = Placa.from("ABC1234");
        when(veiculoRepository.findByPlaca(placa)).thenReturn(Optional.empty());

        //When - Then
        assertThatThrownBy(() -> veiculoService.buscarVeiculoPorPlaca(placa))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Veiculo nao encontrado!"));

        verify(veiculoRepository, times(1)).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve realizar o cadastro de um novo veiculo com sucesso")
    void cadastrarVeiculo_DeveCadastrarVeiculo_ComSucesso() {
        //Given
        Placa placa = Placa.from("ABC1234");
        when(veiculoRepository.existsByPlaca(placa)).thenReturn(false);

        CadastroVeiculoRequest veiculoRequest = new CadastroVeiculoRequest("MARCA", "MODELO", "COR", placa.getPlaca(), "CARRO");

        //When
        veiculoService.cadastrarVeiculo(veiculoRequest);

        //Then
        ArgumentCaptor<Veiculo> veiculoArgumentCaptor = ArgumentCaptor.forClass(Veiculo.class);

        verify(veiculoRepository, times(1)).save(veiculoArgumentCaptor.capture());

        Veiculo veiculo = veiculoArgumentCaptor.getValue();

        assertThat(veiculo.getMarca()).isEqualTo(veiculoRequest.marca());
        assertThat(veiculo.getModelo()).isEqualTo(veiculoRequest.modelo());
        assertThat(veiculo.getCor()).isEqualTo(veiculoRequest.cor());
        assertThat(veiculo.getPlaca().getPlaca()).isEqualTo(veiculoRequest.placa());
        assertThat(veiculo.getTipoVeiculo().name()).isEqualTo(veiculoRequest.tipoVeiculo());
    }

    @Test
    @DisplayName("Deve lancar excecao quando a placa do veiculo for duplicada ao cadastrar")
    void cadastrarVeiculo_DeveLancarException_QuandoPlacaForDuplicada() {
        //Given
        Placa placa = Placa.from("ABC1234");
        when(veiculoRepository.existsByPlaca(placa)).thenReturn(true);

        CadastroVeiculoRequest veiculoRequest = new CadastroVeiculoRequest("MARCA", "MODELO", "COR", placa.getPlaca(), "CARRO");

        //When
        assertThatThrownBy(() -> veiculoService.cadastrarVeiculo(veiculoRequest))
                .isInstanceOf(RecursoDuplicadoException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Placa ja cadastrada!"));

        //Then
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar o veiculo alterando todas as propriedades do veiculo")
    void atualizarVeiculo_DeveAtualizarVeiculo_AlterandoTodasPropriedades() {
        //Given
        int id = 1;
        Veiculo veiculo = new Veiculo(1, "MARCA", "MODELO", "COR", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        Placa novaPlaca = Placa.from("GHJ7654");
        AtualizarVeiculoRequest veiculoRequest = new AtualizarVeiculoRequest("NOVA MARCA", "NOVO MODELO", "NOVA COR", novaPlaca.getPlaca(), "MOTO");

        //When
        veiculoService.atualizarVeiculo(id, veiculoRequest);

        //Then
        ArgumentCaptor<Veiculo> veiculoArgumentCaptor = ArgumentCaptor.forClass(Veiculo.class);

        verify(veiculoRepository).save(veiculoArgumentCaptor.capture());
        Veiculo veiculoCapturado = veiculoArgumentCaptor.getValue();

        assertThat(veiculoCapturado.getMarca()).isEqualTo(veiculoRequest.marca());
        assertThat(veiculoCapturado.getModelo()).isEqualTo(veiculoRequest.modelo());
        assertThat(veiculoCapturado.getCor()).isEqualTo(veiculoRequest.cor());
        assertThat(veiculoCapturado.getPlaca().getPlaca()).isEqualTo(veiculoRequest.placa());
        assertThat(veiculoCapturado.getTipoVeiculo().name()).isEqualTo(veiculoRequest.tipoVeiculo());
    }

    @Test
    @DisplayName("Deve atualizar o veiculo alterando apenas a placa do veiculo")
    void atualizarVeiculo_DeveAtualizarVeiculo_AlterandoApenasPlacaDoVeiculo() {
        //Given
        int id = 1;
        Veiculo veiculo = new Veiculo(1, "MARCA", "MODELO", "COR", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        Placa novaPlaca = Placa.from("GHJ7654");

        AtualizarVeiculoRequest veiculoRequest = new AtualizarVeiculoRequest(null, null, null, novaPlaca.getPlaca(), null);
        when(veiculoRepository.existsByPlaca(novaPlaca)).thenReturn(false);

        //When
        veiculoService.atualizarVeiculo(id, veiculoRequest);

        //Then
        ArgumentCaptor<Veiculo> veiculoArgumentCaptor = ArgumentCaptor.forClass(Veiculo.class);

        verify(veiculoRepository).save(veiculoArgumentCaptor.capture());
        Veiculo veiculoCapturado = veiculoArgumentCaptor.getValue();

        assertThat(veiculoCapturado.getMarca()).isEqualTo(veiculo.getMarca());
        assertThat(veiculoCapturado.getModelo()).isEqualTo(veiculo.getModelo());
        assertThat(veiculoCapturado.getCor()).isEqualTo(veiculo.getCor());
        assertThat(veiculoCapturado.getPlaca()).isEqualTo(novaPlaca);
        assertThat(veiculoCapturado.getTipoVeiculo()).isEqualTo(veiculo.getTipoVeiculo());
    }

    @Test
    @DisplayName("Deve atualizar o veiculo alterando apenas a cor do veiculo")
    void atualizarVeiculo_DeveAtualizarVeiculo_AlterandoApenasCorDoVeiculo() {
        //Given
        int id = 1;
        Veiculo veiculo = new Veiculo(1, "MARCA", "MODELO", "COR", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        AtualizarVeiculoRequest veiculoRequest = new AtualizarVeiculoRequest(null, null, "AMARELO", null, null);

        //When
        veiculoService.atualizarVeiculo(id, veiculoRequest);

        //Then
        ArgumentCaptor<Veiculo> veiculoArgumentCaptor = ArgumentCaptor.forClass(Veiculo.class);

        verify(veiculoRepository).save(veiculoArgumentCaptor.capture());
        Veiculo veiculoCapturado = veiculoArgumentCaptor.getValue();

        assertThat(veiculoCapturado.getMarca()).isEqualTo(veiculo.getMarca());
        assertThat(veiculoCapturado.getModelo()).isEqualTo(veiculo.getModelo());
        assertThat(veiculoCapturado.getCor()).isEqualTo(veiculoRequest.cor());
        assertThat(veiculoCapturado.getPlaca()).isEqualTo(veiculo.getPlaca());
        assertThat(veiculoCapturado.getTipoVeiculo()).isEqualTo(veiculo.getTipoVeiculo());
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar um veiculo com uma placa duplicada")
    void atualizarVeiculo_DeveLancarException_QuandoPlacaForDuplicada() {
        //Given
        int id = 1;
        Veiculo veiculo = new Veiculo(1, "MARCA", "MODELO", "COR", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        Placa novaPlaca = Placa.from("GHJ7654");

        AtualizarVeiculoRequest veiculoRequest = new AtualizarVeiculoRequest(null, null, null, novaPlaca.getPlaca(), null);
        when(veiculoRepository.existsByPlaca(novaPlaca)).thenReturn(true);

        //When
        assertThatThrownBy(() -> veiculoService.atualizarVeiculo(id, veiculoRequest))
                .isInstanceOf(RecursoDuplicadoException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Placa ja cadastrada!"));

        //Then
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando nao alterar nenhuma propriedade do veiculo")
    void atualizarVeiculo_DeveLancarException_QuandoNaoHouverAlteracoes() {
        //Given
        int id = 1;
        Veiculo veiculo = new Veiculo(1, "MARCA", "MODELO", "COR", Placa.from("ABC1234"), TipoVeiculo.CARRO);
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        AtualizarVeiculoRequest veiculoRequest = new AtualizarVeiculoRequest(veiculo.getMarca(), veiculo.getModelo(), veiculo.getCor(), veiculo.getPlaca().getPlaca(), veiculo.getTipoVeiculo().name());

        //When
        assertThatThrownBy(() -> veiculoService.atualizarVeiculo(id, veiculoRequest))
                .isInstanceOf(RuntimeException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Nenhuma alteracao encontrada!"));

        //Then
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve remover o veiculo por ID")
    void removerVeiculo_DeveRemoverVeiculo_QuandoIdDoVeiculoExistir() {
        //Given
        int id = 1;

        when(veiculoRepository.existsById(id)).thenReturn(true);

        //When
        veiculoService.removerVeiculo(id);

        //Then
        verify(veiculoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lancar excecao quando o ID do veiculo nao existir")
    void removerVeiculo_DeveLancarException_QuandoIdDoVeiculoNaoExistir() {
        //Given
        int id = 1;
        when(veiculoRepository.existsById(id)).thenReturn(false);

        //When
        assertThatThrownBy(() -> veiculoService.removerVeiculo(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).isEqualTo("Veiculo nao encontrado!"));

        //Then
        verify(veiculoRepository, never()).deleteById(id);
    }
}
