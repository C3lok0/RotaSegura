/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */
public class VeiculoSedan extends Veiculo {
    private double capacidadePortaMalasLitros;

    public VeiculoSedan(String placa, String marca, String modelo, int ano, double valorBaseDiaria, double capacidadePortaMalasLitros) {
        super(placa, marca, modelo, ano, valorBaseDiaria);
        this.capacidadePortaMalasLitros = capacidadePortaMalasLitros;
    }

    @Override
    public double calcularValorDiaria() {
        return getValorBaseDiaria() * 1.20;
    }

    @Override
    public double calcularSeguro(int dias) {
        return 45.0 * dias;
    }
}
