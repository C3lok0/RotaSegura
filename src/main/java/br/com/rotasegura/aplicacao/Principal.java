/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.aplicacao;

/**
 *
 * @author MARCELO JUNIOR
 */
import br.com.rotasegura.excecao.DadosInvalidosException;
import br.com.rotasegura.excecao.VeiculoIndisponivelException;
import br.com.rotasegura.modelo.*;
import br.com.rotasegura.servico.LocacaoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private static final LocacaoService service = new LocacaoService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        carregarDadosIniciais(); // Popula o sistema para testes rápidos

        int opcao = -1;
        while (opcao != 0) {
            exibirMenu();
            try {
                System.out.print("Escolha uma opção: ");
                opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1 -> cadastrarVeiculo();
                    case 2 -> listarVeiculos();
                    case 3 -> cadastrarCliente();
                    case 4 -> listarClientes();
                    case 5 -> abrirLocacao();
                    case 6 -> realizarDevolucao();
                    case 7 -> listarContratos();
                    case 0 -> System.out.println("\nEncerrou o sistema Rota Segura. Até logo!");
                    default -> System.out.println("\n[ERRO] Opção inválida! Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n[ERRO] Entrada inválida. Por favor, digite apenas números.");
            } catch (Exception e) {
                System.out.println("\n[ERRO INESPERADO] " + e.getMessage());
            }
        }
    }

    private static void exibirMenu() {
        System.out.println("\n=========================================");
        System.out.println("      LOCADORA ROTA SEGURA - MENU       ");
        System.out.println("=========================================");
        System.out.println("1. Cadastrar Veículo");
        System.out.println("2. Listar Veículos");
        System.out.println("3. Cadastrar Cliente");
        System.out.println("4. Listar Clientes");
        System.out.println("5. Abrir Nova Locação (Contrato)");
        System.out.println("6. Realizar Devolução");
        System.out.println("7. Listar Contratos / Histórico");
        System.out.println("0. Sair");
        System.out.println("=========================================");
    }

    // --- CARGA INICIAL DE DADOS ---
    private static void carregarDadosIniciais() {
        try {
            service.cadastrarVeiculo(new VeiculoPopular("ABC1234", "Fiat", "Uno", 2021, 80.0, true));
            service.cadastrarVeiculo(new VeiculoSedan("XYZ9876", "Toyota", "Corolla", 2023, 180.0, 470.0));
            service.cadastrarVeiculo(new VeiculoSUV("SUV5555", "Jeep", "Compass", 2024, 250.0, true));

            service.cadastrarCliente(new Cliente("12345678901", "Carlos Silva", "CNH123456"));
            service.cadastrarCliente(new Cliente("98765432100", "Ana Souza", "CNH987654"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados iniciais: " + e.getMessage());
        }
    }

    // --- CADASTRO DE VEÍCULO ---
    private static void cadastrarVeiculo() {
        System.out.println("\n--- Cadastrar Veículo ---");
        System.out.println("Tipo: 1-Popular | 2-Sedan | 3-SUV");
        System.out.print("Escolha o tipo: ");
        int tipo = Integer.parseInt(scanner.nextLine());

        System.out.print("Placa (min 7 caracteres): ");
        String placa = scanner.nextLine();
        System.out.print("Marca: ");
        String marca = scanner.nextLine();
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        System.out.print("Ano: ");
        int ano = Integer.parseInt(scanner.nextLine());
        System.out.print("Valor Base Diária (R$): ");
        double valorBase = Double.parseDouble(scanner.nextLine());

        try {
            Veiculo v = switch (tipo) {
                case 1 -> {
                    System.out.print("Possui Ar Condicionado? (s/n): ");
                    boolean ar = scanner.nextLine().equalsIgnoreCase("s");
                    yield new VeiculoPopular(placa, marca, modelo, ano, valorBase, ar);
                }
                case 2 -> {
                    System.out.print("Capacidade do Porta-malas (Litros): ");
                    double cap = Double.parseDouble(scanner.nextLine());
                    yield new VeiculoSedan(placa, marca, modelo, ano, valorBase, cap);
                }
                case 3 -> {
                    System.out.print("Possui Tração 4x4? (s/n): ");
                    boolean tracao = scanner.nextLine().equalsIgnoreCase("s");
                    yield new VeiculoSUV(placa, marca, modelo, ano, valorBase, tracao);
                }
                default -> throw new IllegalArgumentException("Tipo de veículo inválido.");
            };

            service.cadastrarVeiculo(v);
            System.out.println("\n[SUCESSO] Veículo cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("\n[ERRO AO CADASTRAR] " + e.getMessage());
        }
    }

    private static void listarVeiculos() {
        System.out.println("\n--- Lista de Veículos ---");
        List<Veiculo> lista = service.listarVeiculos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum veículo cadastrado.");
        } else {
            lista.forEach(v -> System.out.println(v.gerarComprovante()));
        }
    }

    // --- CADASTRO DE CLIENTE ---
    private static void cadastrarCliente() {
        System.out.println("\n--- Cadastrar Cliente ---");
        try {
            System.out.print("CPF (11 dígitos): ");
            String cpf = scanner.nextLine();
            System.out.print("Nome Completo: ");
            String nome = scanner.nextLine();
            System.out.print("Número CNH: ");
            String cnh = scanner.nextLine();

            Cliente cliente = new Cliente(cpf, nome, cnh);
            service.cadastrarCliente(cliente);
            System.out.println("\n[SUCESSO] Cliente cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("\n[ERRO AO CADASTRAR] " + e.getMessage());
        }
    }

    private static void listarClientes() {
        System.out.println("\n--- Lista de Clientes ---");
        List<Cliente> lista = service.listarClientes();
        if (lista.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
        } else {
            lista.forEach(c -> System.out.println(c.gerarComprovante()));
        }
    }

    // --- ABRIR LOCAÇÃO ---
    private static void abrirLocacao() {
        System.out.println("\n--- Abrir Nova Locação ---");
        try {
            System.out.print("CPF do Cliente: ");
            String cpf = scanner.nextLine();
            System.out.print("Placa do Veículo: ");
            String placa = scanner.nextLine();

            System.out.print("Data de Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine(), fmt);
            System.out.print("Data Prevista de Devolução (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine(), fmt);

            Contrato contrato = service.abrirLocacao(cpf, placa, inicio, fim);
            System.out.println("\n[SUCESSO] Locação realizada com sucesso!");
            System.out.println(contrato.gerarComprovante());
        } catch (DateTimeParseException e) {
            System.out.println("\n[ERRO] Formato de data inválido! Use o padrão dd/MM/yyyy.");
        } catch (DadosInvalidosException | VeiculoIndisponivelException e) {
            System.out.println("\n[ERRO DE NEGÓCIO] " + e.getMessage());
        }
    }

    // --- DEVOLUÇÃO ---
    private static void realizarDevolucao() {
        System.out.println("\n--- Realizar Devolução ---");
        try {
            System.out.print("ID do Contrato: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Data Efetiva de Devolução (dd/MM/yyyy): ");
            LocalDate dataDev = LocalDate.parse(scanner.nextLine(), fmt);

            service.realizarDevolucao(id, dataDev);
            System.out.println("\n[SUCESSO] Devolução realizada e contrato finalizado!");
        } catch (DateTimeParseException e) {
            System.out.println("\n[ERRO] Formato de data inválido! Use o padrão dd/MM/yyyy.");
        } catch (Exception e) {
            System.out.println("\n[ERRO DE NEGÓCIO] " + e.getMessage());
        }
    }

    private static void listarContratos() {
        System.out.println("\n--- Histórico de Contratos ---");
        List<Contrato> lista = service.listarContratos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum contrato registrado.");
        } else {
            lista.forEach(c -> System.out.println(c.gerarComprovante() + "\n"));
        }
    }
}
