/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.controller.IconController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.extension.OWL2PropertyViewerPanel;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;

/**
 * This is a dialog to ask a user to select an OWL property.
 * It is reusing {@link OWL2PropertyViewerPanel}
 * @author Shou Matsumoto
 *
 */
public class PropertySelectionDialog extends JDialog {
	private JButton okButton;
	private OWL2PropertyViewerPanel propertyListPanel;
	private ResourceBundle resource;
	private MultiEntityBayesianNetwork mebn;
	private JButton cancelButton;
	private JPanel buttonPanel;
	
	/** Use this method as default constructor */
	public static PropertySelectionDialog newInstance(MultiEntityBayesianNetwork mebn) {
		PropertySelectionDialog ret =  new PropertySelectionDialog();
		ret.setMebn(mebn);
		ret.initComponents();
		ret.initListeners();
		return ret;
	}
	

	/**
	 * Constructor is not made public. Use {@link #newInstance()} instead
	 */
	protected PropertySelectionDialog () {
		super();
		try {
			this.resource = ResourceController.newInstance().getBundle(
					unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
					Locale.getDefault(),
					this.getClass().getClassLoader());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * This method initializes components of this object.
	 * @see #initListeners()
	 */
	protected void initComponents() {
//		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModal(true);
		this.setTitle(getResource().getString("SelectOWLPropertyToolTip"));		
		this.getContentPane().setLayout(new BorderLayout());
		
		propertyListPanel = (OWL2PropertyViewerPanel)OWL2PropertyViewerPanel.newInstance(getMebn());
		// disable multiple selection of properties
		propertyListPanel.getPropertyList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// disable drag and drop
		propertyListPanel.getPropertyList().setDragEnabled(false);
		// change text border
		propertyListPanel.setBorder(MebnToolkit.getBorderForTabPanel(getResource().getString("SelectOWLPropertyToolTip")));
		// add property list
		this.getContentPane().add(propertyListPanel, BorderLayout.CENTER);
		
		// create panel to hold buttons
		this.setButtonPanel(new JPanel(new FlowLayout(FlowLayout.CENTER)));
		this.getContentPane().add(this.getButtonPanel(), BorderLayout.SOUTH);
		
		// create the OK button
		this.setOkButton(new JButton(IconController.getInstance().getMoreIcon()));
		this.getOkButton().setToolTipText(getResource().getString("DefineUncertaintyOfOWLProperty"));
		this.getButtonPanel().add(this.getOkButton());
		
		// create cancel button
		this.setCancelButton(new JButton(IconController.getInstance().getLessIcon()));
		this.getCancelButton().setToolTipText(getResource().getString("cancelToolTip"));
		this.getButtonPanel().add(this.getCancelButton());
		
		Dimension preferredSize = new Dimension(480, 600);
		this.setPreferredSize(preferredSize);
		this.setSize(preferredSize);
		this.pack();
	}
	
	/**
	 * This method initializes listeners of components of this object
	 * @see #initComponents()
	 */
	protected void initListeners() {
		this.getCancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// clear current selection
				getPropertyListPanel().getPropertyList().clearSelection();
				// hide when cancel is pressed
				PropertySelectionDialog.this.setVisible(false);
				if (PropertySelectionDialog.this.getDefaultCloseOperation() == PropertySelectionDialog.DISPOSE_ON_CLOSE
						|| PropertySelectionDialog.this.getDefaultCloseOperation() == PropertySelectionDialog.EXIT_ON_CLOSE) {
					PropertySelectionDialog.this.dispose();
				}
			}
		});
		
		// hide when OK is pressed
		this.getOkButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PropertySelectionDialog.this.setVisible(false);
//				PropertySelectionDialog.this.dispose();
			}
		});
	}
	
	/**
	 * This method will return the selected OWL property.
	 * @return
	 */
	public OWLProperty getSelectedValue() {
		try {
			return (OWLProperty)this.getPropertyListPanel().getPropertyList().getSelectedValue();
		} catch (ClassCastException e) {
			Debug.println(this.getClass(), e.getMessage(), e);
		}
		return null;
	}
	
	public JButton getOkButton() {return okButton;}
	public void setOkButton(JButton okButton) {this.okButton = okButton;}
	/**
	 * @return the propertyListPanel
	 */
	public OWL2PropertyViewerPanel getPropertyListPanel() {
		return propertyListPanel;
	}
	/**
	 * @param propertyListPanel the propertyListPanel to set
	 */
	public void setPropertyListPanel(OWL2PropertyViewerPanel propertyListPanel) {
		this.propertyListPanel = propertyListPanel;
	}
	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}
	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

	/**
	 * @return the mebn
	 */
	public  MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}

	/**
	 * @param mebn the mebn to set
	 */
	public  void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}


	/**
	 * @return the cancelButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}


	/**
	 * @param cancelButton the cancelButton to set
	 */
	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}


	/**
	 * @return the buttonPanel
	 */
	public JPanel getButtonPanel() {
		return buttonPanel;
	}


	/**
	 * @param buttonPanel the buttonPanel to set
	 */
	public void setButtonPanel(JPanel buttonPanel) {
		this.buttonPanel = buttonPanel;
	}
}
