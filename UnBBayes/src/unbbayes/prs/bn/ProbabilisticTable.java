package unbbayes.prs.bn;

import java.util.ResourceBundle;

import unbbayes.prs.*;
import unbbayes.prs.id.*;

/**
 * Probabilistic Potential Table
 * @author Michael
 */
public class ProbabilisticTable extends PotentialTable implements java.io.Serializable {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

    public ProbabilisticTable() {
    }

    /**
     *  Retira a variável da tabela. Utilizado também para marginalização generalizada.
     *
     *@param  variavel  Variavel a ser retirada da tabela.
     */
    public void removeVariable(Node variavel) {
    	calcularFatores();
        int index = variaveis.indexOf(variavel);
        if (variavel.getType() == Node.DECISION_NODE_TYPE) {
            DecisionNode decision = (DecisionNode) variavel;
            int statesSize = variavel.getStatesSize();
            if (decision.hasEvidence()) {
                finding(variaveis.size()-1, index, new int[variaveis.size()], decision.getEvidence());
            } else {
//                sum(variaveis.size()-1, index, 0, 0);
                sum(index);
                for (int i = dados.size-1; i >= 0; i--) {
                    dados.data[i] = dados.data[i] / statesSize;
                }
            }
        } else {
//          sum(variaveis.size()-1, index, 0, 0);
          sum(index);
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
            auxNo = variaveis.get(k);
            noCol *= auxNo.getStatesSize();
        }

        float soma;
        for (int j = 0; j < noCol; j++) {
            soma = 0;
            for (int i = 0; i < noLin; i++) {
                soma += dados.data[j * noLin + i] * 100;
            }

            if (Math.abs(soma - 100.0) > 0.01) {
                throw new Exception(resource.getString("variableTableName") + variaveis.get(0) + resource.getString("inconsistencyName") + soma + "%\n");
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