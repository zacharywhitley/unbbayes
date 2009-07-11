package unbbayes.io.exception;

import java.io.IOException;
import java.util.ResourceBundle;

public class UBIOException extends IOException{

	public static final String ED_OPEN_FILE = "OpenFileError"; 
	public static final String ED_CREATE_FILE = "CreationFileError"; 
	public static final String ED_READWRITE_FILE = "WriteReaderFileError"; 
	
  	private static ResourceBundle resource =
		    ResourceBundle.getBundle("unbbayes.io.resources.IoResources");
	
  	public UBIOException(String description){
		super(resource.getString(description)); 
	}
  	
	public UBIOException(String description, String object){
		super(resource.getString(description) + ": " + object); 
	}
	
}
