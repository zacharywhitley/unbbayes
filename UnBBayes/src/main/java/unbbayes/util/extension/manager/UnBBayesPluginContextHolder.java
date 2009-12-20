/**
 * 
 */
package unbbayes.util.extension.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

import unbbayes.io.exception.UBIOException;
import unbbayes.util.ApplicationPropertyHolder;


/**
 * This is a class that holds a virtually static (singleton) instance
 * of plugin manager used by UnBBayes core.
 * This class can be used to access plugin utility.
 * @author Shou Matsumoto
 *
 */
public class UnBBayesPluginContextHolder {
	
	private static String pluginsDirectoryName = null;
	private static String pluginCoreID = null;
	static {
		pluginsDirectoryName = ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginsDirectoryName");
		if (pluginsDirectoryName == null) {
			pluginsDirectoryName = "plugins";
		}
		pluginCoreID = ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginCoreID");
		if (pluginCoreID == null) {
			pluginCoreID = "unbbayes.util.extension.core";
		}
	}
	
	/** Tells us if the plugin infrastructure is already initialized (published) */
	private static boolean initialized = false;

	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of Plugin manager
     */
    private static class SingletonHolder { 
    	private static final PluginManager INSTANCE = ObjectFactory.newInstance().createManager();
    }
	
	/**
	 * Default constructor is protected in order to help
	 * subclasses.
	 */
	protected UnBBayesPluginContextHolder() {}
	
	/**
	 * Return a singleton instance of plugin manager used
	 * by UnBBayes
	 * @return a singleton instance of plugin manager.
	 */
	public static PluginManager getPluginManager() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Loads plugins situated at {@link #getPluginsDirectoryName()} folder
	 * and publish them (make them usable).
	 * @throws IOException
	 */
	public static synchronized void publishPlugins() throws IOException {
		// search for files inside plugin directory
		File pluginsDir = new File(getPluginsDirectoryName());
		File[] plugins = pluginsDir.listFiles();

		// publish (load) plugins
		try {
	        Set<PluginLocation> locations = new HashSet<PluginLocation>(plugins.length);
	        for (File file : plugins) {
				PluginLocation location = StandardPluginLocation.create(file);
				if (location != null) {
					locations.add(location);
				}
			}
	        
	        // enable plugin
	        getPluginManager().publishPlugins(locations.toArray(new PluginLocation[locations.size()]));
	    } catch (Exception e) {
	    	throw new UBIOException(e);
	    }
	    
	    // if we published the plugins, they are initialized.
	    initialized = true;
	}
	
//	/**
//	 * Activates all plugins connected to a given extension point.
//	 * Basically, it does {@link #getPluginManager()}.activatePlugin() for
//	 * every connected extensions of a given extension point.
//	 * @param point : extension point to activate.
//	 */
//	public static void activateAllExtensionPoint(ExtensionPoint point) {
//		for (Extension extension : point.getConnectedExtensions()) {
//			PluginDescriptor descr = extension.getDeclaringPluginDescriptor();
//            try {
//				getPluginManager().activatePlugin(descr.getId());
//			} catch (PluginLifecycleException e) {
//				e.printStackTrace();
//				// we could not load this plugin, but we shall continue
//				continue;
//			}
//		}
//	}

	/**
	 * @return the pluginsDirectoryName
	 */
	public static String getPluginsDirectoryName() {
		return pluginsDirectoryName;
	}

	/**
	 * @param pluginsDirectoryName the pluginsDirectoryName to set
	 */
	public static void setPluginsDirectoryName(String pluginsDirectoryName) {
		UnBBayesPluginContextHolder.pluginsDirectoryName = pluginsDirectoryName;
	}
	
	/**
	 * The ID of the core plugin.
	 * @return the pluginCoreID
	 */
	public static String getPluginCoreID() {
		return pluginCoreID;
	}

	/**
	 * The ID of the core plugin.
	 * @param pluginCoreID the pluginCoreID to set
	 */
	public static void setPluginCoreID(String newPluginCoreID) {
		pluginCoreID = newPluginCoreID;
	}

	/**
	 * Tells us if the plugin infrastructure is already initialized (published).
	 * {@link #publishPlugins()} initializes the plugins.
	 * @return true if the plugins were published. False otherwise.
	 */
	public static boolean isInitialized() {
		return initialized;
	}


}
