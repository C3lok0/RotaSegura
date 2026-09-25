/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */
public class VeiculoPopular extends Veiculo {
    private boolean possuiArCondicionado;

    public VeiculoPopular(String placa, String marca, String modelo, int ano, double valorBaseDiaria, boolean possuiArCondicionado) {
        super(placa, marca, modelo, ano, valorBaseDiaria);
        this.possuiArCondicionado = possuiArCondicionado;
    }

    @Override
    public double calcularValorDiaria() {
        return possuiArCondicionado ? getValorBaseDiaria() * 1.10 : getValorBaseDiaria();
    }

    @Override
    public double calcularSeguro(int dias) {
        return 25.0 * dias;
    }
}
