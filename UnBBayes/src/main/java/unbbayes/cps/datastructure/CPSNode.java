package unbbayes.cps.datastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

import unbbayes.util.datastructure.Node;
import unbbayes.util.datastructure.Tree;
 
/**
 * Non-generic subclass of Tree<Task>
 */
public class CPSNode extends Node<CPSNodeData> {
	
	// calculation methods
	public static String PLUS  	= "PLUS";
	public static String MINUS  	= "MINUS";
	public static String MULTIPLY  = "MULTIPLY";
	public static String DIVIDE  	= "DIVIDE";
	
	// node type
	public static String NODE_ROOT		= "Node Root";
	public static String NODE_PN  		= "Node PN";
	public static String NODE_STATE  	= "Node State";
	
    public CPSNode() {
        super();
    }

    public void setType(String v) {

    	CPSNodeData data = getData();
    	
    	if( data == null ){
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setType(v);
    }
    
    public String getType() {

    	return getData().getType();
    }
    
    public void setValue(String v) {

    	CPSNodeData data = getData();
    	
    	if( data == null ) {
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setValue(v);
    }
    
    public String getValue() {

    	return getData().getValue();
    }
    
    public String getName() {

    	if( getData() == null )
    		return null;
    	
    	return getData().getName();
    }
    
    public void setName(String n) {

    	CPSNodeData data = getData();
    	
    	if( data == null ) {
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setName(n);
    }
    
    
     
    public void setOrigin(String v) {

    	CPSNodeData data = getData();
    	
    	if( data == null ) {
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setOrigin(v);
    }
    
    public String getOrigin() {

    	return getData().getOrigin();
    }
     
    public void setIndex(String v) {

    	CPSNodeData data = getData();
    	
    	if( data == null ) {
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setIndex(v);
    }
    
    public String getIndex() {

    	return getData().getIndex();
    }

    
    public void setNumber(String v) {

    	CPSNodeData data = getData();
    	
    	if( data == null ) {
    		data = new CPSNodeData();
    		setData(data);
    	}
    	
    	getData().setNumber(v);
    }
    
    public String getNumber() {

    	return getData().getNumber();
    }
    
    public CPSNode addChild(String str) {
    	CPSNode nodeC = new CPSNode();
    	nodeC.setName(str);
    	addChild( nodeC);
    	return nodeC;
    }	
     
    public CPSNode getParent() {
    	return (CPSNode) super.getParent();
    }
    
    public CPSNode getChild(int index){
    	 if (this.children == null)
    		 return null;
    	return (CPSNode)super.children.get(index);
    }
    
    public CPSNode findNode( String n )
	{
    	return findNode( this, n );
	}
    
    public CPSNode findNode( CPSNode parent, String n )
	{
		CPSNode r = null; 
		
		if( parent == null )
			return null;
				
		if( parent.getData().getName().equals(n) )
		{	
			return parent; 
		}
		
		List<Node<CPSNodeData>> list = parent.getChildren();
 	    Iterator it = list.iterator();
	    
	    while(it.hasNext())
	    {
	    	CPSNode node = (CPSNode) it.next();
	 		
	    	if( node != null )
	    	{
	    		r =  findNode( node, n );
	    		
	    		if( r != null ) return r;
	    	}
		}
	    
		return r;	
	}    
    
    public String toStringTree() 
    {
    	if( this.getNumberOfChildren() == 0 )
    	{
    		String str = this.getName();
    		
    		if( this.getValue() != null )
    		if( !this.getValue().isEmpty() )
    			str += " = " + this.getValue();
    		
    		return str;
    	}
    	
    	StringBuffer buf = new StringBuffer();
    	
    	buf.append("(");
    	if( this.getName() == null )
    		buf.append("temp");
    	else
    		buf.append(this.getName());
    	buf.append(' ');
    	
    	List<Node<CPSNodeData>> list = getChildren();
 	    Iterator it = list.iterator();
	    
	    while(it.hasNext())
	    {
	    	CPSNode node = (CPSNode) it.next();
    		buf.append(' ');    	
    		buf.append(node.toStringTree());
    	}
    	
    	buf.append(")\n");
    	
    	return buf.toString();
   	}
 
    public String toStringTreeNumber() 
    {
    	StringBuffer buf = new StringBuffer();
    	
    	if( this.getNumberOfChildren() == 0 )
    	{
    		String str = this.getName();
    		
    		if( this.getValue() != null )
    		if( !this.getValue().isEmpty() )
    			str += " = " + this.getValue()+"\n";
    			
    		return str;
    	}
    	 
    	buf.append("(");
    	if( this.getName() == null )
    		buf.append("temp");
    	else
    	{
    		if( this.getNumber() == null )
    		{
    			buf.append(this.getName());
    			buf.append(": ");
    		}
    		else
    			buf.append(this.getNumber());
    	}
    	buf.append(' ');
    	
    	List<Node<CPSNodeData>> list = getChildren();
 	    Iterator it = list.iterator();
	    
	    while(it.hasNext())
	    {
	    	CPSNode node = (CPSNode) it.next();
    		buf.append(' ');    	
    		buf.append(node.toStringTreeNumber());
    			
    	}
	    
	    buf.append(")");
	     
	    	
    	    	
    	return buf.toString();
   	}

    public CPSNode addChildrenAtLast( CPSNode source )
	{  
    	CPSNode nodeT = new CPSNode();
    	
    	for( int i = 0; i < getNumberOfChildren(); i++ )
    	{
    		CPSNode child1 = getChild(i);
    		
    		for( int j = 0; j < source.getNumberOfChildren(); j++ )
        	{   
    			CPSNode child2= source.getChild(j);
    			    			
    			CPSNode newChild = new CPSNode();
    			newChild.assign(child1);
    			CPSNode newChild2 = newChild.lastChild();
    			CPSNode newChild3 = new CPSNode();
    			newChild2.addChild(newChild3);
    			newChild3.assign(child2);
    			
    			nodeT.addChild(newChild);
        	}
    	}
    	
    	return nodeT;
	}
    
    public void assignData(CPSNode source)
	{    	
    	CPSNodeData d = new CPSNodeData();
    	d.assign(source.getData());
    	this.setData(d); 
	}
    
    public void assign(CPSNode source)
	{
    	CPSNodeData d = new CPSNodeData();
    	d.assign(source.getData());
    	this.setData(d);
    	
    	assignChildren(source);
	}
    
    public void assignChildrenWithSubchildren( CPSNode source )
	{ 
    	for( int i = 0; i < source.getNumberOfChildren(); i++ )
    	{
    		CPSNode childSource = source.getChild(i); 
    		CPSNode childNew = new CPSNode();
    		 
    		childNew.assignData(childSource);
    		
    		childNew.setName("%temps1_Del%");
    		 
    		addChild(childNew);
    		
    		childNew.addChild(childSource);
    		
    		this.setName("%temp_T%");
    	}
	}
    
    public void assignChildren( CPSNode source )
	{ 
    	assignChildren( this, source );
    	 
	}
        
    public CPSNode lastChild()
    {
    	if( getNumberOfChildren() == 0 )
    		return this;
    	
    	for( int i = 0; i < getNumberOfChildren(); i++ )
    	{
			 CPSNode child = (CPSNode) getChild(i);
			 return child.lastChild();
    	}
    	
    	return null;
    }
    
	public void assignChildren( CPSNode target, CPSNode source )
	{  
		for( int i = 0; i < source.getNumberOfChildren(); i++ )
    	{
			 CPSNode childS = (CPSNode) source.getChild(i);
			 CPSNode child = new CPSNode();
			 CPSNodeData childData = new CPSNodeData(); 
			 
			 childData.assign(childS.getData());
			 
			 child.setData(childData);
			 
			 target.addChild(child);
			 
			 assignChildren( child, childS );
	     }
	}
	
	public void changeValueAll( String data )
	{
		setValue(data);
		
		for( int i = 0; i < getNumberOfChildren(); i++ )
    	{
			CPSNode child = (CPSNode)getChild(i);
			child.changeValueAll( data );
    	}
	}
	
	public int getDepth()
	{	
		return getDepth(0);
	}
	
	public int getDepth( int depth )
	{	
		if( getNumberOfChildren() >= 1 )
		{
			depth++;
			depth = ((CPSNode)getChild(0)).getDepth( depth );
		}
		
		return depth;
	}
	
	public CPSNode getChildAtDepth( int depth )
	{
		int n = 0;
		return getChildAtDepth( n, depth );
	}
	
	
	public CPSNode getChildAtDepth( int n, int depth )
	{	
		CPSNode child = null;
		n++;
		if( n >= depth )
			return this;
		
		if( getNumberOfChildren() >= 1 )		{
			
			child = ((CPSNode)getChild(0)).getChildAtDepth( n, depth );
		}	
		
		return child;
	}
	 
	public CPSNode changeChildren(int nDepth)
	{    	
    	// new T'
		CPSNode nodeT = new CPSNode();
    	for( int i = 0; i < this.getNumberOfChildren(); i++ )
    	{
    	 	CPSNode child = (CPSNode) getChild(i);   
    		CPSNode child2 = child.getChildAtDepth(nDepth);
  
    		for( int j = 0; j < child2.getNumberOfChildren(); j++ )
        	{
    			CPSNode child3 = (CPSNode) child2.getChild(j); 
        	 
	    		CPSNode childNew = new CPSNode();
	    		childNew.assignData(child3);
	    		nodeT.addChild(childNew);
	    		nodeT.setName("%temps1_Del%");        	
        	}
    	}
    	 
    	// add children
    	for( int i = 0; i < getNumberOfChildren(); i++ )
    	{
    		CPSNode child = (CPSNode)getChild(i);
    		CPSNode child2 = (CPSNode)nodeT.getChild(i);
    		child.changeChildren(child2);
    	} 
		
		return nodeT;
	}
	
	public void changeChildren(CPSNode nodeT)
	{  
		if( !this.getName().equals("%temps1_Del%") )
		{ 
			CPSNode childNew = new CPSNode();
    		childNew.assignData(this);
    		nodeT.addChild(childNew);
		}
		
		for( int i = 0; i < getNumberOfChildren(); i++ )
    	{
    		CPSNode child = (CPSNode)getChild(i);
    		CPSNode child2 = (CPSNode)nodeT.getChild(i);
    		if( child2 == null ) child2 = nodeT;
    		child.changeChildren(child2);
    	} 
	}
	
    public CPSNode changeChildNumber(int nDepth)
	{  
    	CPSNode nodeNew = new CPSNode(); 
    	
    	    	
    	for( int i = 0; i < this.getNumberOfChildren(); i++ )
    	{
    		CPSNode child = (CPSNode) getChild(i);  
    		  
    		for( int j = 0; j < child.getNumberOfChildren(); j++ )
        	{    			
    			CPSNode childSub = (CPSNode) child.getChild(j);
    			
        		CPSNode childNew = new CPSNode();
         		childNew.assignData(childSub);
        		nodeNew.addChild(childNew);
        		
        		CPSNode childNew2 = new CPSNode();
        		childNew2.assignData(child);
        		childNew.addChild(childNew2);
        		
        		childNew2.assignChildren(childSub);
        	}    		
    	} 
    	
    	return nodeNew;
	}    
    
    class MergeList 
    {
    	CPSNode childPrev = null;
    	Double d = 0.0;
    }
    
    public void merge()
	{
    	CPSNode childPrev = null;
    	Double d2 = 0.0;
    	List<MergeList> checkList = new ArrayList<MergeList>();
    	List<Integer> removeList = new ArrayList<Integer>();
    	boolean bCheck = false;
    	int nSize = this.getNumberOfChildren();
    	
    	for( int i = 0; i < nSize; i++ )
    	{
    		CPSNode child = (CPSNode) getChild(i);
    		Double d1 = Double.parseDouble(child.getNumber());
    		bCheck = true;
    		
    		for( int j = 0; j < checkList.size(); j++ )
    		{    		
	    		if( d1.equals(checkList.get(j).d) )
	    		{
	    			checkList.get(j).childPrev.assignChildren(child);
	    				 
	        		removeList.add(i);
	        			
	        		bCheck = false;
	    		}
    		}	
    		
    		if( bCheck == true )
    		{
    			MergeList e = new MergeList();
    			e.childPrev = child;
    			e.d = d1;
    			checkList.add(e);
    		}
    	} 

    	nSize = removeList.size();
    	for( int i = nSize; i > 0;  )
    	{    		
    		i--;
    		this.removeChildAt(removeList.get(i));
    	}
	}
    
    public void calcuate(String strFunction, int nDepth)
	{  
    	for( int i = 0; i < this.getNumberOfChildren(); i++ )
    	{
    	 	CPSNode child = (CPSNode) getChild(i);  
    		
    		Double d1 = Double.parseDouble(child.getNumber());
    		
    		CPSNode child2 = child.getChildAtDepth(nDepth);
    		 
    		CPSNode childSub = (CPSNode) child2.getChild(0);   
    		        		
    		Double d2 = 0.0;
    		
    		if( strFunction.equals(PLUS))
    			d2 = Double.parseDouble(childSub.getNumber()) + d1;
    		else
    		if( strFunction.equals(MINUS))
        		d2 = Double.parseDouble(childSub.getNumber()) - d1;
    		else
    		if( strFunction.equals(MULTIPLY))
        		d2 = Double.parseDouble(childSub.getNumber()) * d1;
    		else
    		if( strFunction.equals(DIVIDE))
        		d2 = Double.parseDouble(childSub.getNumber()) / d1;
    		        	
    		childSub.setNumber(d2.toString());
        	    		
    	}
	}
};

