/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.jprs.jbn;

import java.awt.Color;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;


/**
 *  Classe que representa um n� gen�rico.
 *
 *@author     Michael e Rommel
 */
public abstract class Node implements Comparable {
    private String description = "";
    protected String name;
    private Point2D.Double posicao;
    protected List parents;
    private List children;
    protected List states;
    private List adjacents;
    private boolean selecionado;
    private static int altura;
    private static int largura;


    /**
     *  Constr�i um novo n� e faz as devidas inicializa��es.
     */
    public Node() {
        adjacents = new ArrayList();
        parents = new ArrayList();
        children = new ArrayList();
        states = new ArrayList();
        altura = 35;
        largura = 35;
        posicao = new Point2D.Double();
        selecionado = false;
    }


    /**
     *  Modifica o nome do n�.
     *
     *@param  texto  descri��o do n�.
     */
    public void setDescription(String texto) {
        this.description = texto;
    }


    /**
     *  Modifica a sigla do n�.
     *
     *@param  sigla sigla do n�.
     */
    public void setName(String sigla) {
        this.name = sigla;
    }


    /**
     *  Insere nova lista de filhos.
     *
     *@param  filhos  List de n�s que representam os filhos.
     */
    public void setChildren(List filhos) {
        this.children = filhos;
    }


    /**
     *  Insere nova lista de pais.
     *
     *@param  pais  List de n�s que representam os pais.
     */
    public void setParents(List pais) {
        this.parents = pais;
    }


    /**
     *  Modifica a posicao do n�.
     *
     *@param  x  Posi��o x do n�.
     *@param  y  Posi��o y do n�.
     */
    public void setPosicao(double x, double y) {
        posicao.setLocation(x, y);
    }


    /**
     *  Modifica a largura do n�.
     *
     *@param  l  Nova largura do n�.
     */
    public static void setLargura(int l) {
        largura = l;
    }


    /**
     *  Modifica a altura do n�.
     *
     *@param  a  A nova altura do n�.
     */
    public static void setAltura(int a) {
        altura = a;
    }

    /**
     *  Modifica o status de sele��o do n�.
     *
     *@param  b  Status de sele��o.
     */
    public void setSelected(boolean b) {
        selecionado = b;
    }


    /**
     *  Retorna o nome do n�.
     *
     *@return    descri��o do n�.
     */
    public String getDescription() {
        return description;
    }


    /**
     *  Retorna a lista de adjacentes.
     *
     *@return    Refer�ncia para os adjacentes do n�.
     */
    public List getAdjacents() {
        return adjacents;
    }


    /**
     *  Retorna a sigla do n�.
     *
     *@return    Sigla do n�.
     */
    public String getName() {
        return name;
    }


    /**
     *  Retorna a lista de filhos.
     *
     *@return    Lista de filhos.
     */
    public List getChildren() {
        return children;
    }


    /**
     *  Retorna a lista de pais.
     *
     *@return    Lista de Pais.
     */
    public List getParents() {
        return parents;
    }

    /**
     *  Retorna a posi��o gr�fica do n�.
     *
     *@return    Posi��o do n�.
     */
    public Point2D.Double getPosicao() {
        return posicao;
    }


    /**
     *  Retorna a largura do n�.
     *
     *@return    largura do n�.
     */
    public static int getLargura() {
        return largura;
    }


    /**
     *  Retorna a altura do n�.
     *
     *@return    Altura do n�.
     */
    public static int getAltura() {
        return altura;
    }

    /**
     *  Retorna o status de sele��o do n�.
     *
     *@return    Status de sele��o do n�.
     */
    public boolean isSelecionado() {
        return selecionado;
    }


    /**
     *  Insere um estado com o nome especificado no final da lista.
     *
     *@param  estado  Nome do estado a ser inserido.
     */
    public void appendState(String estado) {
        updateTables();
        states.add(estado);
    }


    /**
     *  Retira o estado criado mais recentemente.
     *  Isto �, o �ltimo estado da lista.
     */
    public void removeLastState() {
        if (states.size() > 1) {
            updateTables();
            states.remove(states.size() - 1);
        }
    }


    /**
     *  Substitui o estado da posi��o especificada pelo estado especificado.
     *
     *@param  estado  Nome do estado atualizado.
     *@param  index   �ndice em que deseja-se modificar, come�ando do 0.
     */
    public void setStateAt(String estado, int index) {
        states.set(index, estado);
    }


    /**
     *  Retorna o n�mero de estados do n�.
     *
     *@return    Retorna o n�mero de estados do n�.
     */
    public int getStatesSize() {
        return states.size();
    }


    /**
     *  Retorna o estado da posi��o <code>index</code>
     *
     *@param  index  �ndice do estado a ser lido.
     *@return        Nome do estado da posi��o <code>index</code>
     */
    public String getStateAt(int index) {
        return (String)(states.get(index));
    }


    /**
     *  Imprime a descri��o do n� no formato: "descri��o (sigla)" (sem aspas)
         *  � utilizado no JTree da Interface quando a rede � compilada.
     *
     *@return    descri��o do n� formatado.
     */
    public String toString() {
        return description + " (" + name + ")";
    }


    /**
     *  Monta lista de n�s adjacentes.
     */
    protected void montaAdjacentes() {
        adjacents.addAll(parents);
        adjacents.addAll(children);
    }


    /**
     *  Desmonta a lista de n�s adjacentes.
     */
    protected void desmontaAdjacentes() {
        adjacents.clear();
    }


    /**
     *  Adiciona um n� na lista de pais.
     *
     *@param  pai  pai a ser inserido
     */
    protected void adicionaPai(Node pai) {
        parents.add(pai);
    }

    /**
     * M�todo utilizado para a ordena��o dos n�s por Descri��o.
     */
    public int compareTo(Object o) {
        Node no = (Node)o;
        return description.compareToIgnoreCase(no.getDescription());
    }


    /**
     * Utilizado para notificar as tabelas de que esta vari�vel faz parte de que houve uma
     * modifica��o na estrutura desta vari�vel.
     */
    private void updateTables() {
        ITabledVariable aux;
        if (this instanceof ITabledVariable) {
           aux = (ITabledVariable)this;
           aux.getPotentialTable().variableModified();
        }

        for (int i = children.size()-1; i >= 0; i--) {
            if (children.get(i) instanceof ITabledVariable) {
               aux = (ITabledVariable)children.get(i);
               aux.getPotentialTable().variableModified();
            }
        }
    }
}
