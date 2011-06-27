package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;



import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.SearchModelGoal;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;

public class TableEntities extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	
	
	
	Object[] dataAux = new Object[4];
	Integer i = 0;

	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"ID","Entity","",""};

	Object[][] data = {};
	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  public TableEntities(UmpstModule janelaPai) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	
    	    	this.add(createScrolltableEntities(columnNames,data));
    		    
    	  }
    	 
    	  
    	  
    	  /**
    	   * SingletonHolder is loaded on the first execution of
    	TableGoals.getInstance()
    	   * or the first access to SingletonHolder.INSTANCE, not before.
    	   */
    	 private static class SingletonHolder {
    		  	public static final TableGoals INSTANCE = new TableGoals(null);
    	  }
    	  
    	  
    	  public static TableGoals getInstance(UmpstModule janelaPai,String[] columnNames, Object[][] data) {

    		TableGoals ret = SingletonHolder.INSTANCE;
    	    ret.setJanelaPai(janelaPai,columnNames,data);
    	    return ret;
    	  }
    	  
    	  public void setJanelaPai(UmpstModule janelaPai,String[] columnNames, Object[][] data){
    		// super(janelaPai);
  	    	
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableEntities(columnNames,data));
    		  
    	  }
    
   
	
	/**
	 * @return the table
	 */
	public JTable createTable(String[] columnNames,final Object[][] data) {
		

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(new ImageIcon("images/edit.gif") );

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length -2);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = data[row][0].toString();
				EntityModel entityAux = UMPSTProject.getInstance().getMapEntity().get(key);
				changePanel(new EntitiesAdd(getFatherPanel(), entityAux )   );
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
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete entity "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							String key = data[row][0].toString();
							EntityModel entityToBeDeleted = UMPSTProject.getInstance().getMapEntity().get(key);
							
							/*Updating MapSearch*/
							deleteFromSearchMap(entityToBeDeleted);
	
							/*Updating goalRelated foward tracking*/
							if (entityToBeDeleted.getBacktracking()!=null){
								JList list = entityToBeDeleted.getBacktracking();
								list.getModel().getElementAt(i);
								list.getModel().getSize();
								
								for (int i = 0; i < list.getModel().getSize(); i++) {
									Set<String> keysGoals = UMPSTProject.getInstance().getMapGoal().keySet();
									TreeSet<String> sortedKeysGoals = new TreeSet<String>(keysGoals);
									for (String keyGoal : sortedKeysGoals){
										if (list.getModel().getElementAt(i).equals(UMPSTProject.getInstance().getMapGoal().get(keyGoal).getGoalName()));{
											UMPSTProject.getInstance().getMapGoal().get(keyGoal).getFowardTrackingEntity().remove(entityToBeDeleted);
										}
											
									}
								}
								
								
								
							}
							
							UMPSTProject.getInstance().getMapEntity().remove(entityToBeDeleted.getId());
							
							
							 
							Object[][] dataDel = new Object[UMPSTProject.getInstance().getMapEntity().size()][4];
							Integer i=0;
						    
							Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
							TreeSet<String> sortedKeys = new TreeSet<String>(keys);
							
							for (String chave: sortedKeys){
								dataDel[i][0] = UMPSTProject.getInstance().getMapEntity().get(chave).getId();						
								dataDel[i][1] = UMPSTProject.getInstance().getMapEntity().get(chave).getEntityName();
								dataDel[i][2] = "";
								dataDel[i][3] = "";
								i++;
							}
							
							
							UmpstModule pai = getFatherPanel();
							 changePanel(pai.getMenuPanel());
							 
							 //TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
							 String[] colunas = {"ID","Entity","",""};
							 JTable table = createTable(colunas,dataDel);
							 
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
	
	
	public JScrollPane createScrolltableEntities(String[] columnNames, Object[][] data){
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
    
  
    public void deleteFromSearchMap(EntityModel entityToBeDeleted){
    	Set<EntityModel> aux = new HashSet<EntityModel>();
		EntityModel entityBeta;
		String[] strAux= entityToBeDeleted.getEntityName().split(" ");

	    for (int i = 0; i < strAux.length; i++) {
    		if(UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i])!=null){
    			UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i]).getEntitiesRelated().remove(entityToBeDeleted);
    			aux = UMPSTProject.getInstance().getMapSearchEntity().get(strAux[i]).getEntitiesRelated();   
    	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
    	    		entityBeta = it.next();
    	   		}
    		}
    		
	    	
	    }
    }
	    
		/************/

}
