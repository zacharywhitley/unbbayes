package unbbayes.io.exception;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.util.Debug;

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
	
  	private static ResourceBundle resource = null;
  	static {
  		// attempt to gradually restrict ResourceBundle on error
  		try {
  			resource = unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.resources.IoResources.class.getName());
		} catch (Throwable t) {
			try {
				Debug.println(UBIOException.class, "Error obtaining UBIOException's classloader using plugins. Using UBIOException's classloader instead...", t);
				resource = unbbayes.util.ResourceController.newInstance().getBundle(
							unbbayes.io.resources.IoResources.class.getName(), 
							Locale.getDefault(), 
							UBIOException.class.getClassLoader()
						);
			} catch (Throwable t2) {
				Debug.println(UBIOException.class, "Error obtaining UBIOException's classloader using unbbayes.util.ResourceController. Using ResourceBundle instead...", t2);
				resource = ResourceBundle.getBundle(unbbayes.io.resources.IoResources.class.getName());
			}
		}
  	}
	
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
