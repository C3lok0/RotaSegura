/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.rotasegura.repositorio;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author MARCELO JUNIOR
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositorioGenerico<T> {
    private final List<T> elementos;

    public RepositorioGenerico() {
        this.elementos = new ArrayList<>();
    }

    public void adicionar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("Não é possível adicionar um elemento nulo.");
        }
        this.elementos.add(elemento);
    }

    public boolean remover(T elemento) {
        return this.elementos.remove(elemento);
    }

    public List<T> listarTodos() {
        return Collections.unmodifiableList(this.elementos);
    }

    public int quantidade() {
        return this.elementos.size();
    }
}
