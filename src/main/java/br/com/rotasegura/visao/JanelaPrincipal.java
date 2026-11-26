package br.com.rotasegura.visao;

import br.com.rotasegura.servico.LocacaoService;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Dimension;

/** Janela principal da aplicação, com uma aba para cada área do sistema. */
@SuppressWarnings({"serial", "this-escape"})
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal(LocacaoService service) {
        super("Locadora Rota Segura");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Clientes", new PainelClientes(service));
        abas.addTab("Veículos", new PainelVeiculos(service));
        abas.addTab("Locações", new PainelLocacoes(service));
        abas.addChangeListener(e -> ((Atualizavel) abas.getSelectedComponent()).atualizar());
        add(abas);

        ((Atualizavel) abas.getSelectedComponent()).atualizar();
        setMinimumSize(new Dimension(1000, 560));
        setSize(1150, 650);
        setLocationRelativeTo(null);
    }
}
