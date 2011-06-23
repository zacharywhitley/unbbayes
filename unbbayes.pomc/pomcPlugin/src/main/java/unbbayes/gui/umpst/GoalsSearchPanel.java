package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;

public class GoalsSearchPanel extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelGoal;
	
	private JButton buttonSearch;
	private JButton buttonAddGoal,buttonCancel;
	private JButton buttonSave,buttonLoad;
	private JTextField textGoal;
	

	
	
	
	public GoalsSearchPanel(UmpstModule janelaPai){
		super(janelaPai);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonSearch());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonCancel());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonAddGoal());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonSave());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonLoad());

		

		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}
	
	

	/**
	 * @return the buttonSave
	 */
	public JButton getButtonSave() {
		
		if (buttonSave==null){
			buttonSave = new JButton("save file");
			buttonSave.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					try {
						saveUbf();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}
		return buttonSave;
	}



	/**
	 * @return the buttonLoad
	 */
	public JButton getButtonLoad() {
		if(buttonLoad==null){
			buttonLoad = new JButton("load file");
			buttonLoad.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					loadUbf();
					
				}
			});
		}
		return buttonLoad;
	}



	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {
		
		if (buttonCancel == null){
			buttonCancel = new JButton ("cancel search");
			buttonCancel.setForeground(Color.blue);
			buttonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					textGoal.setText("");
					returnTableGoals();
				}
			});
		}
		
		return buttonCancel;
	} 
	
	/**
	 * @return the buttonAddGoal
	 */
	public JButton getButtonAddGoal() {
		
		if (buttonAddGoal == null){
			buttonAddGoal = new JButton ("add new goal");
			buttonAddGoal.setForeground(Color.blue);
			buttonAddGoal.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					alterarJanelaAtual(getGoalsAdd(null));
				}
			});
		}
		
		return buttonAddGoal;
	} 
	
	public GoalsAdd getGoalsAdd(GoalModel goal){
		
		GoalsAdd ret = new GoalsAdd(getFatherPanel(),goal,null);
		
		return ret;
		
	}
	
	/*public GoalsMainPanel getGoalsMainPanel(GoalModel goal){
		
		GoalsMainPanel ret = new GoalsMainPanel(getJanelaPai(),goal,null);
		
		return ret;
		
	}/*
	
	
	/**
	 * @return the labelGoal
	 */
	public JLabel getLabelGoal() {
		
		if(labelGoal == null){
			labelGoal = new JLabel("Search for a goal: ");
			labelGoal.setForeground(Color.white);
		}
		
		return labelGoal;
	}


	
	/**
	 * @return the buttonSearch
	 */
	public JButton getButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton("Search: ");
			buttonSearch.setForeground(Color.blue);
		}
	
			
		buttonSearch.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (!textGoal.getText().equals("")){
					if( UMPSTProject.getInstance().getMapSearchGoal().get(textGoal.getText()) == null) {
						JOptionPane.showMessageDialog(null,"Goal "+textGoal.getText()+" not found!!");

					}
					else{
						JOptionPane.showMessageDialog(null,"Goal "+textGoal.getText()+" found! Updating table: ");
						updateTableGoals();
					}
				}
				else{
					JOptionPane.showMessageDialog(null, "Search is empty!");
				}
			}
		});
		
		return buttonSearch;
	}


	/**
	 * @return the textGoal
	 */
	public JTextField getTextGoal() {
		
		if (textGoal == null){
			textGoal = new JTextField(10);
		}
		
		return textGoal;
	}
	
	public void updateTableGoals(){
    	String[] columnNames = {"ID","Goal","","",""};
    	
    	
		Set<GoalModel> aux = UMPSTProject.getInstance().getMapSearchGoal().get(textGoal.getText()).getGoalsRelated();
		GoalModel goal;
		Object[][] data = new Object[UMPSTProject.getInstance().getMapSearchGoal().get(textGoal.getText()).getGoalsRelated().size()][5];
		//Object[][] data = new Object[setGoal2.size()][5];

		Integer i=0;
		
	   
    	for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
    	     goal = it.next();  // No downcasting required.
    	     
    	 	data[i][0] = goal.getId();
			data[i][1] = goal.getGoalName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
    	}
    	
	    
   
	    UmpstModule pai = getFatherPanel();
	    alterarJanelaAtual(pai.getMenuPanel());
	    
	    TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
	    JTable table = goalsTable.createTable(columnNames,data);
	    
	    goalsTable.getScrollPanePergunta().setViewportView(table);
	    goalsTable.getScrollPanePergunta().updateUI();
	    goalsTable.getScrollPanePergunta().repaint();
	    goalsTable.updateUI();
	    goalsTable.repaint();
    }
	
	 public void returnTableGoals(){
	    	String[] columnNames = {"ID","Goal","","",""};
	    	
	    	
		    
			Object[][] data = new Object[UMPSTProject.getInstance().getMapGoal().size()][5];
			Integer i=0;
		    
			Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = UMPSTProject.getInstance().getMapGoal().get(key).getId();
				data[i][1] = UMPSTProject.getInstance().getMapGoal().get(key).getGoalName();			
				data[i][2] = "";
				data[i][3] = "";
				data[i][4] = "";
				i++;
			}
	   
		    UmpstModule pai = getFatherPanel();
		    alterarJanelaAtual(pai.getMenuPanel());
		    
		    TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
		    JTable table = goalsTable.createTable(columnNames,data);
		    
		    goalsTable.getScrollPanePergunta().setViewportView(table);
		    goalsTable.getScrollPanePergunta().updateUI();
		    goalsTable.getScrollPanePergunta().repaint();
		    goalsTable.updateUI();
		    goalsTable.repaint();
	    }
	 
	 public void saveUbf() throws FileNotFoundException{
			
			File file = new File("images/file.ump");
			String numberSubgoals;
			PrintStream printStream = new PrintStream(new FileOutputStream(file));
			
			Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			printStream.println("Number of goals in the map:");
			printStream.println(UMPSTProject.getInstance().getMapGoal().size());
			
			printStream.println("All goals cadastred in the map:");
			for (String key : sortedKeys){
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getId());
			}
			printStream.println("********************");						
			printStream.println("Goals details:");			
			for (String key : sortedKeys){
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getId());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getGoalName());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getDate());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getComments());
				if (UMPSTProject.getInstance().getMapGoal().get(key).getGoalFather()==null){
					printStream.println("null");
				}
				else{
					printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getGoalFather().getId());	
				}
				printStream.println("Number of subgoals");
				if (UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size()>0){
					printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size());
					Set<String> keysSub = UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().keySet();
					TreeSet<String> sortedKeysSub = new TreeSet<String>(keysSub);
					for (String keySub : sortedKeysSub){
						printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().get(keySub).getId());
					}
				}
				else{
					printStream.println("null");
				}

			}
			
		}
	 
	 
	 	public  void loadUbf() {

		    File file = new File("images/file.ump");
		    FileInputStream fis = null;
		    BufferedInputStream bis = null;
		    BufferedReader bufferReader = null;
		    String st;
		    
		    try {
		      fis = new FileInputStream(file);

		      // Here BufferedInputStream is added for fast reading.
		      bis = new BufferedInputStream(fis);
		      bufferReader = new BufferedReader(new InputStreamReader(fis));
		      GoalModel goal;
		      // dis.available() returns 0 if the file does not have more lines.
		      
		      String lixo = bufferReader.readLine();  //"Number of goals in the map:"
		      int tamanho = Integer.parseInt(bufferReader.readLine());;
		      lixo = bufferReader.readLine(); //All goals in the map
		      
		      String id,goalName,author,date,comments,idFather, numberSubgoals;
		      int numberIntSubgoals;
		      
		      
		      for (int i = 0; i < tamanho; i++) {
		    	  	id = bufferReader.readLine();
		    	  	goal = new GoalModel(id, "","", "", "" , null, null, null, null, null);
		    	  	UMPSTProject.getInstance().getMapGoal().put(goal.getId(), goal);
			  }
		      
		      lixo = bufferReader.readLine(); //************
		      lixo = bufferReader.readLine(); //goals detail's
		      
		      for (int i = 0; i < tamanho; i++) {
		    	  	id = bufferReader.readLine();
		    	  	goalName = bufferReader.readLine();
		    	  	author = bufferReader.readLine();
		    	  	date = bufferReader.readLine();
		    	  	comments =bufferReader.readLine();
		    	  	idFather = bufferReader.readLine();
		    	  	lixo = bufferReader.readLine();
		    	  	numberSubgoals = bufferReader.readLine();
		    	  	
		    	  	goal = UMPSTProject.getInstance().getMapGoal().get(id);
		    	  	goal.setGoalName(goalName);
		    	  	goal.setAuthor(author);
		    	  	goal.setDate(date);
		    	  	goal.setComments(comments);
		    	  	
		    	  	if (!idFather.equals("null")){
		    	 
		    	  		goal.setGoalFather(UMPSTProject.getInstance().getMapGoal().get(idFather));
		    	  	}
		    	  	
		    	  	if (!numberSubgoals.equals("null")){
		    	  		numberIntSubgoals = Integer.parseInt(numberSubgoals);
		    	  		GoalModel subgoal;
		    	  		String idSubgoal;
		    	  		for (int j = 0; j < numberIntSubgoals; j++) {
		    	  			idSubgoal = bufferReader.readLine();
							subgoal = UMPSTProject.getInstance().getMapGoal().get(idSubgoal);
							goal.getSubgoals().put(subgoal.getId(), subgoal);
							
						}
		    	  	}
			}
		      // this statement reads the line from the file and print it to
		        // the console.
		      

		      // dispose all the resources after using them.
		      fis.close();
		      bis.close();
		      bufferReader.close();

		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		  }
	

}
