
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import unbbayes.controlador.MainController;
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
    	TJanelaEdicao window = new TJanelaEdicao(variables, net);	
    	int length  = variables.size();    	
        for(int i = 0; i < length; i++) {
            variable  = (TVariavel)variables.get(i);
            arrayNijk = getFrequencies(variable,variable.getPais());                        
            table     = variable.getProbabilidades();
            table.addVariable(variable);
            parentsLength = variable.getTamanhoPais();
            for (int j = 0; j < parentsLength; j++) {
                table.addVariable(variable.getPais().get(j));
            }
            getProbability(arrayNijk, variable); 
        }                
    }
}
