package com.paulomarchon.parking.veiculo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VeiculoRepositoryTest {

    @Autowired
    VeiculoRepository veiculoRepository;
    @Autowired
    TestEntityManager entityManager;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        veiculoRepository.deleteAll();
    }

    @Test
    @DisplayName("")
    void deveRetornarVeiculo_QuandoPlacaEstiverCadastrada() {
        Placa placa = Placa.from("ABC1234");
        Veiculo veiculo = new Veiculo("marca", "modelo", "cor", placa, TipoVeiculo.CARRO);
        entityManager.persist(veiculo);

        Optional<Veiculo> teste = veiculoRepository.findByPlaca(placa);

        assertThat(teste).isPresent();
    }

    @Test
    @DisplayName("")
    void deveRetornarVeiculoVazio_QuandoPlacaNaoEstiverCadastrada() {
        Placa placa = Placa.from("ABC1234");
        Veiculo veiculo = new Veiculo("marca", "modelo", "cor", placa, TipoVeiculo.CARRO);
        entityManager.persist(veiculo);

        Optional<Veiculo> teste = veiculoRepository.findByPlaca(Placa.from("JHG6543"));

        assertThat(teste).isEmpty();
    }

    @Test
    @DisplayName("")
    void deveVerificarExistenciaDaPlacaDoVeiculo_QuandoPlacaEstiverCadastrada() {
        Placa placa = Placa.from("ABC1234");
        Veiculo veiculo = new Veiculo("marca", "modelo", "cor", placa, TipoVeiculo.CARRO);
        veiculoRepository.save(veiculo);

        var atual = veiculoRepository.existsByPlaca(placa);

        assertThat(atual).isTrue();
    }

    @Test
    @DisplayName("")
    void deveVerificarExistenciaDaPlacaDoVeiculo_QuandoPlacaNaoEstiverCadastrada() {
        Placa placa = Placa.from("ABC1234");
        Veiculo veiculo = new Veiculo(
                "marca",
                "modelo",
                "cor",
                Placa.from("EFG7654"),
                TipoVeiculo.CARRO
        );
        veiculoRepository.save(veiculo);

        var atual = veiculoRepository.existsByPlaca(placa);

        assertThat(atual).isFalse();
    }

}
