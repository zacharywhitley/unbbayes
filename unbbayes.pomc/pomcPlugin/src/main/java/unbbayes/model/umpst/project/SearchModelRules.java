package unbbayes.model.umpst.project;

import java.util.HashSet;
import java.util.Set;

import unbbayes.model.umpst.rules.RulesModel;

public class SearchModelRules {
	
	private String keyWord;
	private Set<RulesModel> rulesRelated;
	
	public SearchModelRules(String key, Set<RulesModel> rulesRealted){
		
		this.keyWord = key;
		this.rulesRelated = rulesRealted;
		if (rulesRealted==null){
			this.setRulesRelated(new HashSet<RulesModel>());
		}
		
	}

	/**
	 * @return the keyWord
	 */
	public String getKeyWord() {
		return keyWord;
	}

	/**
	 * @param keyWord the keyWord to set
	 */
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	/**
	 * @return the rulesRelated
	 */
	public Set<RulesModel> getRulesRelated() {
		return rulesRelated;
	}

	/**
	 * @param rulesRelated the rulesRelated to set
	 */
	public void setRulesRelated(Set<RulesModel> rulesRelated) {
		this.rulesRelated = rulesRelated;
	}
	
	

}
