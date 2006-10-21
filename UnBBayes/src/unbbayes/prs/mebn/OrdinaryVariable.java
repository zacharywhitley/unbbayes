package unbbayes.prs.mebn;

public class OrdinaryVariable {
 
	
	private String name; 
	
	private MFrag mFrag;
	
	public OrdinaryVariable(String name, MFrag mFrag){
		
		this.name = name; 
		this.mFrag = (MFrag)mFrag; 
		
	}
	
	/**
	 * Method responsible for removing this variable from its MFrag.
	 *
	 */
	public void removeFromMFrag() {
		mFrag = null;
	}
	
	/**
	 * Method responsible for return the MFrag where the Ordinary 
	 * Variable are inside.
	 */	
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	 
	/**
	 * Method responsible for return the name of the OV. 
	 */	
	
	public String getName(){
		return name; 
	}
	 	
	
}
 
