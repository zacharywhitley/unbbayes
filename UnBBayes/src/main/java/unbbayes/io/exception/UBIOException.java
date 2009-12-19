package unbbayes.io.exception;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * A wrapper for IOException thrown within UnBBayes.
 * @author Shou Matsumoto
 *
 */
public class UBIOException extends IOException{

	private static final long serialVersionUID = -960272033310829358L;
	
	public static final String ED_OPEN_FILE = "OpenFileError"; 
	public static final String ED_CREATE_FILE = "CreationFileError"; 
	public static final String ED_READWRITE_FILE = "WriteReaderFileError"; 
	
  	private static ResourceBundle resource =
		    ResourceBundle.getBundle(unbbayes.io.resources.IoResources.class.getName());
	
  	public UBIOException(String description){
		super(resource.getString(description)); 
	}
  	
	public UBIOException(String description, String object){
		super(resource.getString(description) + ": " + object); 
	}
	
	public UBIOException (Throwable e){
		super();
		this.setStackTrace(e.getStackTrace());
		this.initCause(e);
	}
	
	public UBIOException (String description, Throwable e){
		this(description);
		this.setStackTrace(e.getStackTrace());
		this.initCause(e);
	}
	
}
