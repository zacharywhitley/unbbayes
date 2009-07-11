 
package unbbayes.draw;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;

import unbbayes.prs.Node;

public class UShapeText extends UShape  
{     
	protected Rectangle2D rect;
	
	protected static JTextField Input;
	protected static FlowLayout Layout;
	
	public UShapeText(UShape s, int x, int y, int w, int h)
	{
		super(s.getCanvas(), null, x, y, w, h);
		s.add(this);
		setUseSelection(false);
		  
	
	    Input = new JTextField ("Input", 10);
	    Input.setBounds(20, 20, w-40, h-40); 
		 
	    Input.setBackground (Color.white);
		  
		add (Input);  
		 
		
		JTextField nameTextField = new JTextField("Input", 10);
		s.add(nameTextField, BorderLayout.NORTH);

	    KeyListener keyListener = new KeyListener() {
	      public void keyPressed(KeyEvent keyEvent) {
	        printIt("Pressed", keyEvent);
	      }

	      public void keyReleased(KeyEvent keyEvent) {
	        printIt("Released", keyEvent);
	      }

	      public void keyTyped(KeyEvent keyEvent) {
	        printIt("Typed", keyEvent);
	      }

	      private void printIt(String title, KeyEvent keyEvent) {
	        int keyCode = keyEvent.getKeyCode();
	        String keyText = KeyEvent.getKeyText(keyCode);
	        System.out.println(title + " : " + keyText + " / " + keyEvent.getKeyChar());
	      }
	    };
	    
	    nameTextField.addKeyListener(keyListener);
	    
	    InitShape();
    }     
	
	public void InitShape() 
	{
		rect = new Rectangle2D.Double(GAP ,GAP,getWidth()-GAP*4-1,getHeight()-GAP*4-1 );
	} 
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;		
	  	g2.draw (rect);
	}	    
	
	public boolean contain(double x, double y) 
	{
		return false;
	}   
}

