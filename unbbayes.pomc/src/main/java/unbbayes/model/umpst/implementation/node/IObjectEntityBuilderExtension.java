/**
 * 
 */
package unbbayes.model.umpst.implementation.node;

import unbbayes.prs.mebn.entity.IObjectEntityBuilder;


public interface IObjectEntityBuilderExtension extends IObjectEntityBuilder {
	
	public boolean isToCreateEntity();
	
	public void setToCreateEntity(boolean isToCreateEntity);

}
