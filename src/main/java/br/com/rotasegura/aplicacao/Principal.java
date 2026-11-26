package br.com.rotasegura.aplicacao;

import br.com.rotasegura.dao.ConexaoBD;
import br.com.rotasegura.excecao.PersistenciaException;
import br.com.rotasegura.servico.LocacaoService;
import br.com.rotasegura.visao.JanelaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author MARCELO JUNIOR
 */
public class Principal {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // mantém o visual padrão do Swing
            }
            try {
                ConexaoBD.inicializar();
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro ao iniciar o banco de dados",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new JanelaPrincipal(new LocacaoService()).setVisible(true);
        });
    }
}
