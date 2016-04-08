package unbbayes.gui.mebn.extension.kb.triplestore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.triplestore.TriplestoreKnowledgeBase;
import unbbayes.triplestore.Parameters;
import unbbayes.triplestore.Triplestore;

public class TriplestoreOptionPanelBuilder extends JScrollPane implements IKBOptionPanelBuilder {

	private TriplestoreKnowledgeBase kb = null;
	
	public TriplestoreOptionPanelBuilder(){
		
		super(); 
		
		this.setName("Available OWL2 Reasoners");
		
	}
	
	@Override
	public JComponent getPanel() {
		return this;
	}

	@Override
	public void commitChanges() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discardChanges() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKB(KnowledgeBase kb) {
		if (this.kb == kb) {
			// no change. Do nothing
			return;
		}
		this.kb = (TriplestoreKnowledgeBase)kb;
	}

	@Override
	public KnowledgeBase getKB() {
		return kb;
	}
	
	public void updateUI() {
		
		JTextField repositoryNameTextField; 
		JTextField databaseURLTextField; 
		JButton btnConnect; 
		
		JPanel newView = new JPanel(new BorderLayout());
		
		newView.setBackground(Color.WHITE);
		newView.setBorder(new TitledBorder(this.getName()));
		
		
		JPanel formPanel = new JPanel(new GridLayout(4,0)); 
		
		formPanel.add(new JLabel("Connection Configuration")); 
		
		JPanel line = new JPanel(); 
		line.add(new JLabel("   Database URL:")); 
		databaseURLTextField = new JTextField(30); 
		databaseURLTextField.setText("http://localhost:8080/graphdb-workbench-free/");
		line.add(databaseURLTextField); 
		
		formPanel.add(line); 
		
		line = new JPanel(); 
		line.add(new JLabel("Repository Name:")); 
		repositoryNameTextField = new JTextField(30); 
		repositoryNameTextField.setText("LUBM1RL");
		line.add(repositoryNameTextField);
		formPanel.add(line); 
		
		line = new JPanel(); 
		
		btnConnect = new JButton("Connect"); 
		
		JButton btnStatus = new JButton("OFF"); 
		btnStatus.setBackground(Color.RED);
		btnStatus.repaint();
		
		
		line.add(btnConnect);
		line.add(btnStatus); 
		
		formPanel.add(line); 
		
		btnConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	Parameters params = new Parameters(new String[0]);
            	
        		params.setDefaultValue(Triplestore.PARAM_URL, databaseURLTextField.getText());
        		params.setDefaultValue(Triplestore.PARAM_REPOSITORY, repositoryNameTextField.getText());
            	
        		kb.getTriplestoreController().startConnection(params);
        		
            	try {
            		kb.getTriplestoreController().iterateNamespaces();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            	btnStatus.setBackground(Color.green);
            	btnStatus.setText("ON");
            	btnStatus.repaint();
            	System.out.println("Connected!");
            }
        });
		
		
		newView.add(formPanel, BorderLayout.PAGE_START); 
		
		newView.add(new JPanel(), BorderLayout.CENTER); 
		
		this.setViewportView(newView);
		
		super.updateUI();
		
		this.repaint();
		
	}
	

}
