package unbbayes.gui.util;


/**
 * Represents a object that have your state observable
 * 
 * @author Laecio
 */
public interface StatusObservable {

	public void registerObserver(StatusObserver observer); 
	
	public void removeObserver(StatusObserver observer);
	
	public void notityObservers(StatusChangedEvent event);
	
}
