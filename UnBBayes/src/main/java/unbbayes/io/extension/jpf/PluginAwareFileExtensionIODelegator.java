/**
 * 
 */
package unbbayes.io.extension.jpf;

import java.util.ArrayList;
import java.util.Collection;

import org.java.plugin.PluginLifecycleException;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Extension.Parameter;

import unbbayes.io.BaseIO;
import unbbayes.io.DneIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This is an extension of {@link FileExtensionIODelegator}
 * which uses JPF in order to load IO classes as plugins.
 * @author Shou Matsumoto
 *
 */
public class PluginAwareFileExtensionIODelegator extends
		FileExtensionIODelegator {

	/** 
	 * The default value of the extension point ID expected by the plugin manager 
	 * in order to find plugins for PN's IO routines.
	 */
	private String extensionPointID = "PNIO";
	
	/** The default name of the "class" parameter of PNIO extension point */
	private String extensionPointClassParam = "class";
	
	/**
	 * Default constructor is public for plugin support.
	 * If you want to use this directly as ordinal java class, use {@link #newInstance()} instead.
	 * 
	 * Initializes the {@link #getDelegators()} using the following IO classes:
	 * 		- {@link NetIO};
	 * 		- {@link XMLBIFIO};
	 * 		- {@link DneIO};
	 * 		- contents from {@link #loadIOAsPlugins(getExtensionPointID())};
	 * 
	 * @deprecated use {@link #newInstance()} instead
	 */
	public PluginAwareFileExtensionIODelegator() {
		super();
	}
	
	/**
	 * Constructor method.
	 * Initializes the {@link #getDelegators()} using the following IO classes:
	 * 		- {@link NetIO};
	 * 		- {@link XMLBIFIO};
	 * 		- {@link DneIO};
	 * 		- contents from {@link #loadIOAsPlugins(getExtensionPointID())};
	 * @return a new instance of PluginAwareFileExtensionIODelegator.
	 */
	public static PluginAwareFileExtensionIODelegator newInstance() {
		PluginAwareFileExtensionIODelegator ret = new PluginAwareFileExtensionIODelegator();
		ret.setDelegators(new ArrayList<BaseIO>());
		// now, the below 3 I/O classes are also loaded as plugins
//		ret.getDelegators().add(new NetIO());
//		ret.getDelegators().add(new XMLBIFIO());
//		ret.getDelegators().add(new DneIO());
		ret.getDelegators().addAll(ret.loadIOAsPlugins());
		return ret;
	}
	
	/**
	 * Obtains IO classes using plugins loaded from plugin folder, using {@link #getExtensionPointID()}
	 * as extension point ID (which is usually "PNIO").
	 * 		  (see plugin.xml with ID unbbayes.util.extension.core) for declaration.
	 * The plugins are loaded using {@link UnBBayesPluginContextHolder#getPluginManager()}
	 * @return a collection of BaseIO instances loaded as plugins.
	 */
	protected Collection<BaseIO> loadIOAsPlugins() {
		
		Collection<BaseIO> ret = new ArrayList<BaseIO>();
		
		// loads the "core" plugin, which is a stub that we use to declare extension points for core
	    PluginDescriptor core = UnBBayesPluginContextHolder.getPluginManager().getRegistry().getPluginDescriptor(
	    			UnBBayesPluginContextHolder.getPluginCoreID()
	    		);
        
	    // load the IO extension point for PN.
	    ExtensionPoint point = UnBBayesPluginContextHolder.getPluginManager().getRegistry().getExtensionPoint(
	    			core.getId(), 
	    			this.getExtensionPointID()
	    		);

		// initializes the extension point and loads IO classes
	    for (Extension ext : point.getConnectedExtensions()) {
	    	PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
            
            try {
				UnBBayesPluginContextHolder.getPluginManager().activatePlugin(descr.getId());
			} catch (PluginLifecycleException e) {
				e.printStackTrace();
				// we could not load this plugin, but we shall continue
				continue;
			}
			
			// extracting parameters
			Parameter classParam = ext.getParameter(this.getExtensionPointClassParam());
			
			// extracting plugin class 
			ClassLoader classLoader = UnBBayesPluginContextHolder.getPluginManager().getPluginClassLoader(descr);
            Class pluginCls = null;	// class for the plugin or its builder (UnBBayesModuleBuilder)
            try {
            	pluginCls = classLoader.loadClass(classParam.valueAsString());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				continue;
				// it is OK to ignore a plugin failure, since it is not fatal.
			}
			
			try {
				ret.add((BaseIO)pluginCls.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
				// it is OK to ignore a plugin failure, since it is not fatal.
			}
						
		}
	    
	    
		return ret;
	}

	/**
	 * The default value of the extension point ID expected by the plugin manager 
	 * in order to find plugins for PN's IO routines.
	 * @return the extensionPointID
	 */
	public String getExtensionPointID() {
		return extensionPointID;
	}

	/**
	 * The default value of the extension point ID expected by the plugin manager 
	 * in order to find plugins for PN's IO routines.
	 * @param extensionPointID the extensionPointID to set
	 */
	public void setExtensionPointID(String extensionPointID) {
		this.extensionPointID = extensionPointID;
	}

	/**
	 * The default name of the "class" parameter of PNIO extension point
	 * @return the extensionPointClassParam
	 */
	public String getExtensionPointClassParam() {
		return extensionPointClassParam;
	}

	/**
	 * The default name of the "class" parameter of PNIO extension point
	 * @param extensionPointClassParam the extensionPointClassParam to set
	 */
	public void setExtensionPointClassParam(String extensionPointClassParam) {
		this.extensionPointClassParam = extensionPointClassParam;
	}

}
