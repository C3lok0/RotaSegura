package br.com.rotasegura.dao;

import br.com.rotasegura.excecao.PersistenciaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a conexão com o banco SQLite, a criação inicial das tabelas e as transações.
 * O arquivo do banco é "rotasegura.db" na pasta de execução
 * (pode ser trocado com -Drotasegura.db=caminho/arquivo.db).
 */
public final class ConexaoBD {

    private static final String ARQUIVO = System.getProperty("rotasegura.db", "rotasegura.db");
    private static final String SCRIPT = "/db/rota_segura.sql";
    private static final int VERSAO_ESQUEMA = 1;

    private static Connection conexao;

    private ConexaoBD() {
    }

    public static synchronized Connection getConexao() throws PersistenciaException {
        try {
            if (conexao == null || conexao.isClosed()) {
                conexao = DriverManager.getConnection("jdbc:sqlite:" + ARQUIVO);
                try (Statement st = conexao.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return conexao;
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível abrir o banco de dados (" + ARQUIVO + "): "
                    + e.getMessage() + ". Verifique se a dependência sqlite-jdbc foi carregada pelo Maven.", e);
        }
    }

    /** Cria as tabelas e a carga inicial na primeira execução. */
    public static void inicializar() throws PersistenciaException {
        Connection con = getConexao();
        int versao;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            versao = rs.getInt(1);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao consultar a versão do banco: " + e.getMessage(), e);
        }
        if (versao >= VERSAO_ESQUEMA) {
            return;
        }
        iniciarTransacao();
        try (Statement st = con.createStatement()) {
            for (String comando : lerScript()) {
                st.execute(comando);
            }
            confirmarTransacao();
        } catch (SQLException | PersistenciaException e) {
            desfazerTransacao();
            if (e instanceof PersistenciaException pe) {
                throw pe;
            }
            throw new PersistenciaException("Erro ao criar o banco de dados: " + e.getMessage(), e);
        }
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA user_version = " + VERSAO_ESQUEMA);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao registrar a versão do banco: " + e.getMessage(), e);
        }
    }

    private static java.util.List<String> lerScript() throws PersistenciaException {
        try (InputStream in = ConexaoBD.class.getResourceAsStream(SCRIPT)) {
            if (in == null) {
                throw new PersistenciaException("Script SQL não encontrado no classpath: " + SCRIPT);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    if (!linha.trim().startsWith("--")) {
                        sb.append(linha).append('\n');
                    }
                }
            }
            java.util.List<String> comandos = new java.util.ArrayList<>();
            for (String parte : sb.toString().split(";")) {
                if (!parte.trim().isEmpty()) {
                    comandos.add(parte.trim());
                }
            }
            return comandos;
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao ler o script SQL: " + e.getMessage(), e);
        }
    }

    // --- Transações ---

    public static void iniciarTransacao() throws PersistenciaException {
        try {
            getConexao().setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao iniciar transação: " + e.getMessage(), e);
        }
    }

    public static void confirmarTransacao() throws PersistenciaException {
        try {
            Connection con = getConexao();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao confirmar transação: " + e.getMessage(), e);
        }
    }

    public static void desfazerTransacao() {
        try {
            Connection con = getConexao();
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException | PersistenciaException ignorada) {
            // nada mais a fazer: a transação não foi confirmada
        }
    }
}
