/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.servico;

/**
 *
 * @author MARCELO JUNIOR
 */
import br.com.rotasegura.excecao.DadosInvalidosException;
import br.com.rotasegura.excecao.VeiculoIndisponivelException;
import br.com.rotasegura.modelo.Cliente;
import br.com.rotasegura.modelo.Contrato;
import br.com.rotasegura.modelo.Veiculo;
import br.com.rotasegura.repositorio.RepositorioGenerico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class LocacaoService {

    private final RepositorioGenerico<Veiculo> repoVeiculos;
    private final RepositorioGenerico<Cliente> repoClientes;
    private final RepositorioGenerico<Contrato> repoContratos;

    public LocacaoService() {
        this.repoVeiculos = new RepositorioGenerico<>();
        this.repoClientes = new RepositorioGenerico<>();
        this.repoContratos = new RepositorioGenerico<>();
    }

    // --- Cadastros ---
    public void cadastrarVeiculo(Veiculo veiculo) {
        repoVeiculos.adicionar(veiculo);
    }

    public void cadastrarCliente(Cliente cliente) {
        repoClientes.adicionar(cliente);
    }

    // --- Consultas ---
    public List<Veiculo> listarVeiculos() {
        return repoVeiculos.listarTodos();
    }

    public List<Cliente> listarClientes() {
        return repoClientes.listarTodos();
    }

    public List<Contrato> listarContratos() {
        return repoContratos.listarTodos();
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) {
        return repoVeiculos.listarTodos().stream()
                .filter(v -> v.getPlaca().equalsIgnoreCase(placa))
                .findFirst()
                .orElse(null);
    }

    public Cliente buscarClientePorCpf(String cpf) {
        return repoClientes.listarTodos().stream()
                .filter(c -> c.getCpf().equalsIgnoreCase(cpf))
                .findFirst()
                .orElse(null);
    }

    // --- Fluxo de Locação ---
    public Contrato abrirLocacao(String cpfCliente, String placaVeiculo, LocalDate inicio, LocalDate fimPrevisto)
            throws DadosInvalidosException, VeiculoIndisponivelException {

        // Validação de Datas
        if (inicio == null || fimPrevisto == null || fimPrevisto.isBefore(inicio)) {
            throw new DadosInvalidosException("A data de término prevista deve ser igual ou posterior à data inicial.");
        }

        Cliente cliente = buscarClientePorCpf(cpfCliente);
        if (cliente == null) {
            throw new DadosInvalidosException("Cliente com CPF " + cpfCliente + " não encontrado.");
        }

        Veiculo veiculo = buscarVeiculoPorPlaca(placaVeiculo);
        if (veiculo == null) {
            throw new DadosInvalidosException("Veículo com placa " + placaVeiculo + " não encontrado.");
        }

        // Validação de Disponibilidade (Resiliência)
        if (!veiculo.isDisponivel()) {
            throw new VeiculoIndisponivelException("O veículo com placa " + placaVeiculo + " encontra-se OCUPADO/ALUGADO.");
        }

        Contrato contrato = new Contrato(cliente, veiculo, inicio, fimPrevisto);
        repoContratos.adicionar(contrato);
        salvarComprovanteEmArquivo(contrato);

        return contrato;
    }

    // --- Devolução de Veículo ---
    public void realizarDevolucao(int idContrato, LocalDate dataDevolucao) throws DadosInvalidosException {
        Contrato contrato = repoContratos.listarTodos().stream()
                .filter(c -> c.getId() == idContrato && c.isAtivo())
                .findFirst()
                .orElse(null);

        if (contrato == null) {
            throw new DadosInvalidosException("Contrato ativo com ID " + idContrato + " não foi encontrado.");
        }

        contrato.fecharContrato(dataDevolucao);
        salvarComprovanteEmArquivo(contrato);
    }

    // --- Persistência em Arquivo de Texto ---
    private void salvarComprovanteEmArquivo(Contrato contrato) {
        String nomeArquivo = "contrato_" + contrato.getId() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            writer.write(contrato.gerarComprovante());
        } catch (IOException e) {
            System.err.println("Erro ao salvar comprovante no arquivo: " + e.getMessage());
        }
    }
}
