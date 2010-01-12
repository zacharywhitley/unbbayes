/**
 * 
 */
package unbbayes.cps.gui.extension;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import unbbayes.cps.gui.CPSController;
import unbbayes.gui.PNEditionPane;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;

/**
 * This class builds a very simple panel containing a button, which
 * opens CPSController.
 * This class contains the code previously situated in {@link PNEditionPane#setTable(javax.swing.JTable, Node)},
 * associated with cps
 * @author Shou Matsumoto
 *
 */
public class CPSPanelBuilder implements IProbabilityFunctionPanelBuilder {

	private Node probabilityFunctionOwner;
	
	private JPanel lastBuiltPanel;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.cps.resources.Resources.class.getName());
	
	/**
	 * The default constructor must be public for plugin compatibility
	 */
	public CPSPanelBuilder() {
		super();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {
		//young2010 
        JButton btnCPS   = new JButton(this.resource.getString("editCPS"));
        btnCPS.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCPS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) { 
            	lastBuiltPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
            	new CPSController(getProbabilityFunctionOwner());
            	lastBuiltPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

        });
        
//        this.lastBuiltPanel = new JPanel(new BoxLayout(lastBuiltPanel, BoxLayout.Y_AXIS));
        this.lastBuiltPanel = new JPanel();
        lastBuiltPanel.setBorder(new TitledBorder(this.resource.getString("borderMessage")));
        
        lastBuiltPanel.add(btnCPS);
		
        return lastBuiltPanel;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#getProbabilityFunctionOwner()
	 */
	public Node getProbabilityFunctionOwner() {
		return this.probabilityFunctionOwner;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#setProbabilityFunctionOwner(unbbayes.prs.Node)
	 */
	public void setProbabilityFunctionOwner(Node node) {
		this.probabilityFunctionOwner = node;
	}

}
