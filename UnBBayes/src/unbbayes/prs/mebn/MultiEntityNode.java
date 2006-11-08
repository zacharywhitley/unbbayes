package unbbayes.prs.mebn;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList; 

import unbbayes.prs.Node;

public class MultiEntityNode extends Node {
 
	private static final long serialVersionUID = -5435895970322752281L;

	private MFrag mFrag;
	
	private List<Argument> argumentList;
	 
	private List<MultiEntityNode> innerTermOfList;
	 
	private List<MultiEntityNode> innerTermFromList;


	private static Color color;
	
	/**
	 * Constructs a MultiEntityNode
	 */	
	
	public MultiEntityNode(){
		
		super();
		
		//TODO Melhorar isso!!! 
		// width
		size.x = 100;
		// height
		size.y = 20;		
		
		argumentList = new ArrayList<Argument>(); 
		innerTermOfList = new ArrayList<MultiEntityNode>();
		innerTermFromList = new ArrayList<MultiEntityNode>(); 
	}
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	
	/**
     *  Gets all context node's color.
     *
     * @return The color of all context node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all context node.
     *
     * @return The new color of all context node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }	
	
	/**
	 * Method responsible for removing this node from its MFrag.
	 *
	 */
	public void removeFromMFrag() {
		mFrag = null;
	}
	
	/**
	 * 
	 */	
	
	public void addArgument(Argument arg){
		argumentList.add(arg); 
	}

	public void addInnerTermOfList(MultiEntityNode instance){
		innerTermOfList.add(instance); 
	}	
	
	public void addInnerTermFromList(MultiEntityNode instance){
		innerTermFromList.add(instance); 
	}	
	
	public List<Argument> getArgumentList(){
		return argumentList; 
	}
	
	public List<MultiEntityNode> getInnerTermOfList(){
		return innerTermOfList; 
	}
	
	public List<MultiEntityNode> getInnerTermFromList(){
		return innerTermFromList; 
	}		
}
 
