package unbbayes.controller;

import java.lang.Thread;

/** This class loads the instances in a separate thread for 
 *  the FileController instance
 *
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *  @version $1.0 $ (07/04/2003)
 */
public class LoadingActivity extends Thread
{
	/** Position of the last instance loaded */
	private int current;
	/** Position of the last instance */
	private int target;
	/** Data used for the loadings */
	private IProgress progress;
	private boolean cancel;
	
	//---------------------------------------------------------------------//
	
	/**
   	* Creates a new instance of this class based on a loader
   	*
   	* @param loader Loader
	*/
	public LoadingActivity(IProgress l, int t)
	{
		current = 0;
		target = t;
		progress = l;
		cancel=false;
	}
	
	//---------------------------------------------------------------------//
	
	/**
   	* Gets the position of the last instance loaded
   	*
   	* @return Current instance
   	*/
	public int getCurrent()
	{
		return current;
	}
	
	//--------------------------------------------------------------------//
	
	/**
   	* Gets the position of the last instance 
   	*
   	* @return Target instance
   	*/
	public int getTarget()
	{
		return target;
	}
	
	//-------------------------------------------------------------------//
	
	public void requestCancel()
	{
		cancel=true;
	}
	
	//	-------------------------------------------------------------------//
	
	/**
   	* Starts the loading
   	*/
	public void run()
	{
    	while (current < target)
		{  
			if (cancel)
			{
				progress.cancel();
				return;
			}
			if((!progress.next()) || isInterrupted())
			{
				current = target;
				return;
			}
			current++;
			Thread.yield();
		}	
	}
}