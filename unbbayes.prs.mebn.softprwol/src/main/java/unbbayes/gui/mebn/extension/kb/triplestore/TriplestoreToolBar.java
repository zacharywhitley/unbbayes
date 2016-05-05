package unbbayes.gui.mebn.extension.kb.triplestore;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JToolBar;

import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.triplestore.TriplestoreKnowledgeBase;

/**
 * Tool Bar with options to work with triplestore. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class TriplestoreToolBar extends JToolBar implements DatabaseStatusObserver{

	TriplestoreKnowledgeBase kb; 

	boolean databaseConnectionStatus = false; 
	
	JButton btnStatus; 
	JButton btnConfigure; 
	JButton btnConnect; 
	JButton btnLoadTBox; 
	JButton btnQuery; 

	
	public TriplestoreToolBar(){
		super(); 
		
		btnStatus = new JButton(""); 
		
		this.setStatusOff();
		
		btnConfigure = new JButton("Configure");  
		btnConnect= new JButton("Connect"); 
		btnLoadTBox= new JButton("LoadTBox"); 
		btnQuery =new JButton("Query"); 
		
		add(btnStatus); 
//		add(btnConfigure);  
//		add(btnConnect);     
//		add(btnLoadTBox); 
//		add(btnQuery); 
		
	}
	
	private void setStatusOff() {
		btnStatus.setText("OFF");
		btnStatus.setBackground(Color.RED);
		this.repaint();
	}
	
	private void setStatusOn() {
		btnStatus.setBackground(Color.green);
    	btnStatus.setText("ON");
		btnStatus.setToolTipText(this.getKb().getTriplestoreController().getTriplestore().getRepositoryURI());
    	this.repaint();
	}
	
	public TriplestoreKnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {

		this.kb = (TriplestoreKnowledgeBase)kb;
		
		if(this.kb.getTriplestoreController().isConnected()){
			this.setStatusOn();
		}else{
			this.setStatusOff();
		}
		
		this.getKb().getTriplestoreController().atach(this);
	}
	
	public boolean getDabatabaseConnectionStatus() {
		return databaseConnectionStatus;
	}

	public void setDatabaseConnectionStatus(boolean status) {
		this.databaseConnectionStatus = status;
	}

	@Override
	public void update(boolean state) {

		if(state){
			this.setStatusOn();
		}else{
			this.setStatusOff();
		}
		
	}

	
}
