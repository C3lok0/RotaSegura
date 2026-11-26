package br.com.rotasegura.dao;

import br.com.rotasegura.excecao.PersistenciaException;

import java.util.List;

/**
 * Contrato genérico do padrão DAO.
 *
 * @param <T> tipo da entidade
 * @param <K> tipo da chave primária
 */
public interface DAO<T, K> {
    void inserir(T entidade) throws PersistenciaException;
    void atualizar(T entidade) throws PersistenciaException;
    void remover(K chave) throws PersistenciaException;
    T buscarPorId(K chave) throws PersistenciaException;
    List<T> listarTodos() throws PersistenciaException;
}
