package unbbayes.prs.bn;

/**
 * Utility Potential Table
 * @author Michael
 */
public class UtilityTable extends PotentialTable implements java.io.Serializable {

    public UtilityTable() {
    }

    /**
     * Returns a new instance of UtilityTable. Implements the abstract method from PotentialTable.
     * @return a new instance of UtilityTable.
     */
    public PotentialTable newInstance() {
        return new UtilityTable();
    }

    /**
     *  Retira a variável da tabela. Utilizado também para marginalização generalizada.
     *
     *@param  variavel  Variavel a ser retirada da tabela.
     */
    public void removeVariable(Node variavel) {
    	calcularFatores();
        int index = variaveis.indexOf(variavel);
        if (variavel.getType() == Node.PROBABILISTIC_NODE_TYPE) {
//            sum(variaveis.size()-1, index, new int[variaveis.size()]);
			sum(variaveis.size()-1, index, 0, 0);			
        } else {        	
            DecisionNode decision = (DecisionNode) variavel;
            if (decision.hasEvidence()) {
                finding(variaveis.size()-1, index, new int[variaveis.size()], decision.getEvidence());
            } else {
                argMax(variaveis.size()-1, index, new int[variaveis.size()]);
            }
        }
        variableModified();
        variaveis.remove(index);
    }

    protected void argMax(int control, int index, int coord[]) {
        if (control == -1) {
            int linearCoordToKill = getLinearCoord(coord);
            int linearCoordDestination = linearCoordToKill - coord[index]*fatores[index];
            float value = Math.max(dados.data[linearCoordDestination], dados.data[linearCoordToKill]);
            dados.data[linearCoordDestination] = value;
            dados.remove(linearCoordToKill);
            return;
        }

        int fim = (index == control) ? 1 : 0;
        Node node = variaveis.get(control);
        for (int i = node.getStatesSize()-1; i >= fim; i--) {
            coord[control] = i;
            argMax(control-1, index, coord);
        }
    }
}