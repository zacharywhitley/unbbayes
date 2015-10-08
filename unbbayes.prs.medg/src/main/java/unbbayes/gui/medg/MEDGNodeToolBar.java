/**
 * 
 */
package unbbayes.gui.medg;

import java.awt.GridLayout;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;

/**
 * This is simply a reimplementation of {@link unbbayes.gui.mebn.ResidentPaneOptions}.
 * It was created because the original class was not completely closed for extensions.
 * @author Shou Matsumoto
 *
 */
public class MEDGNodeToolBar extends JToolBar {

	private static final long serialVersionUID = 5423754394775896658L;

	private IconController iconController = IconController.getInstance(); 

	private ResourceBundle resource =
		unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.resources.Resources.class.getName(),
				Locale.getDefault(),
				getClass().getClassLoader()
			);

	private JButton btnEditStates;

	private JButton btnEditTable;

	private JButton btnEditArguments;

	private JLabel btnTriangle;

	/**
	 * This constructor simply initializes all components without initializing any listener.
	 * @see #initComponents()
	 */
	public MEDGNodeToolBar(){
		super(); 
		initComponents();
	}

	/**
	 * Simply resets all components.
	 * It won't initialize listeners.
	 * @see #getBtnEditArguments()
	 * @see #getBtnEditStates()
	 * @see #getBtnEditTable()
	 * @see #getBtnTriangle()
	 */
	public void initComponents() {
		this.removeAll();
		setLayout(new GridLayout(1,4)); 
		btnEditStates = new JButton(iconController.getStateIcon());
		btnEditStates.setBackground(MebnToolkit.getColorTabPanelButton()); 
		btnEditTable = new JButton(iconController.getGridIcon());
		btnEditTable.setBackground(MebnToolkit.getColorTabPanelButton()); 
		btnEditArguments = new JButton(iconController.getArgumentsIcon());
		btnEditArguments.setBackground(MebnToolkit.getColorTabPanelButton()); 

		btnTriangle = new JLabel(iconController.getTriangleIcon());
		btnTriangle.setBackground(MebnToolkit.getColorTabPanelButton()); 
		btnTriangle.setEnabled(false); 

		btnEditStates.setToolTipText(resource.getString("stateEditionTip")); 
		btnEditTable.setToolTipText(resource.getString("tableEditionTip")); 
		btnEditArguments.setToolTipText(resource.getString("argumentEditionTip")); 
		
		add(btnTriangle);
		add(btnEditStates);
		add(btnEditArguments);
		add(btnEditTable);
		setFloatable(false); 
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return this.iconController;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}

	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return this.resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

	/**
	 * @return the btnEditStates
	 */
	public JButton getBtnEditStates() {
		return this.btnEditStates;
	}

	/**
	 * @param btnEditStates the btnEditStates to set
	 */
	public void setBtnEditStates(JButton btnEditStates) {
		this.btnEditStates = btnEditStates;
	}

	/**
	 * @return the btnEditTable
	 */
	public JButton getBtnEditTable() {
		return this.btnEditTable;
	}

	/**
	 * @param btnEditTable the btnEditTable to set
	 */
	public void setBtnEditTable(JButton btnEditTable) {
		this.btnEditTable = btnEditTable;
	}

	/**
	 * @return the btnEditArguments
	 */
	public JButton getBtnEditArguments() {
		return this.btnEditArguments;
	}

	/**
	 * @param btnEditArguments the btnEditArguments to set
	 */
	public void setBtnEditArguments(JButton btnEditArguments) {
		this.btnEditArguments = btnEditArguments;
	}

	/**
	 * @return the btnTriangle
	 */
	public JLabel getBtnTriangle() {
		return this.btnTriangle;
	}

	/**
	 * @param btnTriangle the btnTriangle to set
	 */
	public void setBtnTriangle(JLabel btnTriangle) {
		this.btnTriangle = btnTriangle;
	}
}
