
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.Node;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.jprs.jbn.PotentialTable;
import unbbayes.fronteira.TJanelaEdicao;


public class ProbabilisticController extends LearningToolkit{

    private boolean ok; 
    
    public ProbabilisticController(NodeList variables,byte[][] matrix,
                       int[] vector,long caseNumber, MainController controller, boolean compacted){
        this.compacted = compacted;
        this.dataBase = matrix;
        this.vector = vector;
        this.caseNumber = caseNumber;
    	TVariavel variable;
    	int parentsLength;
    	int[][] arrayNijk;
    	PotentialTable table;
    	ProbabilisticNetwork net    = controller.makeNetwork(variables);
    	int length  = variables.size();    	
        for(int i = 0; i < length; i++) {
            variable  = (TVariavel)variables.get(i);
            table     = variable.getProbabilidades();
            table.addVariable(variable);
        }
    	TJanelaEdicao window = new TJanelaEdicao(net);
        for(int i = 0; i < length; i++) {
            variable  = (TVariavel)variables.get(i);
            arrayNijk = getFrequencies(variable,variable.getPais());                        
            table     = variable.getProbabilidades();
            parentsLength = variable.getTamanhoPais();
for2:       for (int j = 0; j < parentsLength; j++) {
            	Node pai = variable.getPais().get(j);
            	for (int k = 0; k < table.variableCount(); k++) {
            		if (pai == table.getVariableAt(k)) {
            			continue for2;
            		}            		
            	}
                table.addVariable(pai);
            }
            getProbability(arrayNijk, variable); 
        }                
        controller.showNetwork(net);
    }
}
