package unbbayes.util.longtask;

public class LongTaskProgressChangedEvent {

	public String msg; 
	public int percentageDone;
	
	public LongTaskProgressChangedEvent(String msg, int percentageDone) {
		this.msg = msg;
		this.percentageDone = percentageDone;
	}
	
	public LongTaskProgressChangedEvent(int percentageDone) {
		this("", percentageDone);
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	/**
	 * Returns the percentage of the task completed. 
	 * @return the percentage of the task completed. An 
	 *  integer from 0 to 10000.
	 */
	public int getPercentageDone() {
		return percentageDone;
	}
	
	/**
	 * Sets the percentage of the task completed. 
	 * @param percentageDone the percentage of the task completed. 
	 * 	It has to be an integer from 0 to 10000.
	 */
	public void setPercentageDone(int percentageDone) {
		this.percentageDone = percentageDone;
	} 
	
}
