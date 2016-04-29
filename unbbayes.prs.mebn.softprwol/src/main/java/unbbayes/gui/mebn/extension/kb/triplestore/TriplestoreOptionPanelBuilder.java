package unbbayes.gui.mebn.extension.kb.triplestore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.triplestore.TriplestoreKnowledgeBase;
import unbbayes.triplestore.SAILTriplestoreParameters;
import unbbayes.triplestore.Triplestore;
import unbbayes.triplestore.exception.TriplestoreException;
import unbbayes.util.Parameters;
import unbbayes.util.ResourceController;

public class TriplestoreOptionPanelBuilder extends JScrollPane implements 
                                                          IKBOptionPanelBuilder,
                                                          DatabaseStatusObserver{

	private TriplestoreKnowledgeBase kb;
	
	private JButton btnStatus; 
	
  	private static ResourceBundle resource = ResourceController.newInstance().getBundle(
  			unbbayes.gui.mebn.extension.kb.triplestore.resources.Resources.class.getName());
  	
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
		}else{
			if(this.kb != null){
				this.kb.getTriplestoreController().detach(this);
			}
			this.kb = (TriplestoreKnowledgeBase)kb;
			this.kb.getTriplestoreController().atach(this);
		}
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
		btnStatus = new JButton(""); 
		
		setStatusOff();
		
		line.add(btnConnect);
		line.add(btnStatus); 
		
		formPanel.add(line); 
		
		btnConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	Parameters params = new SAILTriplestoreParameters();
            	
        		params.setParameterValue(Triplestore.PARAM_URL, databaseURLTextField.getText());
        		params.setParameterValue(Triplestore.PARAM_REPOSITORY, repositoryNameTextField.getText());
            	
        		try {
					kb.getTriplestoreController().startConnection(params);
					kb.getTriplestoreController().iterateNamespaces();
					
					setStatusOn();
					
				} catch (TriplestoreException e2) {
					JOptionPane.showMessageDialog(null, 
							e2.getMessage(),
							resource.getString("ConnectionError"), 
							JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, 
							e1.getMessage(),
							resource.getString("ConnectionError"), 
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
            }
        });
		
		
		newView.add(formPanel, BorderLayout.PAGE_START); 
		
		newView.add(new JPanel(), BorderLayout.CENTER); 
		
		this.setViewportView(newView);
		
		super.updateUI();
		
		this.repaint();
		
	}

	private void setStatusOff() {
		btnStatus.setText("OFF");
		btnStatus.setBackground(Color.RED);
		btnStatus.repaint();
	}
	
	private void setStatusOn() {
		btnStatus.setBackground(Color.green);
    	btnStatus.setText("ON");
    	btnStatus.repaint();
	}

	@Override
	public void update(boolean state) {
		if(state){
			setStatusOn(); 
		}else{
			setStatusOff();
		}
	}
	
}
