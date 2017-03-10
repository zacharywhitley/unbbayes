package unbbayes.model.umpst.implementation.node;

import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EventMappingType;
import unbbayes.model.umpst.rule.RuleModel;

public class UndefinedNode {
	
	private MFragExtension mfragExtension;
	private Object eventRelated;
	private RuleModel ruleRelated;
	private EventMappingType mappingType;
	
	/**
	 * Node that was not defined in the criteria of selection.
	 * @param event
	 * @param mfragExtension
	 */
	public UndefinedNode(Object event, MFragExtension mfragExtension) {
		setEventRelated(event);
		setMfragExtension(mfragExtension);
		setRuleRelated(null);
		setMappingType(EventMappingType.UNDEFINED);
	}

	/**
	 * @return the mfragExtension
	 */
	public MFragExtension getMfragExtension() {
		return mfragExtension;
	}

	/**
	 * @param mfrag the mfragExtension to set
	 */
	public void setMfragExtension(MFragExtension mfragExtension) {
		this.mfragExtension = mfragExtension;
	}
	
	/**
	 * The event can be a {@link CauseVariableModel}
	 * @return object
	 */
	public Object getEventRelated() {
		return eventRelated;
	}
	
	/**
	 * {@link CauseVariableModel} related
	 * @param eventRelated
	 */
	public void setEventRelated(Object eventRelated) {
		this.eventRelated = eventRelated;
	}

	public RuleModel getRuleRelated() {
		return ruleRelated;
	}

	public void setRuleRelated(RuleModel ruleRelated) {
		this.ruleRelated = ruleRelated;
	}

	public EventMappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(EventMappingType mappingType) {
		this.mappingType = mappingType;
	}	
}
