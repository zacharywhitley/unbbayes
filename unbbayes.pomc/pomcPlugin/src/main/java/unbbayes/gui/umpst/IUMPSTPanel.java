package unbbayes.gui.umpst;

import java.awt.GridLayout;

import javax.swing.JPanel;


public abstract class IUMPSTPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UmpstModule fatherPanel;
	
	public UmpstModule getFatherPanel(){
		return fatherPanel;
	}
	
	public void alterarJanelaAtual(JPanel painel){
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

}
