package unbbayes.prs.mebn.ssbn;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters of a algorithm
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public abstract class Parameters {

	Map<Integer, String> parameterList = new HashMap<Integer, String>();   
	
	protected void addParameter(int parameter, String initialValue){
		parameterList.put(parameter, initialValue); 
	}
	
	/**
	 * Set a value of a parameter. 
	 * @param parameter Parameter to have its value setted
	 * @param value Value of the parameter
	 * @return true if the set is sucessful, else otherside
	 */
	public boolean setParameterValue(int parameter, String value){
		if(parameterList.get(parameter) != null){
			parameterList.put(parameter, value);
			return true; 
		}else{
			return false; 
		}
	}
	
	/**
	 * @param parameter Parameter
	 * @return The value of the parameter. 
	 */
	public String getParameterValue(int parameter){
		return parameterList.get(parameter); 
	}
	
}
