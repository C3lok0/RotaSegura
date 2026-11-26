package br.com.rotasegura.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author MARCELO JUNIOR
 */
public class Contrato implements Imprimivel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id; // 0 enquanto não foi gravado no banco (o id é gerado pelo banco)
    private Cliente cliente;
    private Veiculo veiculo;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista;
    private LocalDate dataDevolucaoEfetiva;
    private boolean ativo;
    private Double valorFinal; // preenchido no fechamento do contrato

    /** Novo contrato (ainda não gravado). */
    public Contrato(Cliente cliente, Veiculo veiculo, LocalDate dataInicio, LocalDate dataFimPrevista) {
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.ativo = true;
        this.veiculo.setDisponivel(false); // Marca o veículo como alugado ao iniciar
    }

    /** Reconstrói um contrato já gravado (usado pelo DAO). */
    public Contrato(int id, Cliente cliente, Veiculo veiculo, LocalDate dataInicio, LocalDate dataFimPrevista,
                    LocalDate dataDevolucaoEfetiva, boolean ativo, Double valorFinal) {
        this.id = id;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
        this.ativo = ativo;
        this.valorFinal = valorFinal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public LocalDate getDataDevolucaoEfetiva() { return dataDevolucaoEfetiva; }
    public Double getValorFinal() { return valorFinal; }
    public boolean isAtivo() { return ativo; }

    public void setDataFimPrevista(LocalDate novaData) {
        if (!ativo) {
            throw new IllegalStateException("Contrato já encerrado não pode ser alterado.");
        }
        if (novaData == null || novaData.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de término prevista deve ser igual ou posterior à data inicial.");
        }
        this.dataFimPrevista = novaData;
    }

    /** Dias cobrados até a data informada (mínimo de 1 diária). */
    public int calcularDias(LocalDate dataFim) {
        int dias = (int) ChronoUnit.DAYS.between(dataInicio, dataFim);
        return Math.max(dias, 1);
    }

    /** Dias da locação: prevista enquanto ativa, efetiva depois de encerrada. */
    public int getDiasLocacao() {
        return calcularDias(ativo || dataDevolucaoEfetiva == null ? dataFimPrevista : dataDevolucaoEfetiva);
    }

    /** Valor para uma devolução na data informada (polimorfismo: diária e seguro dependem do veículo). */
    public double calcularValorPara(LocalDate dataFim) {
        int dias = calcularDias(dataFim);
        return veiculo.calcularValorDiaria() * dias + veiculo.calcularSeguro(dias);
    }

    /** Estimado (prevista) enquanto ativo; valor final gravado depois de encerrado. */
    public double calcularValorTotal() {
        if (!ativo && valorFinal != null) {
            return valorFinal;
        }
        return calcularValorPara(dataFimPrevista);
    }

    public void fecharContrato(LocalDate dataDevolucao) {
        if (!ativo) {
            throw new IllegalStateException("O contrato já está encerrado.");
        }
        if (dataDevolucao == null || dataDevolucao.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de devolução não pode ser anterior à data de início.");
        }
        this.valorFinal = calcularValorPara(dataDevolucao);
        this.dataDevolucaoEfetiva = dataDevolucao;
        this.ativo = false;
        this.veiculo.setDisponivel(true); // Libera o veículo novamente
    }

    /** Resumo/relatório de encerramento para uma devolução na data informada. */
    public String gerarRelatorioEncerramento(LocalDate dataDevolucao) {
        if (dataDevolucao == null || dataDevolucao.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de devolução não pode ser anterior à data de início.");
        }
        int dias = calcularDias(dataDevolucao);
        double diaria = veiculo.calcularValorDiaria();
        double seguro = veiculo.calcularSeguro(dias);
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("   RELATÓRIO DE ENCERRAMENTO - CONTRATO #").append(id).append("\n");
        sb.append("=========================================\n");
        sb.append(cliente.gerarComprovante()).append("\n");
        sb.append(veiculo.gerarComprovante()).append("\n");
        sb.append("Categoria: ").append(veiculo.getCategoria()).append("\n");
        sb.append("Data Início: ").append(dataInicio.format(FMT)).append("\n");
        sb.append("Devolução Prevista: ").append(dataFimPrevista.format(FMT)).append("\n");
        sb.append("Devolução Efetiva: ").append(dataDevolucao.format(FMT)).append("\n");
        sb.append("Dias cobrados: ").append(dias).append("\n");
        sb.append(String.format("Diária da categoria: R$ %.2f%n", diaria));
        sb.append(String.format("Subtotal diárias: R$ %.2f%n", diaria * dias));
        sb.append(String.format("Seguro: R$ %.2f%n", seguro));
        sb.append(String.format("VALOR FINAL: R$ %.2f%n", diaria * dias + seguro));
        sb.append("=========================================");
        return sb.toString();
    }

    @Override
    public String gerarComprovante() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        CONTRATO DE LOCAÇÃO #").append(id).append("\n");
        sb.append("=========================================\n");
        sb.append(cliente.gerarComprovante()).append("\n");
        sb.append(veiculo.gerarComprovante()).append("\n");
        sb.append("Data Início: ").append(dataInicio.format(FMT)).append("\n");
        sb.append("Data Devolução Prevista: ").append(dataFimPrevista.format(FMT)).append("\n");
        if (dataDevolucaoEfetiva != null) {
            sb.append("Data Devolução Efetiva: ").append(dataDevolucaoEfetiva.format(FMT)).append("\n");
        }
        sb.append("Duração: ").append(getDiasLocacao()).append(" dia(s)\n");
        sb.append(String.format("VALOR TOTAL: R$ %.2f%n", calcularValorTotal()));
        sb.append("Status: ").append(ativo ? "EM ANDAMENTO" : "CONCLUÍDO").append("\n");
        sb.append("=========================================");
        return sb.toString();
    }
}
