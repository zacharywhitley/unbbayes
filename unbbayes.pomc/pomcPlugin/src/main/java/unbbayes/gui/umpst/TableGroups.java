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

import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rules.RulesModel;

public class TableGroups extends IUMPSTPanel{
	
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

	
	String[] columnNames = {"ID","Group","",""};

	Object[][] data = {};
	
	
    	  public TableGroups(UmpstModule janelaPai,UMPSTProject umpstProject) {
    		  
    		    super(janelaPai);
    		    this.setUmpstProject(umpstProject);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	
    	    	this.add(createScrolltableRules(columnNames,data));
    		    
    	  }
    	 
    	  
   
    	  
    	  public void setJanelaPai(UmpstModule janelaPai,String[] columnNames, Object[][] data){
    		// super(janelaPai);
  	    	
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableRules(columnNames,data));
    		  
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
				GroupsModel groupAux = getUmpstProject().getMapGroups().get(key);
				changePanel(new GroupsAdd(getFatherPanel(),getUmpstProject(), groupAux )   );
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
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete Group "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							String key = data[row][0].toString();
							GroupsModel groupToBeDeleted = getUmpstProject().getMapGroups().get(key);
							
							/*Updating MapSearch*/
							deleteFromSearchMap(groupToBeDeleted);
	
							
							
							getUmpstProject().getMapRules().remove(groupToBeDeleted.getId());
							
							
							 
							Object[][] dataDel = new Object[getUmpstProject().getMapGroups().size()][4];
							Integer i=0;
						    
							Set<String> keys = getUmpstProject().getMapGroups().keySet();
							TreeSet<String> sortedKeys = new TreeSet<String>(keys);
							
							for (String chave: sortedKeys){
								dataDel[i][0] = getUmpstProject().getMapGroups().get(chave).getId();						
								dataDel[i][1] = getUmpstProject().getMapGroups().get(chave).getGroupName();
								dataDel[i][2] = "";
								dataDel[i][3] = "";
								i++;
							}
							
							
							UmpstModule pai = getFatherPanel();
							 changePanel(pai.getMenuPanel());
							 
							 String[] colunas = {"ID","Group","",""};
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
	
	
	public JScrollPane createScrolltableRules(String[] columnNames, Object[][] data){
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
    
  
    public void deleteFromSearchMap(GroupsModel groupToBeDeleted){
    	Set<GroupsModel> aux = new HashSet<GroupsModel>();
		GroupsModel groupBeta;
		String[] strAux= groupToBeDeleted.getGroupName().split(" ");

	    for (int i = 0; i < strAux.length; i++) {
    		if(getUmpstProject().getMapSearchGroups().get(strAux[i])!=null){
    			getUmpstProject().getMapSearchGroups().get(strAux[i]).getRelatedGroups().remove(groupToBeDeleted);
    			aux = getUmpstProject().getMapSearchGroups().get(strAux[i]).getRelatedGroups();   
    	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
    	    		groupBeta = it.next();
    	   		}
    		}
    		
	    	
	    }
    }
	    
		/************/

}