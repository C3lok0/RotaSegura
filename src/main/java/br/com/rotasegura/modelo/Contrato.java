/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Contrato implements Imprimivel {
    private static int contadorId = 1;

    private int id;
    private Cliente cliente;
    private Veiculo veiculo;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista;
    private LocalDate dataDevolucaoEfetiva;
    private boolean ativo;

    public Contrato(Cliente cliente, Veiculo veiculo, LocalDate dataInicio, LocalDate dataFimPrevista) {
        this.id = contadorId++;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.ativo = true;
        this.veiculo.setDisponivel(false); // Marca o veículo como alugado ao iniciar
    }

    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public boolean isAtivo() { return ativo; }

    public int getDiasLocacao() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFimPrevista);
    }

    public double calcularValorTotal() {
        int dias = getDiasLocacao();
        if (dias <= 0) dias = 1; // Garante diária mínima
        
        // Polimorfismo sendo aplicado aqui:
        double valorDiarias = veiculo.calcularValorDiaria() * dias;
        double valorSeguro = veiculo.calcularSeguro(dias);
        
        return valorDiarias + valorSeguro;
    }

    public void fecharContrato(LocalDate dataDevolucao) {
        this.dataDevolucaoEfetiva = dataDevolucao;
        this.ativo = false;
        this.veiculo.setDisponivel(true); // Libera o veículo novamente
    }

    @Override
    public String gerarComprovante() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        CONTRATO DE LOCAÇÃO #").append(id).append("\n");
        sb.append("=========================================\n");
        sb.append(cliente.gerarComprovante()).append("\n");
        sb.append(veiculo.gerarComprovante()).append("\n");
        sb.append("Data Início: ").append(dataInicio.format(fmt)).append("\n");
        sb.append("Data Devolução Prevista: ").append(dataFimPrevista.format(fmt)).append("\n");
        sb.append("Duração: ").append(getDiasLocacao()).append(" dia(s)\n");
        sb.append(String.format("VALOR TOTAL: R$ %.2f\n", calcularValorTotal()));
        sb.append("Status: ").append(ativo ? "EM ANDAMENTO" : "CONCLUÍDO").append("\n");
        sb.append("=========================================");
        return sb.toString();
    }
}
