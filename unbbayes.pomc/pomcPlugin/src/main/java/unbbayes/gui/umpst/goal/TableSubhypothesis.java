package unbbayes.gui.umpst.goal;

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
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;


/**
 * Subhypothesis: Evidences for answer a query
 * 
 */
public class TableSubhypothesis extends IUMPSTPanel{

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollpanePergunta;

	private UmpstModule janelaPaiAux; 
	private GoalModel goalRelated;
	private HypothesisModel hypothesisRelated;

	private static int WIDTH_COLUMN_ID = 50; 
	private static int WIDTH_COLUMN_EDIT = 25; 

	private IconController iconController = IconController.getInstance(); 

	String[] columnNames = {"id","","","","Hypothesis"};
	Object[][] data = {};

	private static final int COLUMN_IDTF = 3; 
	private static final int COLUMN_DESC = 4; 
	private static final int COLUMN_BTN1 = 0; 
	private static final int COLUMN_BTN2 = 1; 
	private static final int COLUMN_BTN3 = 2; 

	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	protected TableSubhypothesis(UmpstModule janelaPai, 
			GoalModel goalRelated,
			HypothesisModel hypothesisRelated) {

		super(janelaPai);
		this.setLayout(new GridLayout(1,0));

		this.janelaPaiAux = janelaPai;
		this.goalRelated=goalRelated;
		this.hypothesisRelated=hypothesisRelated;

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

		if (hypothesisRelated!=null){			
			data = new Object[hypothesisRelated.getMapSubHypothesis().size()][5];


			Set<String> keys = hypothesisRelated.getMapSubHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){

				data[i][COLUMN_IDTF] = hypothesisRelated.getMapSubHypothesis().get(key).getId();
				data[i][COLUMN_DESC] = hypothesisRelated.getMapSubHypothesis().get(key).getHypothesisName();
				data[i][COLUMN_BTN1] = "";
				data[i][COLUMN_BTN2] = "";
				data[i][COLUMN_BTN3] = "";
				i++;
			}
		}

		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		table = new JTable(tableModel);

		TableColumn columnId = table.getColumnModel().getColumn(COLUMN_IDTF);
		columnId.setMaxWidth(WIDTH_COLUMN_ID);

		TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getEditIcon() );

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(COLUMN_BTN1);
		buttonColumn1.setMaxWidth(WIDTH_COLUMN_EDIT);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);

		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String hypothesisAdd = data[row][COLUMN_IDTF].toString();
				HypothesisModel hypothesisAux = hypothesisRelated.getMapSubHypothesis().get(hypothesisAdd);

				changePanel(
						new HypothesisAdd(
								getFatherPanel(),
								getUmpstProject(), 
								goalRelated,
								hypothesisAux, 
								hypothesisAux.getFather() )   );

			}
		});

		TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getAddIcon());

			}
		});

		TableColumn buttonColumn2 = table.getColumnModel().getColumn(COLUMN_BTN2);
		buttonColumn2.setMaxWidth(WIDTH_COLUMN_EDIT);
		buttonColumn2.setCellRenderer(buttonAdd);
		buttonColumn2.setCellEditor(buttonAdd);

		buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				String key = data[row][COLUMN_IDTF].toString();
				HypothesisModel hypothesis =  hypothesisRelated.getMapSubHypothesis().get(key);
				changePanel(
						new HypothesisAdd(
								getFatherPanel(),
								getUmpstProject(),
								goalRelated,
								null,
								hypothesis));
			}
		});


		TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
		{
			public void customize(JButton button, int row, int column)
			{
				button.setIcon(iconController.getDeleteIcon());

			}
		});
		TableColumn buttonColumn3 = table.getColumnModel().getColumn(COLUMN_BTN3);
		buttonColumn3.setMaxWidth(WIDTH_COLUMN_EDIT);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);

		buttonDel.addHandler(new TableButton.TableButtonPressedHandler() {	


			public void onButtonPress(int row, int column) {

				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete Hypothesis "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

					String key = data[row][COLUMN_IDTF].toString();
					goalRelated.getMapHypothesis().remove(key);
					hypothesisRelated.getMapSubHypothesis().remove(key);
					//UMPSTProject.getInstance().getMapHypothesis().remove(key);

					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalRelated)	);

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
