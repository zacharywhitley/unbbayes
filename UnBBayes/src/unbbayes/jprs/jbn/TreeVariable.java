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

package unbbayes.jprs.jbn;

/**
 * Interface para variáveis que serão visualizadas na árvore.
 * Interface para o DecisionNode e ProbabilisticNode.
 */
public abstract class TreeVariable extends Node implements java.io.Serializable {

    // Clique que a variável está associada.
    protected ITabledVariable cliqueAssociado;

    // Armazena marginais e evidências.
    protected double[] marginais;

    private int evidence = -1;

    /**
     * Tem que ser sobrescrito para atualizar as marginais
     * que serão visualizadas na árvore da interface.
     */
    abstract void marginal();

    /**
     *  Retorna o valor da marginal de determinado índice.
     *
     *@param index returna a marginal do estado especificado pelo parâmetro <code>index</code>
     *@return    valor da marginal de determinado índice.
     */
    public double getMarginalAt(int index) {
        return marginais[index];
    }

    void setMarginalAt(int index, double value) {
        marginais[index] = value;
    }

    /**
     * Limpa o flag de evidência.
     */
    void resetEvidence() {
        evidence = -1;
    }


    /**
     * Retorna true se esta variável contém alguma evidência e false caso contrário.
     *
     * @return true se esta variável contém alguma evidência e false caso contrário.
     */
    boolean hasEvidence() {
        return (evidence != -1);
    }


    int getEvidence() {
        return evidence;
    }


    /**
     * Adiciona um finding (evidência) no estado especificado.
     *
     * @param stateNo índice do estado a ser adicionado o finding.
     */
    public void addFinding(int stateNo) {
        double[] likelihood = new double[getStatesSize()];
        evidence = stateNo;
        likelihood[stateNo] = 1.0;
        addLikeliHood(likelihood);
    }

    /**
     * Adicina um likelihood nesta variável
     *
     * @param valores array contendo o likelihood de cada estado da variável.
     */
    public void addLikeliHood(double valores[]) {
        for (int i = 0; i < getStatesSize(); i++) {
            setMarginalAt(i, valores[i]);
        }
    }

    /**
     *  Retorna o clique associado a esta variavel
     *
     *@return    clique associado
     */
    public ITabledVariable getAssociatedClique() {
        return this.cliqueAssociado;
    }

    /**
     *  Associa esta variavel ao clique do parametro.
     *
     *@param  clique  clique associado a esta variavel.
     */
    void setAssociatedClique(ITabledVariable clique) {
        this.cliqueAssociado = clique;
    }
}