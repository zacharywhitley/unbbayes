package unbbayes.model.umpst.project;

import java.util.HashSet;
import java.util.Set;

import unbbayes.model.umpst.groups.GroupsModel;

public class SearchModelGroup {
	
	String keyWord;
	Set<GroupsModel> relatedGroups;
	
	public SearchModelGroup(String keyWord,Set<GroupsModel> relatedGroups){
		this.keyWord=keyWord;
		this.relatedGroups=relatedGroups;
		if (relatedGroups==null){
			this.setRelatedGroups(new HashSet<GroupsModel>());
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
	 * @return the relatedGroups
	 */
	public Set<GroupsModel> getRelatedGroups() {
		return relatedGroups;
	}

	/**
	 * @param relatedGroups the relatedGroups to set
	 */
	public void setRelatedGroups(Set<GroupsModel> relatedGroups) {
		this.relatedGroups = relatedGroups;
	}

	
}

