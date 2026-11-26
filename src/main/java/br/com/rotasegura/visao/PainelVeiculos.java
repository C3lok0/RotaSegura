package br.com.rotasegura.visao;

import br.com.rotasegura.modelo.Veiculo;
import br.com.rotasegura.modelo.VeiculoPopular;
import br.com.rotasegura.modelo.VeiculoSUV;
import br.com.rotasegura.modelo.VeiculoSedan;
import br.com.rotasegura.servico.LocacaoService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

/** Cadastro, edição, consulta, listagem e exclusão de veículos (Popular, Sedan e SUV). */
@SuppressWarnings({"serial", "this-escape"})
class PainelVeiculos extends JPanel implements Atualizavel {

    private static final String[] TIPOS = {"Popular", "Sedan", "SUV"};

    private final LocacaoService service;
    private final ModeloTabela<Veiculo> modelo = new ModeloTabela<>(
            new String[]{"Placa", "Categoria", "Marca", "Modelo", "Ano", "Diária base", "Diária da categoria", "Status"},
            v -> new Object[]{v.getPlaca(), v.getCategoria(), v.getMarca(), v.getModelo(), v.getAno(),
                    UtilGUI.moeda(v.getValorBaseDiaria()), UtilGUI.moeda(v.calcularValorDiaria()),
                    v.isDisponivel() ? "Disponível" : "Alugado"});
    private final JTable tabela = new JTable(modelo);

    private final JTextField txtBusca = new JTextField(20);
    private final JComboBox<String> cmbTipo = new JComboBox<>(TIPOS);
    private final JTextField txtPlaca = new JTextField();
    private final JTextField txtMarca = new JTextField();
    private final JTextField txtModelo = new JTextField();
    private final JTextField txtAno = new JTextField();
    private final JTextField txtValorBase = new JTextField();
    private final JCheckBox chkAr = new JCheckBox("Possui ar-condicionado");
    private final JTextField txtPortaMalas = new JTextField();
    private final JCheckBox chkTracao = new JCheckBox("Possui tração 4x4");
    private final JPanel pnlEspecifico = new JPanel(new CardLayout());

    private Veiculo selecionado; // null = novo cadastro

    PainelVeiculos(LocacaoService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));

        JPanel busca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Listar todos");
        busca.add(new JLabel("Placa, marca ou modelo:"));
        busca.add(txtBusca);
        busca.add(btnBuscar);
        busca.add(btnTodos);
        add(busca, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel form = UtilGUI.formulario("Dados do veículo");
        UtilGUI.linha(form, 0, "Categoria:", cmbTipo);
        UtilGUI.linha(form, 1, "Placa (mín. 7 caracteres):", txtPlaca);
        UtilGUI.linha(form, 2, "Marca:", txtMarca);
        UtilGUI.linha(form, 3, "Modelo:", txtModelo);
        UtilGUI.linha(form, 4, "Ano:", txtAno);
        UtilGUI.linha(form, 5, "Valor base da diária (R$):", txtValorBase);
        UtilGUI.linha(form, 6, "Específico da categoria:", pnlEspecifico);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNovo = new JButton("Novo");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        botoes.add(btnNovo);
        botoes.add(btnSalvar);
        botoes.add(btnExcluir);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 7;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(botoes, c);
        add(form, BorderLayout.EAST);

        JPanel pnlSedan = new JPanel(new BorderLayout(5, 0));
        pnlSedan.add(new JLabel("Porta-malas (litros):"), BorderLayout.WEST);
        pnlSedan.add(txtPortaMalas, BorderLayout.CENTER);
        pnlEspecifico.add(chkAr, "Popular");
        pnlEspecifico.add(pnlSedan, "Sedan");
        pnlEspecifico.add(chkTracao, "SUV");
        cmbTipo.addActionListener(e -> ajustarCamposEspecificos());
        ajustarCamposEspecificos();

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
            modelo.setDados(service.buscarVeiculos(termo));
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    /** Mostra somente o campo específico da categoria escolhida. */
    private void ajustarCamposEspecificos() {
        ((CardLayout) pnlEspecifico.getLayout()).show(pnlEspecifico, (String) cmbTipo.getSelectedItem());
    }

    private void selecionar(Veiculo v) {
        selecionado = v;
        cmbTipo.setSelectedItem(v.getCategoria());
        cmbTipo.setEnabled(false); // a categoria não muda depois de cadastrada
        txtPlaca.setText(v.getPlaca());
        txtPlaca.setEnabled(false); // a placa é a chave e não pode ser alterada
        txtMarca.setText(v.getMarca());
        txtModelo.setText(v.getModelo());
        txtAno.setText(String.valueOf(v.getAno()));
        txtValorBase.setText(String.valueOf(v.getValorBaseDiaria()));
        chkAr.setSelected(v instanceof VeiculoPopular p && p.isPossuiArCondicionado());
        txtPortaMalas.setText(v instanceof VeiculoSedan s ? String.valueOf(s.getCapacidadePortaMalasLitros()) : "");
        chkTracao.setSelected(v instanceof VeiculoSUV s && s.isTracao4x4());
    }

    private void limparFormulario() {
        selecionado = null;
        tabela.clearSelection();
        cmbTipo.setEnabled(true);
        cmbTipo.setSelectedIndex(0);
        txtPlaca.setEnabled(true);
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAno.setText("");
        txtValorBase.setText("");
        chkAr.setSelected(false);
        txtPortaMalas.setText("");
        chkTracao.setSelected(false);
        txtPlaca.requestFocus();
    }

    /** Monta o objeto da categoria escolhida a partir dos campos do formulário. */
    private Veiculo montarVeiculo() {
        String placa = txtPlaca.getText();
        String marca = txtMarca.getText();
        String modelo = txtModelo.getText();
        int ano = UtilGUI.lerInteiro(txtAno.getText(), "Ano");
        double base = UtilGUI.lerDecimal(txtValorBase.getText(), "Valor base da diária");
        return switch ((String) cmbTipo.getSelectedItem()) {
            case "Popular" -> new VeiculoPopular(placa, marca, modelo, ano, base, chkAr.isSelected());
            case "Sedan" -> new VeiculoSedan(placa, marca, modelo, ano, base,
                    UtilGUI.lerDecimal(txtPortaMalas.getText(), "Porta-malas (litros)"));
            default -> new VeiculoSUV(placa, marca, modelo, ano, base, chkTracao.isSelected());
        };
    }

    private void salvar() {
        try {
            Veiculo v = montarVeiculo();
            if (selecionado == null) {
                service.cadastrarVeiculo(v);
                UtilGUI.info(this, "Veículo cadastrado com sucesso!");
            } else {
                v.setDisponivel(selecionado.isDisponivel()); // edição não altera o status
                service.atualizarVeiculo(v);
                UtilGUI.info(this, "Veículo atualizado com sucesso!");
            }
            limparFormulario();
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }

    private void excluir() {
        if (selecionado == null) {
            UtilGUI.aviso(this, "Selecione um veículo na tabela para excluir.");
            return;
        }
        if (!UtilGUI.confirmar(this, "Excluir o veículo " + selecionado + "?")) {
            return;
        }
        try {
            service.removerVeiculo(selecionado.getPlaca());
            UtilGUI.info(this, "Veículo excluído com sucesso!");
            limparFormulario();
            atualizar();
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }
}
