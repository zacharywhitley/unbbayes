package unbbayes.gui.umpst.goal;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ResourceBundle;
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
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class TableGoals extends TableObject{

	private static final long serialVersionUID = 1L;

	private JTable table;
	private JScrollPane scrollpanePergunta;

	private IconController iconController = IconController.getInstance(); 
	
	Integer i = 0;

	String[] columnNames = {"ID","Goal","","",""};

	Object[][] data = {};

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());
	
	private Controller controller; 
	
	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	public TableGoals(UmpstModule janelaPai, UMPSTProject umpstProject) {

		super(janelaPai, umpstProject);

		controller = Controller.getInstance(umpstProject); 
		
		this.setLayout(new GridLayout(1,0));
		
		this.add(createScrolltableGoals(columnNames,data));
		
		getScrollPanePergunta().setViewportView(table);
		getScrollPanePergunta().updateUI();
		getScrollPanePergunta().repaint();
		
		updateUI();
		repaint();
	}

	public void setJanelaPai(UmpstModule janelaPai,String[] columnNames, Object[][] data){
		
		this.setLayout(new GridLayout(1,0));
		this.add(createScrolltableGoals(columnNames,data));

	}

	public JTable createTable() {

		Object[][] data = new Object[getUmpstProject().getMapGoal().size()][5];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGoal().get(key).getId();
			data[i][1] = getUmpstProject().getMapGoal().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}
		
		return createTable(data); 
	}
	
	/**
	 * @return the table
	 */
	public JTable createTable(final Object[][] data) {

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		
		table = new JTable(tableModel);

		//---------------------- Edit Button ------------------------------------------------

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){
				button.setIcon(iconController.getEditUMPIcon());
			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length -3);
		buttonColumn1.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				String key = data[row][0].toString();
				GoalModel goalAux = getUmpstProject().getMapGoal().get(key);
				changePanel(new GoalsEditionPanel(getFatherPanel(),
						getUmpstProject(), 
						goalAux, 
						goalAux.getGoalFather()));
			}
		});


		//---------------------- Add Button ------------------------------------------------

		TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer(){
			public void customize(JButton button, int row, int column){                               
				button.setIcon(iconController.getAddIcon());
			}
		});

		TableColumn buttonColumn2 = table.getColumnModel().getColumn(columnNames.length-2);
		buttonColumn2.setMaxWidth(SIZE_COLUMN_BUTTON);
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
		buttonColumn3.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);

		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	

			public void onButtonPress(int row, int column) {

				if( JOptionPane.showConfirmDialog(null, resource.getString("qtDeleteGoal"), "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

					String key = data[row][0].toString();
					
					controller.deleteGoal(key);

					Object[][] dataDel = new Object[getUmpstProject().getMapGoal().size()][5];
					Integer i=0;

					Set<String> keys = getUmpstProject().getMapGoal().keySet();
					TreeSet<String> sortedKeys = new TreeSet<String>(keys);

					for (String chave: sortedKeys){
						dataDel[i][0] = getUmpstProject().getMapGoal().get(chave).getId();						
						dataDel[i][1] = getUmpstProject().getMapGoal().get(chave).getName();
						dataDel[i][2] = "";
						dataDel[i][3] = "";
						dataDel[i][4] = "";
						i++;
					}

					JTable table = createTable();

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

		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(SIZE_COLUMN_INDEX);
		
		TableColumn columnText = table.getColumnModel().getColumn(1);

		return table;
	}

	public JTable getTable(){
		return table; 
	}

	public JScrollPane createScrolltableGoals(String[] columnNames, Object[][] data){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(createTable());
			scrollpanePergunta.setMinimumSize(new Dimension(300,150));
		}

		return scrollpanePergunta;
	}

	public JScrollPane getScrollPanePergunta(){

		return scrollpanePergunta; 
	}

	/************/

}
