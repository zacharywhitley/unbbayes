
package unbbayes.aprendizagem;
import unbbayes.util.NodeList;
import javax.swing.DefaultListModel;
import javax.swing.JList;


public class RelationInterationController {
	
	private RelationsWindow frame;
	private NodeList variables;
	
	public RelationInterationController(RelationsWindow frame, NodeList variables){
		this.frame = frame;
		this.variables = variables;
	}
	
	public void addEvent(Object index1, Object index2){
		DefaultListModel relationListModel = frame.getRelationModel();		        
        String relationIndex  = index1 +  "-->" + index2;
        boolean flag = false;
        if (!(relationIndex.equals("null-->null"))){
            for(int i = 0; i < variables.size(); i++){
                TVariavel aux = (TVariavel)variables.get(i);
                if (aux.getName().equals(""+index2)){
                    for(int j = 0; j < variables.size(); j++){
                        if (i != j){
                          aux = (TVariavel)variables.get(j);
                          if (aux.getName().equals(""+index1)){
                              aux = (TVariavel)variables.get(j);
                              if (!(aux.getPai(""+index2).equals(""+index2))){
                                  aux = (TVariavel)variables.get(i);
                                  NodeList auxVector = aux.getPais();
                                  for (int k = 0; k < auxVector.size() ; k++ ){
                                      TVariavel aux1 = (TVariavel)auxVector.get(k);
                                      if (aux1.getName().equals(""+index1)){
                                          flag = true;
                                          break;      
                                      }                                
                                  }
                                  if (!flag){ 
                                   	  aux.adicionaPai((TVariavel)variables.get(j));
                                      relationListModel.addElement(relationIndex);
                                  }
                              }
                          }
                        }
                    }
                }
            }
        }		
	}
	
	public void removeEvent(Object relation){	    
        String relationName = (String)relation;
        NodeList auxVector  = new NodeList();
        JList relationList  = frame.getRelationList();
        DefaultListModel relationListModel = (DefaultListModel)relationList.getModel();
        if (!relationList.isSelectionEmpty() && relation != null){
            int index     = relationName.indexOf('-');
            int length    = relationName.length();
            boolean flag  = false;
            String parentName = relationName.substring(0,index);
            String sunName     = relationName.substring(index +3,length);
            for (int i = 0; i < variables.size() ; i++ ){
                TVariavel aux = (TVariavel)variables.get(i);
                if (aux.getName().equals(sunName)){
                    auxVector =  aux.getPais();
                    for (int j = 0 ; j < auxVector.size() ;j++ ){
                        TVariavel aux1 = (TVariavel)auxVector.get(j);
                        if (aux1.getName().equals(parentName)){
                            auxVector.remove(j);
                            relationListModel.removeElement(relation);
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        break;
                    }
                }
            }
        }	 		
	}
	
	public void continueEvent(){
		frame.dispose();		
	}
	

}
