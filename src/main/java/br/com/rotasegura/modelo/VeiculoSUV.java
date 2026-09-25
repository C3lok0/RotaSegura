/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */
public class VeiculoSUV extends Veiculo {
    private boolean tracao4x4;

    public VeiculoSUV(String placa, String marca, String modelo, int ano, double valorBaseDiaria, boolean tracao4x4) {
        super(placa, marca, modelo, ano, valorBaseDiaria);
        this.tracao4x4 = tracao4x4;
    }

    @Override
    public double calcularValorDiaria() {
        return tracao4x4 ? getValorBaseDiaria() * 1.40 : getValorBaseDiaria() * 1.25;
    }

    @Override
    public double calcularSeguro(int dias) {
        return 75.0 * dias;
    }
}
