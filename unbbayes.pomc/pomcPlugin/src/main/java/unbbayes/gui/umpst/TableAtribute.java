package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

public class TableAtribute extends IUMPSTPanel{
	

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	private UmpstModule janelaPaiAux; 
	private EntityModel entityRelated;

	private Set<String> keys = new HashSet<String>();
	private TreeSet<String> sortedKeys = new TreeSet<String>();
	private Set<AtributeModel> set = new HashSet<AtributeModel>();
	private Set<AtributeModel> setAux = new HashSet<AtributeModel>();
	
	String[] columnNames = {"id","Atribute","","",""};
	Object[][] data = {};
	
	AtributeModel atribute;
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	


	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TableAtribute(UmpstModule janelaPai, EntityModel entityRelated) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	this.janelaPaiAux = janelaPai;
    	    	this.entityRelated=entityRelated;
    	    	
    	    	this.add(createScrolltableAtribute());
    		    
    	  }
    	
    	  
    	  
    	  public void setJanelaPai(UmpstModule janelaPai){
    		// super(janelaPai);
  	    	
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableAtribute());
    		  
    	  }
    
   
	
	/**
	 * @return the table
	 */
	public JTable createTable() {
		
		int i = 0;
		
		if (entityRelated!=null){			
			keys = UMPSTProject.getInstance().getMapAtribute().keySet();
			sortedKeys = new TreeSet<String>(keys);
			set = new HashSet<AtributeModel>();
			
			for (String key: sortedKeys){
				atribute = UMPSTProject.getInstance().getMapAtribute().get(key);
				if (atribute.getEntityRelated().contains(entityRelated)){
					if (!set.contains(atribute)){
						i++;
					}
					set.add(atribute);  /**this set works to not allow to add duplicated hypothesis*/


					if (atribute.getMapSubAtributes().size()>0){
						Set<String> keysSub = atribute.getMapSubAtributes().keySet();
						TreeSet<String> sortedKeysSub = new TreeSet<String>(keysSub);
						AtributeModel atributeSub;
						
						for (String keySub : sortedKeysSub){
							atributeSub = atribute.getMapSubAtributes().get(keySub);
							if (atributeSub.getEntityRelated().contains(entityRelated)){

								if (!set.contains(atributeSub)){
									i++;
								}
								set.add(atributeSub);
							}
							
						}
					}

				}
			}
		}
		
		
		data = new Object[i][5];

		keys = UMPSTProject.getInstance().getMapAtribute().keySet();
		sortedKeys = new TreeSet<String>(keys);
		i=0;
		setAux = new HashSet<AtributeModel>();
		
		if (entityRelated!=null){			
			keys = UMPSTProject.getInstance().getMapAtribute().keySet();
			sortedKeys = new TreeSet<String>(keys);
			set = new HashSet<AtributeModel>();
			
			for (String key: sortedKeys){
				atribute = UMPSTProject.getInstance().getMapAtribute().get(key);
				if (atribute.getEntityRelated().contains(entityRelated)){
					if (!set.contains(atribute)){
						data[i][0] = atribute.getId();
						data[i][1] = atribute.getAtributeName();

						data[i][2] = "";
						data[i][3] = "";
						data[i][4] = "";
						i++;
					}
					set.add(atribute);  /**this set works to not allow to add duplicated hypothesis*/


					if (atribute.getMapSubAtributes().size()>0){
						Set<String> keysSub = atribute.getMapSubAtributes().keySet();
						TreeSet<String> sortedKeysSub = new TreeSet<String>(keysSub);
						AtributeModel atributeSub;
						
						for (String keySub : sortedKeysSub){
							atributeSub = atribute.getMapSubAtributes().get(keySub);
							if (atributeSub.getEntityRelated().contains(entityRelated)){

								if (!set.contains(atributeSub)){
									data[i][0] = atributeSub.getId();
									data[i][1] = atributeSub.getAtributeName();

									data[i][2] = "";
									data[i][3] = "";
									data[i][4] = "";
									i++;
								}
								set.add(atributeSub);
							}
							
						}
					}

				}
			}
		}
		
		/**
		Integer i=0;

		if (entityRelated!=null){			
			data = new Object[entityRelated.getMapAtributes().size()][5];

			
			Set<String> keys = entityRelated.getMapAtributes().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			
			
			for (String key: sortedKeys){
		
				data[i][0] = entityRelated.getMapAtributes().get(key).getId();
				data[i][1] = entityRelated.getMapAtributes().get(key).getAtributeName();
				data[i][2] = "";
				data[i][3] = "";
				data[i][4] = "";
				i++;
			}
		}**/
		
		
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
				
				String atributeAdd = data[row][0].toString();
				AtributeModel atributeAux = entityRelated.getMapAtributes().get(atributeAdd);
				changePanel(new AtributeAdd(getFatherPanel(), entityRelated,atributeAux, atributeAux.getFather() )   );
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
				AtributeModel atributeRelated =  entityRelated.getMapAtributes().get(key);
				changePanel(new AtributeAdd(getFatherPanel(),entityRelated,null,atributeRelated));
			
				
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

							if (entityRelated.getMapAtributes().get(key).getMapSubAtributes().size()>0){
								Set<String> keysSubAtribute = entityRelated.getMapAtributes().get(key).getMapSubAtributes().keySet();
								TreeSet<String> sortedkeysSubAtribute = new TreeSet<String>(keysSubAtribute);
								for (String keySubHypo : sortedkeysSubAtribute){
									entityRelated.getMapAtributes().get(key).getMapSubAtributes().get(keySubHypo).setFather(null);
								}
							}

							
							if(UMPSTProject.getInstance().getMapAtribute().get(key).getMapSubAtributes().size()>0){
								
								Set<String> keysSubAtribute = UMPSTProject.getInstance().getMapAtribute().get(key).getMapSubAtributes().keySet();
								TreeSet<String> sortedkeysSubHypo = new TreeSet<String>(keysSubAtribute);
								for (String keySubHypo : sortedkeysSubHypo){
									UMPSTProject.getInstance().getMapAtribute().get(key).getMapSubAtributes().get(keySubHypo).setFather(null);
								}
							}
							
							if(entityRelated.getMapAtributes().get(key).getFather()!=null){
								if(entityRelated.getMapAtributes().get(key).getFather().getMapSubAtributes().size()>0)
									entityRelated.getMapAtributes().get(key).getFather().getMapSubAtributes().remove(key);
							}
							
							if (UMPSTProject.getInstance().getMapAtribute().get(key).getFather()!=null){
								if(UMPSTProject.getInstance().getMapAtribute().get(key).getFather().getMapSubAtributes().size()>0)
									UMPSTProject.getInstance().getMapAtribute().get(key).getFather().getMapSubAtributes().remove(key);
							}
							
							entityRelated.getMapAtributes().remove(key);
							UMPSTProject.getInstance().getMapAtribute().get(key).getEntityRelated().remove(entityRelated);

							
							if (UMPSTProject.getInstance().getMapAtribute().get(key).getEntityRelated().size()==0){
								UMPSTProject.getInstance().getMapAtribute().remove(key);
							}

							
							
							UmpstModule pai = getFatherPanel();
						    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entityRelated)	);
							 
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
	
		
	
	public JScrollPane createScrolltableAtribute(){
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
