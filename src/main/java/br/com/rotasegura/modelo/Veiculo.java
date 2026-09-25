/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */

public abstract class Veiculo implements Imprimivel {
    private String placa;
    private String marca;
    private String modelo;
    private int ano;
    private double valorBaseDiaria;
    private boolean disponivel;

    public Veiculo(String placa, String marca, String modelo, int ano, double valorBaseDiaria) {
        setPlaca(placa);
        this.marca = marca;
        this.modelo = modelo;
        setAno(ano);
        setValorBaseDiaria(valorBaseDiaria);
        this.disponivel = true;
    }

    public abstract double calcularValorDiaria();
    public abstract double calcularSeguro(int dias);

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) {
        if (placa == null || placa.trim().length() < 7) {
            throw new IllegalArgumentException("Placa inválida. Deve conter ao menos 7 caracteres.");
        }
        this.placa = placa.toUpperCase();
    }

    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }

    public int getAno() { return ano; }
    public void setAno(int ano) {
        if (ano < 1900) {
            throw new IllegalArgumentException("Ano do veículo inválido.");
        }
        this.ano = ano;
    }

    public double getValorBaseDiaria() { return valorBaseDiaria; }
    public void setValorBaseDiaria(double valorBaseDiaria) {
        if (valorBaseDiaria <= 0) {
            throw new IllegalArgumentException("O valor base da diária deve ser maior que zero.");
        }
        this.valorBaseDiaria = valorBaseDiaria;
    }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    @Override
    public String gerarComprovante() {
        return String.format("Veículo: %s %s | Placa: %s | Diária Base: R$ %.2f | Status: %s",
                marca, modelo, placa, valorBaseDiaria, (disponivel ? "Disponível" : "Alugado"));
    }
}