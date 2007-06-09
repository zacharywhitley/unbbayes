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

    protected BooleanStatesEntity(String name) {
		super(name, TypeContainer.typeBoolean);
	}

}
