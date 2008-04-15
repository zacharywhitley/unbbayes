package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

public class ProgressBarPanel extends JPanel {

    private JProgressBar progressBar;
    private JButton cancelButton;
    
	public ProgressBarPanel(){
		super(new BorderLayout()); 
		
		progressBar = new JProgressBar(0, 100); 
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true); 
        
        JPanel panel = new JPanel(new BorderLayout());

        cancelButton = new JButton("Cancel"); 
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(cancelButton, BorderLayout.LINE_END);
        
        add(panel, BorderLayout.CENTER); 
        
        this.validate(); 
        this.repaint(); 
        
//        Timer timer = new Timer(1000, new ActionListener() {            
//        	int i = 0; 
//        	public void actionPerformed(ActionEvent evt) {
//            	i= i + 10; 
//                System.out.println("i = " + i);
//                progressBar.setValue(i);
//                progressBar.repaint(); 
//            }
//        });
//        timer.start();
	}
	
	public static void main(String... args){
		ProgressBarPanel progressPanel = new ProgressBarPanel(); 
		JDialog dialog = new JDialog(); 
		dialog.setContentPane(progressPanel); 
		dialog.setVisible(true); 
		dialog.setModal(true); 
//		dialog.setOpaque(true); //content panes must be opaque
		dialog.pack(); 
		dialog.repaint(); 
		dialog.setLocationRelativeTo(UnBBayesFrame.getIUnBBayes()); 

	}
	
}
