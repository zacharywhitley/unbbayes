package unbbayes.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import unbbayes.controller.resources.ControllerResources;
import unbbayes.gui.resources.GuiResources;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This class is used to facilite the use of the 
 * diverses files of resources of the project. It
 * contains methods to get this resources. 
 * 
 * ...only an experience...
 * 
 * @author Laecio
 * 
 * 
 * @author Shou Matsumoto
 * @since 2009 - 12 - 19
 * @version 19/12/2009
 * Altered this class in order to use singleton pattern and 
 * make it possible to change the classloader/locales dinamically (e.g. plugins).
 * If any classloader modification is needed, you may extend/implement
 * a new ClassLoader and set it as this class' default classloader
 * (as a example, you may implement a network classloader and set it as
 * this singleton instance's default classloader).
 * The plugin support is implemented by setting the default classloader
 * as the plugin classloader.
 */
public class ResourceController {
	
	public static ResourceBundle RS_GUI = ResourceController.newInstance()
	.getBundle(GuiResources.class.getName());
	
	//TODO change the name of the repetitives resouces
	
	public static ResourceBundle RS_COMPILER = ResourceController.newInstance()
	.getBundle(unbbayes.prs.mebn.compiler.resources.Resources.class.getName());

	public static ResourceBundle RS_MEBN = ResourceController.newInstance()
	.getBundle(unbbayes.prs.mebn.resources.Resources.class.getName());
	
	public static ResourceBundle RS_SSBN = ResourceController.newInstance()
	.getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
	public static ResourceBundle RS_BN = ResourceController.newInstance()
	.getBundle(unbbayes.prs.bn.resources.BnResources.class.getName());
	
	public static ResourceBundle RS_HYBRID_BN = ResourceController.newInstance()
	.getBundle(unbbayes.prs.hybridbn.resources.HybridBnResources.class.getName());

	
	
	
	private ClassLoader defaultClassLoader = this.getClass().getClassLoader();
	
	private Locale defaultLocale = Locale.getDefault();
	
	private String extensionPointID = "ResourceBundle";
	
	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of Plugin manager
     */
    private static class SingletonHolder { 
    	private static final ResourceController INSTANCE = new ResourceController();
    }
    
    /**
     * Default constructor is made protected just to make it easy to extend.
     * Usually, you must use #newInstance().
     * This method just uses {@link UnBBayesPluginContextHolder} in order
     * to obtain the plugin classloader and sets it as the default classloader
     * using #loadPluginClassLoader()
     */
    protected ResourceController() {
    	this.setDefaultClassLoader(this.loadPluginClassLoader());
    }
    
    /**
     * Obtains a ClassLoader which loads classes bound by plugins.
     * @return
     */
    protected ClassLoader loadPluginClassLoader() {
    	
    	// loads the "core" plugin, which is a stub that we use to declare extension points for core
	    PluginDescriptor core = UnBBayesPluginContextHolder.getPluginManager().getRegistry().getPluginDescriptor(
	    			UnBBayesPluginContextHolder.getPluginCoreID()
	    		);
        
	    // load the resource extension point for PN.
	    ExtensionPoint point = UnBBayesPluginContextHolder.getPluginManager().getRegistry().getExtensionPoint(
	    			core.getId(), 
	    			this.getExtensionPointID()
	    		);
	    
    	
	    ListClassLoaderDelegator ret = new ListClassLoaderDelegator(new ArrayList<ClassLoader>());
	    
    	for (Extension ext : point.getConnectedExtensions()) {
    		PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
    		ret.getListOfLoaders().add(UnBBayesPluginContextHolder.getPluginManager().getPluginClassLoader(descr));
    	}
    	
    	return ret;
    }
    
    /**
     * @return a singleton instance of this class.
     */
    public static ResourceController newInstance() {
    	return SingletonHolder.INSTANCE;
    }

	/**
	 * The default class loader to be used in order to load the resources.
	 * @return the defaultClassLoader
	 */
	public ClassLoader getDefaultClassLoader() {
		return defaultClassLoader;
	}

	/**
	 * The default class loader to be used in order to load the resources.
	 * @param defaultClassLoader the defaultClassLoader to set
	 */
	public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
		this.defaultClassLoader = defaultClassLoader;
	}

	/**
	 * This is the locale to be used in order to load the resource file.
	 * The initial value is Locale.getDefault()
	 * @return the defaultLocale
	 */
	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * This is the locale to be used in order to load the resource file.
	 * The initial value is Locale.getDefault()
	 * @param defaultLocale the defaultLocale to set
	 */
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	
	/**
	 * Obtains a ResourceBundle using current configuration of locale and classloader.
	 * It calls ResourceBundle.getBundle(baseName, this.getDefaultLocale(), this.getDefaultClassLoader()).
	 * @param baseName : base name of the class to be loaded.
	 * @return a instance of ResourceBundle
	 */
	public ResourceBundle getBundle(String baseName) {
		return ResourceBundle.getBundle(baseName, this.getDefaultLocale(), this.getDefaultClassLoader());
	}
	
	/**
	 * Delegator to ResourceBundle.getBundle(baseName, locale, classLoader)
	 * @param baseName
	 * @param locale
	 * @param classLoader
	 * @return
	 */
	public ResourceBundle getBundle(
				String baseName,
				Locale locale,
				ClassLoader classLoader ) {
		return ResourceBundle.getBundle(baseName, locale, classLoader);
	}
	
	public static ResourceBundle RS_CONTROLLER = ResourceBundle.getBundle(
			ControllerResources.class.getName());

	

	/**
	 * ID of the extension point, used by plugin loader to load Resource plugins.
	 * The default value is usually "ResourceBundle"
	 * @return the extensionPointID
	 */
	public String getExtensionPointID() {
		return extensionPointID;
	}

	/**
	 * ID of the extension point, used by plugin loader to load Resource plugins.
	 * The default value is usually "ResourceBundle"
	 * @param extensionPointID the extensionPointID to set
	 */
	public void setExtensionPointID(String extensionPointID) {
		this.extensionPointID = extensionPointID;
	}
	
	/**
	 * A class loader which contains a list of class loader.
	 * It basically delegates method calls into this list, in order.
	 * By default, its public methods acts as if the last element of the
	 * list were this.getClass().getClassLoader().
	 * @author Shou Matsumoto
	 *
	 */
	class ListClassLoaderDelegator extends ClassLoader {
		private List<ClassLoader> listOfLoaders;
		public ListClassLoaderDelegator(List<ClassLoader> listOfLoaders) {
			this.setListOfLoaders(listOfLoaders);
		}
		public List<ClassLoader> getListOfLoaders() {
			return listOfLoaders;
		}
		public void setListOfLoaders(List<ClassLoader> listOfLoaders) {
			this.listOfLoaders = listOfLoaders;
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#clearAssertionStatus()
		 */
		@Override
		public synchronized void clearAssertionStatus() {
			for (ClassLoader loader : this.getListOfLoaders()) {
				loader.clearAssertionStatus();
			}
			this.getClass().getClassLoader().clearAssertionStatus();
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#getResource(java.lang.String)
		 */
		@Override
		public URL getResource(String name) {
			for (ClassLoader loader : this.getListOfLoaders()) {
				try {
					URL ret = loader.getResource(name);
					if (ret != null) {
						return ret;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			return this.getClass().getClassLoader().getResource(name);
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
		 */
		@Override
		public InputStream getResourceAsStream(String name) {
			for (ClassLoader loader : this.getListOfLoaders()) {
				try {
					InputStream ret = loader.getResourceAsStream(name);
					if (ret != null) {
						return ret;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			return this.getClass().getClassLoader().getResourceAsStream(name);
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#getResources(java.lang.String)
		 */
		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			for (ClassLoader loader : this.getListOfLoaders()) {
				try {
					Enumeration<URL> ret = loader.getResources(name);
					if (ret != null && ret.hasMoreElements()) {
						return ret;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			return this.getClass().getClassLoader().getResources(name);
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#loadClass(java.lang.String)
		 */
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			for (ClassLoader loader : this.getListOfLoaders()) {
				try {
					Class<?>  ret = loader.loadClass(name);
					if (ret != null) {
						return ret;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			return this.getClass().getClassLoader().loadClass(name);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#setClassAssertionStatus(java.lang.String, boolean)
		 */
		@Override
		public synchronized void setClassAssertionStatus(String className,
				boolean enabled) {
			for (ClassLoader loader : this.getListOfLoaders()) {
				loader.setClassAssertionStatus(className, enabled);
			}
			this.getClass().getClassLoader().setClassAssertionStatus(className, enabled);
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#setDefaultAssertionStatus(boolean)
		 */
		@Override
		public synchronized void setDefaultAssertionStatus(boolean enabled) {
			for (ClassLoader loader : this.getListOfLoaders()) {
				loader.setDefaultAssertionStatus(enabled);
			}
			this.getClass().getClassLoader().setDefaultAssertionStatus(enabled);
		}
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#setPackageAssertionStatus(java.lang.String, boolean)
		 */
		@Override
		public synchronized void setPackageAssertionStatus(String packageName,
				boolean enabled) {
			for (ClassLoader loader : this.getListOfLoaders()) {
				loader.setPackageAssertionStatus(packageName, enabled);
			}
			this.getClass().getClassLoader().setPackageAssertionStatus(packageName, enabled);
		}
		
	}
	 
}
