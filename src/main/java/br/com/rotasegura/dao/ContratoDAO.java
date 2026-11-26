package br.com.rotasegura.dao;

import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.modelo.Cliente;
import br.com.rotasegura.modelo.Contrato;
import br.com.rotasegura.modelo.Veiculo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoDAO implements DAO<Contrato, Integer> {

    private static final String SELECT = "SELECT ct.id, ct.data_inicio, ct.data_fim_prevista, "
            + "ct.data_devolucao_efetiva, ct.ativo, ct.valor_final, "
            + "cl.cpf, cl.nome, cl.cnh, "
            + "v.placa, v.tipo, v.marca, v.modelo, v.ano, v.valor_base_diaria, v.disponivel, "
            + "v.possui_ar_condicionado, v.capacidade_porta_malas, v.tracao_4x4 "
            + "FROM contrato ct "
            + "JOIN cliente cl ON cl.cpf = ct.cpf_cliente "
            + "JOIN veiculo v ON v.placa = ct.placa_veiculo ";

    @Override
    public void inserir(Contrato c) throws PersistenciaException {
        String sql = "INSERT INTO contrato (cpf_cliente, placa_veiculo, data_inicio, data_fim_prevista, "
                + "data_devolucao_efetiva, ativo, valor_final) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCliente().getCpf());
            ps.setString(2, c.getVeiculo().getPlaca());
            ps.setString(3, c.getDataInicio().toString());
            ps.setString(4, c.getDataFimPrevista().toString());
            ps.setString(5, c.getDataDevolucaoEfetiva() == null ? null : c.getDataDevolucaoEfetiva().toString());
            ps.setInt(6, c.isAtivo() ? 1 : 0);
            if (c.getValorFinal() == null) {
                ps.setNull(7, java.sql.Types.REAL);
            } else {
                ps.setDouble(7, c.getValorFinal());
            }
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    c.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir contrato: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Contrato c) throws PersistenciaException {
        String sql = "UPDATE contrato SET data_fim_prevista = ?, data_devolucao_efetiva = ?, ativo = ?, "
                + "valor_final = ? WHERE id = ?";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, c.getDataFimPrevista().toString());
            ps.setString(2, c.getDataDevolucaoEfetiva() == null ? null : c.getDataDevolucaoEfetiva().toString());
            ps.setInt(3, c.isAtivo() ? 1 : 0);
            if (c.getValorFinal() == null) {
                ps.setNull(4, java.sql.Types.REAL);
            } else {
                ps.setDouble(4, c.getValorFinal());
            }
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar contrato: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(Integer id) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement("DELETE FROM contrato WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao remover contrato: " + e.getMessage(), e);
        }
    }

    @Override
    public Contrato buscarPorId(Integer id) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(SELECT + "WHERE ct.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar contrato: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contrato> listarTodos() throws PersistenciaException {
        return buscarPorTermo("");
    }

    /** Consulta por número do contrato, nome/CPF do cliente, placa ou modelo do veículo. */
    public List<Contrato> buscarPorTermo(String termo) throws PersistenciaException {
        String sql = SELECT + "WHERE CAST(ct.id AS TEXT) LIKE ? OR cl.nome LIKE ? OR cl.cpf LIKE ? "
                + "OR v.placa LIKE ? OR v.modelo LIKE ? ORDER BY ct.id DESC";
        List<Contrato> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            String filtro = "%" + termo.trim() + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar contratos: " + e.getMessage(), e);
        }
        return lista;
    }

    public int contarPorCliente(String cpf) throws PersistenciaException {
        return contar("SELECT COUNT(*) FROM contrato WHERE cpf_cliente = ?", cpf);
    }

    public int contarPorVeiculo(String placa) throws PersistenciaException {
        return contar("SELECT COUNT(*) FROM contrato WHERE placa_veiculo = ?", placa);
    }

    private int contar(String sql, String parametro) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao consultar contratos: " + e.getMessage(), e);
        }
    }

    private static Contrato mapear(ResultSet rs) throws SQLException {
        Cliente cliente = ClienteDAO.mapear(rs);
        Veiculo veiculo = VeiculoDAO.mapear(rs);
        String devolucao = rs.getString("data_devolucao_efetiva");
        double valor = rs.getDouble("valor_final");
        Double valorFinal = rs.wasNull() ? null : valor;
        return new Contrato(rs.getInt("id"), cliente, veiculo,
                LocalDate.parse(rs.getString("data_inicio")),
                LocalDate.parse(rs.getString("data_fim_prevista")),
                devolucao == null ? null : LocalDate.parse(devolucao),
                rs.getInt("ativo") == 1, valorFinal);
    }
}
