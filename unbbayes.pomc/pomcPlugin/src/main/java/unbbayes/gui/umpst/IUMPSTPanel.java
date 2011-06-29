package unbbayes.gui.umpst;

import java.awt.GridLayout;

import javax.swing.JPanel;

import unbbayes.model.umpst.project.UMPSTProject;


public abstract class IUMPSTPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UmpstModule fatherPanel;
	private UMPSTProject umpstProject ;

	
	public UmpstModule getFatherPanel(){
		return fatherPanel;
	}
	
	public void changePanel(JPanel painel){
		getFatherPanel().setContentPane(painel);
		getFatherPanel().paintComponents(getFatherPanel().getGraphics());
	}
	
	public IUMPSTPanel(UmpstModule fatherPanel) {
		// TODO Auto-generated constructor stub
		this.fatherPanel = fatherPanel;
	}

	public IUMPSTPanel(GridLayout gridLayout, UmpstModule fatherPanel) {
		super(gridLayout);
		this.fatherPanel = fatherPanel;
	}
		

	/**
	 * @return the umpstProject
	 */
	public UMPSTProject getUmpstProject() {
		return umpstProject;
	}

	/**
	 * @param umpstProject the umpstProject to set
	 */
	public void setUmpstProject(UMPSTProject umpstProject) {
		this.umpstProject = umpstProject;
	}
	
	

}
