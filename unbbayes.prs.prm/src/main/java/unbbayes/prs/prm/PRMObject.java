/**
 * 
 */
package unbbayes.prs.prm;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link IPRMObject}
 * @author Shou Matsumoto
 *
 */
public class PRMObject implements IPRMObject {

	
	
	private Map<IAttributeDescriptor, IAttributeValue> attributeValueMap;
	private IPRMClass prmClass;

	/**
	 * At least one constructor is visible for subclasses to allow inheritance
	 */
	protected PRMObject() {
		this.attributeValueMap = new HashMap<IAttributeDescriptor, IAttributeValue>();
	}
	
	/**
	 * Default construction method using fields
	 * @param prmClass
	 * @return
	 */
	public static PRMObject newInstance(IPRMClass prmClass) {
		PRMObject ret = new PRMObject();
		ret.setPRMClass(prmClass);
		ret.initValues();
		return ret;
	}

	/**
	 * Initialize {@link IAttributeValue} of {@link #getAttributeValueMap()}
	 */
	protected void initValues() {
		if (this.getPRMClass() == null) {
			// cannot extract prm class -> cannot extract attributes. Abort
			return;
		}
		// fill default values = null
		for (IAttributeDescriptor attribute : this.getPRMClass().getAttributeDescriptors()) {
			this.getAttributeValueMap().put(attribute, AttributeValue.newInstance(this, attribute));
		}
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMObject#getAttributeValueMap()
	 */
	public Map<IAttributeDescriptor, IAttributeValue> getAttributeValueMap() {
		return this.attributeValueMap;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMObject#getPRMClass()
	 */
	public IPRMClass getPRMClass() {
		return this.prmClass;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMObject#setAttributeValueMap(java.util.Map)
	 */
	public void setAttributeValueMap(
			Map<IAttributeDescriptor, IAttributeValue> attributeValueMap) {
		this.attributeValueMap = attributeValueMap;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMObject#setPRMClass(unbbayes.prs.prm.IPRMClass)
	 */
	public void setPRMClass(IPRMClass prmClass) {
		this.prmClass = prmClass;
		if (this.prmClass != null && this.prmClass.getPRMObjects() != null && !this.prmClass.getPRMObjects().contains(this)) {
			this.prmClass.getPRMObjects().add(this);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// TODO support multiple PK
		// string = <ClassName>_<PKValue>
		try {
			return this.getPRMClass().getName() 
			+ "_"
			+ this.getPRMClass().getPRMObjects().indexOf(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.toString();
	}
	
	

}
