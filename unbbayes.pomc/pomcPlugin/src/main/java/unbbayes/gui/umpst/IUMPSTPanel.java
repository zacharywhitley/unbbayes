package unbbayes.gui.umpst;

import java.awt.GridLayout;

import javax.swing.JPanel;


public abstract class IUMPSTPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UmpstModule janelaPai;
	
	public UmpstModule getJanelaPai(){
		return janelaPai;
	}
	
	public void alterarJanelaAtual(JPanel painel){
		getJanelaPai().setContentPane(painel);
		getJanelaPai().paintComponents(getJanelaPai().getGraphics());
	}
	
	public IUMPSTPanel(UmpstModule janelaPai) {
		// TODO Auto-generated constructor stub
		this.janelaPai = janelaPai;
	}

	public IUMPSTPanel(GridLayout gridLayout, UmpstModule janelaPai) {
		super(gridLayout);
		this.janelaPai = janelaPai;
	}

}
