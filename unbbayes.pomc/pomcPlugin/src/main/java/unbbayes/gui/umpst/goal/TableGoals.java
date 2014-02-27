package unbbayes.gui.umpst.goal;

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



import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.TableButton.TableButtonCustomizer;
import unbbayes.gui.umpst.TableButton.TableButtonPressedHandler;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;

public class TableGoals extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	private IconController iconController = IconController.getInstance(); 
	
	Integer i = 0;
	
	String[] columnNames = {"ID","Goal","","",""};

	Object[][] data = {};

    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  public TableGoals(UmpstModule janelaPai, UMPSTProject umpstProject) {
    		  
    		    super(janelaPai);
    		    
    		    this.setUmpstProject(umpstProject);
    		    
    	    	this.setLayout(new GridLayout(1,0));
    	    	this.add(createScrolltableGoals(columnNames,data));
    		    
    	  }
    	
    	  public void setJanelaPai(UmpstModule janelaPai,String[] columnNames, Object[][] data){
    		// super(janelaPai);
  	    	
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableGoals(columnNames,data));
    		  
    	  }
    
   
	
	/**
	 * @return the table
	 */
	public JTable createTable(String[] columnNames,final Object[][] data) {
		

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		
		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(50);
		
		//---------------------- Edit Button ------------------------------------------------
		
		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getEditIcon());

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length -3);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = data[row][0].toString();
				GoalModel goalAux = getUmpstProject().getMapGoal().get(key);
				changePanel(new GoalsEditionPanel(getFatherPanel(),getUmpstProject(), goalAux, goalAux.getGoalFather() )   );
			}
		});
		
		
		//---------------------- Add Button ------------------------------------------------
		
		
		TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{                               
				button.setIcon(iconController.getAddIcon());

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
				changePanel(new GoalsEditionPanel(getFatherPanel(),getUmpstProject(),null,goalAux));
				
			}
		});
		
		
		//---------------------- Del Button ------------------------------------------------
		
		TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getDeleteIcon());

			}
		});
		
		TableColumn buttonColumn3 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn3.setMaxWidth(25);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);
		
		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	
			
			
			public void onButtonPress(int row, int column) {
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete goal "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
					
							String key = data[row][0].toString();
							GoalModel goalToBeDeleted = getUmpstProject().getMapGoal().get(key);
							
							//Remove external references. 
							if (goalToBeDeleted.getGoalFather()!=null){
								goalToBeDeleted.getGoalFather().getSubgoals().remove(goalToBeDeleted.getId());
							}

							/*Updating MapSearch*/
							deleteFromSearchMap(goalToBeDeleted);
				
							if(goalToBeDeleted.getSubgoals() !=null){
								int numberSubgoal = goalToBeDeleted.getSubgoals().size();
								for (int i = 1; i < numberSubgoal; i++) {
									if (goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather()!=null){
										goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather().getSubgoals().remove(goalToBeDeleted.getId());
									}
									
									goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setGoalFather(null);
									//goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setId("D"+i);
								}
							}
							
							getUmpstProject().getMapGoal().remove(goalToBeDeleted.getId());
							
							
							 
							Object[][] dataDel = new Object[getUmpstProject().getMapGoal().size()][5];
							Integer i=0;
						    
							Set<String> keys = getUmpstProject().getMapGoal().keySet();
							TreeSet<String> sortedKeys = new TreeSet<String>(keys);
							
							for (String chave: sortedKeys){
								dataDel[i][0] = getUmpstProject().getMapGoal().get(chave).getId();						
								dataDel[i][1] = getUmpstProject().getMapGoal().get(chave).getGoalName();
								dataDel[i][2] = "";
								dataDel[i][3] = "";
								dataDel[i][4] = "";
								i++;
							}
							
							 //TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
							 String[] colunas = {"ID","Goal","","",""};
							 JTable table = createTable(colunas,dataDel);
							
							UmpstModule pai = getFatherPanel();
							 changePanel(pai.getMenuPanel());
							 
						
							 
							 getScrollPanePergunta().setViewportView(table);
							 getScrollPanePergunta().updateUI();
							 getScrollPanePergunta().repaint();
							 updateUI();
							 repaint();
							   
			
				}
			}
		});
		
		
		TableColumn buttonindex = table.getColumnModel().getColumn(0);
		TableColumn buttontext = table.getColumnModel().getColumn(1);
		
		return table;
       }
	
	public JTable getTable(){
		return table; 
	}
	
	public JScrollPane createScrolltableGoals(String[] columnNames, Object[][] data){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(createTable(columnNames,data));
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
		
		//Quebra o nome do goal em diversos pedaços??? 
		String[] strAux = goalToBeDeleted.getGoalName().split(" ");

	    for (int i = 0; i < strAux.length; i++) {
    		if(getUmpstProject().getMapSearchGoal().get(strAux[i]) != null){

    			//Deleta dos goals relacionados!!! (isto com uma palavra so... esquisito) 
    			
    			getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated().remove(goalToBeDeleted);
    			aux = getUmpstProject().getMapSearchGoal().get(strAux[i]).getGoalsRelated();
    			
    			//????? Isto nao esta fazendo nada!!! 
    	    	for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
    	    		goalBeta = it.next();
    	   		}
    		}
	    }
    }
	    
		/************/

}
