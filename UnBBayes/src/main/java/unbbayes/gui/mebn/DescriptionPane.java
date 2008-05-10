package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.util.ResourceController;


/**
 * Pane that show the description of the selected object
 */
public class DescriptionPane extends JPanel{

    /* Icon Controller */
    private final IconController iconController = IconController.getInstance();

	/* Load resource file from this package */
  	private static ResourceBundle resource = ResourceController.RS_GUI; 

	
	public static final int DESCRIPTION_PANE_RESIDENT  = 1; 
	public static final int DESCRIPTION_PANE_INPUT =     2; 
	public static final int DESCRIPTION_PANE_CONTEXT =   3; 
	public static final int DESCRIPTION_PANE_OVARIABLE = 4; 
	public static final int DESCRIPTION_PANE_MFRAG =     5; 
	public static final int DESCRIPTION_PANE_MTHEORY =   6; 


	private JTextArea textArea;
	private JToolBar toolBar; 
	
	private final MEBNController mebnController; 

	public DescriptionPane(MEBNController mebnController){

		super(new BorderLayout());

		this.mebnController = mebnController; 
		
//		TitledBorder titledBorder;
//
//		titledBorder = BorderFactory.createTitledBorder(
//				BorderFactory.createLineBorder(Color.BLUE),
//				resource.getString("descriptionLabel"));
//		titledBorder.setTitleColor(Color.BLUE);
//		titledBorder.setTitleJustification(TitledBorder.CENTER);
//
//		setBorder(titledBorder);
//		setBorder(BorderFactory.createLineBorder(Color.blue)); 

		textArea = new JTextArea(5, 10);
		JScrollPane scrollPane =
			new JScrollPane(textArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		textArea.setEditable(true);

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

	public void setDescriptionText(String description, int type){
		textArea.setText(description);

		JButton btn = new JButton(); 

		switch (type){

		case DESCRIPTION_PANE_RESIDENT:
			btn = new JButton(iconController.getResidentNodeIcon()); 
			break; 

		case DESCRIPTION_PANE_INPUT:
			btn = new JButton(iconController.getInputNodeIcon()); 
			break;

		case DESCRIPTION_PANE_CONTEXT:
			btn = new JButton(iconController.getContextNodeIcon()); 
			break; 

		case DESCRIPTION_PANE_OVARIABLE:
			btn = new JButton(iconController.getOVariableNodeIcon()); 
			break; 

		case DESCRIPTION_PANE_MFRAG:
			btn = new JButton(iconController.getMFragIcon()); 
			break; 

		case DESCRIPTION_PANE_MTHEORY:
			btn = new JButton(iconController.getMTheoryNodeIcon()); 
			break; 

		default:
			break; 
		}

		btn.setBackground(Color.WHITE); 
		btn.setSize(20, 20); 
//		btn.se

		JLabel labelDescription = new JLabel(" " + resource.getString("descriptionLabel")); 
		labelDescription.setBackground(Color.WHITE); 
		
		toolBar.removeAll(); 
		toolBar.add(btn); 
		toolBar.add(labelDescription); 
	}

	public String getDescriptionText(){
		return textArea.getText();
	}

}