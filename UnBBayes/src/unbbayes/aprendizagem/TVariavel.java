/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
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
 package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.prs.bn.*;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * Classe que define um variável de um banco de casos, as variáveis
 * possuem um vetor de pais(do tipo TVariavel), um veto de estados
 * (do tipo String), um vetor de predecessores, que são os candidatos
 * a pais (do tipo TVariavel), um nome e um numero máximo de pais.
 * O modelo ainda possui uma variavel que informa o numero de casos
 * em um determinado banco de casos.
 * @version 1.0
 * @author Danilo Custódio da Silva
 */

public class TVariavel extends ProbabilisticNode implements Cloneable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private NodeList predecessores;
    private TVariavel variavelAux;
    private int numeroMaximoPais;
    private int pos;
    private boolean rep;
    private boolean participa;

    /**
     * Constutor de uma TVariavel.
     * @param nome - O Nome de uma TVariavel(<code>String<code>)
     * @param pos - A posição de uma TVariavel em um vetor de
     * variáveis(<code>int<code>)
     * @see List
     */
    TVariavel(String nome, int pos){
        super();
        setName(nome);
        predecessores = new NodeList();
        numeroMaximoPais = 10;
        participa = true;
        this.pos = pos;
    }

    /**
     * Método para determinar a posição de uma variável no vetor
     * de variáveis
     * @param pos - Posição que a variável ocupará no vetor(<code>
     * int<code>)
     */
    public void setPos(int pos){
        this.pos = pos;
    }

    /**
     * Método que seta a variável a condição de que ela será ou
     * não a variável que conterá o numero de vezes que um deter
     * minado registro se repete.
     * @param rep - Indica se a condiçao para essa variável é ver
     * dadeira ou falsa(<code>boolean<code>)
     */
    public void isRep(boolean rep){
        this.rep = rep;
    }

    /**
     * Método que indica se uma determinada variável é ou não
     * a variável que contem o número de vezes que um registro
     * se repete.
     * @return boolean - Indica se a variável e ou nao a variável
     * que contem o número de vezes que um registro se repete
     */
    public boolean getRep(){
        return rep;
    }

    /**
     * Método que clona a variável original.
     * @return Object - Retorna um objeto que é uma cópia
     * da variável original, mas com uma nova referência
     * @see TVariavel
     */
    public Object clone(){
        TVariavel variavel = new TVariavel(this.name, this.pos);
        variavel.setPais(SetToolkit.clone(parents));
        variavel.setEstados(SetToolkit.clone(states));
        variavel.setPredecessores(SetToolkit.clone(predecessores));
        variavel.setNumeroMaximoPais(this.numeroMaximoPais);
        return variavel;
    }

    /**
     * Método que indica a posição da variável no vetor de variáveis
     * @return int - Posição da variável dentro do vetor de variáveis
     */
    public int getPos(){
        return this.pos;
    }

    /**
     * Método que coloca na variável o numero máximo de pais
     * permitidos a ela.
     * @param numero - Número máximo de pais(<code>int<code>)
     */
    public void setNumeroMaximoPais(int numero){
        numeroMaximoPais = numero;
    }

    /**
     * Método que indica o número máximo de pais que uma
     * variável pode ter.
     * @return int - Retorna o número máximo de pais que uma
     * variável pode conter
     */
    public int getNumeroMaximoPais(){
        return this.numeroMaximoPais;
    }

    /**
     * Método que retorna os estados de uma variável.
     * @return List - Array de estados da variável
     * @see List
     */
    public List getEstados(){
        return states;
    }

    /**
     * Método que retorna os predecessores de uma variável,
     * ou seja, as variáveis que podem ser pais dessa variável.
     * @return List - Array de predecessores da variável
     * @see List
     */
    public NodeList getPredecessores(){
        return predecessores;
    }

    /**
     * Método que retorna os pais de uma variável.
     * @return List - Array de pais da variável
     * @see List
     */
    public NodeList getPais(){
        return parents;
    }

    /**
     * Método que retorna a tabela com todas as probabilidades
     * possíveis para as variáveis dependendo dos pais.
     * @return TTabPot - Tabela com as probabilidades(<code>
     * TTabPo<code>)
     * @see TTabPor
     */
    public PotentialTable getProbabilidades(){
        return this.getPotentialTable();
    }

    /**
     * Método que indica o tamanho do número de predecessores
     * de uma variável.
     * @return int - Número de predecessores de uma variável
     * @see List
     */
    public int getTamanhoPredecessores(){
        return predecessores.size();
    }

    /**
     * Método que indica o tamanho do número de pais de uma
     * variável.
     * @return int - Número de pais de uma variável
     * @see List
     */
    public int getTamanhoPais(){
        if (parents == null){
            parents = new NodeList();
        }
        return parents.size();
    }

    /**
     * Método que retorna o estado que está em um determinado
     * indice do vetor de estados.
     * @param indexo - Indice do estado no vetor de estados
     * (<code>int<code>
     * @return String - Nome do estado
     * @see String
     */
    public String getEstado(int index){

        return (String)states.get(index);
    }

    /**
     * Método que indica o numero de estados da variável.
     * @return int - Tamanho do vetor de estados
     * @see List
     */
    public int getEstadoTamanho(){
        return states.size();
    }

    /**
     * Método para adicionar um predecessor à variável.
     * @param predecessor - Variável que será inserida no
     * vetor de predecessores(<code>TVariavel<code>)
     * @see List
     */
    public void adicionaPredecessor(TVariavel predecessor){
        predecessores.add(predecessor);
    }

    /**
     * Método para adicionar um estado à variável.
     * @param predecessor - Estado que será inserido no
     * vetor de estados(<code>String<code>)
     * @see List
     */
    public void adicionaEstado(String estado){
        states.add(estado);
    }

    /**
     * Método que retorna o nome do Ancestral da váriavel
     * que possui o nome específico.
     * @param nome - Nome do ancestral(<code>String<code>)
     * @return String - Nome do ancestral, caso nao exista
     * retorna string vazia
     * @see String
     */
    public String getPai(String nome){
        for (int i = 0; i < parents.size();i++ ){
            variavelAux = (TVariavel)parents.get(i);
                if (variavelAux.getName().equals(nome)){
                    return nome;
                }
                if((variavelAux.getPai(nome).equals(nome))){
                   return nome;
                }
        }
        return "";
    }

    /**
     * Método para adicionar um pai à variável.
     * @param pai - Variável que será inserida no
     * vetor de pais(<code>TVariavel<code>)
     * @see List
     */
    public void adicionaPai(TVariavel pai){
        parents.add(pai);
    }

    /**
     * Método que indica se um estado está ou não presente na
     * variável
     * @param nomeEstado - Nome do estado que está sendo procurado
     * (<code>String<code>)
     * @return boolean - Se o estado existe ou não
     * @see List
     */
    public boolean existeEstado(String nomeEstado){
        int tamanho = states.size();
        for(int tamanhoEstado = 0; tamanhoEstado < tamanho; tamanhoEstado++){
            if(states.get(tamanhoEstado).equals(nomeEstado)){
                return true;
            }
        }
        return false;
    }

    /**
     * Método que retorna a posição de um estado no vetor
     * de estados.
     * @param nomeEstado - Nome do estado que esta sendo procurado
     * (<code>String<code>)
     * @return int - Posição do estado
     */
    public int getEstadoPosicao(String nomeEstado){
        int tamanho = states.size();
        for(int tamanhoEstado = 0; tamanhoEstado < tamanho; tamanhoEstado++){
            if(states.get(tamanhoEstado).equals(nomeEstado)){
                return tamanhoEstado;
            }
        }
        return 0;
    }

    public void setParticipa(boolean estado){
       participa = estado;
    }

    public boolean getParticipa(){
        return participa;
    }

    /**
     * Método para colocar uma lista de pais na variável.
     * Serve para o método clone.
     * @param parents - Lista de pais(<code>Object<code>)
     * @see Clone()
     */
    private void setPais(Object parents){
        this.parents = (NodeList)parents;
    }

    /**
     * Método para colocar uma lista de estados na variável.
     * Serve para o método clone.
     * @param states - Lista de states(<code>Object<code>)
     * @see Clone()
     */
    private void setEstados(Object states){
        this.states = (List)states;
    }

    /**
     * Método para colocar uma lista de predecessores na variável.
     * Serve para o método clone.
     * @param predecessores - Lista de predecessores(<code>Object<code>)
     * @see Clone()
     */
    private void setPredecessores(Object predecessores){
        this.predecessores = (NodeList)predecessores;
    }
}