package unbbayes.cps;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import unbbayes.cps.datastructure.CPSNode;
import unbbayes.cps.datastructure.CPSTree;

public class CPSCompiler {

	public static CommonTree root = null;
	
	public static CPSNode entityRoot = null;
	public static CPSNode nodeRoot = null;
	CPSTree tree = null;
	TempMathFunctions uDist = null;  
	
	public CPSCompiler()
	{ 
		uDist = new TempMathFunctions(); 
		InitNodeTree();
	}
	
	public CPSNode addNode(String n)
	{
		CPSNode newNode = nodeRoot.addChild(n);
		newNode.setType(CPSNode.NODE_PN);;
		
		return newNode;
	}
	
	public  CPSNode addState(CPSNode parentNode, String orinalParentName, String stateIndex,  String stateName, String stateNumber, String stateValue)
	{
		CPSNode newNode = parentNode.addChild(stateName);
		newNode.setType(CPSNode.NODE_STATE);
		newNode.setValue(stateValue);
		newNode.setIndex(stateIndex);
		newNode.setNumber(stateNumber);
		newNode.setOrigin(orinalParentName);
		
		return newNode;
	}
	
	public  CPSNode getNode(String n)
	{
		return nodeRoot.findNode(n);
	}
	
	public  void InitNodeTree()
	{
		tree = new CPSTree();
		entityRoot = new CPSNode();
		
		tree.setRootElement(entityRoot);
		entityRoot.setName("root");
		entityRoot.setType(CPSNode.NODE_ROOT);
		
		nodeRoot = entityRoot.addChild("node");
		nodeRoot.setType(CPSNode.NODE_PN);
	/*	UNode nodeC0 = nodeN.addChild("SafetyPartial");
 		UNode nodeC01 = nodeC0.addChild("s1");
 		nodeC01.setNumber("0");		
		nodeC01.setValue("0.961");
		UNode nodeC02 = nodeC0.addChild("s2");
		nodeC02.setNumber("1");		
		nodeC02.setValue("0.384");
		UNode nodeC03 = nodeC0.addChild("s3");
		nodeC03.setNumber("2");		
		nodeC03.setValue("0.077");
		
		UNode nodeC1 = nodeN.addChild("SafetyPartial1");
		nodeC1.setType(UNode.NODE_PN);
		UNode nodeC11 = nodeC1.addChild("s1");
		nodeC11.setNumber("0");		
		nodeC11.setValue("0.961");
		UNode nodeC12 = nodeC1.addChild("s2");
		nodeC12.setNumber("1");		
		nodeC12.setValue("0.384");
		UNode nodeC13 = nodeC1.addChild("s3");
		nodeC13.setNumber("2");		
		nodeC13.setValue("0.077");
		
		UNode nodeC2 = nodeN.addChild("FieldGoal");
		nodeC2.setType(UNode.NODE_PN);
		UNode nodeC21 = nodeC2.addChild("s1");
		nodeC21.setNumber("0");		
		nodeC21.setValue("0.226");
		UNode nodeC22 = nodeC2.addChild("s2");
		nodeC22.setNumber("1");		
		nodeC22.setValue("0.267");
		UNode nodeC23 = nodeC2.addChild("s3");
		nodeC23.setNumber("2");		
		nodeC23.setValue("0.267");
		UNode nodeC24 = nodeC2.addChild("s4");
		nodeC24.setNumber("3");		
		nodeC24.setValue("0.24");
		 
		UNode nodeC3 = nodeN.addChild("TouchdownNoExtra");
		nodeC3.setType(UNode.NODE_PN);
		UNode nodeC31 = nodeC3.addChild("s1");
		nodeC31.setNumber("0");		
		nodeC31.setValue("0.78");
		UNode nodeC32 = nodeC3.addChild("s2");
		nodeC32.setNumber("1");		
		nodeC32.setValue("0.17");
		UNode nodeC33 = nodeC3.addChild("s3");
		nodeC33.setNumber("2");		
		nodeC33.setValue("0.0394");
		UNode nodeC34 = nodeC3.addChild("s4");
		nodeC34.setNumber("3");		
		nodeC34.setValue("0.0303");
		
		UNode nodeC4 = nodeN.addChild("TouchdownConversion");
		nodeC4.setType(UNode.NODE_PN);
		UNode nodeC41 = nodeC4.addChild("s1");
		nodeC41.setNumber("0");		
		nodeC41.setValue("0.753");
		UNode nodeC42 = nodeC4.addChild("s2");
		nodeC42.setNumber("1");		
		nodeC42.setValue("0.187");
		UNode nodeC43 = nodeC4.addChild("s3");
		nodeC43.setNumber("2");		
		nodeC43.setValue("0.0598"); 
		
		UNode nodeC5 = nodeN.addChild("TouchdownExtra");
		nodeC5.setType(UNode.NODE_PN);
		UNode nodeC51 = nodeC5.addChild("s1");
		nodeC51.setNumber("0");		
		nodeC51.setValue("0.305");
		UNode nodeC52 = nodeC5.addChild("s2");
		nodeC52.setNumber("1");		
		nodeC52.setValue("0.363");
		UNode nodeC53 = nodeC5.addChild("s3");
		nodeC53.setNumber("2");		
		nodeC53.setValue("0.332");  
		
		
		UNode nodeNew0 = nodeN.addChild("Safety");
		nodeNew0.setType(UNode.NODE_PN);
		UNode nodeNew1 = nodeN.addChild("S1");
		nodeNew1.setType(UNode.NODE_PN);
 		UNode nodeNew2 = nodeN.addChild("S2");
 		nodeNew2.setType(UNode.NODE_PN);
	 	UNode nodeNew3 = nodeN.addChild("S3");
	 	nodeNew3.setType(UNode.NODE_PN);
	 	UNode nodeNew4 = nodeN.addChild("S4");
	 	nodeNew4.setType(UNode.NODE_PN);*/
		
	/*	 
	
		*/
		
/*		UNode nodeC0 = nodeN.addChild("a");
		UNode nodeC01 = nodeC0.addChild("1");
		nodeC01.setNumber("1");		
		nodeC01.setValue("0.5");
		UNode nodeC02 = nodeC0.addChild("2");
		nodeC02.setNumber("2");		
		nodeC02.setValue("0.5");

		UNode nodeC1 = nodeN.addChild("b");
		UNode nodeC11 = nodeC1.addChild("3");
		nodeC11.setNumber("3");		
		nodeC11.setValue("0.6");
		UNode nodeC12 = nodeC1.addChild("4");
		nodeC12.setNumber("4");		
		nodeC12.setValue("0.4");
		
		UNode nodeC2 = nodeN.addChild("c");
		UNode nodeC21 = nodeC2.addChild("5");
		nodeC21.setNumber("5");		
		nodeC21.setValue("0.7");
		UNode nodeC22 = nodeC2.addChild("6");
		nodeC22.setNumber("6");		
		nodeC22.setValue("0.3");
		
		UNode nodeC3 = nodeN.addChild("d");*/
		
		/*
		UNode nodeC0 = nodeN.addChild("Game1");
		UNode nodeC01 = nodeC0.addChild("Score3");
		nodeC01.setNumber("3");		
		nodeC01.setValue("0.5");
		UNode nodeC02 = nodeC0.addChild("Score6");
		nodeC02.setNumber("6");		
		nodeC02.setValue("0.3");
		UNode nodeC03 = nodeC0.addChild("Score7");
		nodeC03.setNumber("7");		
		nodeC03.setValue("0.15");
		UNode nodeC04 = nodeC0.addChild("Score8");
		nodeC04.setNumber("8");		
		nodeC04.setValue("0.05");
		
		UNode nodeC1 = nodeN.addChild("Game2");
		UNode nodeC11 = nodeC1.addChild("Score3");
		nodeC11.setNumber("3");		
		nodeC11.setValue("0.6");
		UNode nodeC12 = nodeC1.addChild("Score6");
		nodeC12.setNumber("6");		
		nodeC12.setValue("0.2");
		UNode nodeC13 = nodeC1.addChild("Score7");
		nodeC13.setNumber("7");		
		nodeC13.setValue("0.15");
		UNode nodeC14 = nodeC1.addChild("Score8");
		nodeC14.setNumber("8");		
		nodeC14.setValue("0.05");*/
				
	 	String str = ("Internal Nodes = " + nodeRoot.toStringTree() );
	  
			System.out.println(str);
	}
	/*
	public void compileNumericalState( UNode node, UNode node )
	{
		return 0.5f;
	} 
	*/
	public float normalDist( float x, float a, float b )
	{
		return 0.5f;
	} 
	 
	//////
	public CPSNode getFirstParent( CPSNode source )
	{
		CPSNode parent = source;
		
		if( source.getParent() == null )
			return source; 
		
		while( parent != null )
		{		
			if( parent.getName().equals("SLIST") || parent.getName().equals("root") )
				return parent;
			
			parent = parent.getParent();
		}
		
		return null;
	}  
	
	public CommonTree getFirstChild( CommonTree source )
	{
		CommonTree parent = source;
		
		if( source.getChildCount() == 0 )
			return source; 
		
		return getFirstChild( (CommonTree) parent.getChild(0) );
	}  
	
	public CPSNode getVariable( CPSNode node, CommonTree source )
	{
		CPSNode parent   = null;
		CPSNode nodeVar  = null;
		String dataStr = null; 
	
		parent  = getFirstParent( node );
		nodeVar = FindVariablePositionNodeTree( parent, source );
		
		while( nodeVar == null )
		{				
			if( parent.getParent() == null )
				break;
			
			parent  = getFirstParent( (CPSNode) parent.getParent() );
			nodeVar = FindVariablePositionNodeTree( parent, source );
		}
			
		return nodeVar;
	}  
	
	public Object getData( CPSNode node, CommonTree source )
	{ 
		String dataStr = null;
		CPSNode nodeVar  = getVariable( node, source );

		if( nodeVar != null )
			dataStr = nodeVar.getValue(); 
		
		if( dataStr == null )
			dataStr = source.getText(); 
		
		return dataStr;
	}
	
	public void setData( CPSNode node, String s )
	{
		if( node!= null )
			node.setValue(s); 
	
	} 
	  
	
	//Check CommonTree has "node."
	public boolean isNodeState( CommonTree parent )
	{
		CommonTree child = getFirstChild(parent);
				
		if( child.getText().equals("node") && !parent.getText().equals("node") )
			return true;
		
		return false;
	}
	
	//Check CommonTree has "+-/"
	public boolean isFunction( String str )
	{
		if( str.equals("+") ||
			str.equals("-") ||
			str.equals("/") ||
			str.equals("*") 	
		)
			return true;
		
		return false;
	}
 
	public void combineState( CPSNode node, CPSNode node1, CPSNode node2 )
	{
		int n1 = node1.getNumberOfChildren();
		int n2 = node2.getNumberOfChildren();
		
		//create new states
		List<Integer> listState = new ArrayList<Integer>();
					 
		for( Integer i = 0; i < n1; i++ )
		{
			CPSNode nodeI = node1.getChild(i);
			
			for( Integer j = 0; j < n2; j++ )
			{
				CPSNode nodeJ = node2.getChild(j);
				 
				Integer number = Integer.parseInt(nodeI.getNumber()) + Integer.parseInt(nodeJ.getNumber());
				
				if( !listState.contains(number) )
					listState.add( number );
			}
		}
		
		for( Integer k = 0; k < listState.size(); k++ )
		{
			CPSNode nodeN = node.addChild(listState.get(k).toString());
			nodeN.setNumber(listState.get(k).toString());
			
			for( Integer i = 0; i < n1; i++ )
			{
				CPSNode nodeI = node1.getChild(i);
				
				CPSNode nodeN1 = nodeN.addChild(nodeI.getNumber());
				
				for( Integer j = 0; j < n2; j++ )
				{
					CPSNode nodeJ = node2.getChild(j);
					CPSNode nodeN2 = nodeN1.addChild(nodeJ.getNumber());
					
					Integer number1 = Integer.parseInt(nodeN.getNumber());
					Integer number2 = Integer.parseInt(nodeI.getNumber()) + Integer.parseInt(nodeJ.getNumber());
					
					if( number1 == number2 )
						nodeN2.setValue("1.0");
					else
						nodeN2.setValue("0.0");
					
				}
			}
		}
		
		System.out.println("nodetree = \n" + node.toStringTree() ); 
	}
	
	public CPSNode FindVariablePositionNodeTree( CPSNode parent, CommonTree source )
	{
		CPSNode r = parent; 
		int s = source.getChildCount();
		
		for( int i = 0; i < s; i++ )
		{	 
			CommonTree child = (CommonTree) source.getChild(i);
			r = FindVariablePositionNodeTree( parent, child );
		}
	 
        r = parent.findNode( r, source.toString() );
		
		return r;	
	}  
	
	public Object run( CommonTree parent, CPSNode node)
	{
		Object resultChild[] = new Object[10];
		Object resultThis = null;
		int reCount = 0;
		
		int s = parent.getChildCount();
		 
		for( int i = 0; i < s; i++ )
		{		 
			CommonTree child = (CommonTree) parent.getChild(i);
		
			resultChild[reCount++] = run(child, node);
		}
		
		System.out.println(parent.toString() ); 
		
		Object n0 = (Object)resultChild[0];
		Object n1 = (Object)resultChild[1];
		
		if( parent.toString().equals("=") )
		{ 
			//node state operation
			if( n0 instanceof CPSNode && n1 instanceof CPSNode  ) 
			{  
				CPSNode nodeVar = node.findNode( ((CPSNode)n0).getName() );
				nodeVar.assignChildren(((CPSNode)n1));
				nodeVar.changeValueAll("1");
				String str = ("result = \n" + ((CPSNode)node).toStringTreeNumber() ); 
				System.out.println(str);
			}
			else
			{
				CPSNode 		nodeTreeV   	= null;
				String 		dataString	 	= null;
				 
				//************************
				//Change this code for using "result"
		//		nodeTreeV = getVariable( node, vari );			 
		//		setData(nodeTreeV, result.toString());
			}
		}
		else
		if( parent.toString().equals("+") ||
			parent.toString().equals("*") ||
			parent.toString().equals("-") ||
			parent.toString().equals("/") )
		{			 
			//node state operation
			if( n0 instanceof CPSNode || n1 instanceof CPSNode  ) 
			{  							
				//Step 0
				if( !(n0 instanceof CPSNode) ){
					CPSNode n = new CPSNode();
					CPSNode c = new CPSNode();
					n.setName("N");
					c.setName("%temps1_Del%");
					c.setValue((String)n0);
					c.setNumber((String)n0);
					n.addChild(c);
					n0 = n;
				}
				
				if( !(n1 instanceof CPSNode) ){
					CPSNode n = new CPSNode();
					CPSNode c = new CPSNode();
					n.setName("N");
					c.setName("%temps1_Del%");
					c.setValue((String)n1);
					c.setNumber((String)n1);
					n.addChild(c);
					n1 = n;
				}
								
			//	System.out.println("n0 = \n" + ((UNode)n0).toStringTreeNumber() );
			//	System.out.println("n1 = \n" + ((UNode)n1).toStringTreeNumber() );
				
				resultThis = new CPSNode();
				((CPSNode)resultThis).setName(parent.getText());
				
				//Step 1
				((CPSNode)resultThis).assignChildrenWithSubchildren( ((CPSNode)n0) );  
			//	System.out.println("Step 1 = \n" + ((UNode)resultThis).toStringTreeNumber() );  
				
				//Step 2
				int nDepth = ((CPSNode)n1).getDepth();
				resultThis = ((CPSNode)n1).addChildrenAtLast(((CPSNode)resultThis));  
			//	System.out.println("Step 2 = \n" + ((UNode)resultThis).toStringTreeNumber() ); 
				
				//Step 3
				if( parent.toString().equals("+") )
					((CPSNode)resultThis).calcuate(CPSNode.PLUS, nDepth);
				else if( parent.toString().equals("-") )
					((CPSNode)resultThis).calcuate(CPSNode.MINUS, nDepth);
				else if( parent.toString().equals("*") )
					((CPSNode)resultThis).calcuate(CPSNode.MULTIPLY, nDepth);
				else if( parent.toString().equals("/") )
					((CPSNode)resultThis).calcuate(CPSNode.DIVIDE, nDepth);
			//	System.out.println("Step 3 = \n" + ((UNode)resultThis).toStringTreeNumber() );
				
				//Step 4
				resultThis = ((CPSNode)resultThis).changeChildren(nDepth);
			//	System.out.println("Step 4 = \n" + ((UNode)resultThis).toStringTreeNumber() );
			
			} 
			else
			{//number operation 
		//		Object a0 = getData(node, c0);
		//		Object a1 = getData(node, c1);
		//		Object a2 = Double.parseDouble((String) a0) + Double.parseDouble((String) a1);
		//		parent.getToken().setText(a2.toString());
			}
		}
		else
	 	//No child, do this
		{
			if( parent.parent == null )
				return null;
			 
			//node state operation
			if( isNodeState(parent) )
			{				
				resultThis = new CPSNode();
				((CPSNode)resultThis).setName(parent.getText()); 
				
				((CPSNode)resultThis).assignChildren( getVariable(node, parent) );
				System.out.println("nodetree = \n" + ((CPSNode)resultThis).toStringTreeNumber() );
				/*if( isFirstNodeState(parent) )
				{
					//Step 1
					((UNode)resultThis).assignChildrenWithSubchildren( getVariable(node, parent) );  	
					System.out.println("nodetree = \n" + ((UNode)resultThis).toStringTreeNumber() );
				}
				else
				{
					//Step 1
					((UNode)resultThis).assignChildren( getVariable(node, parent) );   
				}*/
			}
			else
			{
				resultThis = getData(node, parent);
			}
		} 
		
		return resultThis;
		
		/*
		if( parent.toString().equals("SLIST") )
		{
			node.removeAll();
		}
		
		if( parent.toString().equals("VAR") )
		{
			CommonTree type = (CommonTree)parent.getChild(0);
			CommonTree vari = (CommonTree)parent.getChild(1);
			CommonTree data = (CommonTree)parent.getChild(2);
			
			UNode nodeVAR = node.addChild(vari.toString());
			nodeVAR.setValue(data.toString());
			nodeVAR.setType(type.toString());
		}
		
		if( parent.toString().equals("=") )
		{
			CommonTree vari = (CommonTree)parent.getChild(0);
			CommonTree data = (CommonTree)parent.getChild(1);
			UNode 		nodeTreeV   	= null;
			String 		dataString	 	= null;
			 
			//************************
			//Change this code for using "result"
			nodeTreeV = getVariable( node, vari );			
		 	//dataString = (String)getData( node, data );  
			setData(nodeTreeV, result.toString());
		}
		
		if( parent.toString().equals("+") )
		{			
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
					
			if( isFunction(c0.getText()) )
			{
				UNode resultNode = (UNode)result;
				
				
				if( resultNode== null )
				{
					resultNode = new UNode();
					resultNode.setName(parent.getText());
				}
				
				UNode resultNew = new UNode();
				resultNew.setName(parent.getText());
												
				//Step 1
			//	resultNew.assignChildrenWithSubchildren( getVariable(node, c1) ); 	
				
				//Step 2
				resultNew.assignChildren( getVariable(node, c1) );
				resultNew.addChildrenAtLast(resultNode); 
				
				//Step 3
				resultNew.calcuateAdd(); 
				 
				//Step 4
				resultNode = resultNew.changeChildNumber();
				 
				//Step 5
				resultNode.merge();
				System.out.println("nodetree = \n" + resultNode.toStringTreeNumber() );

				return resultNode;
			} 
			else
			//node state operation
			if( isNodeState(c0) )
			{
				UNode resultNode = new UNode();
				resultNode.setName(parent.getText());
				
				UNode tempNode = new UNode();
				tempNode.setName("temp");
								
				//Step 1
				tempNode.assignChildrenWithSubchildren( getVariable(node, c0) ); 						
				//Step 2
				resultNode.assignChildren( getVariable(node, c1) );
				resultNode.addChildrenAtLast(tempNode); 
				
				//Step 3
				resultNode.calcuateAdd(); 
				 
				//Step 4
				resultNode = resultNode.changeChildNumber();
				
				//Step 5
				resultNode.merge();
				System.out.println("nodetree = \n" + resultNode.toStringTreeNumber() );
				

				return resultNode;
			}
		
			else
			{//number operation 
				Object a0 = getData(node, c0);
				Object a1 = getData(node, c1);
				Object a2 = Double.parseDouble((String) a0) + Double.parseDouble((String) a1);
				parent.getToken().setText(a2.toString());
			}
		}
		
		if( parent.toString().equals("-") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			
			// -1 or -3.33
			if( c1 == null ){
				Object a0 = getData(node, c0);
				return -Double.parseDouble((String) a0);
			}
							
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) - Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		}
		
		if( parent.toString().equals("*") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) * Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		}
		
		if( parent.toString().equals("/") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) / Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		} 
		
		if( parent.toString().equals("EXP") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			Object a0 = getData(node, c0);
			if( a0.toString().equals("false") )
				return 1;
		} 
		
		if( parent.toString().equals("&&") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			if( a0.toString().equals("true") && a1.toString().equals("true"))
				parent.getToken().setText("true");
			else
				parent.getToken().setText("false");
		} 
		
		if( parent.toString().equals("||") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			if( a0.toString().equals("true") || a1.toString().equals("true"))
				parent.getToken().setText("true");
			else
				parent.getToken().setText("false");
		} 
		
		if( parent.toString().equals("==") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			if( a0.toString().equals(a1.toString()) )
				parent.getToken().setText("true");
			else
			{	
				double d1 = 0;
				double d2 = 0;
				
				try{ d1 = Double.parseDouble(a0.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
				
				try{ d2 = Double.parseDouble(a1.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
				
				if( d1 == d2 )
					parent.getToken().setText("true");
			}
		} 
		
		if( parent.toString().equals("!=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			if( !a0.toString().equals(a1.toString()) )
				parent.getToken().setText("true");
			else
			{			
				double d1 = 0;
				double d2 = 0;
				
				try{ d1 = Double.parseDouble(a0.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
				
				try{ d2 = Double.parseDouble(a1.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
				
				if( d1 != d2 )
					parent.getToken().setText("true");
				else
					parent.getToken().setText("false");
			}
		} 
		 
		if( parent.toString().equals(">") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 > d2 )
				parent.getToken().setText("true");
		} 
		
		if( parent.toString().equals(">=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 >= d2 )
				parent.getToken().setText("true");
		} 
		
		if( parent.toString().equals("<") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			  
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 < d2 )
				parent.getToken().setText("true");
	 
		} 
		
		if( parent.toString().equals("<=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			 
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 <= d2 )
				parent.getToken().setText("true"); 
		} 
		  
		if( parent.toString().equals("print") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
			System.out.println("Print(" + c0.toString() +") -> " + a0);
		}  
		
		if( parent.toString().equals("abs") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.abs( (Double)result );
 			else result = Math.abs(Double.parseDouble((String) a0));			 
		}
		 */ 
	}
	
	public Object run2( CommonTree parent, CPSNode node, Object r )
	{
		Object result = r; 
		
		int s = parent.getChildCount();
		
		System.out.println(parent.toString() ); 
		
		if( parent.toString().equals("SLIST") )
		{
			CPSNode nodeSlist = node.addChild(parent.toString());
			node = nodeSlist;
		}
		
		for( int i = 0; i < s; i++ )
		{		 
			CommonTree child = (CommonTree) parent.getChild(i);
		
			//i += run(child, node);
			result = run2(child, node, result);
		}
		
		if( parent.toString().equals("SLIST") )
		{
			node.removeAll();
		}
		
		if( parent.toString().equals("VAR") )
		{
			CommonTree type = (CommonTree)parent.getChild(0);
			CommonTree vari = (CommonTree)parent.getChild(1);
			CommonTree data = (CommonTree)parent.getChild(2);
			
			CPSNode nodeVAR = node.addChild(vari.toString());
			nodeVAR.setValue(data.toString());
			nodeVAR.setType(type.toString());
		}
		
		if( parent.toString().equals("=") )
		{
			CommonTree vari = (CommonTree)parent.getChild(0);
			CommonTree data = (CommonTree)parent.getChild(1);
			CPSNode 		nodeTreeV   	= null;
			String 		dataString	 	= null;
			 
			//************************
			//Change this code for using "result"
			nodeTreeV = getVariable( node, vari );			
		 	//dataString = (String)getData( node, data );  
			setData(nodeTreeV, result.toString());
		}
		
		if( parent.toString().equals("+") )
		{			
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
					
			if( isFunction(c0.getText()) )
			{
				CPSNode resultNode = (CPSNode)result;
				
				if( resultNode== null )
				{
					resultNode = new CPSNode();
					resultNode.setName(parent.getText());
				}
				
				CPSNode resultNew = new CPSNode();
				resultNew.setName(parent.getText());
												
				//Step 1
			//	resultNew.assignChildrenWithSubchildren( getVariable(node, c1) ); 	
				
				//Step 2
				resultNew.assignChildren( getVariable(node, c1) );
				resultNew.addChildrenAtLast(resultNode); 
				
				//Step 3
		//		resultNew.calcuateAdd(); 
				 
				//Step 4
				//resultNode = resultNew.changeChildNumber();
				 
				//Step 5
				resultNode.merge();
				System.out.println("nodetree = \n" + resultNode.toStringTreeNumber() );

				return resultNode;
			} 
			else
			//node state operation
			if( isNodeState(c0) )
			{
				CPSNode resultNode = new CPSNode();
				resultNode.setName(parent.getText());
				
				CPSNode tempNode = new CPSNode();
				tempNode.setName("temp");
								
				//Step 1
				tempNode.assignChildrenWithSubchildren( getVariable(node, c0) ); 						
				//Step 2
				resultNode.assignChildren( getVariable(node, c1) );
				resultNode.addChildrenAtLast(tempNode); 
				
				//Step 3
		//		resultNode.calcuateAdd(); 
				 
				//Step 4
			//	resultNode = resultNode.changeChildNumber();
				
				//Step 5
				resultNode.merge();
				System.out.println("nodetree = \n" + resultNode.toStringTreeNumber() );
				

				return resultNode;
			}
		
			else
			{//number operation 
				Object a0 = getData(node, c0);
				Object a1 = getData(node, c1);
				Object a2 = Double.parseDouble((String) a0) + Double.parseDouble((String) a1);
				parent.getToken().setText(a2.toString());
			}
		}
		
		if( parent.toString().equals("-") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			
			// -1 or -3.33
			if( c1 == null ){
				Object a0 = getData(node, c0);
				return -Double.parseDouble((String) a0);
			}
							
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) - Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		}
		
		if( parent.toString().equals("*") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) * Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		}
		
		if( parent.toString().equals("/") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			Object a2 = Double.parseDouble((String) a0) / Double.parseDouble((String) a1);
			parent.getToken().setText(a2.toString());
		} 
		
		if( parent.toString().equals("EXP") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			Object a0 = getData(node, c0);
			if( a0.toString().equals("false") )
				return 1;
		} 
		
		if( parent.toString().equals("&&") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			if( a0.toString().equals("true") && a1.toString().equals("true"))
				parent.getToken().setText("true");
			else
				parent.getToken().setText("false");
		} 
		
		if( parent.toString().equals("||") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			if( a0.toString().equals("true") || a1.toString().equals("true"))
				parent.getToken().setText("true");
			else
				parent.getToken().setText("false");
		} 
		
		if( parent.toString().equals("==") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			if( a0.toString().equals(a1.toString()) )
				parent.getToken().setText("true");
			else
			{	
				double d1 = 0;
				double d2 = 0;
				
				try{ d1 = Double.parseDouble(a0.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
				
				try{ d2 = Double.parseDouble(a1.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
				
				if( d1 == d2 )
					parent.getToken().setText("true");
			}
		} 
		
		if( parent.toString().equals("!=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			if( !a0.toString().equals(a1.toString()) )
				parent.getToken().setText("true");
			else
			{			
				double d1 = 0;
				double d2 = 0;
				
				try{ d1 = Double.parseDouble(a0.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
				
				try{ d2 = Double.parseDouble(a1.toString()); } 
				catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
				
				if( d1 != d2 )
					parent.getToken().setText("true");
				else
					parent.getToken().setText("false");
			}
		} 
		 
		if( parent.toString().equals(">") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 > d2 )
				parent.getToken().setText("true");
		} 
		
		if( parent.toString().equals(">=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 >= d2 )
				parent.getToken().setText("true");
		} 
		
		if( parent.toString().equals("<") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			  
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 < d2 )
				parent.getToken().setText("true");
	 
		} 
		
		if( parent.toString().equals("<=") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			Object a0 = getData(node, c0);
			Object a1 = getData(node, c1);
			 
			double d1 = 0;
			double d2 = 0;
			
			try{ d1 = Double.parseDouble(a0.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0; }
			
			try{ d2 = Double.parseDouble(a1.toString()); } 
			catch( NumberFormatException e){ parent.getToken().setText("false"); return 0;}
			
			if( d1 <= d2 )
				parent.getToken().setText("true"); 
		} 
		  
		if( parent.toString().equals("print") )
		{
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
			System.out.println("Print(" + c0.toString() +") -> " + a0);
		}  
		
		if( parent.toString().equals("abs") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.abs( (Double)result );
 			else result = Math.abs(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("cos") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.cos( (Double)result );
 			else result = Math.cos(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("sin") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.sin( (Double)result );
 			else result = Math.sin(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("tan") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.tan( (Double)result );
 			else result = Math.tan(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("asin") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.asin( (Double)result );
 			else result = Math.asin(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("acos") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.acos( (Double)result );
 			else result = Math.acos(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("atan") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.atan( (Double)result );
 			else result = Math.atan(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("log") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.log( (Double)result );
 			else result = Math.log(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("sqrt") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.sqrt( (Double)result );
 			else result = Math.sqrt(Double.parseDouble((String) a0));			 
		} 
		
		if( parent.toString().equals("exp") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = Math.exp( (Double)result );
 			else result = Math.exp(Double.parseDouble((String) a0));			 
		} 
		 
		if( parent.toString().equals("expRand") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
 			Object a0 = getData(node, c0);
 			if( ((String) a0).equals("-") )	result = uDist.exponential( (Double)result );
 			else result = uDist.exponential((Double.parseDouble((String) a0)));			 
		} 
		
		if( parent.toString().equals("uniRand") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
 			Object a0 = getData(node, c0);
 			Object a1 = getData(node, c1);
 			result = uDist.uniform(Double.parseDouble((String) a0), Double.parseDouble((String) a1)); 			 
		}
		
		if( parent.toString().equals("normRand") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
 			Object a0 = getData(node, c0);
 			Object a1 = getData(node, c1);
 			result = uDist.normal(Double.parseDouble((String) a0), Double.parseDouble((String) a1));			 
		}
		
		if( parent.toString().equals("triRand") ){
			CommonTree c0 = (CommonTree)parent.getChild(0);
			CommonTree c1 = (CommonTree)parent.getChild(1);
			CommonTree c2 = (CommonTree)parent.getChild(2);
 			Object a0 = getData(node, c0);
 			Object a1 = getData(node, c1);
 			Object a2 = getData(node, c2);
 			result = uDist.triangular(Double.parseDouble((String) a0), Double.parseDouble((String) a1), Double.parseDouble((String) a2));			 
		}
		 
		return result;
	}
	
	public void Compile(String strInput)
	{
		 DistributionLexer lex;
		 ANTLRStringStream stream = new ANTLRStringStream(strInput); 
		 lex = new DistributionLexer(stream);
		 CommonTokenStream tokens = new CommonTokenStream(lex);
		 DistributionParser g = new DistributionParser(tokens, null);
		 try {
		    	DistributionParser.program_return parserResult = g.program();
		    	
		    	root = (CommonTree)parserResult.getTree(); 
		    	
		     	run(root, nodeRoot);
		 } 
		 catch (RecognitionException e) 
		 {
		     e.printStackTrace();
		 }	      
	}
	
	public static void main(String args[]) throws Exception {
            	
        	CPSCompiler math = new CPSCompiler();
        	
        	// Test( "node.C0 = node.C1 + 6*node.C2;" )
        	/*
        	UNode c0 = math.addNode("C0");
        	UNode c1 = math.addNode("C1");
        	math.addState(c1, "C1", "0", "state0", "0", "0.0");
        	math.addState(c1, "C1", "1", "state1", "1", "0.0");
        	UNode c2 = math.addNode("C2");
        	math.addState(c2, "C2", "0", "state0", "0", "0.0");
        	math.addState(c2, "C2", "1", "state1", "1", "0.0");
        	math.Compile("node.C0 = node.C1 + 6*node.C2;");
        	*/
        	
        	/*
        	
        	UNode nodeC0 = nodeN.addChild("a");
    		UNode nodeC01 = nodeC0.addChild("s1");
    		nodeC01.setNumber("1");		
    		nodeC01.setValue("0.5");
    		UNode nodeC02 = nodeC0.addChild("s2");
    		nodeC02.setNumber("2");		
    		nodeC02.setValue("0.5");

    		UNode nodeC1 = nodeN.addChild("b");
    		UNode nodeC11 = nodeC1.addChild("s3");
    		nodeC11.setNumber("3");		
    		nodeC11.setValue("0.6");
    		UNode nodeC12 = nodeC1.addChild("s4");
    		nodeC12.setNumber("4");		
    		nodeC12.setValue("0.4");
    		
    		UNode nodeC2 = nodeN.addChild("c");
    		UNode nodeC21 = nodeC2.addChild("s5");
    		nodeC21.setNumber("5");		
    		nodeC21.setValue("0.6");
    		UNode nodeC22 = nodeC2.addChild("s6");
    		nodeC22.setNumber("6");		
    		nodeC22.setValue("0.4");
    		
    		UNode nodeC3 = nodeN.addChild("d");
    		UNode nodeC31 = nodeC3.addChild("s7");
    		nodeC31.setNumber("7");		
    		nodeC31.setValue("0.7");
    		UNode nodeC32 = nodeC3.addChild("s8");
    		nodeC32.setNumber("8");		
    		nodeC32.setValue("0.3");
    		
    		UNode nodeC4 = nodeN.addChild("k");*/
	}
     
}
