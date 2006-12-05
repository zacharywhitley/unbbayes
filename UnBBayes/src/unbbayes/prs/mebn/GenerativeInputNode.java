package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawRoundedRectangle;
import unbbayes.gui.draw.DrawTwoBaseRectangle;

public class GenerativeInputNode extends InputNode {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 7377146558744109802L;
	
	private List<DomainResidentNode> residentNodeChildList;

	private DomainMFrag mFrag;
	
	private DrawTwoBaseRectangle drawInputNode; 
	
	private static Color color = new Color(220, 220, 220); 		
	
	public GenerativeInputNode(String name, DomainMFrag mFrag){
		
	   super(); 
	   setName(name); 
	   setLabel(" "); 
	   
	   this.mFrag = mFrag;
	   
	   residentNodeChildList = new ArrayList<DomainResidentNode>(); 
	
	   size.x = 100;
	   size.y = 20; 
	   drawInputNode = new DrawTwoBaseRectangle(position, size);
	   drawElement.add(drawInputNode);	
	}
	
	
    
	/**
	 * Method responsible for deleting this generative input node. It makes sure to clean 
	 * the residentNodeChildList.
	 *
	 */    
    
    public void delete(){
    
    	for(ResidentNode resident : residentNodeChildList){
    		residentNodeChildList.remove(resident); 
    	}
    	
    	mFrag.removeGenerativeInputNode(this); 
    }
	
	
	
	/**
	 * Remove the node of the resident node child list. 
	 */
	public void removeResidentNodeChild(DomainResidentNode node){
		residentNodeChildList.remove(node);
		node.removeInputNodeFatherList(this); 
		
		mFrag.removeEdgeByNodes(this, node);
		
	}	
	
	public void addResidentNodeChild(DomainResidentNode resident){
		residentNodeChildList.add(resident); 
	}
	
	public List<DomainResidentNode> getResidentNodeChildList(){
		return residentNodeChildList; 
	}
	
	public void setInputInstanceOf(DomainResidentNode residentNode){
		super.setInputInstanceOf(residentNode); 
		residentNode.addInputInstanceFromList(this); 
		updateLabel(); 
	}
	
	public void setInputInstanceOf(BuiltInRV builtInRV){
		super.setInputInstanceOf(builtInRV); 
		builtInRV.addInputInstance(this); 
		updateLabel(); 
	}
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
	
	
	
	
	/*--------------------------------------------------------------*/

	@Override
	public void setSelected(boolean b) {
		drawInputNode.setSelected(b);
		super.setSelected(b);
	}    	
		
	
	/**
     *  Gets all generative input node node's color.
     *
     * @return The color of all generative input node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all generative input node node.
     *
     * @return The new color of all generative input node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }		
	
	@Override
	public void paint(Graphics2D graphics) {
		drawInputNode.setFillColor(getColor());
		super.paint(graphics);
	}	

	/**
	 * Atualiza o texto do label apresentado pelo no... 
	 * O label de um nó de input contem o nome do resident ou 
	 * built in o qual este nó representa.
	 */
	
    public void updateLabel(){
    	
    	Object inputInstanceOf = super.getInputInstanceOf();
    	
    	if(inputInstanceOf != null){
    		if(inputInstanceOf instanceof DomainResidentNode){
    			this.setLabel(((DomainResidentNode)inputInstanceOf).getLabel()); 
    		}
    		else{
    			this.setLabel(((BuiltInRV)inputInstanceOf).getName()); 
        	}
    	}
    	
    }	

	
}
