
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;


public abstract class K2Toolkit extends PonctuationToolkit{
	
     
    protected void constructPredecessors(NodeList list){ 
        TVariavel aux;
        int length = list.size();
        for(int i = length - 1; i > 0  ; i--){
            aux = (TVariavel)list.get(i);
                for (int j = i-1; j > -1 ; j--){
                    aux.adicionaPredecessor((TVariavel)list.get(j));
                }
        }
    }    
    
    protected Object[] getZMax(TVariavel variable){    	
        TVariavel z = null;
        NodeList parents;
        NodeList zVector;
        double maxAux;
        maxAux = 0.0;
        double max = -1*Double.MAX_VALUE;       
        zVector = difference(SetToolkit.clone(variable.getPredecessores()), variable.getPais());
        int length = zVector.size();
        for (int i = 0 ; i < length; i++ ){
            parents = union(SetToolkit.clone(variable.getPais()), (TVariavel)zVector.get(i));
            maxAux  = getG(variable,parents);
            if (max < maxAux){
                max = maxAux;
                z = (TVariavel)zVector.get(i);
            }
        }
        return new Object[]{z,new Double(max)};        
    }    
    
    protected NodeList union(NodeList list, TVariavel variable){
        if (list == null){
            list = new NodeList();
        }
        list.add(variable);
        return list;
    }
    
    protected NodeList difference(NodeList list1, NodeList list2){
        TVariavel aux;
        TVariavel aux2;
        NodeList listReturn = SetToolkit.clone(list1);
        for (int i = 0 ;  i < listReturn.size(); i++ ){
            aux = (TVariavel)listReturn.get(i);
            for (int j = 0 ; j < list2.size() ; j++ ){
                aux2 = (TVariavel)list2.get(j);
                if (aux2.getName().equals(aux.getName())){
                    listReturn.remove(i);
                }
            }
        }
        return listReturn;
    }
    
    
    

}
