package unbbayes.gui.util;

public class StatusChangedEvent {

	public String msg; 
	public int conclude;
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public int getPercentageConclude() {
		return conclude;
	}
	
	public void setPercentageConclude(int conclude) {
		this.conclude = conclude;
	} 
	
}
