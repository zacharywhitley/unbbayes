package unbbayes.gui.umpst.group;

import java.awt.BorderLayout;
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

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class TableGroups extends TableObject{

	private static final long serialVersionUID = 1L;

	private JTable table;
	private JScrollPane scrollpanePergunta;

	private IconController iconController = IconController.getInstance(); 

	Object[] dataAux = new Object[4];
	Integer i = 0;

	String[] columnNames = {"ID","Group","",""};

	Object[][] data = {};


	public TableGroups(UmpstModule janelaPai,UMPSTProject umpstProject) {

		super(janelaPai, umpstProject);
//		this.setLayout(new GridLayout(1,0));
		this.setLayout(new BorderLayout());
		this.add(createScrolltableRules(columnNames), BorderLayout.CENTER);

	}

	public void setJanelaPai(UmpstModule janelaPai){
		// super(janelaPai);

		this.setLayout(new GridLayout(1,0));
		this.add(createScrolltableRules(columnNames));

	}

	public JTable createTable(String[] columnNames){
		
		Object[][] data = new Object[getUmpstProject().getMapGroups().size()][4];
		
		Integer i=0;

//		Set<String> keys = new TreeSet<String>;
		
		Set<Integer> keysInteger = new TreeSet<Integer>() ; 
		
		for (String key: getUmpstProject().getMapGroups().keySet()){
			keysInteger.add(new Integer(key)); 
		}
		
//		TreeSet<Integer> sortedKeys = new TreeSet<Integer>(keysInteger);

		for (Integer keyInteger: keysInteger){
			String key = keyInteger.toString(); 
			data[i][0] = getUmpstProject().getMapGroups().get(key).getId();
			data[i][1] = getUmpstProject().getMapGroups().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
		
		return createTable(columnNames, data); 
		
	}
	
	/**
	 * @return the table
	 */
	public JTable createTable(String[] columnNames,final Object[][] data) {


		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
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
				GroupModel groupAux = getUmpstProject().getMapGroups().get(key);
				changePanel(new GroupsEditionPanel(getFatherPanel(),getUmpstProject(), groupAux )   );
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

				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete Group "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
					String key = data[row][0].toString();
					GroupModel groupToBeDeleted = getUmpstProject().getMapGroups().get(key);

					getUmpstProject().getMapGroups().remove(groupToBeDeleted.getId());

					Object[][] dataDel = new Object[getUmpstProject().getMapGroups().size()][4];
					Integer i=0;

					Set<String> keys = getUmpstProject().getMapGroups().keySet();
					TreeSet<String> sortedKeys = new TreeSet<String>(keys);

					for (String chave: sortedKeys){
						dataDel[i][0] = getUmpstProject().getMapGroups().get(chave).getId();						
						dataDel[i][1] = getUmpstProject().getMapGroups().get(chave).getName();
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
		
		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(SIZE_COLUMN_INDEX);

		return table;
	}


	public JScrollPane createScrolltableRules(String[] columnNames){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(createTable(columnNames));
			scrollpanePergunta.setSize(new Dimension(300,150));
		}

		return scrollpanePergunta;
	}

	public JScrollPane getScrollPanePergunta(){

		return scrollpanePergunta; 
	}

}
