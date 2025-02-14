package com.paulomarchon.parking.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {
    Optional<Veiculo> findByPlaca(Placa placa);
    boolean existsByPlaca(Placa placa);
}
