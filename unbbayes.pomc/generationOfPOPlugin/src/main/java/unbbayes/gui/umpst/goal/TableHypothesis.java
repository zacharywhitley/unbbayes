package unbbayes.gui.umpst.goal;

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

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class TableHypothesis extends IUMPSTPanel{

	private static final long serialVersionUID = 1L;

	private JTable table;
	private JScrollPane scrollpanePergunta;
	private Set<HypothesisModel> set = new HashSet<HypothesisModel>();
	private Set<HypothesisModel> setAux = new HashSet<HypothesisModel>();

	private HypothesisModel hypothesis;

	private UmpstModule janelaPaiAux; 
	private GoalModel goalRelated;

	private Set<String> keys = new HashSet<String>();
	private TreeSet<String> sortedKeys = new TreeSet<String>();

	private IconController iconController = IconController.getInstance(); 

	private static final int COLUMN_IDTF = 3; 
	private static final int COLUMN_DESC = 4; 
	private static final int COLUMN_BTN1 = 0; 
	private static final int COLUMN_BTN2 = 1; 
	private static final int COLUMN_BTN3 = 2; 

	private static int WIDTH_COLUMN_ID = 50; 
	private static int WIDTH_COLUMN_EDIT = 25; 

	String[] columnNames = {"","","","id","Hypothesis"};
	Object[][] data = {};


	/**private constructors make class extension almost impossible,
    	that's why this is protected*/
	protected TableHypothesis(UmpstModule janelaPai,
			UMPSTProject umpstProject, 
			GoalModel goalRelated) {

		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.setLayout(new GridLayout(1,0));

		this.janelaPaiAux = janelaPai;
		this.goalRelated=goalRelated;

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

		if (goalRelated!=null){			
			keys = getUmpstProject().getMapHypothesis().keySet();
			sortedKeys = new TreeSet<String>(keys);
			set = new HashSet<HypothesisModel>();

			for (String key: sortedKeys){
				hypothesis = getUmpstProject().getMapHypothesis().get(key);
				if (hypothesis.getGoalRelated().contains(goalRelated)){
					if (!set.contains(hypothesis)){
						i++;
					}
					set.add(hypothesis);  /**this set works to not allow to add duplicated hypothesis*/


					if (hypothesis.getMapSubHypothesis().size()>0){
						Set<String> keysSub = hypothesis.getMapSubHypothesis().keySet();
						TreeSet<String> sortedKeysSub = new TreeSet<String>(keysSub);
						HypothesisModel hypoSub;

						for (String keySub : sortedKeysSub){
							hypoSub = hypothesis.getMapSubHypothesis().get(keySub);
							if (hypoSub.getGoalRelated().contains(goalRelated)){

								if (!set.contains(hypoSub)){
									i++;
								}
								set.add(hypoSub);
							}

						}
					}
				}
			}
			data = new Object[i][5];

			keys = getUmpstProject().getMapHypothesis().keySet();
			sortedKeys = new TreeSet<String>(keys);
			i=0;
			setAux = new HashSet<HypothesisModel>();

			for (String key: sortedKeys){
				hypothesis = getUmpstProject().getMapHypothesis().get(key);
				if (hypothesis.getGoalRelated().contains(goalRelated)){
					if (!setAux.contains(hypothesis)){
						data[i][COLUMN_IDTF] = hypothesis.getId();
						data[i][COLUMN_DESC] = hypothesis.getName();

						data[i][COLUMN_BTN1] = "";
						data[i][COLUMN_BTN2] = "";
						data[i][COLUMN_BTN3] = "";
						i++;

					}
					setAux.add(hypothesis);  /**this set works to not allow to add duplicated hypothesis*/


					if (hypothesis.getMapSubHypothesis().size()>0){
						Set<String> keysSub = hypothesis.getMapSubHypothesis().keySet();
						TreeSet<String> sortedKeysSub = new TreeSet<String>(keysSub);
						HypothesisModel hypoSub;
						for (String keySub : sortedKeysSub){

							hypoSub = hypothesis.getMapSubHypothesis().get(keySub);
							if (hypoSub.getGoalRelated().contains(goalRelated)){

								if (!setAux.contains(hypoSub)){
									data[i][COLUMN_IDTF] = hypoSub.getId();
									data[i][COLUMN_DESC] = hypoSub.getName();
									data[i][COLUMN_BTN1] = "";
									data[i][COLUMN_BTN2] = "";
									data[i][COLUMN_BTN3] = "";
									i++;
								}
								setAux.add(hypoSub);
							}
						}
					}

				}
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
				button.setIcon(iconController.getEditUMPIcon());

			}
		});

		TableColumn buttonColumn1 = table.getColumnModel().getColumn(COLUMN_BTN1);
		buttonColumn1.setMaxWidth(WIDTH_COLUMN_EDIT);
		buttonColumn1.setCellRenderer(buttonEdit);
		buttonColumn1.setCellEditor(buttonEdit);


		TableColumn buttonColumnDesc = table.getColumnModel().getColumn(COLUMN_DESC);
		buttonColumnDesc.setMinWidth(1000);


		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {

				String hypothesisAdd = data[row][COLUMN_IDTF].toString();
				HypothesisModel hypothesisAux = goalRelated.getMapHypothesis().get(hypothesisAdd);
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(), goalRelated,hypothesisAux, hypothesisAux.getFather() )   );
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
				HypothesisModel hypothesisRelated =  goalRelated.getMapHypothesis().get(key);
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(),goalRelated,null,hypothesisRelated));


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
					//System.out.println("No mapa goal: "+getUmpstProject().getMapGoal().get(goalRelated.getId()).getMapHypothesis().get(key).getHypothesisName());

					if (goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().size()>0){
						Set<String> keysSubHypo = goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().keySet();
						TreeSet<String> sortedkeysSubHypo = new TreeSet<String>(keysSubHypo);
						for (String keySubHypo : sortedkeysSubHypo){
							goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().get(keySubHypo).getFather().setFather(null);
						}
					}


					if(getUmpstProject().getMapHypothesis().get(key).getMapSubHypothesis().size()>0){

						Set<String> keysSubHypo = getUmpstProject().getMapHypothesis().get(key).getMapSubHypothesis().keySet();
						TreeSet<String> sortedkeysSubHypo = new TreeSet<String>(keysSubHypo);
						for (String keySubHypo : sortedkeysSubHypo){
							getUmpstProject().getMapHypothesis().get(key).getMapSubHypothesis().get(keySubHypo).getFather().setFather(null);
						}
					}

					if(goalRelated.getMapHypothesis().get(key).getFather()!=null){
						if(goalRelated.getMapHypothesis().get(key).getFather().getSubHypothesis().size()>0)
							goalRelated.getMapHypothesis().get(key).getFather().getSubHypothesis().remove(key);
					}

					if (getUmpstProject().getMapHypothesis().get(key).getFather()!=null){
						if(getUmpstProject().getMapHypothesis().get(key).getFather().getSubHypothesis().size()>0)
							getUmpstProject().getMapHypothesis().get(key).getFather().getSubHypothesis().remove(key);
					}

					goalRelated.getMapHypothesis().remove(key);
					getUmpstProject().getMapHypothesis().get(key).getGoalRelated().remove(goalRelated);


					if (getUmpstProject().getMapHypothesis().get(key).getGoalRelated().size()==0){
						getUmpstProject().getMapHypothesis().remove(key);
					}


					UmpstModule pai = getFatherPanel();
					changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalRelated)	);

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
