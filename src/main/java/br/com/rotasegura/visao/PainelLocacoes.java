package br.com.rotasegura.visao;

import br.com.rotasegura.modelo.Cliente;
import br.com.rotasegura.modelo.Contrato;
import br.com.rotasegura.modelo.Veiculo;
import br.com.rotasegura.servico.LocacaoService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.time.LocalDate;
import java.util.List;

/** Abertura, consulta, listagem, edição, devolução e exclusão de locações. */
@SuppressWarnings({"serial", "this-escape"})
class PainelLocacoes extends JPanel implements Atualizavel {

    private final LocacaoService service;
    private final ModeloTabela<Contrato> modelo = new ModeloTabela<>(
            new String[]{"Nº", "Cliente", "Placa", "Veículo", "Início", "Devolução prevista", "Devolução efetiva", "Valor", "Status"},
            c -> new Object[]{c.getId(), c.getCliente().getNome(), c.getVeiculo().getPlaca(),
                    c.getVeiculo().getMarca() + " " + c.getVeiculo().getModelo(),
                    UtilGUI.formatarData(c.getDataInicio()), UtilGUI.formatarData(c.getDataFimPrevista()),
                    UtilGUI.formatarData(c.getDataDevolucaoEfetiva()),
                    UtilGUI.moeda(c.calcularValorTotal()), c.isAtivo() ? "EM ANDAMENTO" : "CONCLUÍDO"});
    private final JTable tabela = new JTable(modelo);

    private final JTextField txtBusca = new JTextField(20);
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<Veiculo> cmbVeiculo = new JComboBox<>();
    private final JTextField txtInicio = new JTextField();
    private final JTextField txtFim = new JTextField();

    PainelLocacoes(LocacaoService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));

        JPanel busca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Listar todas");
        busca.add(new JLabel("Nº, cliente, placa ou modelo:"));
        busca.add(txtBusca);
        busca.add(btnBuscar);
        busca.add(btnTodos);
        add(busca, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDevolucao = new JButton("Registrar devolução...");
        JButton btnAlterar = new JButton("Alterar data prevista...");
        JButton btnVer = new JButton("Ver comprovante");
        JButton btnExcluir = new JButton("Excluir locação");
        acoes.add(btnDevolucao);
        acoes.add(btnAlterar);
        acoes.add(btnVer);
        acoes.add(btnExcluir);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        centro.add(acoes, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        JPanel form = UtilGUI.formulario("Nova locação");
        UtilGUI.linha(form, 0, "Cliente:", cmbCliente);
        UtilGUI.linha(form, 1, "Veículo disponível:", cmbVeiculo);
        UtilGUI.linha(form, 2, "Início (dd/MM/yyyy):", txtInicio);
        UtilGUI.linha(form, 3, "Devolução prevista (dd/MM/yyyy):", txtFim);
        JButton btnAbrir = new JButton("Abrir locação");
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(btnAbrir, c);
        add(form, BorderLayout.EAST);

        txtInicio.setText(UtilGUI.formatarData(LocalDate.now()));

        btnBuscar.addActionListener(e -> carregarTabela(txtBusca.getText()));
        txtBusca.addActionListener(e -> carregarTabela(txtBusca.getText()));
        btnTodos.addActionListener(e -> {
            txtBusca.setText("");
            carregarTabela("");
        });
        btnAbrir.addActionListener(e -> abrirLocacao());
        btnDevolucao.addActionListener(e -> registrarDevolucao());
        btnAlterar.addActionListener(e -> alterarDataPrevista());
        btnVer.addActionListener(e -> verComprovante());
        btnExcluir.addActionListener(e -> excluir());
    }

    @Override
    public void atualizar() {
        carregarTabela(txtBusca.getText());
        carregarCombos();
    }

    private void carregarTabela(String termo) {
        try {
            modelo.setDados(service.buscarContratos(termo));
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void carregarCombos() {
        try {
            List<Cliente> clientes = service.listarClientes();
            List<Veiculo> veiculos = service.listarVeiculosDisponiveis();
            cmbCliente.removeAllItems();
            clientes.forEach(cmbCliente::addItem);
            cmbVeiculo.removeAllItems();
            veiculos.forEach(cmbVeiculo::addItem);
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private Contrato contratoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            UtilGUI.aviso(this, "Selecione uma locação na tabela.");
            return null;
        }
        return modelo.get(tabela.convertRowIndexToModel(linha));
    }

    private void abrirLocacao() {
        try {
            Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
            Veiculo veiculo = (Veiculo) cmbVeiculo.getSelectedItem();
            if (cliente == null || veiculo == null) {
                UtilGUI.aviso(this, "Selecione um cliente e um veículo disponível.");
                return;
            }
            LocalDate inicio = UtilGUI.lerData(txtInicio.getText(), "Início");
            LocalDate fim = UtilGUI.lerData(txtFim.getText(), "Devolução prevista");
            Contrato contrato = service.abrirLocacao(cliente.getCpf(), veiculo.getPlaca(), inicio, fim);
            txtFim.setText("");
            atualizar();
            UtilGUI.mostrarTexto(this, "Locação realizada com sucesso!", contrato.gerarComprovante());
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
            atualizar();
        }
    }

    private void registrarDevolucao() {
        Contrato selecionado = contratoSelecionado();
        if (selecionado == null) {
            return;
        }
        try {
            Contrato ativo = service.buscarContratoAtivo(selecionado.getId());
            Frame dono = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            new DialogoDevolucao(dono, service, ativo).setVisible(true);
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void alterarDataPrevista() {
        Contrato selecionado = contratoSelecionado();
        if (selecionado == null) {
            return;
        }
        if (!selecionado.isAtivo()) {
            UtilGUI.aviso(this, "Somente locações em andamento podem ser alteradas.");
            return;
        }
        String entrada = JOptionPane.showInputDialog(this,
                "Nova data de devolução prevista (dd/MM/yyyy):", UtilGUI.formatarData(selecionado.getDataFimPrevista()));
        if (entrada == null) {
            return;
        }
        try {
            service.alterarDataPrevista(selecionado.getId(), UtilGUI.lerData(entrada, "Devolução prevista"));
            UtilGUI.info(this, "Data prevista alterada com sucesso!");
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void verComprovante() {
        Contrato selecionado = contratoSelecionado();
        if (selecionado != null) {
            UtilGUI.mostrarTexto(this, "Contrato #" + selecionado.getId(), selecionado.gerarComprovante());
        }
    }

    private void excluir() {
        Contrato selecionado = contratoSelecionado();
        if (selecionado == null) {
            return;
        }
        String aviso = selecionado.isAtivo()
                ? "Excluir a locação #" + selecionado.getId() + "? Ela está em andamento: o veículo voltará a ficar disponível."
                : "Excluir a locação #" + selecionado.getId() + " do histórico?";
        if (!UtilGUI.confirmar(this, aviso)) {
            return;
        }
        try {
            service.removerLocacao(selecionado.getId());
            UtilGUI.info(this, "Locação excluída com sucesso!");
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }
}
