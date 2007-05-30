package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import unbbayes.draw.DrawTwoBaseRectangle;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.CycleFoundException;

/**
 * Generative Input Node
 * 
 * Notes: 
 *   
 */

public class GenerativeInputNode extends InputNode {
	
	private static final long serialVersionUID = 7377146558744109802L;
	
	private List<DomainResidentNode> residentNodeChildList;
	
	private DomainMFrag mFrag;
	
	private DrawTwoBaseRectangle drawInputNode; 
	
	private static Color color = new Color(220, 220, 220); 		
	
	private ResidentNodePointer residentNodePointer; 
	
	/**
	 * @param name Name of the GenerativeInputNode
	 * @param mFrag where this node is in 
	 */
	
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
		
		while(!residentNodeChildList.isEmpty()){
			DomainResidentNode resident = residentNodeChildList.get(0); 
			this.removeResidentNodeChild(resident); 
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
		resident.addInputNodeFather(this); 
	}
	
	public List<DomainResidentNode> getResidentNodeChildList(){
		return residentNodeChildList; 
	}
	
	/**
	 * 
	 * @param residentNode
	 */
	public void setInputInstanceOf(DomainResidentNode residentNode) throws CycleFoundException{
		
		for(ResidentNode resident: residentNodeChildList){
			if(ConsistencyUtilities.hasCycle(residentNode, (DomainResidentNode)resident)){
				throw new CycleFoundException(); 
			}
		}
		
		super.setInputInstanceOf(residentNode); 
		residentNodePointer = new ResidentNodePointer(residentNode, this);
		residentNode.addInputInstanceFromList(this); 
		updateLabel(); 
		
	}
	
	/**
	 * Update the resident node pointer. 
	 * This is necessary if the list of arguments of the resident pointed for
	 * this pointer change. After the update the list of arguments will 
	 * be empty.
	 */
	public void updateResidentNodePointer(){
		if (residentNodePointer != null){
			residentNodePointer = new ResidentNodePointer(residentNodePointer.getResidentNode(), this); 
		}
	}
	
	
	public void setInputInstanceOf(BuiltInRV builtInRV){
		super.setInputInstanceOf(builtInRV); 
		builtInRV.addInputInstance(this); 
		updateLabel();
		
		residentNodePointer = null; 	
	}
	
	public void setInputInstanceOf(){
		super.setInputInstanceOf(); 
		updateLabel(); 
		
		residentNodePointer = null; 
		
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
				ResidentNodePointer pointer = getResidentNodePointer();
				String newLabel = ""; 
				
				newLabel+= pointer.getResidentNode().getName(); 
				newLabel+= "("; 
				
				for(OrdinaryVariable ov: pointer.getOrdinaryVariableList()){
					
					if(ov!=null) newLabel+= ov.getName(); 
					else newLabel+= " ";
					
					newLabel+= ","; 
				}
				
				//delete the last virgle
				if(pointer.getOrdinaryVariableList().size() > 0){
					newLabel = newLabel.substring(0, newLabel.length() - 1); 
				}
				
				newLabel+= ")"; 
				
				setLabel(newLabel); 
			}
			else{
				setLabel(((BuiltInRV)inputInstanceOf).getName()); 
			}
		}
		else{
			setLabel(" "); 
		}
	}	
	
	public Vector<OrdinaryVariable> getOrdinaryVariableList() {
		return residentNodePointer.getOrdinaryVariableList();
	}
	
	public Vector<Type> getTypesOfOrdinaryVariableList() {
		return residentNodePointer.getTypesOfOrdinaryVariableList(); 
	}
	
	public ResidentNodePointer getResidentNodePointer() {
		return residentNodePointer;
	}
	
}
