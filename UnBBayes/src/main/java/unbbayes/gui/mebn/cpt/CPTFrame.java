package unbbayes.gui.mebn.cpt;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import unbbayes.controller.MEBNController;
import unbbayes.gui.GUIUtils;
import unbbayes.prs.mebn.ResidentNode;

public class CPTFrame extends JFrame{

	private final MEBNController mebnController; 
	private final ResidentNode residentNode; 
	
	public CPTFrame(MEBNController mebnController_, ResidentNode residentNode_){
		super(residentNode_.getName());
		mebnController = mebnController_; 
		residentNode = residentNode_; 
    	CPTEditionPane cptEditionPane = new CPTEditionPane(mebnController, residentNode);
    	setContentPane(cptEditionPane);
    	setLocation(GUIUtils.getCenterPositionForComponent(750,300));
    	pack(); 
    	setVisible(true); 
    	
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    	
    	addWindowListener(new WindowAdapter() {
    		    public void windowClosing(WindowEvent we) {
    		        mebnController.closeCPTDialog(residentNode); 
    		    }
    	});
	}
	
}
