/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.entity.Type;

/**
 * @author Shou Matsumoto
 *
 */
public class LiteralEntityInstance {
	private String instanceName = null;
	private Type type = null;
	
	private LiteralEntityInstance () {
		super();
	}
	
	public static LiteralEntityInstance getInstance ( String instanceName , Type type) {
		LiteralEntityInstance ei = new LiteralEntityInstance();
		ei.setInstanceName(instanceName);
		ei.setType(type);
		return ei;
	}

	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof LiteralEntityInstance)){
			LiteralEntityInstance entityInstance = (LiteralEntityInstance) obj;
		   return ((entityInstance.getInstanceName().equals(this.getInstanceName())) &&
		            (entityInstance.getType().equals(this.getType())));
		}else{
			return false; //obj == null && this != null 
		}
		
	}
	
	public String toString(){
		return "LiteralEntityInstance:" + instanceName + "[" + type + "]";
	}
	
	
}
