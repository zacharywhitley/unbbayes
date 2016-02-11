package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.util.ResourceController;


/**
 * Pane that show the description of the selected object
 */
public class DescriptionPane extends JPanel{

    /* Icon Controller */
    private final IconController iconController = IconController.getInstance();

	/* Load resource file from this package */
  	private static ResourceBundle resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.resources.Resources.class.getName());; 

	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_RESIDENT  = 1; 
	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_INPUT =     2; 
	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_CONTEXT =   3; 
	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_OVARIABLE = 4; 
	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_MFRAG =     5; 
	/** @see #setDescriptionText(String, int)*/
	public static final int DESCRIPTION_PANE_MTHEORY =   6; 


	private JTextArea textArea;
	private JToolBar toolBar; 
	private JScrollPane scrollPane; 
	
	private final MEBNController mebnController; 

	public DescriptionPane(MEBNController mebnController){

		super(new BorderLayout());

		this.mebnController = mebnController; 

		textArea = new JTextArea(5, 10);
		scrollPane =
			new JScrollPane(textArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textArea.setEditable(true);
		
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		textArea.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				DescriptionPane.this.mebnController.setDescriptionTextForSelectedObject(textArea.getText()); 
			}

		}); 

		toolBar = new JToolBar(); 
		toolBar.setFloatable(false); 
		toolBar.setBackground(Color.white); 

		JLabel labelDescription = new JLabel(" " + resource.getString("descriptionLabel")); 
		labelDescription.setBackground(Color.WHITE); 
		toolBar.add(labelDescription); 
		
		add(scrollPane, BorderLayout.CENTER);
		add(toolBar, BorderLayout.PAGE_START); 
	}

	/**
	 * Changes the text of the description pane and its icon.
	 * @param description
	 * @param icon
	 */
	public void setDescriptionText(String description, ImageIcon icon) {
		textArea.setText(description);
		if (icon == null) {
			icon =  iconController.getTxtFileIcon();
		}
		JButton btn = new JButton(icon); 

		btn.setBackground(Color.WHITE); 
		btn.setSize(20, 20); 

		JLabel labelDescription = new JLabel(" " + resource.getString("descriptionLabel")); 
		labelDescription.setBackground(Color.WHITE); 
		
		toolBar.removeAll(); 
		toolBar.add(btn); 
		toolBar.add(labelDescription); 
		
		textArea.setCaretPosition(0);
		
		
	}
	
	/**
	 * Changes the text of the description pane and its icon, using
	 * default icons depending on the value of type.
	 * @param description
	 * @param type
	 * @see  #setDescriptionText(String, ImageIcon)}
	 * 
	 */
	public void setDescriptionText(String description, int type){
		ImageIcon icon = null;
		
		switch (type){

		case DESCRIPTION_PANE_RESIDENT:
			icon = iconController.getResidentNodeIcon(); 
			break; 

		case DESCRIPTION_PANE_INPUT:
			icon = iconController.getInputNodeIcon(); 
			break;

		case DESCRIPTION_PANE_CONTEXT:
			icon = iconController.getContextNodeIcon(); 
			break; 

		case DESCRIPTION_PANE_OVARIABLE:
			icon = iconController.getOVariableNodeIcon(); 
			break; 

		case DESCRIPTION_PANE_MFRAG:
			icon = iconController.getMFragIcon(); 
			break; 

		case DESCRIPTION_PANE_MTHEORY:
			icon = iconController.getMTheoryNodeIcon(); 
			break; 
		default:
			// icon = null;
			break; 
		}

		this.setDescriptionText(description, icon);
	}

	public String getDescriptionText(){
		return textArea.getText();
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}

}