package br.com.rotasegura.dao;

import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.modelo.Cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements DAO<Cliente, String> {

    @Override
    public void inserir(Cliente c) throws PersistenciaException {
        String sql = "INSERT INTO cliente (cpf, nome, cnh) VALUES (?, ?, ?)";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, c.getCpf());
            ps.setString(2, c.getNome());
            ps.setString(3, c.getCnh());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Cliente c) throws PersistenciaException {
        String sql = "UPDATE cliente SET nome = ?, cnh = ? WHERE cpf = ?";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCnh());
            ps.setString(3, c.getCpf());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String cpf) throws PersistenciaException {
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement("DELETE FROM cliente WHERE cpf = ?")) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao remover cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Cliente buscarPorId(String cpf) throws PersistenciaException {
        String sql = "SELECT cpf, nome, cnh FROM cliente WHERE cpf = ?";
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> listarTodos() throws PersistenciaException {
        return buscarPorTermo("");
    }

    /** Consulta por parte do nome ou do CPF. */
    public List<Cliente> buscarPorTermo(String termo) throws PersistenciaException {
        String sql = "SELECT cpf, nome, cnh FROM cliente WHERE nome LIKE ? OR cpf LIKE ? ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexaoBD.getConexao().prepareStatement(sql)) {
            String filtro = "%" + termo.trim() + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Lê as colunas cpf, nome e cnh da linha atual. */
    static Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getString("cpf"), rs.getString("nome"), rs.getString("cnh"));
    }
}
