-- Script de criação do banco "Rota Segura" (SQLite).
-- É executado automaticamente na primeira inicialização da aplicação
-- (controlado por PRAGMA user_version), mas também pode ser usado manualmente.

CREATE TABLE IF NOT EXISTS cliente (
    cpf   TEXT PRIMARY KEY,
    nome  TEXT NOT NULL,
    cnh   TEXT NOT NULL
);

-- Hierarquia de veículos em uma única tabela (coluna "tipo" como discriminador).
-- As colunas específicas de cada subclasse ficam NULL nos outros tipos.
CREATE TABLE IF NOT EXISTS veiculo (
    placa                   TEXT PRIMARY KEY,
    tipo                    TEXT NOT NULL CHECK (tipo IN ('POPULAR', 'SEDAN', 'SUV')),
    marca                   TEXT NOT NULL,
    modelo                  TEXT NOT NULL,
    ano                     INTEGER NOT NULL,
    valor_base_diaria       REAL NOT NULL CHECK (valor_base_diaria > 0),
    disponivel              INTEGER NOT NULL DEFAULT 1 CHECK (disponivel IN (0, 1)),
    possui_ar_condicionado  INTEGER,
    capacidade_porta_malas  REAL,
    tracao_4x4              INTEGER
);

CREATE TABLE IF NOT EXISTS contrato (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    cpf_cliente             TEXT NOT NULL REFERENCES cliente (cpf),
    placa_veiculo           TEXT NOT NULL REFERENCES veiculo (placa),
    data_inicio             TEXT NOT NULL,
    data_fim_prevista       TEXT NOT NULL,
    data_devolucao_efetiva  TEXT,
    ativo                   INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
    valor_final             REAL
);

-- Carga inicial (mesmos dados de teste da parte 1)
INSERT INTO cliente (cpf, nome, cnh) VALUES ('12345678901', 'Carlos Silva', 'CNH123456');
INSERT INTO cliente (cpf, nome, cnh) VALUES ('98765432100', 'Ana Souza', 'CNH987654');

INSERT INTO veiculo (placa, tipo, marca, modelo, ano, valor_base_diaria, disponivel, possui_ar_condicionado)
VALUES ('ABC1234', 'POPULAR', 'Fiat', 'Uno', 2021, 80.0, 1, 1);
INSERT INTO veiculo (placa, tipo, marca, modelo, ano, valor_base_diaria, disponivel, capacidade_porta_malas)
VALUES ('XYZ9876', 'SEDAN', 'Toyota', 'Corolla', 2023, 180.0, 1, 470.0);
INSERT INTO veiculo (placa, tipo, marca, modelo, ano, valor_base_diaria, disponivel, tracao_4x4)
VALUES ('SUV5555', 'SUV', 'Jeep', 'Compass', 2024, 250.0, 1, 1);
