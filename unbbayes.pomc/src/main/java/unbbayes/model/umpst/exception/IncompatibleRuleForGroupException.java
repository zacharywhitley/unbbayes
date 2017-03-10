/**
 * 
 */
package unbbayes.model.umpst.exception;

import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * {@link GroupModel} does not contain all the elemets present in {@link RuleModel}.
 * @author Diego Marques
 */
public class IncompatibleRuleForGroupException extends Exception {
	
	public IncompatibleRuleForGroupException() {
		super();
	}
	
	public IncompatibleRuleForGroupException(String msg) {
		super(msg);
	}
}
