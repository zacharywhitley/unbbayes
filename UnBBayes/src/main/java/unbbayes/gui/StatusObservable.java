package unbbayes.gui;

/**
 * Represents a object that have your state observable
 * 
 * @author Laecio
 */
public interface StatusObservable {

	public void attach(StatusObserver observer); 
	
	public void detach(StatusObserver observer);
	
	public void notity(StatusChangedEvent event);
	
}
