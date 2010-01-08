package unbbayes.cps.gui;

import java.awt.BorderLayout; 
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent; 

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.cps.CPSCompiler;
import unbbayes.cps.datastructure.CPSNode;
 
public class CPSDialog extends GeneralDialog{

	  public JTextArea resultTextArea; 
	  Action setRun; 
	  Action setCancel;	  
	  JTree  tree; 
	  public CPSCompiler  math;
 	     
	  public CPSDialog() 
	  {
		  super("UWindow");
				
		  math = new CPSCompiler(); 
	  }
	   
	  public void Init() 
	  {  
		  JScrollPane t = createTreePane();
		  JTextPane p = createTextOut(); 
		  JPanel b = createButtonPane();
		
		  getFrame().add(p, BorderLayout.CENTER); 
		  getFrame().add(t, BorderLayout.WEST); 
		  getFrame().add(b, BorderLayout.SOUTH); 
		  
		  mainImpl(); 
	  }
	  
	  public void setScript(String str) 
	  { 
		  resultTextArea.setText(str);
	  }
	  
	  public JTextPane createTextOut() 
	  { 
		  JTextPane textPane = new JTextPane();     	 
		  textPane.setLayout(new BoxLayout(textPane, BoxLayout.X_AXIS)); 
		  resultTextArea = new TextOutArea();
		  resultTextArea.setEditable(true); 
		  textPane.add(new JScrollPane(resultTextArea));
			 
		  return textPane;
	   }
		
	  public JPanel createButtonPane() 
	  {  
		  ButtonGroup group = new ButtonGroup(); 
	 
		  // Text Button Panel
	 	  JPanel ButtonPanel = new JPanel();
	 	  ButtonPanel.setLayout(new BoxLayout(ButtonPanel, BoxLayout.X_AXIS)); 
	 	  ButtonPanel.setBorder(border5); 
	 	   
	 	  
	 	  // Buttons 
	 	  JPanel p2 = createHorizontalPanel(false); 
	 	  ButtonPanel.add(p2); 
	 	  p2.setBorder(new CompoundBorder( new TitledBorder(null, "Compile", 
	 	 									 TitledBorder.LEFT, TitledBorder.TOP), border5)); 
	 	  
	 	  // Add Button
	 	  JButton btn;
	 	  btn = createRunButton(); 
	 	  p2.add(btn); 
	 	  group.add(btn);  
	 	  p2.add(Box.createRigidArea(VGAP5));
	 	  
	 	  btn = createCancelButton(); 
	 	  p2.add(btn); 
	 	  group.add(btn);  
	 	  p2.add(Box.createRigidArea(VGAP5));
	 	  	 	 	 
	 	 return ButtonPanel;
	 }
	 
	 public DefaultMutableTreeNode createEntityTree(CPSNode node)
	 {
		 DefaultMutableTreeNode t = new DefaultMutableTreeNode("Entity");
		 addEntityTree( t, node );
		 		  
		 return t;
	 }
	 
	 public void addEntityTree(DefaultMutableTreeNode target, CPSNode node)
	 {
		 if( node.getType() == null )
			 return;
		 
		 if( !node.getType().equals(CPSNode.NODE_PN) && !node.getType().equals(CPSNode.NODE_ROOT) )
			 return;
		 		
		 DefaultMutableTreeNode temp = null;
		 
		 if( node.getType().equals(CPSNode.NODE_PN) )
			 temp = new DefaultMutableTreeNode("node." + node.getName()); 
		 else 
			 temp = new DefaultMutableTreeNode(node.getName());
		 
		 target.add(temp);
		 
		 for( int i = 0; i < node.getNumberOfChildren(); i++ ){
			 CPSNode c = node.getChild(i);
			 addEntityTree( temp, c );
		 }
	 }
	 
	 public DefaultMutableTreeNode createMathFunctionTree()
	 {
		 DefaultMutableTreeNode t = new DefaultMutableTreeNode("Math Functions");
		 DefaultMutableTreeNode temp = null;   
         
		 temp = new DefaultMutableTreeNode("+"); t.add(temp);
         temp = new DefaultMutableTreeNode("-"); t.add(temp);
         temp = new DefaultMutableTreeNode("*"); t.add(temp);
         temp = new DefaultMutableTreeNode("/"); t.add(temp);
         temp = new DefaultMutableTreeNode("abs()"); t.add(temp); 
         temp = new DefaultMutableTreeNode("sqrt()"); t.add(temp);
         temp = new DefaultMutableTreeNode("log()"); t.add(temp);
         temp = new DefaultMutableTreeNode("cos()"); t.add(temp);
         temp = new DefaultMutableTreeNode("sin()"); t.add(temp);         
		  
		 return t;
	 }
	 
	 public DefaultMutableTreeNode createProbFunctionTree()
	 {
		 DefaultMutableTreeNode t = new DefaultMutableTreeNode("Probability Functions");
		 DefaultMutableTreeNode temp = null;   
         
		 temp = new DefaultMutableTreeNode("Normal()"); t.add(temp);
		 temp = new DefaultMutableTreeNode("Poisson()"); t.add(temp);
		 		  
		 return t;
	 }
	 
	 public DefaultMutableTreeNode createControlFunctionTree()
	 {
		 DefaultMutableTreeNode t = new DefaultMutableTreeNode("Control Functions");
		 DefaultMutableTreeNode temp = null;   
         
		 temp = new DefaultMutableTreeNode("if()"); t.add(temp);
		 temp = new DefaultMutableTreeNode("="); t.add(temp);
		 temp = new DefaultMutableTreeNode(";"); t.add(temp);
		 		  
		 return t;
	 }
	 
	 public JScrollPane createTreePane()  
	 { 
         DefaultMutableTreeNode top = new DefaultMutableTreeNode("CPS"); 

         top.add(createEntityTree(CPSCompiler.nodeRoot));  
         top.add(createControlFunctionTree());
         top.add(createProbFunctionTree());
         top.add(createMathFunctionTree());
                  
         // new tree
	 	 tree = new JTree(top);
	 	 tree.setEditable(false); 
        
	 	 // click tree
	 	 tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if( selPath != null ){
					DefaultMutableTreeNode node =(DefaultMutableTreeNode) selPath.getLastPathComponent();
					if (node.isLeaf()) {	
						
						String type = (String)node.getUserObject();
						 
						resultTextArea.insert( node.toString() , resultTextArea.getText().length() );
						
						System.out.println("  = \n" + node.toString() );
					}
				}
			}
		 });
	 	 
	 	 // Expand all
	 	 for (int i = 0; i < tree.getRowCount(); i++) 
	 		tree.expandRow(i);
		                     
         return new JScrollPane(tree); 
     } 
      
	 public void onCompiled(String error){
		 
	 }
	  
	 public void onClose(){
		 
	 }
 
	 // Button [Ok, Cancel]
	 public JButton createRunButton(){  
	 	setRun = new AbstractAction("Ok"){ 
	 		public void actionPerformed(ActionEvent e) {   
	 			math.Compile(resultTextArea.getText());
	 			onCompiled("ok");
	 		} 
	 	};   	 
	 	return createButton(setRun); 
	 } 
	 
	 public JButton createCancelButton(){  
	 	setCancel = new AbstractAction("Cancel"){ 
	 		public void actionPerformed(ActionEvent e) {   
	 			onClose();
	 		} 
	 	};   	 
	 	return createButton(setCancel); 
	 } 
		
	 
	  /** 
	   * TextArea Pane 
	   */ 
	 class TextOutArea extends JTextArea{ 
		public TextOutArea(){ 
			  super(null, 0, 0); 
			  setEditable(false); 
			  setText(""); 
		} 
 
		public float getAlignmentX (){	 
			  return LEFT_ALIGNMENT; 
		} 
  
		public float getAlignmentY (){	 
			  return TOP_ALIGNMENT; 
		} 
	 }  	

	 public static void main(String[] args) 
	 { 
		 CPSDialog w = new CPSDialog();
		 w.Init();
	 } 
}
