package com.paulomarchon.parking.veiculo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class Placa {

    @Column(name = "placa", nullable = false, unique = true)
    private final String placa;

    private Placa(String placa) {
        if (!isValid(placa))
            throw new IllegalArgumentException("Invalid placa");

        this.placa = placa;
    }

    protected Placa() {
        this.placa = null;
    }

    public static Placa from(final String placa) {
        return new Placa(placa);
    }

    public String getPlaca() {
        return placa;
    }

    private boolean isValid(String placa) {
        return true; //TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placa placa1 = (Placa) o;
        return Objects.equals(placa, placa1.placa);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(placa);
    }
}
