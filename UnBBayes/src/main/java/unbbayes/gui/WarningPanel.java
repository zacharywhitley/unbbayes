package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.ssbn.SSBNWarning;
import unbbayes.util.ResourceController;

public class WarningPanel extends JPanel{

	   /* Icon Controller */
    private final IconController iconController = IconController.getInstance();

	/* Load resource file from this package */
  	private static ResourceBundle resource = ResourceController.RS_GUI; 

	private JTextArea textArea;
	private JToolBar toolBar; 
	
	private final MEBNController mebnController; 

	public WarningPanel(final MEBNController mebnController){

		super(new BorderLayout());
		
		textArea = new JTextArea(); 
		toolBar = new JToolBar(); 

		this.mebnController = mebnController; 
		
		JScrollPane scrollPane =
			new JScrollPane(textArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		textArea.setEditable(true);

		toolBar = new JToolBar(); 
		toolBar.setFloatable(false);
		FlowLayout layoutManager = new FlowLayout();
		layoutManager.setAlignment(FlowLayout.RIGHT); 
		toolBar.setLayout(layoutManager); 
		toolBar.setBackground(Color.white); 

		JButton btnEnd = new JButton("" + resource.getString("closeButton")); 
		btnEnd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				WarningPanel.this.mebnController.closeWarningDialog();  
			}
			
		}); 
		toolBar.add(btnEnd); 
		
		add(scrollPane, BorderLayout.CENTER);
		add(toolBar, BorderLayout.PAGE_END); 
	}

	public void setListWarningAndUpdateText(List<SSBNWarning> listSSBNWarning){
		
		StringBuilder builder = new StringBuilder(); 
		
		builder.append(resource.getString("warningFound") + ":\n");
		
		int countWarning = 0; 
		
		builder.append("----------\n"); 
		for(SSBNWarning w: listSSBNWarning){
			builder.append("[" + countWarning + "]\n"); 
			builder.append(w.getNodeCause().getName() + "\n"); 
			builder.append(w.getExplanation() + "\n");
			builder.append(w.getDetalhedExplanation() + "\n");
			builder.append("----------\n"); 
			countWarning++; 
		}
		
		textArea.setText(builder.toString());
	}

	public String getDescriptionText(){
		return textArea.getText();
	}

	
}
