package unbbayes.util.longtask;


/**
 * Represents a long task that has its progress observable.
 * 
 * @author Laecio
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */
public interface ILongTaskProgressObservable {

	public void registerObserver(ILongTaskProgressObserver observer); 
	
	public void removeObserver(ILongTaskProgressObserver observer);
	
	public void notityObservers(LongTaskProgressChangedEvent event);
	
	/**
	 * The maximum number allowed for this long task progress. 
	 * It represents 100%.
	 * @return the maximum number allowed for this long task progress. 
	 * It represents 100%.
	 */
	public int getMaxProgress();
	
	/**
	 * The current number of this long task progress. 
	 * It represents a percentage.
	 * @return the current number of this long task progress. 
	 * It represents a percentage.
	 */
	public int getCurrentProgress();
	
	/**
	 * Returns the percentage of the progress done so far. 
	 * It should be currentProgress * 10000 / maxProgress, 
	 * a number between 0 and 10000.
	 * @return the percentage of the progress done so far. 
	 */
	public int getPercentageDone();
	
	/**
	 * Returns a message with a description of the current 
	 * status of the long task progress.
	 * @return a message with a description of the current 
	 * status of the long task progress.
	 */
	public String getCurrentProgressStatus();
	
}
