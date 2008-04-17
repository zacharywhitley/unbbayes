package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressBarPanel extends JPanel implements StatusObserver{

    private JProgressBar progressBar;
    private JButton cancelButton;
    
    
//	public ProgressBarPanel(final GUICommand cancelCommand)  -> with threads...
	
	public ProgressBarPanel(){
		super(new BorderLayout()); 
		
		progressBar = new JProgressBar(0, 100); 
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true); 
        
        JPanel panel = new JPanel(new BorderLayout());

//        cancelButton = new JButton("Cancel"); 
//        cancelButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				cancelCommand.execute(); 
//			}
//        }); 
        
        panel.add(progressBar, BorderLayout.CENTER);
//        panel.add(cancelButton, BorderLayout.LINE_END);
        
        add(panel, BorderLayout.CENTER); 
        
        validate(); 
		paintImmediately(0, 0, this.getWidth(), this.getHeight()); 
	}

	public void update(){
		paintImmediately(0, 0, this.getWidth(), this.getHeight()); 
	}
	
	public void update(StatusChangedEvent status) {
		progressBar.setValue(status.getPercentageConclude()); 
		this.paintImmediately(0, 0, this.getWidth(), this.getHeight()); 
	}
	
}
