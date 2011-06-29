package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashSet;
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
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

public class TableSubGoals extends IUMPSTPanel{
	

	private static final long serialVersionUID = 1L;
		
	private JTable table;
	private JScrollPane scrollpanePergunta;
	private UmpstModule janelaPaiAux; 
	private GoalModel goalRelated;

	
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"id","Hypothesis","","",""};
	Object[][] data = {};

	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TableSubGoals(UmpstModule janelaPai,UMPSTProject umpstProject, GoalModel goalRelated) {
    		  
    		    super(janelaPai);
    		    
    		    this.setUmpstProject(umpstProject);
    		    
    	    	this.setLayout(new GridLayout(1,0));
    	    	this.janelaPaiAux = janelaPai;
    	    	this.goalRelated=goalRelated;
    	    	
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

		
		Integer i=0;

		if (goalRelated!=null){			
			data = new Object[goalRelated.getSubgoals().size()][5];

			
			Set<String> keys = goalRelated.getSubgoals().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			
			
			for (String key: sortedKeys){
		
				data[i][0] = goalRelated.getSubgoals().get(key).getId();
				data[i][1] = goalRelated.getSubgoals().get(key).getGoalName();
				data[i][2] = "";
				data[i][3] = "";
				data[i][4] = "";
				i++;
			}
		}
		
		
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/edit.gif") );

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-3);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = data[row][0].toString();
				GoalModel goalAux = getUmpstProject().getMapGoal().get(key);
				changePanel(new SubgoalsAdd(getFatherPanel(),getUmpstProject(), goalAux, goalAux.getGoalFather() )   );
			}
		});
		
		

		
		TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/add.gif") );

			}
		});

		TableColumn buttonColumn2 = table.getColumnModel().getColumn(columnNames.length-2);
		buttonColumn2.setMaxWidth(22);
		buttonColumn2.setCellRenderer(buttonAdd);
		buttonColumn2.setCellEditor(buttonAdd);
		
		buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				String key = data[row][0].toString();
				GoalModel goalAux = getUmpstProject().getMapGoal().get(key);
				changePanel(new SubgoalsAdd(getFatherPanel(),getUmpstProject(),null,goalAux));	
			}
		});
		
		
		TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/del.gif") );

			}
		});
		TableColumn buttonColumn3 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn3.setMaxWidth(25);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);
		
		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
			
			
			public void onButtonPress(int row, int column) {
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete the goal: "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							
						String key = data[row][0].toString();
						GoalModel goalToBeDeleted = getUmpstProject().getMapGoal().get(key);

						goalRelated.getSubgoals().remove(key);
						getUmpstProject().getMapHypothesis().remove(key);

						
						/*Updating MapSearch*/
						deleteFromSearchMap(goalToBeDeleted);

						
						
						
						if(goalToBeDeleted.getSubgoals() !=null){
							int numberSubgoal = goalToBeDeleted.getSubgoals().size()+1;
							for (int i = 1; i < numberSubgoal; i++) {
								if (goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather()!=null){
									goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather().getSubgoals().remove(goalToBeDeleted.getId());
								}
								
								goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setGoalFather(null);
								//goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setId("D"+i);
							}
						}
						
						
						getUmpstProject().getMapGoal().remove(key);
						
						Set<String> keys = getUmpstProject().getMapGoal().keySet();
						TreeSet<String> sortedKeys = new TreeSet<String>(keys);
						
						for (String keyAux: sortedKeys){
							getUmpstProject().getMapGoal().get(keyAux).getSubgoals().remove(key);
						}
						
						UmpstModule pai = getFatherPanel();
					    changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalRelated)	);
						 
						 JTable table = createTable();
						 
						 getScrollPanePergunta().setViewportView(table);
						 getScrollPanePergunta().updateUI();
						 getScrollPanePergunta().repaint();
						 updateUI();
						 repaint();
						   
			
				}
			}
		});
		
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
    
  
	
    public void deleteFromSearchMap(GoalModel goalToBeDeleted){
    	Set<GoalModel> aux = new HashSet<GoalModel>();
		GoalModel goalBeta;
		String[] strAux = goalToBeDeleted.getGoalName().split(" ");

	    for (int i = 0; i < strAux.length; i++) {
    		if(getUmpstProject().getMapSearchGoal().get(strAux[i])!=null){

    			getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated().remove(goalToBeDeleted);
    			aux = getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated();
    	    	for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
    	    		goalBeta = it.next();
    	   		}
    		}
    		
	    	
	    }
    }

}
