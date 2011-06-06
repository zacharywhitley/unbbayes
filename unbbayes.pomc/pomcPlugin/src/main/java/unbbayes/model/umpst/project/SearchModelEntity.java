package unbbayes.model.umpst.project;

import java.util.Set;

import unbbayes.model.umpst.entities.EntityModel;

public class SearchModelEntity {
	
	private String keyWord;
	private Set<EntityModel> entitiesRelated;
	
	public SearchModelEntity(String keyWord, Set<EntityModel> entitiesRelated){
		this.keyWord = keyWord;
		this.entitiesRelated = entitiesRelated;
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
	 * @return the entitiesRelated
	 */
	public Set<EntityModel> getEntitiesRelated() {
		return entitiesRelated;
	}

	/**
	 * @param entitiesRelated the entitiesRelated to set
	 */
	public void setEntitiesRelated(Set<EntityModel> entitiesRelated) {
		this.entitiesRelated = entitiesRelated;
	}
	
	

}
