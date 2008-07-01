package unbbayes.gui.util;

/**
 * Utilities for files treatment
 * 
 * @author Laecio
 */
public class FileUtils {

	public static String getNameOfFileWithoutPath(String nameWithPath){
		String[] nameComponents = nameWithPath.split("\\"); //TODO for all systems?
		return nameComponents[nameComponents.length - 1]; 
	}
	
}
