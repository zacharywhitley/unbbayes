/**
 * 
 */
package unbbayes.gui.mebn.extension.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Extension.Parameter;

import unbbayes.controller.IconController;
import unbbayes.util.Debug;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This class manages MEBN edition panel's plugins
 * for UnBBayes' MEBN module, using the default JPF plugin infrastructure.
 * @author Shou Matsumoto
 *
 */
public class MEBNEditionPanelPluginManager implements IMEBNEditionPanelPluginManager {

	private String corePluginID = "unbbayes.prs.mebn";
	private String mebnEditionPanelExtensionPointID = "MEBNEditorPanel";
	private String paramNameMEBNEditionPanelBuilder = "class";
	private String paramNameName = "name";
	private String paramNameIcon = "icon";
	private String paramNameDescription = "description";
	
	private UnBBayesPluginContextHolder pluginContextHolder = UnBBayesPluginContextHolder.newInstance();
	
	private Collection<IMEBNEditionPanelPluginComponents> loadedComponents = new ArrayList<IMEBNEditionPanelPluginComponents>();

	/**
	 * Default constructor.
	 * @deprecated use {@link #getInstance()}
	 */
	public MEBNEditionPanelPluginManager() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is the default constructor method.
	 * @param initialize : if set to true, the plugins will be loaded and initialized 
	 * ({@link #initialize()} will be called).
	 * If false, {@link #initialize()} will not be called. Please, note that
	 * you may call {@link #initialize()} afterwards.
	 * @return
	 */
	public static IMEBNEditionPanelPluginManager newInstance(boolean initialize){
		IMEBNEditionPanelPluginManager ret = new MEBNEditionPanelPluginManager();
		if (initialize) {
			try{
				ret.initialize();
			}catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#initialize()
	 */
	public void initialize() {
		this.reloadPlugins();
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#reloadPlugins()
	 */
	public void reloadPlugins() {
		
		Collection<IMEBNEditionPanelPluginComponents> loadedComponents = new ArrayList<IMEBNEditionPanelPluginComponents>();
		
    	try {
        	// let's republish plugins if they're not done yet
    		if (!UnBBayesPluginContextHolder.newInstance().isInitialized()) {
    			UnBBayesPluginContextHolder.newInstance().publishPlugins();
    		}

    	    // loads the "mebn" plugin, which declares general extension points for core (including mebn edition panel)
    	    PluginDescriptor core = this.getPluginContextHolder().getPluginManager().getRegistry().getPluginDescriptor(this.getCorePluginID());
            
    	    // load the extension point for new mebn edition panel (functionalities).
    	    ExtensionPoint point = this.getPluginContextHolder().getPluginManager().getRegistry().getExtensionPoint(core.getId(), this.getMebnEditionPanelExtensionPointID());
        	
    	    // iterate over the connected extension points
    	    for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
    			try {
    				Extension ext = it.next();
    	            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
    	            
    	            if (this.getPluginContextHolder().getPluginManager().isBadPlugin(descr)) {
    	            	continue;
    	            }
    	            this.getPluginContextHolder().getPluginManager().activatePlugin(descr.getId());
    				
    				// extracting parameters
    				Parameter panelBuilderParam = ext.getParameter(this.getParamNameMEBNEditionPanelBuilder());
    				Parameter nameParam = ext.getParameter(this.getParamNameName());
    				Parameter descriptionParam = ext.getParameter(this.getParamNameDescription());
    				Parameter iconParam = ext.getParameter(this.getParamNameIcon());
    				
    				// extracting builder class
    				ClassLoader classLoader = this.getPluginContextHolder().getPluginManager().getPluginClassLoader(descr);
    				Class editorPanelClass = classLoader.loadClass(panelBuilderParam.valueAsString());
    	            
    	            // intantiating the main plugin object (the panel builder)
    		    	IMEBNEditionPanelBuilder panelBuilder = (IMEBNEditionPanelBuilder)editorPanelClass.newInstance();
    		    	
    		    	// extracting icon
        			ImageIcon icon = null;
        			if (iconParam != null) {
        				URL iconUrl = this.getPluginContextHolder().getPluginManager().getPluginClassLoader(ext.getDeclaringPluginDescriptor()).getResource(iconParam.valueAsString());
        				icon = (iconUrl != null) ? new ImageIcon(iconUrl) : null;
        			}        			
        			if (icon == null) {
        				// use default icon if the icon is not found or invalid
        				icon = IconController.getInstance().getMTheoryNodeIcon();
        			}
    		    	
    		    	// extracting name 
        			String name = "";
        			if (nameParam != null) {
        				name = nameParam.valueAsString();
        			}
        			
        			// extracting description
        			String description = "";
        			if (descriptionParam != null) {
        				description = descriptionParam.valueAsString();
        			}
    		    	
    		    	// filling the loadedComponents
    		    	loadedComponents.add(new MEBNEditionPanelPluginComponents(description, name, icon ,panelBuilder));
    				
    			} catch (Throwable e) {
    				e.printStackTrace();
    				continue;
    			} 
    		}
		} catch (Throwable t) {
			Debug.println(this.getClass(), "Error filling MEBN edition panel's plugins", t);
		}
	    
		this.setLoadedComponents(loadedComponents);
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getCorePluginID()
	 */
	public String getCorePluginID() {
		return corePluginID;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setCorePluginID(java.lang.String)
	 */
	public void setCorePluginID(String corePluginID) {
		this.corePluginID = corePluginID;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getMebnEditionPanelExtensionPointID()
	 */
	public String getMebnEditionPanelExtensionPointID() {
		return mebnEditionPanelExtensionPointID;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setMebnEditionPanelExtensionPointID(java.lang.String)
	 */
	public void setMebnEditionPanelExtensionPointID(
			String mebnEditionPanelExtensionPointID) {
		this.mebnEditionPanelExtensionPointID = mebnEditionPanelExtensionPointID;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getParamNameMEBNEditionPanelBuilder()
	 */
	public String getParamNameMEBNEditionPanelBuilder() {
		return paramNameMEBNEditionPanelBuilder;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setParamNameMEBNEditionPanelBuilder(java.lang.String)
	 */
	public void setParamNameMEBNEditionPanelBuilder(
			String paramNameMEBNEditionPanelBuilder) {
		this.paramNameMEBNEditionPanelBuilder = paramNameMEBNEditionPanelBuilder;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getParamNameName()
	 */
	public String getParamNameName() {
		return paramNameName;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setParamNameName(java.lang.String)
	 */
	public void setParamNameName(String paramNameName) {
		this.paramNameName = paramNameName;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getParamNameIcon()
	 */
	public String getParamNameIcon() {
		return paramNameIcon;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setParamNameIcon(java.lang.String)
	 */
	public void setParamNameIcon(String paramNameIcon) {
		this.paramNameIcon = paramNameIcon;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getParamNameDescription()
	 */
	public String getParamNameDescription() {
		return paramNameDescription;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setParamNameDescription(java.lang.String)
	 */
	public void setParamNameDescription(String paramNameDescription) {
		this.paramNameDescription = paramNameDescription;
	}

	/**
	 * @return the loadedComponents
	 */
	public Collection<IMEBNEditionPanelPluginComponents> getLoadedComponents() {
		return loadedComponents;
	}

	/**
	 * @param loadedComponents the loadedComponents to set
	 */
	public void setLoadedComponents(
			Collection<IMEBNEditionPanelPluginComponents> loadedComponents) {
		this.loadedComponents = loadedComponents;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#getPluginContextHolder()
	 */
	public UnBBayesPluginContextHolder getPluginContextHolder() {
		return pluginContextHolder;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager#setPluginContextHolder(unbbayes.util.extension.manager.UnBBayesPluginContextHolder)
	 */
	public void setPluginContextHolder(
			UnBBayesPluginContextHolder pluginContextHolder) {
		this.pluginContextHolder = pluginContextHolder;
	}

	/**
	 * This is a default implementation of {@link IMEBNEditionPanelPluginComponents}
	 * @author Shou Matsumoto
	 */
	public class MEBNEditionPanelPluginComponents implements IMEBNEditionPanelPluginComponents {
		
		private Map<String, Object> properties = new HashMap<String, Object>();
		private String description;
		private String name;
		private ImageIcon icon;
		private IMEBNEditionPanelBuilder panelBuilder;
		
		/**
		 * Default constructor using parameters
		 * @param description : tool tip text of the tab
		 * @param name : title of the tab 
		 * @param icon : icon for the tab
		 * @param panelBuilder : content builder (builds the JPanel to be inserted as a tab)
		 */
		public MEBNEditionPanelPluginComponents(String description,
				String name, ImageIcon icon,
				IMEBNEditionPanelBuilder panelBuilder) {
			super();
			this.description = description;
			this.name = name;
			this.icon = icon;
			this.panelBuilder = panelBuilder;
		}
		
		/**
		 * @return the properties
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}
		/**
		 * @param properties the properties to set
		 */
		public void setProperties(Map<String, Object> properties) {
			this.properties = properties;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the icon
		 */
		public ImageIcon getIcon() {
			return icon;
		}
		/**
		 * @param icon the icon to set
		 */
		public void setIcon(ImageIcon icon) {
			this.icon = icon;
		}
		/**
		 * @return the panelBuilder
		 */
		public IMEBNEditionPanelBuilder getPanelBuilder() {
			return panelBuilder;
		}
		/**
		 * @param panelBuilder the panelBuilder to set
		 */
		public void setPanelBuilder(IMEBNEditionPanelBuilder panelBuilder) {
			this.panelBuilder = panelBuilder;
		}
		/* (non-Javadoc)
		 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager.IMEBNEditionPanelPluginComponents#getProperty(java.lang.String)
		 */
		public Object getProperty(String key) {
			return this.getProperties().get(key);
		}
		/* (non-Javadoc)
		 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager.IMEBNEditionPanelPluginComponents#setProperty(java.lang.String, java.lang.Object)
		 */
		public void setProperty(String key, Object obj) {
			this.getProperties().put(key, obj);
		}
		
	}

}
