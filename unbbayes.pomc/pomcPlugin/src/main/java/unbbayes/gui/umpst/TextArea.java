package unbbayes.gui.umpst;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextArea extends IUMPSTPanel{

	
	private JTextArea textArea1;	
	private JTextArea textArea2;
	private JButton copyButton;
	
	String demo = "goal1"+"goal2"+"hyphotesis1";

	public TextArea(UmpstModule janelaPai){
		super(janelaPai);
    	this.setLayout(new GridLayout(1,0));
    	
    	this.add(getTextArea());
    
    	
    			
	}
	
	public Box getTextArea(){
		//super("text area demo");
		Box box = Box.createHorizontalBox();
		
		textArea1 = new JTextArea(demo,10,15);
		box.add(new JScrollPane(textArea1));
		
		copyButton = new JButton("copy >>");
		copyButton.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						textArea2.setText(textArea1.getSelectedText());						
					}
				}
		
		);
		
		
		textArea2 = new JTextArea(10,15);
		textArea2.setEditable(false);
		box.add(new JScrollPane(textArea2));
		
		return box;
		
	}
	
}
