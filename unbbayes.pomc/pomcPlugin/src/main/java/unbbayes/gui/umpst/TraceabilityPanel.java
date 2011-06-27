package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

public class TraceabilityPanel extends IUMPSTPanel{
	

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	private UmpstModule janelaPaiAux; 
	private GoalModel goal;

	
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"name","type"};
	Object[][] data = {};

	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TraceabilityPanel(UmpstModule janelaPai, GoalModel goal) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	this.janelaPaiAux = janelaPai;
    	    	this.goal=goal;
    	    	
    	    	this.add(createScrolltableHypothesis());
    		    
    	  }
    	
    	  
    	  
    	  public void setJanelaPai(UmpstModule janelaPai){
    		// super(janelaPai);
  	    	
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableHypothesis());
    		  
    	  }
    
   
	
	/**
	 * @return the table
	 */
	public JTable createTable() {

		if ( (goal!=null)&&(goal.getFowardTrackingEntity() !=null) ){			
			data = new Object[10][2];
			
			Set<EntityModel> aux = goal.getFowardTrackingEntity();
			EntityModel entity;
			int i = 0;
			
	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
	    		entity = it.next();
	    		data[i][0] = entity.getEntityName();
	    		data[i][1] = "Entity";
	    	}

			
			
		}
		
		
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		
		
		return table;
       }
	
	
	public JScrollPane createScrolltableHypothesis(){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(createTable());
			scrollpanePergunta.setMinimumSize(new Dimension(300,150));
		}
		
		return scrollpanePergunta;
	}
	
	
	
	public JScrollPane getScrollPanePergunta(){
		
		return scrollpanePergunta; 
	}
	
	 
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MainPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
  
	

}
