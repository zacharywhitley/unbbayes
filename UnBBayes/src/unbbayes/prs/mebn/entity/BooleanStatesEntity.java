package unbbayes.prs.mebn.entity;

import unbbayes.prs.mebn.entity.exception.TypeChangeNotAllowedException;

/**
 * This class is formed by the Boolean truth-value states and are applied to
 * Boolean random variables.
 * 
 * @author Rommel Carvalho
 * 
 */
public class BooleanStatesEntity extends Entity {

	private static BooleanStatesEntity trueStateEntity;

	private static BooleanStatesEntity falseStateEntity;

	private static BooleanStatesEntity absurdStateEntity;

	public void setType(String type) throws TypeChangeNotAllowedException {
		throw new TypeChangeNotAllowedException(
				"This entity is not allowed to change its type.");
	}

	private BooleanStatesEntity(String name) {
		this.type = "Boolean";
		this.name = name;
	}

	public static BooleanStatesEntity getTrueStateEntity() {
		if (trueStateEntity == null)
			trueStateEntity = new BooleanStatesEntity("true");
		return trueStateEntity;
	}

	public static BooleanStatesEntity getFalseStateEntity() {
		if (falseStateEntity == null)
			falseStateEntity = new BooleanStatesEntity("false");
		return falseStateEntity;
	}

	public static BooleanStatesEntity getAbsurdStateEntity() {
		if (absurdStateEntity == null)
			absurdStateEntity = new BooleanStatesEntity("absurd");
		return absurdStateEntity;
	}
}
