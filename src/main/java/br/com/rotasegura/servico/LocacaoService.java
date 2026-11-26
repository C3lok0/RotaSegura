package br.com.rotasegura.servico;

import br.com.rotasegura.dao.ClienteDAO;
import br.com.rotasegura.dao.ConexaoBD;
import br.com.rotasegura.dao.ContratoDAO;
import br.com.rotasegura.dao.VeiculoDAO;
import br.com.rotasegura.excecao.DadosInvalidosException;
import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.excecao.VeiculoIndisponivelException;
import br.com.rotasegura.modelo.Cliente;
import br.com.rotasegura.modelo.Contrato;
import br.com.rotasegura.modelo.Veiculo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio da locadora. Usa os DAOs para persistir e controla as
 * transações (abertura/encerramento de locação alteram contrato e status do veículo juntos).
 *
 * @author MARCELO JUNIOR
 */
public class LocacaoService {

    private static final Path PASTA_RELATORIOS = Path.of("relatorios");

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();

    // ---------------- Clientes ----------------

    public void cadastrarCliente(Cliente cliente) throws DadosInvalidosException, PersistenciaException {
        if (clienteDAO.buscarPorId(cliente.getCpf()) != null) {
            throw new DadosInvalidosException("Já existe um cliente cadastrado com o CPF " + cliente.getCpf() + ".");
        }
        clienteDAO.inserir(cliente);
    }

    public void atualizarCliente(Cliente cliente) throws DadosInvalidosException, PersistenciaException {
        if (clienteDAO.buscarPorId(cliente.getCpf()) == null) {
            throw new DadosInvalidosException("Cliente com CPF " + cliente.getCpf() + " não encontrado.");
        }
        clienteDAO.atualizar(cliente);
    }

    public void removerCliente(String cpf) throws DadosInvalidosException, PersistenciaException {
        if (contratoDAO.contarPorCliente(cpf) > 0) {
            throw new DadosInvalidosException("Este cliente possui locações registradas e não pode ser excluído.");
        }
        clienteDAO.remover(cpf);
    }

    public List<Cliente> listarClientes() throws PersistenciaException {
        return clienteDAO.listarTodos();
    }

    public List<Cliente> buscarClientes(String termo) throws PersistenciaException {
        return clienteDAO.buscarPorTermo(termo);
    }

    public Cliente buscarClientePorCpf(String cpf) throws PersistenciaException {
        return clienteDAO.buscarPorId(cpf.replaceAll("\\D", ""));
    }

    // ---------------- Veículos ----------------

    public void cadastrarVeiculo(Veiculo veiculo) throws DadosInvalidosException, PersistenciaException {
        if (veiculoDAO.buscarPorId(veiculo.getPlaca()) != null) {
            throw new DadosInvalidosException("Já existe um veículo cadastrado com a placa " + veiculo.getPlaca() + ".");
        }
        veiculoDAO.inserir(veiculo);
    }

    public void atualizarVeiculo(Veiculo veiculo) throws DadosInvalidosException, PersistenciaException {
        if (veiculoDAO.buscarPorId(veiculo.getPlaca()) == null) {
            throw new DadosInvalidosException("Veículo com placa " + veiculo.getPlaca() + " não encontrado.");
        }
        veiculoDAO.atualizar(veiculo);
    }

    public void removerVeiculo(String placa) throws DadosInvalidosException, PersistenciaException {
        if (contratoDAO.contarPorVeiculo(placa) > 0) {
            throw new DadosInvalidosException("Este veículo possui locações registradas e não pode ser excluído.");
        }
        veiculoDAO.remover(placa);
    }

    public List<Veiculo> listarVeiculos() throws PersistenciaException {
        return veiculoDAO.listarTodos();
    }

    public List<Veiculo> listarVeiculosDisponiveis() throws PersistenciaException {
        return veiculoDAO.listarDisponiveis();
    }

    public List<Veiculo> buscarVeiculos(String termo) throws PersistenciaException {
        return veiculoDAO.buscarPorTermo(termo);
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) throws PersistenciaException {
        return veiculoDAO.buscarPorId(placa.trim().toUpperCase());
    }

    // ---------------- Locações ----------------

    public List<Contrato> listarContratos() throws PersistenciaException {
        return contratoDAO.listarTodos();
    }

    public List<Contrato> buscarContratos(String termo) throws PersistenciaException {
        return contratoDAO.buscarPorTermo(termo);
    }

    /** Abre a locação: grava o contrato e marca o veículo como locado na mesma transação. */
    public Contrato abrirLocacao(String cpfCliente, String placaVeiculo, LocalDate inicio, LocalDate fimPrevisto)
            throws DadosInvalidosException, VeiculoIndisponivelException, PersistenciaException {

        if (inicio == null || fimPrevisto == null || fimPrevisto.isBefore(inicio)) {
            throw new DadosInvalidosException("A data de término prevista deve ser igual ou posterior à data inicial.");
        }

        ConexaoBD.iniciarTransacao();
        try {
            Cliente cliente = buscarClientePorCpf(cpfCliente);
            if (cliente == null) {
                throw new DadosInvalidosException("Cliente com CPF " + cpfCliente + " não encontrado.");
            }
            Veiculo veiculo = buscarVeiculoPorPlaca(placaVeiculo);
            if (veiculo == null) {
                throw new DadosInvalidosException("Veículo com placa " + placaVeiculo + " não encontrado.");
            }
            if (!veiculo.isDisponivel()) {
                throw new VeiculoIndisponivelException("O veículo com placa " + veiculo.getPlaca() + " encontra-se OCUPADO/ALUGADO.");
            }

            Contrato contrato = new Contrato(cliente, veiculo, inicio, fimPrevisto); // marca o veículo como locado
            contratoDAO.inserir(contrato);
            veiculoDAO.atualizarDisponibilidade(veiculo.getPlaca(), false);
            ConexaoBD.confirmarTransacao();

            salvarArquivo("contrato_" + contrato.getId() + ".txt", contrato.gerarComprovante());
            return contrato;
        } catch (DadosInvalidosException | VeiculoIndisponivelException | PersistenciaException | RuntimeException e) {
            ConexaoBD.desfazerTransacao();
            throw e;
        }
    }

    /** Altera a data de devolução prevista de uma locação em andamento. */
    public void alterarDataPrevista(int idContrato, LocalDate novaData) throws DadosInvalidosException, PersistenciaException {
        Contrato contrato = contratoDAO.buscarPorId(idContrato);
        if (contrato == null || !contrato.isAtivo()) {
            throw new DadosInvalidosException("Contrato ativo com ID " + idContrato + " não foi encontrado.");
        }
        try {
            contrato.setDataFimPrevista(novaData);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException(e.getMessage());
        }
        contratoDAO.atualizar(contrato);
    }

    /** Busca um contrato em andamento (usado pela tela de devolução). */
    public Contrato buscarContratoAtivo(int idContrato) throws DadosInvalidosException, PersistenciaException {
        Contrato contrato = contratoDAO.buscarPorId(idContrato);
        if (contrato == null || !contrato.isAtivo()) {
            throw new DadosInvalidosException("Contrato ativo com ID " + idContrato + " não foi encontrado.");
        }
        return contrato;
    }

    /** Encerra a locação: fecha o contrato e libera o veículo na mesma transação. */
    public Contrato realizarDevolucao(int idContrato, LocalDate dataDevolucao)
            throws DadosInvalidosException, PersistenciaException {

        ConexaoBD.iniciarTransacao();
        try {
            Contrato contrato = buscarContratoAtivo(idContrato);
            if (dataDevolucao == null || dataDevolucao.isBefore(contrato.getDataInicio())) {
                throw new DadosInvalidosException("A data de devolução não pode ser anterior à data de início da locação.");
            }
            contrato.fecharContrato(dataDevolucao); // calcula o valor final e libera o veículo
            contratoDAO.atualizar(contrato);
            veiculoDAO.atualizarDisponibilidade(contrato.getVeiculo().getPlaca(), true);
            ConexaoBD.confirmarTransacao();
            return contrato;
        } catch (DadosInvalidosException | PersistenciaException | RuntimeException e) {
            ConexaoBD.desfazerTransacao();
            throw e;
        }
    }

    /** Remove a locação; se estava em andamento, o veículo volta a ficar disponível (mesma transação). */
    public void removerLocacao(int idContrato) throws DadosInvalidosException, PersistenciaException {
        ConexaoBD.iniciarTransacao();
        try {
            Contrato contrato = contratoDAO.buscarPorId(idContrato);
            if (contrato == null) {
                throw new DadosInvalidosException("Contrato " + idContrato + " não foi encontrado.");
            }
            contratoDAO.remover(idContrato);
            if (contrato.isAtivo()) {
                veiculoDAO.atualizarDisponibilidade(contrato.getVeiculo().getPlaca(), true);
            }
            ConexaoBD.confirmarTransacao();
        } catch (DadosInvalidosException | PersistenciaException | RuntimeException e) {
            ConexaoBD.desfazerTransacao();
            throw e;
        }
    }

    /** Grava o relatório de encerramento em relatorios/contrato_N_encerramento.txt e devolve o caminho. */
    public Path emitirRelatorioEncerramento(Contrato contrato) throws PersistenciaException {
        String texto = contrato.gerarRelatorioEncerramento(contrato.getDataDevolucaoEfetiva());
        Path arquivo = salvarArquivo("contrato_" + contrato.getId() + "_encerramento.txt", texto);
        if (arquivo == null) {
            throw new PersistenciaException("Não foi possível gravar o relatório de encerramento em arquivo.");
        }
        return arquivo;
    }

    private Path salvarArquivo(String nome, String conteudo) {
        try {
            Files.createDirectories(PASTA_RELATORIOS);
            Path arquivo = PASTA_RELATORIOS.resolve(nome);
            Files.writeString(arquivo, conteudo, StandardCharsets.UTF_8);
            return arquivo.toAbsolutePath();
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo " + nome + ": " + e.getMessage());
            return null;
        }
    }
}
