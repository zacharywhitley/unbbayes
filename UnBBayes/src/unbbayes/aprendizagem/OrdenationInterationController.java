
package unbbayes.aprendizagem;

import unbbayes.controlador.MainController;
import unbbayes.util.NodeList;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import java.awt.event.MouseEvent;

public class OrdenationInterationController {
	
	private NodeList variables;
	private OrdenationWindow frame;
	/*Paradigm algorithm metric parameter*/
	private String[] pamp;
	
	public OrdenationInterationController(NodeList variables, OrdenationWindow frame){
		this.variables = variables;
		this.frame = frame;				
		pamp = new String[4];		
	}
	
	public void upEvent(){
		JList ordenationJL = frame.getOrdenationJL();		
		DefaultListModel listModel   =  (DefaultListModel)ordenationJL.getModel();
		NodeList auxVector = new NodeList();
        String auxName = (String)ordenationJL.getSelectedValue();
        TVariavel aux = null;
        int index = ordenationJL.getSelectedIndex();
        if (index != 0){
            listModel.remove(index);
            index--;
            listModel.add(index, auxName);
            for (int i = 0 ; i < variables.size() ; i++ ){
                 aux = (TVariavel)variables.get(i);
                 if (aux.getName().equals(auxName)){
                     variables.remove(i);
                     for(int j = 0; j < i -1 ; j++){
                         auxVector.add(variables.get(j));
                     }
                     auxVector.add(aux);
                     for (int k = i -1 ; k < variables.size() ; k++){
                          auxVector.add(variables.get(k));
                     }
                     variables = auxVector;
                     for (int g = 0 ;g < variables.size() ; g++ ){
                          aux = (TVariavel)variables.get(g);
                     }
                     ordenationJL.setSelectedIndex(index);
                     break;
                 }
            }
        }			
	}
	
	public void downEvent(){
		JList ordenationJL = frame.getOrdenationJL();		
		DefaultListModel listModel   =  (DefaultListModel)ordenationJL.getModel();
		NodeList auxVector = new NodeList();
        String auxName = (String)ordenationJL.getSelectedValue();
        TVariavel aux = null;
        int index = ordenationJL.getSelectedIndex();
        if (index < listModel.getSize()-1){
            listModel.remove(index);
            index++;
            listModel.add(index, auxName);
            for (int i = 0 ; i < variables.size() ; i++ ){
                 aux = (TVariavel)variables.get(i);
                 if (aux.getName().equals(auxName)){
                     variables.remove(i);
                     for(int j = 0; j < i +1 ; j++){
                         auxVector.add(variables.get(j));
                     }
                     auxVector.add(aux);
                     for (int k = i +1 ; k < variables.size() ; k++){
                          auxVector.add(variables.get(k));
                     }
                     variables = auxVector;
                     for (int g = 0 ;g < variables.size() ; g++ ){
                          aux = (TVariavel)variables.get(g);
                     }
                     ordenationJL.setSelectedIndex(index);
                     break;
                 }
            }
        }				
	}
	
	public String[] getPamp(){
		return pamp;		
	}	
	
	public void continueEvent(String p, String a, String m, String param){
		/*Paradigm Algorithm Metric Parameter*/
		pamp[0] = p;
		pamp[1] = a;
		pamp[2] = m;
		pamp[3] = param; 	
		frame.dispose();					
	}
	
	public void relationsEvent(){
		new RelationsWindow(variables);		
	}
	

	public void doubleClickEvent(MouseEvent e){
		int nClick = e.getClickCount();
        if (nClick == 2){
             JList list = (JList)e.getSource();
             if (!list.isSelectionEmpty()){
                 TVariavel aux = null;
                 Object object = list.getSelectedValue();
                 String name = object.toString();
                 for (int i = 0 ; i < variables.size(); i++ ){
                     aux = (TVariavel)variables.get(i);
                     if (aux.getName().equals(name)){
                         break;
                     }
                 }
                 new OptionsWindow(aux);
             }
        }				
	}
	
	public void paradigmEvent(int index){			
    	JComboBox algorithmsList = frame.getAlgorithmList(); 		
        algorithmsList.removeAllItems();	    	
		JComboBox metricList = frame.getMetricList();
   	    metricList.removeAllItems();    
        if(index ==0 ){            	        	
          	for(int i = 0; i < frame.getPonctuationSize(); i++){			    
           		algorithmsList.addItem(frame.getPonctuatioAlgorithms(i));            		
           	}
           	for(int i = 0; i < frame.getMetricsSize(); i++){			    
           		metricList.addItem(frame.getMetrics(i));            		
           	}
           	metricList.enable();
	    } else if(index == 1) {	    	
	 	    for(int i = 0; i < frame.getIcSize(); i++){			    
           		algorithmsList.addItem(frame.getIcAlgorithms(i));            		
		    }            	            	
		    metricList.disable();
        }		
	}	

}
