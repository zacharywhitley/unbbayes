package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;


public class HypothesisAdd extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private ImageIcon iconSubhypothesis = createImageIcon("images/sub.png");

	
	private GridBagConstraints c     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonSubhypothesis    = new JButton(iconSubhypothesis);
	
	private JTextField dateText,authorText;
	private JTextField hypothesisText,commentsText;
	private HypothesisModel hypothesis,hypothesisFather;
	private Map<String,HypothesisModel> hypothesisChildren;
	private GoalModel goalRelated;


	
	public HypothesisAdd(UmpstModule janelaPai,GoalModel goalRelated, HypothesisModel hypothesis, HypothesisModel hypothesisFather){
		super(janelaPai);
		
		this.hypothesis = hypothesis;
		this.hypothesisFather = hypothesisFather;
		this.goalRelated = goalRelated;
		
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		labels();
		fields();
		buttons();
		listeners();

		if( hypothesis == null){
			titulo.setText("Add new Hyphetesis");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update hyphotesis");
			buttonAdd.setText(" Update ");
			hypothesisText.setText(hypothesis.getHypothesisName());
			commentsText.setText(hypothesis.getComments());
			authorText.setText(hypothesis.getAuthor());
			dateText.setText(hypothesis.getDate());			
			
		}
		
	}




	public void labels(){
		c.gridx = 0; c.gridy = 2;
		add( new JLabel("Hypothesis Description: "), c);
		c.gridx = 0; c.gridy = 3;
		add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 4;
		add( new JLabel("Author Nome: "), c);
		c.gridx = 0; c.gridy = 5;
		add( new JLabel("Date: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 2;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		add( titulo, d);
		
	}
	
	
	public void fields(){
		
		hypothesisText = new JTextField(50);
		commentsText = new JTextField(50);
		authorText = new JTextField(20);
		dateText = new JTextField(10);
 

		c.gridx = 1; c.gridy = 2;
		add( hypothesisText, c);
		
		c.gridx = 1; c.gridy = 3;
		add( commentsText, c);
		
		c.gridx = 1; c.gridy = 4;
		add( authorText, c);
		
		c.gridx = 1; c.gridy = 5;
		add( dateText, c);
		
	}
		
		
	
	public void buttons(){
		
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		add( buttonCancel, c);
		c.gridx = 1; c.gridy = 7;
		add( buttonAdd, c);
		
		GridBagConstraints d = new GridBagConstraints();
		

		d.gridx = 0; d.gridy = 8; 
		add(buttonSubhypothesis,d);
		
		buttonSubhypothesis.setToolTipText("Add new Sub-hypothesis");
	
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hypothesis == null){
				


					try {
						HypothesisModel hypothesisAdd = updateMapHypothesis();
						updateTable(hypothesisAdd);
						JOptionPane.showMessageDialog(null, "hypothesis successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this hypothesis?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						
						try{
							hypothesis.setHypothesisName(hypothesisText.getText());
							hypothesis.setComments(commentsText.getText());
							hypothesis.setAuthor(authorText.getText());
							hypothesis.setDate(dateText.getText());
							updateTable(hypothesis);
							JOptionPane.showMessageDialog(null, "hypothesis successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
							
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getJanelaPai();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getJanelaPai();
			    alterarJanelaAtual(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsMainPanel(goalRelated)	);
			}
		});

		hypothesisText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
		commentsText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});
		
		authorText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dateText.requestFocus();
			}
		});
		
		dateText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buttonAdd.requestFocus();
			}
		});
		
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

    
    public void updateTable(HypothesisModel hypothesisUpdate){
		
	    UmpstModule pai = getJanelaPai();

	    
	    TableHypothesis hypothesisTable = pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsMainPanel(goalRelated).getHypothesisTable(goalRelated);
	    JTable table = hypothesisTable.createTable();
	    
	    alterarJanelaAtual(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsMainPanel(goalRelated)	);
	    
	    hypothesisTable.getScrollPanePergunta().setViewportView(table);
	    hypothesisTable.getScrollPanePergunta().updateUI();
	    hypothesisTable.getScrollPanePergunta().repaint();
	    hypothesisTable.updateUI();
	    hypothesisTable.repaint();
    }
    
    public HypothesisModel updateMapHypothesis(){
    	String idAux = "";
	
    	
    	Set<String> keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		//int tamanho = UMPSTProject.getInstance().getMapHypothesis().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
		if (hypothesisFather==null){
			
			if ( UMPSTProject.getInstance().getMapHypothesis().size()!=0){
				for (String key: sortedKeys){
					//tamanho = tamanho - UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size();
					idAux= UMPSTProject.getInstance().getMapHypothesis().get(key).getId();
					if (idAux.contains(".")){
						intAux = idAux.indexOf(".");
						idAux2 = idAux.substring(0, intAux);
						if (maior<Integer.parseInt(idAux2)){
							maior = Integer.parseInt(idAux2);
						}
					}
					else{
						if (maior< Integer.parseInt(idAux)){
							maior = Integer.parseInt(idAux);
						}
					}
				}
				maior++;
				idAux = maior+"";
			}
			else{
				idAux = 1+"";
			}
			
		}
		else{
			if (hypothesisFather.getSubHypothesis()!=null){
				idAux = hypothesisFather.getId()+"."+ (hypothesisFather.getSubHypothesis().size()+1);
				
			}
			else{
				idAux = hypothesisFather.getId()+".1";

			}
		}
		

		
		HypothesisModel hypothesisAdd = new HypothesisModel(idAux,hypothesisText.getText(),commentsText.getText(), 
				authorText.getText(), dateText.getText(),goalRelated, hypothesisFather,hypothesisChildren,null,null);
		if (hypothesisFather!=null){
			hypothesisFather.getSubHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		}
		goalRelated.getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		UMPSTProject.getInstance().getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		
		return hypothesisAdd;
    }
	
	
}