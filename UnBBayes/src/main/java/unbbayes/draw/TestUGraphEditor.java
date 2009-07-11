package unbbayes.draw;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.border.Border;
import javax.swing.event.*;
 
import unbbayes.draw.UCanvas;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

public class TestUGraphEditor extends JFrame implements WindowStateListener
{
	protected UCanvas m_Canvas;  
 
	public TestUGraphEditor() 
	{
		super("TestUGraphEditor");
	
	 	addWindowStateListener(this);
	 	
        //Using UGraphEditor
        m_Canvas = new UCanvas();
        m_Canvas.setPreferredSize(new Dimension( 800, 600));
                         
        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
	    int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	    
        JScrollPane scrollPane = new JScrollPane(m_Canvas,v, h);    
        getContentPane().add(scrollPane, BorderLayout.CENTER);
          
     	//Add Buttons 
		JPanel buttonPanel = new JPanel(new GridLayout());
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
        
		   	//Add Listener
		JButton btn;
        
        btn = addButton(buttonPanel, "Create ProbabilisticNode");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		//Test
        		ProbabilisticNode node = new ProbabilisticNode();
        		node.setPosition(50, 50);
        		node.appendState("State1");
        		node.appendState("State2");
        		node.appendState("State3");
        		node.appendState("State4");
        		node.setName("longname-longname-longname-longname-longname-longname-longname-longname");
        		node.setDescription(node.getName());
        		PotentialTable auxTabProb = ((ITabledVariable) node)
        				.getPotentialTable();
        		auxTabProb.addVariable(node);
        		auxTabProb.setValue(0, 1);
        		
        		UShapeProbabilisticNode shape = new UShapeProbabilisticNode(m_Canvas, (ProbabilisticNode) node, (int)node.getPosition().x-node.getWidth()/2, (int)node.getPosition().y-node.getHeight()/2, node.getWidth(), node.getHeight() );
        		m_Canvas.addShape( shape );
        		shape.setShapeType(UShapeProbabilisticNode.STYPE_BAR);
        		 
        		
        		//Test
        		ProbabilisticNode node2 = new ProbabilisticNode();
        		node2.setPosition(160, 80);
        		node2.appendState("firstStateProbabilisticName");
        		node2.setName("123456789abcdefghijklmnopqrstuvwxyz");
        		node2.setDescription(node2.getName());
        		PotentialTable auxTabProb2 = ((ITabledVariable) node2)
        				.getPotentialTable();
        		auxTabProb2.addVariable(node2);
        		auxTabProb2.setValue(0, 1);
        		
        		UShapeProbabilisticNode shape2 = new UShapeProbabilisticNode(m_Canvas, (ProbabilisticNode) node2, (int)node2.getPosition().x-node2.getWidth()/2, (int)node2.getPosition().y-node2.getHeight()/2, node2.getWidth(), node2.getHeight() );
        		m_Canvas.addShape( shape2 );	
        		
            }
        });

        btn = addButton(buttonPanel, "Create Frame");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setFrame(110,110,220,220);
            }
        });
        
        btn = addButton(buttonPanel, "Create Ellipse");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setEllipse(110,110,120,120);
            }
        });
        
        btn = addButton(buttonPanel, "Create RoundRect");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setRoundRect(110,110,120,120);
            }
        });
        
        btn = addButton(buttonPanel, "Create Trapezoid");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setTrapezoid(110,110,120,120);
            }
        });
        
        btn = addButton(buttonPanel, "Select Node");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setState(UCanvas.STATE_NONE);
            }
        });
        
        btn = addButton(buttonPanel, "Connect Line");        
        btn.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		m_Canvas.setState(UCanvas.STATE_CONNECT_COMP);
            }
        }); 
	} 
  
	public JButton addButton(JPanel buttonPanel, String Name) 
  	{
		JButton button = new JButton(Name);
		button.setEnabled(true);
		buttonPanel.add(button );
	    return button;
	}
	

  	//Main
  	public static void main(String argv[]) { 
  		TestUGraphEditor frame = new TestUGraphEditor();
  		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  		frame.setVisible(true);
  		frame.setSize(800, 600);   
  }


	public void windowStateChanged(WindowEvent arg0) {
	 
	 
	}
}
