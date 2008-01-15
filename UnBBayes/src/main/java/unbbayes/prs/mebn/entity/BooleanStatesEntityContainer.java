package unbbayes.prs.mebn.entity;

public class BooleanStatesEntityContainer {

	private BooleanStateEntity trueStateEntity;

	private BooleanStateEntity falseStateEntity;

	private BooleanStateEntity absurdStateEntity;
	
	public BooleanStatesEntityContainer(){
		trueStateEntity = new BooleanStateEntity("true");
		falseStateEntity = new BooleanStateEntity("false");
		absurdStateEntity = new BooleanStateEntity("absurd");
	}
	
	
	public BooleanStateEntity getTrueStateEntity() {
		return trueStateEntity;
	}

	public BooleanStateEntity getFalseStateEntity() {
		return falseStateEntity;
	}

	public BooleanStateEntity getAbsurdStateEntity() {
		return absurdStateEntity;
	}
	
}
