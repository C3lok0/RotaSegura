package br.com.rotasegura.visao;

import br.com.rotasegura.modelo.Cliente;
import br.com.rotasegura.servico.LocacaoService;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/** Cadastro, edição, consulta, listagem e exclusão de clientes. */
@SuppressWarnings({"serial", "this-escape"})
class PainelClientes extends JPanel implements Atualizavel {

    private final LocacaoService service;
    private final ModeloTabela<Cliente> modelo = new ModeloTabela<>(
            new String[]{"CPF", "Nome", "CNH"},
            c -> new Object[]{c.getCpf(), c.getNome(), c.getCnh()});
    private final JTable tabela = new JTable(modelo);

    private final JTextField txtBusca = new JTextField(20);
    private final JTextField txtCpf = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtCnh = new JTextField();

    private Cliente selecionado; // null = novo cadastro

    PainelClientes(LocacaoService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));

        JPanel busca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Listar todos");
        busca.add(new javax.swing.JLabel("Nome ou CPF:"));
        busca.add(txtBusca);
        busca.add(btnBuscar);
        busca.add(btnTodos);
        add(busca, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel form = UtilGUI.formulario("Dados do cliente");
        UtilGUI.linha(form, 0, "CPF (11 dígitos):", txtCpf);
        UtilGUI.linha(form, 1, "Nome completo:", txtNome);
        UtilGUI.linha(form, 2, "Número da CNH:", txtCnh);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNovo = new JButton("Novo");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        botoes.add(btnNovo);
        botoes.add(btnSalvar);
        botoes.add(btnExcluir);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(botoes, c);
        add(form, BorderLayout.EAST);

        btnBuscar.addActionListener(e -> carregar(txtBusca.getText()));
        txtBusca.addActionListener(e -> carregar(txtBusca.getText()));
        btnTodos.addActionListener(e -> {
            txtBusca.setText("");
            carregar("");
        });
        btnNovo.addActionListener(e -> limparFormulario());
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                selecionar(modelo.get(tabela.convertRowIndexToModel(tabela.getSelectedRow())));
            }
        });
    }

    @Override
    public void atualizar() {
        carregar(txtBusca.getText());
    }

    private void carregar(String termo) {
        try {
            modelo.setDados(service.buscarClientes(termo));
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void selecionar(Cliente c) {
        selecionado = c;
        txtCpf.setText(c.getCpf());
        txtNome.setText(c.getNome());
        txtCnh.setText(c.getCnh());
        txtCpf.setEnabled(false); // o CPF é a chave e não pode ser alterado
    }

    private void limparFormulario() {
        selecionado = null;
        tabela.clearSelection();
        txtCpf.setText("");
        txtNome.setText("");
        txtCnh.setText("");
        txtCpf.setEnabled(true);
        txtCpf.requestFocus();
    }

    private void salvar() {
        try {
            if (selecionado == null) {
                service.cadastrarCliente(new Cliente(txtCpf.getText(), txtNome.getText(), txtCnh.getText()));
                UtilGUI.info(this, "Cliente cadastrado com sucesso!");
            } else {
                service.atualizarCliente(new Cliente(selecionado.getCpf(), txtNome.getText(), txtCnh.getText()));
                UtilGUI.info(this, "Cliente atualizado com sucesso!");
            }
            limparFormulario();
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void excluir() {
        if (selecionado == null) {
            UtilGUI.aviso(this, "Selecione um cliente na tabela para excluir.");
            return;
        }
        if (!UtilGUI.confirmar(this, "Excluir o cliente " + selecionado.getNome() + "?")) {
            return;
        }
        try {
            service.removerCliente(selecionado.getCpf());
            UtilGUI.info(this, "Cliente excluído com sucesso!");
            limparFormulario();
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }
}
