package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.entity.exception.TypeChangeNotAllowedException;

/**
 * 
 * 
 * @author Laecio Lima dos Santos
 */

public class CategoricalStatesEntity extends Entity {

	protected CategoricalStatesEntity(String name) {
		super(name, TypeContainer.typeCategoryLabel); 
	}

}
