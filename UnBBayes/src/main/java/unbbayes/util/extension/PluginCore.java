
package unbbayes.util.extension;

import org.java.plugin.Plugin;

/**
 * Core plugin used by UnBBayes in order to restrict extension point.
 * Plugins extending this core plugin are usually new functionalities (modules)
 * for UnBBayes. A new module is usually a new formalism.
 * This class implements JPF framework (http://jpf.sourceforge.net/)
 * @author Shou Matsumoto
 *
 */
public class PluginCore extends Plugin {

	/** Parameter id for the "class" parameter: class to be loaded as extension */
	public static final String PARAMETER_CLASS = "class";
	
	/** Parameter id for the "name" parameter: name of the new module */
	public static final String PARAMETER_NAME = "name";
	
	/** Parameter id for the "description" parameter: tool tip text */
	public static final String PARAMETER_DESCRIPTION = "description";
	
	/** Parameter id for the "icon" parameter: icon to be used by buttons */
	public static final String PARAMETER_ICON = "icon";
	
	
	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// do nothing
	}

}
