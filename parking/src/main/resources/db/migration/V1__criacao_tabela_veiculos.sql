CREATE TABLE veiculos (
    id SERIAL PRIMARY KEY,
    marca VARCHAR NOT NULL,
    modelo VARCHAR NOT NULL,
    cor VARCHAR NOT NULL,
    placa VARCHAR NOT NULL,
    tipo_veiculo VARCHAR NOT NULL
)