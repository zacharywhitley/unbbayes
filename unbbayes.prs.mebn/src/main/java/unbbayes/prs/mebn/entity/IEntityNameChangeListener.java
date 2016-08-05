package unbbayes.prs.mebn.entity;

/**
 * This is a listener which will be called in {@link ObjectEntity#setName(String)}
 * @author Shou Matsumoto
 */
public interface IEntityNameChangeListener {
	/**
	 * This will be invoked at ObjectEntity#setName(String)} 
	 * @param oldName : old name of the entity
	 * @param newName : new name for the entity
	 * @param entity : entity whose name has been changed
	 */
	public void onNameChange(String oldName, String newName, Entity entity);
}
