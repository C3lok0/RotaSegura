package br.com.rotasegura.modelo;

/**
 * @author MARCELO JUNIOR
 */
public class VeiculoSedan extends Veiculo {
    private double capacidadePortaMalasLitros;

    public VeiculoSedan(String placa, String marca, String modelo, int ano, double valorBaseDiaria, double capacidadePortaMalasLitros) {
        super(placa, marca, modelo, ano, valorBaseDiaria);
        this.capacidadePortaMalasLitros = capacidadePortaMalasLitros;
    }

    public double getCapacidadePortaMalasLitros() { return capacidadePortaMalasLitros; }

    @Override
    public double calcularValorDiaria() {
        return getValorBaseDiaria() * 1.20;
    }

    @Override
    public double calcularSeguro(int dias) {
        return 45.0 * dias;
    }

    @Override
    public String getCategoria() { return "Sedan"; }
}
