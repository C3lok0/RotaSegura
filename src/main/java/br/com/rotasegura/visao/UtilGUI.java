package br.com.rotasegura.visao;

import br.com.rotasegura.excecao.DadosInvalidosException;
import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.excecao.VeiculoIndisponivelException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Utilitários da camada visual (diálogos, leitura de campos e layout de formulários). */
final class UtilGUI {

    static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private UtilGUI() {
    }

    // --- Caixas de diálogo ---

    static void info(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Rota Segura", JOptionPane.INFORMATION_MESSAGE);
    }

    static void aviso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** Mostra o erro ao usuário sem encerrar a aplicação. */
    static void erro(Component pai, Exception ex) {
        String msg;
        String titulo = "Erro";
        if (ex instanceof DadosInvalidosException || ex instanceof VeiculoIndisponivelException
                || ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            msg = ex.getMessage();
            titulo = "Dados inválidos";
        } else if (ex instanceof PersistenciaException) {
            msg = ex.getMessage();
            titulo = "Erro no banco de dados";
        } else {
            msg = "Erro inesperado: " + ex;
        }
        JOptionPane.showMessageDialog(pai, msg, titulo, JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirmar(Component pai, String mensagem) {
        return JOptionPane.showConfirmDialog(pai, mensagem, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static void mostrarTexto(Component pai, String titulo, String texto) {
        JTextArea area = areaTexto(texto, 18, 52);
        JOptionPane.showMessageDialog(pai, new JScrollPane(area), titulo, JOptionPane.PLAIN_MESSAGE);
    }

    static JTextArea areaTexto(String texto, int linhas, int colunas) {
        JTextArea area = new JTextArea(texto, linhas, colunas);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return area;
    }

    // --- Leitura de campos ---

    static int lerInteiro(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um número inteiro.");
        }
    }

    static double lerDecimal(String texto, String campo) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um número (ex.: 150,00).");
        }
    }

    static LocalDate lerData(String texto, String campo) {
        try {
            return LocalDate.parse(texto.trim(), FMT_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("O campo \"" + campo + "\" deve estar no formato dd/MM/yyyy.");
        }
    }

    static String formatarData(LocalDate data) {
        return data == null ? "" : data.format(FMT_DATA);
    }

    static String moeda(double valor) {
        return String.format("R$ %.2f", valor);
    }

    // --- Layout de formulários ---

    static void linha(JPanel painel, int linha, String rotulo, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.gridy = linha;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        painel.add(new JLabel(rotulo), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        painel.add(campo, c);
    }

    static JPanel formulario(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createTitledBorder(titulo));
        p.setPreferredSize(new Dimension(470, 10));
        return p;
    }
}
