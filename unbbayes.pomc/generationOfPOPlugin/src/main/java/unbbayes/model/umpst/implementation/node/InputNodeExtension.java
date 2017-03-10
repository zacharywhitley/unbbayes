/**
 * 
 */
package unbbayes.model.umpst.implementation.node;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.prs.mebn.InputNode;

/**
 * Class created to extend properties of UMP-ST to MEBN model
 * @author Diego Marques
 *
 */
public class InputNodeExtension extends InputNode {
	
	private Object eventRelated;
	
	/**
	 * The event can be a {@link RelationshipModel} or an {@link AttributeModel}
	 * @param name
	 * @param mFrag
	 */
	public InputNodeExtension(String name, MFragExtension mFrag, Object event) {
		super(name, mFrag);
		setEventRelated(event);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The event can be a {@link RelationshipModel} or an {@link AttributeModel}
	 * @return the eventRelated
	 */
	public Object getEventRelated() {
		return eventRelated;
	}

	/**
	 * @param eventRelated the eventRelated to set
	 */
	public void setEventRelated(Object eventRelated) {
		this.eventRelated = eventRelated;
	}
}
