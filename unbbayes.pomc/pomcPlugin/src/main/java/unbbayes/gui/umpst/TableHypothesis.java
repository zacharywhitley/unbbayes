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
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;

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
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"id","Hypothesis","","",""};
	Object[][] data = {};

	
	

 
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TableHypothesis(UmpstModule janelaPai, GoalModel goalRelated) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	
    	    	this.janelaPaiAux = janelaPai;
    	    	this.goalRelated=goalRelated;
    	    	
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

		if (goalRelated!=null){			
			keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
			sortedKeys = new TreeSet<String>(keys);
			set = new HashSet<HypothesisModel>();
			
			for (String key: sortedKeys){
				hypothesis = UMPSTProject.getInstance().getMapHypothesis().get(key);
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

			keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
			sortedKeys = new TreeSet<String>(keys);
			i=0;
			setAux = new HashSet<HypothesisModel>();
			
			for (String key: sortedKeys){
				hypothesis = UMPSTProject.getInstance().getMapHypothesis().get(key);
				if (hypothesis.getGoalRelated().contains(goalRelated)){
					if (!setAux.contains(hypothesis)){
						data[i][0] = hypothesis.getId();
						data[i][1] = hypothesis.getHypothesisName();

						data[i][2] = "";
						data[i][3] = "";
						data[i][4] = "";
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
									data[i][0] = hypoSub.getId();
									data[i][1] = hypoSub.getHypothesisName();
									data[i][2] = "";
									data[i][3] = "";
									data[i][4] = "";
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
				
				String hypothesisAdd = data[row][0].toString();
				HypothesisModel hypothesisAux = goalRelated.getMapHypothesis().get(hypothesisAdd);
				changePanel(new HypothesisAdd(getFatherPanel(), goalRelated,hypothesisAux, hypothesisAux.getFather() )   );
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
				HypothesisModel hypothesisRelated =  goalRelated.getMapHypothesis().get(key);
				changePanel(new HypothesisAdd(getFatherPanel(),goalRelated,null,hypothesisRelated));
			
				
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
				
				if( JOptionPane.showConfirmDialog(null,"Do you realy want to delete Hypothesis "	+ data[row][0].toString() + "?", "UMPSTPlugin", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
							
							String key = data[row][0].toString();
							//System.out.println("No mapa goal: "+UMPSTProject.getInstance().getMapGoal().get(goalRelated.getId()).getMapHypothesis().get(key).getHypothesisName());
							
							if (goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().size()>0){
								Set<String> keysSubHypo = goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().keySet();
								TreeSet<String> sortedkeysSubHypo = new TreeSet<String>(keysSubHypo);
								for (String keySubHypo : sortedkeysSubHypo){
									goalRelated.getMapHypothesis().get(key).getMapSubHypothesis().get(keySubHypo).getFather().setFather(null);
								}
							}

							
							if(UMPSTProject.getInstance().getMapHypothesis().get(key).getMapSubHypothesis().size()>0){
								
								Set<String> keysSubHypo = UMPSTProject.getInstance().getMapHypothesis().get(key).getMapSubHypothesis().keySet();
								TreeSet<String> sortedkeysSubHypo = new TreeSet<String>(keysSubHypo);
								for (String keySubHypo : sortedkeysSubHypo){
									UMPSTProject.getInstance().getMapHypothesis().get(key).getMapSubHypothesis().get(keySubHypo).getFather().setFather(null);
								}
							}
							
							if(goalRelated.getMapHypothesis().get(key).getFather()!=null){
								if(goalRelated.getMapHypothesis().get(key).getFather().getSubHypothesis().size()>0)
									goalRelated.getMapHypothesis().get(key).getFather().getSubHypothesis().remove(key);
							}
							
							if (UMPSTProject.getInstance().getMapHypothesis().get(key).getFather()!=null){
								if(UMPSTProject.getInstance().getMapHypothesis().get(key).getFather().getSubHypothesis().size()>0)
									UMPSTProject.getInstance().getMapHypothesis().get(key).getFather().getSubHypothesis().remove(key);
							}
							
							goalRelated.getMapHypothesis().remove(key);
							UMPSTProject.getInstance().getMapHypothesis().get(key).getGoalRelated().remove(goalRelated);

							
							if (UMPSTProject.getInstance().getMapHypothesis().get(key).getGoalRelated().size()==0){
								UMPSTProject.getInstance().getMapHypothesis().remove(key);
							}

							
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
