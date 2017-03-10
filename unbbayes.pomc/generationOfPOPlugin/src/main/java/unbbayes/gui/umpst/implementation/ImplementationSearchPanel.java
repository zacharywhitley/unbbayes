/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * @author diego
 *
 */
public class ImplementationSearchPanel extends IUMPSTPanel{

	/**
	 * @param umpstProject 
	 * @param janelaPai 
	 * 
	 */
	public ImplementationSearchPanel(UmpstModule janelaPai, UMPSTProject umpstProject) {
		super(janelaPai);		
		this.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
	}

}
