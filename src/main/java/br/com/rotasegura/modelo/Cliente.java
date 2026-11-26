package br.com.rotasegura.modelo;

/**
 * @author MARCELO JUNIOR
 */
@SuppressWarnings("this-escape")
public class Cliente implements Imprimivel {
    private String cpf;
    private String nome;
    private String cnh;

    public Cliente(String cpf, String nome, String cnh) {
        setCpf(cpf);
        setNome(nome);
        setCnh(cnh);
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) {
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) {
            throw new IllegalArgumentException("CPF inválido. Deve conter 11 dígitos.");
        }
        this.cpf = cpf.replaceAll("\\D", ""); // guarda somente os dígitos
    }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) {
        if (cnh == null || cnh.trim().isEmpty()) {
            throw new IllegalArgumentException("CNH obrigatória.");
        }
        this.cnh = cnh.trim();
    }

    @Override
    public String gerarComprovante() {
        return String.format("Cliente: %s | CPF: %s | CNH: %s", nome, cpf, cnh);
    }

    @Override
    public String toString() {
        return nome + " (" + cpf + ")";
    }
}
