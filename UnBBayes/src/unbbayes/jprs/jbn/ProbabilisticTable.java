package unbbayes.jprs.jbn;

/**
 * Probabilistic Potential Table
 * @author Michael
 */
public class ProbabilisticTable extends PotentialTable {

    public ProbabilisticTable() {
    }

    /**
     *  Retira a variável da tabela. Utilizado também para marginalização generalizada.
     *
     *@param  variavel  Variavel a ser retirada da tabela.
     */
    public void removeVariable(Node variavel) {
        int index = variaveis.indexOf(variavel);
        if (variavel instanceof DecisionNode) {
            DecisionNode decision = (DecisionNode) variavel;
            int statesSize = variavel.getStatesSize();
            if (decision.hasEvidence()) {
                finding(variaveis.size()-1, index, new int[variaveis.size()], decision.getEvidence());
            } else {
                sum(variaveis.size()-1, index, new int[variaveis.size()]);
                for (int i = dados.size()-1; i >= 0; i--) {
                    dados.set(i, dados.get(i) / statesSize);
                }
            }
        } else {
            sum(variaveis.size()-1, index, new int[variaveis.size()]);
        }
        variableModified();
        variaveis.remove(index);
    }


    /**
     *  Verifica a consistência das probabilidades da tabela.
     *
     * @throws Exception se a tabela não soma 100 para todos os estados fixada
     *                   qualquer configuração de estados dos pais.
     */
    public void verificaConsistencia() throws Exception {
        Node auxNo = variaveis.get(0);
        int noLin = auxNo.getStatesSize();
        int noCol = 1;
        int sizeVariaveis = variaveis.size();
        for (int k = 1; k < sizeVariaveis; k++) {
            auxNo = (Node) variaveis.get(k);
            noCol *= auxNo.getStatesSize();
        }

        double soma;
        for (int j = 0; j < noCol; j++) {
            soma = 0.0;
            for (int i = 0; i < noLin; i++) {
                soma += getValue(j * noLin + i) * 100.0;
            }

            if (Math.abs(soma - 100.0) > 0.01) {
                throw new Exception("Tabela da variável " + variaveis.get(0) + " inconsistente -> " + soma + "%\n");
            }
        }
    }

    /**
     * Returns a new instance of a ProbabilisticTable. Implements the abstract method from PotentialTable.
     * @return a new instance of a ProbabilisticTable.
     */
    public PotentialTable newInstance() {
        return new ProbabilisticTable();
    }
}