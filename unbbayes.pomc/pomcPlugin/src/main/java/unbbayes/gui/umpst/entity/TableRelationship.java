package unbbayes.gui.umpst.entity;

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

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class TableRelationship extends TableObject{

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;

	private IconController iconController = IconController.getInstance(); 

	String[] columnNames = {"Id","Hypothesis","",""};
	Object[][] data = {};

	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	protected TableRelationship(UmpstModule janelaPai,UMPSTProject umpstProject) {

		super(janelaPai, umpstProject);
		this.setUmpstProject(umpstProject);
		this.setLayout(new GridLayout(1,0));

		this.add(createScrolltableHypothesis());

	}  

	public void setJanelaPai(UmpstModule janelaPai){

		this.setLayout(new GridLayout(1,0));
		this.add(createScrolltableHypothesis());

	}

	/**
	 * @return the table
	 */
	public JTable createTable() {


		Integer i=0;

		if (getUmpstProject().getMapRelationship()!=null){			
			data = new Object[getUmpstProject().getMapRelationship().size()][5];

			Set<String> keys = getUmpstProject().getMapRelationship().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){

				data[i][0] = getUmpstProject().getMapRelationship().get(key).getId();
				data[i][1] = getUmpstProject().getMapRelationship().get(key);
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
				button.setIcon(iconController.getEditUMPIcon() );

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-2);
		buttonColumn1.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String relationshipAdd = data[row][0].toString();
				RelationshipModel relationshipAux = getUmpstProject().getMapRelationship().get(relationshipAdd);
				changePanel(new RelationshipEditionPanel(getFatherPanel(),getUmpstProject(), relationshipAux )   );
			}
		});

		TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getDeleteIcon());

			}
		});
		TableColumn buttonColumn3 = table.getColumnModel().getColumn(columnNames.length-1);
		buttonColumn3.setMaxWidth(SIZE_COLUMN_BUTTON);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);

		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	


			public void onButtonPress(int row, int column) {

				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete the Relationship "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

					String key = data[row][0].toString();
					getUmpstProject().getMapRelationship().remove(key);

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane());

					JTable table = createTable();

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




}
