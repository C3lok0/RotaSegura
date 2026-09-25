/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.modelo;

/**
 *
 * @author MARCELO JUNIOR
 */
public class Cliente implements Imprimivel {
    private String cpf;
    private String nome;
    private String cnh;

    public Cliente(String cpf, String nome, String cnh) {
        setCpf(cpf);
        this.nome = nome;
        setCnh(cnh);
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) {
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) {
            throw new IllegalArgumentException("CPF inválido. Deve conter 11 dígitos.");
        }
        this.cpf = cpf;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) {
        if (cnh == null || cnh.trim().isEmpty()) {
            throw new IllegalArgumentException("CNH obrigatória.");
        }
        this.cnh = cnh;
    }

    @Override
    public String gerarComprovante() {
        return String.format("Cliente: %s | CPF: %s | CNH: %s", nome, cpf, cnh);
    }
}
