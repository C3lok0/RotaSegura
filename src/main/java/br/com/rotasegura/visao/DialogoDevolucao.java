package br.com.rotasegura.visao;

import br.com.rotasegura.modelo.Contrato;
import br.com.rotasegura.servico.LocacaoService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Tela de devolução/fechamento do contrato: calcula o valor final na data informada
 * (regras polimórficas de cada categoria), mostra o resumo e emite o relatório de encerramento.
 */
@SuppressWarnings({"serial", "this-escape"})
class DialogoDevolucao extends JDialog {

    private final LocacaoService service;
    private final Contrato contrato;

    private final JTextField txtData = new JTextField(UtilGUI.formatarData(LocalDate.now()), 10);
    private final JTextArea resumo = UtilGUI.areaTexto("", 16, 50);
    private final JButton btnCalcular = new JButton("Calcular");
    private final JButton btnConfirmar = new JButton("Confirmar devolução");
    private final JButton btnFechar = new JButton("Cancelar");

    DialogoDevolucao(Frame dono, LocacaoService service, Contrato contrato) {
        super(dono, "Devolução - Contrato #" + contrato.getId(), true);
        this.service = service;
        this.contrato = contrato;

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Data efetiva de devolução (dd/MM/yyyy):"));
        topo.add(txtData);
        topo.add(btnCalcular);
        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(resumo), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(btnConfirmar);
        rodape.add(btnFechar);
        add(rodape, BorderLayout.SOUTH);

        btnCalcular.addActionListener(e -> calcular());
        txtData.addActionListener(e -> calcular());
        btnConfirmar.addActionListener(e -> confirmar());
        btnFechar.addActionListener(e -> dispose());

        calcular();
        pack();
        setLocationRelativeTo(dono);
    }

    private void calcular() {
        try {
            LocalDate data = UtilGUI.lerData(txtData.getText(), "Data de devolução");
            resumo.setText(contrato.gerarRelatorioEncerramento(data));
            resumo.setCaretPosition(0);
        } catch (Exception ex) {
            resumo.setText("");
            UtilGUI.erro(this, ex);
        }
    }

    private void confirmar() {
        try {
            LocalDate data = UtilGUI.lerData(txtData.getText(), "Data de devolução");
            if (!UtilGUI.confirmar(this, "Confirmar a devolução em " + UtilGUI.formatarData(data) + "?")) {
                return;
            }
            Contrato encerrado = service.realizarDevolucao(contrato.getId(), data);
            resumo.setText(encerrado.gerarRelatorioEncerramento(data));
            resumo.setCaretPosition(0);
            txtData.setEnabled(false);
            btnCalcular.setEnabled(false);
            btnConfirmar.setEnabled(false);
            btnFechar.setText("Fechar");
            try {
                Path arquivo = service.emitirRelatorioEncerramento(encerrado);
                UtilGUI.info(this, "Devolução realizada e contrato finalizado!\nRelatório salvo em:\n" + arquivo);
            } catch (Exception ex) {
                UtilGUI.info(this, "Devolução realizada e contrato finalizado!\n"
                        + "Não foi possível gravar o relatório em arquivo: " + ex.getMessage());
            }
        } catch (Exception ex) {
            UtilGUI.erro(this, ex);
        }
    }
}
