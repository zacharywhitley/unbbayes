package unbbayes.io.mebn;

import unbbayes.io.mebn.protege.Protege41CompatiblePROWL2RLIO;

public class UbfIO2RL extends UbfIO2{

	// in PR-OWL 2 RL format and none in UBF format. 
	
//  Classes present in parent Class: 
//	
//	private MebnIO prowlIO;
//	private String prowlFileExtension = "owl";
//	private String ubfFileExtension = FILE_EXTENSION;
//	private boolean isToUpdateMEBNName = false;
//	private ResourceBundle resource;
	
	public UbfIO2RL() {
		
		super();
		
		//TODO Analyse if is time to change to ubf 3.0. Remember that the changes are 
		this.setUbfVersion(2.5d);
		
		//Same resource of parent class. 
//		try {
//			this.setResource(unbbayes.util.ResourceController.newInstance().getBundle(
//					unbbayes.io.mebn.resources.IoUbfResources.class.getName(),	// same from superclass
//					Locale.getDefault(),										// use OS locale
//					UbfIO2RL.class.getClassLoader()							// use plug-in class loader
//			));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
		
		try {
			this.setProwlIO(Protege41CompatiblePROWL2RLIO.newInstance());		// load PR-OWL ontology using protege
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			this.setName(UbfIO2RL.class.getSimpleName());	// the name must be different from the superclass
		    System.out.println("Name: " + this.getName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Construction method for {@link UbfIO2}
	 * @return {@link UbfIO2} instance
	 */
	public static UbfIO2RL getInstance() {
		UbfIO2RL ret = new UbfIO2RL();
		return ret;
	}
	
	
	
}
