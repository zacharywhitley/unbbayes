package unbbayes.datamining.datamanipulation.resources;

import java.util.ListResourceBundle;

/** Resources file for datamanipulation package. Localization = english.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class DataManipulationResource extends ListResourceBundle {

	/**
	 * Override getContents and provide an array, where each item in the array
	 * is a pair of objects. The first element of each pair is a String key,
	 * and the second is the value associated with that key.
	 */
	public Object[][] getContents() {
		return contents;
	}

	/** The resources */
	static final Object[][] contents = {
		{"normalizeException1", "Can't normalize array. Sum is NaN."},
		{"normalizeException2", "Can't normalize array. Sum is zero."},
		{"normalizeException3", "Can't normalize. Invalid normalization limits."},
		{"readHeaderException1", "Premature end of line."},
		{"readHeaderException2", "keyword @relation expected"},
		{"readHeaderException3", "no valid attribute type or invalid enumeration"},
		{"readHeaderException4", "{ expected at beginning of enumeration "},
		{"readHeaderException5", "} expected at end of enumeration"},
		{"readHeaderException6", "no nominal values found"},
		{"readHeaderException7", "keyword @data expected"},
		{"readHeaderException8", "no attributes declared"},
		{"getLastTokenException1", "end of line expected"},
		{"getNextTokenException1", "premature end of line"},
		{"getNextTokenException2", "premature end of file"},
		{"getInstanceException1", "no header information available"},
		{"getInstanceAuxException1", "a String was read while expecting a number"},
		{"getInstanceFullException1", "not a valid value"},
		{"getInstanceFullException2", "nominal value not declared in header"},
		{"getInstanceFullException3", "number expected"},
		{"getInstanceTXT", "No attribute information available"},
		{"runtimeException1", "Instance doesn't have access to a dataset!"},
		{"runtimeException2", "Class is not set!"},
		{"illegalArgumentException1", "Invalid class index: "},
		{"setAttributeAtException", "Index out of range"},
		{"setClassIndexException", "Invalid class index: "},
		{"outOfRange", "Parameters first and/or toCopy out of range"},
		{"setValueException", "Value can't be inserted. Can't parse string to float."},
		{"emptyInstanceSet", "The instance set contains no instances."},
		{"nominalAttribute", "The attribute to calculate the standard deviation must be numerical"},
		
		
		/**********************************************************************
		 * FileUtils
		 *********************************************************************/
		{"arffFilterText", "Arff Files (*.arff)"},
		{"txtFilterText", "Txt Files (*.txt)"},
		{"besFilterText", "Batch Evaluation Files (*.bev)"},

		{"canceledDialog", "Cancelled by user"},

		/* Bev file */
		{"openBevDialog", "Open Bev file"},
		{"saveBevDialog", "Save Bev file"},
		{"openBevSuccess", "Bev file opened successfully"},
		{"saveBevSuccess", "Bev file saved successfully"},
		
		/* Arff file */
		{"openArffDialog", "Open Arff file"},
		{"saveArffDialog", "Save Arff file"},
		{"openArffSuccess", "Arff file opened successfully"},
		{"saveArffSuccess", "Arff file saved successfully"},
		
		/* Txt file */
		{"openTxtDialog", "Open Txt file"},
		{"saveTxtDialog", "Save Txt file"},
		{"openTxtSuccess", "Txt file opened successfully"},
		{"saveTxtSuccess", "Txt file saved successfully"},

	};
}