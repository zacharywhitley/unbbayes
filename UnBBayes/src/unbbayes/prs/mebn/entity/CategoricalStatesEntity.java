package unbbayes.prs.mebn.entity;

/**
 *
 *
 * @author Laecio Lima dos Santos
 */

public class CategoricalStatesEntity extends Entity {

	private boolean globallyExclusive;

	protected CategoricalStatesEntity(String name) {
		super(name, TypeContainer.typeCategoryLabel);
		globallyExclusive = false;
	}

	public boolean isGloballyExclusive() {
		return globallyExclusive;
	}

	public void setGloballyExclusive(boolean globallyExclusive) {
		this.globallyExclusive = globallyExclusive;
	}

}
