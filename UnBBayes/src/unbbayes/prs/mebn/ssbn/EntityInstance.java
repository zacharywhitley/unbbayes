/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.entity.Type;

/**
 * @author Shou Matsumoto
 *
 */
public class EntityInstance {
	private String instanceName = null;
	private Type type = null;
	
	private EntityInstance () {
		super();
	}
	
	public static EntityInstance getInstance ( String instanceName , Type type) {
		EntityInstance ei = new EntityInstance();
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
	
	
}
