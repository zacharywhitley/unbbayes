/**
 * 
 */
package unbbayes.model.umpst.implementation.node;

import java.util.List;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * Class created to extend properties of UMP-ST to MEBN model
 * @author Diego Marques
 *
 */
public class ResidentNodeExtension extends ResidentNode {
	
	private Object eventRelated;
	
	/**
	 * The event can be a {@link RelationshipModel} or an {@link AttributeModel}
	 * @param name
	 * @param mFrag
	 */
	public ResidentNodeExtension(String name, MFragExtension mFrag, Object eventRelated) {
		super(name, mFrag);
		setEventRelated(eventRelated);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Search whether the {@link ResidentNode} has as {@link Argument}, the {@link Argument} related to the
	 * {@link OrdinaryVariable} added to {@link ResidentNode}.
	 * @param ov
	 * @return
	 */
	public Argument containsArgumentRelatedToOV(OrdinaryVariable ov) {
		List<Argument> argumentList = super.getArgumentList();
		for (int i = 0; i < argumentList.size(); i++) {
			Argument argument = argumentList.get(i);
			if (argument.getOVariable().equals(ov)) {
				return argument;
			}
		}
		return null;
	}
	
//	public void addArgumentRelated(OrdinaryVariable ov) throws ArgumentNodeAlreadySetException, 
//		OVariableAlreadyExistsInArgumentList, ArgumentOVariableAlreadySetException {
//		/**
//		 * Verify whether the resident node has as argument the ordinary variable related. If
//		 * it has, then not it is necessary to create another one.
//		 */
//		Argument arg = containsArgumentRelatedToOV(ov);
//		if(arg != null) {
//			this.addArgument(ov, false);
////			arg.setArgumentTerm(this);
//		} else {
//			this.addArgument(ov, true);
////			arg = containsArgumentRelatedToOV(ov);
////			arg.setArgumentTerm(this);
//		}
//	}

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
