package unbbayes.prs.mebn.entity;

public class BooleanStatesEntityContainer {

	private BooleanStatesEntity trueStateEntity;

	private BooleanStatesEntity falseStateEntity;

	private BooleanStatesEntity absurdStateEntity;
	
	public BooleanStatesEntityContainer(){
		trueStateEntity = new BooleanStatesEntity("true");
		falseStateEntity = new BooleanStatesEntity("false");
		absurdStateEntity = new BooleanStatesEntity("absurd");
	}
	
	
	public BooleanStatesEntity getTrueStateEntity() {
		return trueStateEntity;
	}

	public BooleanStatesEntity getFalseStateEntity() {
		return falseStateEntity;
	}

	public BooleanStatesEntity getAbsurdStateEntity() {
		return absurdStateEntity;
	}
	
}
