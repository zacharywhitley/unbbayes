package unbbayes.gui.umpst.entity;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.Controller;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class TableEntities extends TableObject{

	private static final long serialVersionUID = 1L;

	private JTable table;
	private JScrollPane scrollPaneEntitiesTable;

	private IconController iconController = IconController.getInstance(); 

	Object[] dataAux = new Object[4];
	Integer i = 0;


	String[] columnNames = {"ID","Entity","",""};

	Object[][] data = {};


	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	public TableEntities(UmpstModule janelaPai, UMPSTProject umpstProject) {

		super(janelaPai, umpstProject);
		
		this.setLayout(new GridLayout(1,0));
		this.add(createScrolltableEntities(columnNames,data));

	}


	public void setJanelaPai(UmpstModule janelaPai,String[] columnNames, Object[][] data){

		this.setLayout(new GridLayout(1,0));
		this.add(createScrolltableEntities(columnNames,data));

	}

	public JTable createTable(String[] columnNames){
		
		Object[][] data = new Object[getUmpstProject().getMapEntity().size()][4];
		Integer i=0;

//		Set<String> keys = getUmpstProject().getMapEntity().keySet();
		
		Set<Integer> keysInteger = new TreeSet<Integer>(); 
		
		for (String key: getUmpstProject().getMapEntity().keySet()){
			keysInteger.add(new Integer(key)); 
		}
		
		TreeSet<Integer> sortedKeys = new TreeSet<Integer>(keysInteger);

		for (Integer key: sortedKeys){
			data[i][0] = getUmpstProject().getMapEntity().get("" + key).getId();
			data[i][1] = getUmpstProject().getMapEntity().get("" + key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
		
		return createTable(columnNames, data); 
	}
	
	/**
	 * @return the table
	 */
	public JTable createTable(String[] columnNames,
			final Object[][] data) {

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getEditUMPIcon());
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length -2);
		buttonColumn1.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				String key = data[row][0].toString();
				EntityModel entityAux = getUmpstProject().getMapEntity().get(key);
				changePanel(new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(), entityAux )   );
			}
		});

		TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getDeleteIcon());
			}
		});

		TableColumn buttonColumn3 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn3.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);

		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {

			public void onButtonPress(int row, int column) {

				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete entity "	
						+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

					String key = data[row][0].toString();
					Controller.getInstance(getUmpstProject()).deleteEntity(key);

					Object[][] dataDel = new Object[getUmpstProject().getMapEntity().size()][4];
					Integer i=0;

					Set<String> keys = getUmpstProject().getMapEntity().keySet();
					TreeSet<String> sortedKeys = new TreeSet<String>(keys);

					for (String chave: sortedKeys){
						dataDel[i][0] = getUmpstProject().getMapEntity().get(chave).getId();						
						dataDel[i][1] = getUmpstProject().getMapEntity().get(chave).getName();
						dataDel[i][2] = "";
						dataDel[i][3] = "";
						i++;
					}

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel());

					//TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
					String[] colunas = {"ID",
							"Entity",
							"",
					""};

					JTable table = createTable(colunas,dataDel);

					getScrollPaneEntitiesTable().setViewportView(table);
					getScrollPaneEntitiesTable().updateUI();
					getScrollPaneEntitiesTable().repaint();
					updateUI();
					repaint();

				}
			}
		});

		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(SIZE_COLUMN_INDEX);

		return table;
	}

	public JScrollPane createScrolltableEntities(String[] columnNames, Object[][] data){
		if(scrollPaneEntitiesTable == null){
			scrollPaneEntitiesTable = new JScrollPane(createTable(columnNames));
			scrollPaneEntitiesTable.setMinimumSize(new Dimension(150,150));
		}

		return scrollPaneEntitiesTable;
	}

	public JScrollPane getScrollPaneEntitiesTable(){

		return scrollPaneEntitiesTable; 
	}

	/************/

}
