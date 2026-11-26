package br.com.rotasegura.modelo;

/**
 * @author MARCELO JUNIOR
 */
public class VeiculoSUV extends Veiculo {
    private boolean tracao4x4;

    public VeiculoSUV(String placa, String marca, String modelo, int ano, double valorBaseDiaria, boolean tracao4x4) {
        super(placa, marca, modelo, ano, valorBaseDiaria);
        this.tracao4x4 = tracao4x4;
    }

    public boolean isTracao4x4() { return tracao4x4; }

    @Override
    public double calcularValorDiaria() {
        return tracao4x4 ? getValorBaseDiaria() * 1.40 : getValorBaseDiaria() * 1.25;
    }

    @Override
    public double calcularSeguro(int dias) {
        return 75.0 * dias;
    }

    @Override
    public String getCategoria() { return "SUV"; }
}
