package unbbayes.prs.mebn.exception;

/**
 * A Ordinary Variable utilizada para o preenchimento não é
 * do tipo esperado. 
 * @author Laecio
 */
public class OVDontIsOfTypeExpected extends MEBNException{

	private String typeExpected; 
	
	public OVDontIsOfTypeExpected(String _typeExpected){
		super(); 
		typeExpected = _typeExpected; 
	}
	
	public String getTypeExpected(){
		return typeExpected; 
	}
	
}