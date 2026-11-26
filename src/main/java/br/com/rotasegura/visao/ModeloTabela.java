package br.com.rotasegura.visao;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Modelo de tabela somente leitura, genérico, para exibir listas de entidades. */
@SuppressWarnings({"serial", "this-escape"})
class ModeloTabela<T> extends AbstractTableModel {

    private final String[] colunas;
    private final Function<T, Object[]> linha;
    private List<T> dados = new ArrayList<>();

    ModeloTabela(String[] colunas, Function<T, Object[]> linha) {
        this.colunas = colunas;
        this.linha = linha;
    }

    void setDados(List<T> novos) {
        this.dados = new ArrayList<>(novos);
        fireTableDataChanged();
    }

    T get(int indice) {
        return dados.get(indice);
    }

    @Override
    public int getRowCount() {
        return dados.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int coluna) {
        return colunas[coluna];
    }

    @Override
    public Object getValueAt(int linhaIdx, int coluna) {
        return linha.apply(dados.get(linhaIdx))[coluna];
    }
}
