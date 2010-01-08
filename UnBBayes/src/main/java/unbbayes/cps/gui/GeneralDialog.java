package unbbayes.cps.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.util.Vector;
 
//Following can be changed applet easily
public class GeneralDialog extends JDialog {

	private static final long serialVersionUID = -6372075014284878800L;
	
	// The preferred size of the demo
	private int PREFERRED_WIDTH = 680;
	private int PREFERRED_HEIGHT = 600;

    Dimension VGAP5 = new Dimension(1,5); 
    EmptyBorder border5 = new EmptyBorder(5,5,5,5); 
    Vector radiobuttons = new Vector(); 
    
	Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), 
					  new EmptyBorder(5,5,5,5));
	private JPanel panel = null;
	private String resourceName = null;


	public GeneralDialog(String resourceName)  
	{ 
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
 
		this.resourceName = resourceName;
	}
 
    public String getResourceName() 
    {
    	return resourceName;
    }

    public JPanel getFrame() 
    {
    	return panel;
    }
    
    public void mainImpl() 
    {
    	JFrame frame = new JFrame(getName());
    	frame.getContentPane().setLayout(new BorderLayout());
    	frame.getContentPane().add(getFrame(), BorderLayout.CENTER);
    	getFrame().setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    	frame.pack();
    	frame.show();
    }
    
    public JButton createButton(Action a) 
    { 
   	 	JButton b = new JButton(); 
   	 
   	 	b.putClientProperty("displayActionText", Boolean.TRUE); 
   	 	b.setAction(a); 
   	 	
   	 	return b; 
    }  
 
    public JRadioButton createRadioButton(Action a) 
    { 
   	 	JRadioButton b = new JRadioButton(); 
   	 
   	 	b.putClientProperty("displayActionText", Boolean.TRUE); 
   	 	b.setAction(a); 
   	 	
   	 	return b; 
    }  
    
    public JPanel createHorizontalPanel(boolean threeD) 
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	p.setAlignmentY(TOP_ALIGNMENT);
    	p.setAlignmentX(LEFT_ALIGNMENT);
    	if(threeD) 
    	{
    		p.setBorder(loweredBorder);
    	}
    	return p;
    }
   
    public JPanel createVerticalPanel(boolean threeD) 
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.setAlignmentY(TOP_ALIGNMENT);
    	p.setAlignmentX(LEFT_ALIGNMENT);
    	if(threeD) 
    	{
    		p.setBorder(loweredBorder);
    	}
    	return p;
    }

    public static JPanel createPaneV(JComponent pane1, JComponent pane2 )
    {
		 JPanel pane =  new JPanel();
		 pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS)); 
		 pane1.setAlignmentX(Component.LEFT_ALIGNMENT);
		 pane2.setAlignmentX(Component.LEFT_ALIGNMENT);
		 pane.add(pane1);
		 pane.add(pane2);
		 return pane;
    }
    
    public static JPanel createPaneH(JComponent pane1, JComponent pane2 )
    {
	   	 JPanel pane =  new JPanel();
	   	 pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS)); 
	   	 pane1.setAlignmentY(Component.LEFT_ALIGNMENT);
		 pane2.setAlignmentY(Component.LEFT_ALIGNMENT);
	  	 pane.add(pane1);
	   	 pane.add(pane2);  	 
	   	 return pane;
    }
     
    public void init() 
    {
    	//following is used for applet
    	//getContentPane().setLayout(new BorderLayout());
    	//getContentPane().add(getAppletFrame(), BorderLayout.CENTER);
    }
     
    public static void main(String[] args) 
    { 
    	GeneralDialog sim = new GeneralDialog("asad"); 
   	 	sim.mainImpl(); 
    } 
}
