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

import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

public class TableRelationship extends IUMPSTPanel{
	

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	private UmpstModule janelaPaiAux; 
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"id","Hypothesis","",""};
	Object[][] data = {};

	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TableRelationship(UmpstModule janelaPai) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	this.janelaPaiAux = janelaPai;
    	    	
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

		if (UMPSTProject.getInstance().getMapRelationship()!=null){			
			data = new Object[UMPSTProject.getInstance().getMapRelationship().size()][5];

			
			Set<String> keys = UMPSTProject.getInstance().getMapRelationship().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			
			
			for (String key: sortedKeys){
		
				data[i][0] =UMPSTProject.getInstance().getMapRelationship().get(key).getId();
				data[i][1] = UMPSTProject.getInstance().getMapRelationship().get(key).getRelationshipName();
				data[i][2] = "";
				data[i][3] = "";
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

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-2);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				
				String relationshipAdd = data[row][0].toString();
				RelationshipModel relationshipAux = UMPSTProject.getInstance().getMapRelationship().get(relationshipAdd);
				alterarJanelaAtual(new RelationshipAdd(getFatherPanel(), relationshipAux )   );
			}
		});
		
		

		/*
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
				HypothesisModel hypothesis =  hypothesisRelated.getMapSubHypothesis().get(key);
				alterarJanelaAtual(new HypothesisAdd(getFatherPanel(),goalRelated,null,hypothesis));
			
				
			}
		});*/
		
		
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
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete Relationship "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							
							String key = data[row][0].toString();
							UMPSTProject.getInstance().getMapRelationship().remove(key);

							UmpstModule pai = getFatherPanel();
						    alterarJanelaAtual(pai.getMenuPanel().getEntitiesPane());
							 
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
        java.net.URL imgURL = MenuPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
  
	

}
