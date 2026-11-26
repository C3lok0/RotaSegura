package br.com.rotasegura.dao;

import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.modelo.Veiculo;
import br.com.rotasegura.modelo.VeiculoPopular;
import br.com.rotasegura.modelo.VeiculoSUV;
import br.com.rotasegura.modelo.VeiculoSedan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO implements DAO<Veiculo, String> {

    private static final String COLUNAS = "placa, tipo, marca, modelo, ano, valor_base_diaria, disponivel, "
            + "possui_ar_condicionado, capacidade_porta_malas, tracao_4x4";

    @Override
    public void inserir(Veiculo v) throws PersistenciaException {
        String sql = "INSERT INTO veiculo (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getCategoria().toUpperCase());
            ps.setString(3, v.getMarca());
            ps.setString(4, v.getModelo());
            ps.setInt(5, v.getAno());
            ps.setDouble(6, v.getValorBaseDiaria());
            ps.setInt(7, v.isDisponivel() ? 1 : 0);
            preencherEspecificos(ps, 8, v);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir veículo: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Veiculo v) throws PersistenciaException {
        String sql = "UPDATE veiculo SET marca = ?, modelo = ?, ano = ?, valor_base_diaria = ?, disponivel = ?, "
                + "possui_ar_condicionado = ?, capacidade_porta_malas = ?, tracao_4x4 = ? WHERE placa = ?";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, v.getMarca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAno());
            ps.setDouble(4, v.getValorBaseDiaria());
            ps.setInt(5, v.isDisponivel() ? 1 : 0);
            preencherEspecificos(ps, 6, v);
            ps.setString(9, v.getPlaca());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar veículo: " + e.getMessage(), e);
        }
    }

    /** Altera somente o status (disponível/locado) do veículo. */
    public void atualizarDisponibilidade(String placa, boolean disponivel) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao()
                .prepareStatement("UPDATE veiculo SET disponivel = ? WHERE placa = ?")) {
            ps.setInt(1, disponivel ? 1 : 0);
            ps.setString(2, placa);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar status do veículo: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String placa) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement("DELETE FROM veiculo WHERE placa = ?")) {
            ps.setString(1, placa);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao remover veículo: " + e.getMessage(), e);
        }
    }

    @Override
    public Veiculo buscarPorId(String placa) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM veiculo WHERE placa = ?";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar veículo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Veiculo> listarTodos() throws PersistenciaException {
        return buscarPorTermo("");
    }

    /** Consulta por parte da placa, marca ou modelo. */
    public List<Veiculo> buscarPorTermo(String termo) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM veiculo WHERE placa LIKE ? OR marca LIKE ? OR modelo LIKE ? ORDER BY placa";
        List<Veiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            String filtro = "%" + termo.trim() + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar veículos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Veiculo> listarDisponiveis() throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM veiculo WHERE disponivel = 1 ORDER BY placa";
        List<Veiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar veículos disponíveis: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Preenche as 3 colunas específicas de cada categoria (as demais ficam NULL). */
    private void preencherEspecificos(PreparedStatement ps, int primeiro, Veiculo v) throws SQLException {
        ps.setNull(primeiro, Types.INTEGER);
        ps.setNull(primeiro + 1, Types.REAL);
        ps.setNull(primeiro + 2, Types.INTEGER);
        if (v instanceof VeiculoPopular p) {
            ps.setInt(primeiro, p.isPossuiArCondicionado() ? 1 : 0);
        } else if (v instanceof VeiculoSedan s) {
            ps.setDouble(primeiro + 1, s.getCapacidadePortaMalasLitros());
        } else if (v instanceof VeiculoSUV s) {
            ps.setInt(primeiro + 2, s.isTracao4x4() ? 1 : 0);
        }
    }

    /** Lê as colunas de veículo da linha atual e instancia a subclasse correta. */
    static Veiculo mapear(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        String placa = rs.getString("placa");
        String marca = rs.getString("marca");
        String modelo = rs.getString("modelo");
        int ano = rs.getInt("ano");
        double base = rs.getDouble("valor_base_diaria");
        Veiculo v = switch (tipo) {
            case "POPULAR" -> new VeiculoPopular(placa, marca, modelo, ano, base, rs.getInt("possui_ar_condicionado") == 1);
            case "SEDAN" -> new VeiculoSedan(placa, marca, modelo, ano, base, rs.getDouble("capacidade_porta_malas"));
            case "SUV" -> new VeiculoSUV(placa, marca, modelo, ano, base, rs.getInt("tracao_4x4") == 1);
            default -> throw new SQLException("Tipo de veículo desconhecido no banco: " + tipo);
        };
        v.setDisponivel(rs.getInt("disponivel") == 1);
        return v;
    }
}
