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
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.TableButton.TableButtonCustomizer;
import unbbayes.gui.umpst.TableButton.TableButtonPressedHandler;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

public class TableSubatribute extends IUMPSTPanel{


	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;

	private UmpstModule janelaPaiAux; 
	private EntityModel entityRelated;
	private AttributeModel atributeRelated;

	private IconController iconController = IconController.getInstance(); 


	String[] columnNames = {"id","Attribute","","",""};
	Object[][] data = {};

	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	protected TableSubatribute(UmpstModule janelaPai, 
			EntityModel entityRelated,
			AttributeModel atributeRelated) {

		super(janelaPai);
		this.setLayout(new GridLayout(1,0));

		this.janelaPaiAux = janelaPai;
		this.entityRelated=entityRelated;
		this.atributeRelated=atributeRelated;

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

		if (atributeRelated!=null){			
			data = new Object[atributeRelated.getMapSubAtributes().size()][5];


			Set<String> keys = atributeRelated.getMapSubAtributes().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);



			for (String key: sortedKeys){

				data[i][0] = atributeRelated.getMapSubAtributes().get(key).getId();
				data[i][1] = atributeRelated.getMapSubAtributes().get(key).getAtributeName();
				data[i][2] = "";
				data[i][3] = "";
				data[i][4] = "";
				i++;
			}
		}


		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getEditIcon());

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(columnNames.length-3);
		buttonColumn1.setMaxWidth(28);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String atributeAdd = data[row][0].toString();
				AttributeModel atributeAux = atributeRelated.getMapSubAtributes().get(atributeAdd);
				changePanel(new AtributeEditionPanel(getFatherPanel(),
						getUmpstProject(), 
						entityRelated,
						atributeAux, 
						atributeAux.getFather()));
			}
		});




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
				AttributeModel atribute =  atributeRelated.getMapSubAtributes().get(key);
				changePanel(new AtributeEditionPanel(getFatherPanel(),getUmpstProject(),entityRelated,null,atribute));


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
		buttonColumn3.setMaxWidth(25);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);

		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	


			public void onButtonPress(int row, int column) {

				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete atribute "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

					String key = data[row][0].toString();
					entityRelated.getMapAtributes().remove(key);
					atributeRelated.getMapSubAtributes().remove(key);
					//UMPSTProject.getInstance().getMapHypothesis().remove(key);

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entityRelated) );

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
		java.net.URL imgURL = MainPanel.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}




}
