package unbbayes.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters of a algorithm
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class Parameters {

	Map<String, String> parameterList = new HashMap<String, String>();   
	
	protected void addParameter(String parameter, String initialValue){
		parameterList.put(parameter, initialValue); 
	}
	
	/**
	 * Set a value of a parameter. 
	 * @param parameter Parameter to have its value setted
	 * @param value Value of the parameter
	 * @return true if the set is sucessful, else otherside
	 */
	public boolean setParameterValue(String parameter, String value){
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
	public String getParameterValue(String parameter){
		return parameterList.get(parameter); 
	}
	
}
