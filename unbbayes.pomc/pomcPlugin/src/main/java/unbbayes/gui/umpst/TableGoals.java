package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
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

public class TableGoals extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	
	
	
	Object[] dataAux = new Object[5];
	Integer i = 0;

	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"ID","Goal","","",""};

	Object[][] data = {};
	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  public TableGoals(UmpstModule janelaPai) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	
    	    	this.add(createScrolltableGoals(columnNames,data));
    		    
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
  	          this.add(createScrolltableGoals(columnNames,data));
    		  
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

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length -3);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String key = data[row][0].toString();
				GoalModel goalAux = UMPSTProject.getInstance().getMapGoal().get(key);
				alterarJanelaAtual(new GoalsMainPanel(getJanelaPai(), goalAux, goalAux.getGoalFather() )   );
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
				GoalModel goalAux = UMPSTProject.getInstance().getMapGoal().get(key);
				alterarJanelaAtual(new GoalsMainPanel(getJanelaPai(),null,goalAux));
				
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
				
				if( JOptionPane.showConfirmDialog(null,"Confirma Remocao do goal "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							String key = data[row][0].toString();
							GoalModel goalToBeDeleted = UMPSTProject.getInstance().getMapGoal().get(key);
							
							
							if(goalToBeDeleted.getSubgoals() !=null){
								int numberSubgoal = goalToBeDeleted.getSubgoals().size();
								for (int i = 0; i < numberSubgoal; i++) {
									goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setGoalFather(null);
									//goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setId("D"+i);
								}
							}
							
							
							UMPSTProject.getInstance().getMapGoal().remove(goalToBeDeleted.getId());
							
							
							 
							Object[][] dataDel = new Object[UMPSTProject.getInstance().getMapGoal().size()][5];
							Integer i=0;
						    
							Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
							TreeSet<String> sortedKeys = new TreeSet<String>(keys);
							
							for (String chave: sortedKeys){
								dataDel[i][0] = UMPSTProject.getInstance().getMapGoal().get(chave).getId();						
								dataDel[i][1] = UMPSTProject.getInstance().getMapGoal().get(chave).getGoalName();
								dataDel[i][2] = "";
								dataDel[i][3] = "";
								dataDel[i][4] = "";
								i++;
							}
							
							
							UmpstModule pai = getJanelaPai();
							 alterarJanelaAtual(pai.getMenuPanel());
							 
							 //TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
							 String[] colunas = {"ID","Goal","","",""};
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
        java.net.URL imgURL = MenuPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
  
	

}
