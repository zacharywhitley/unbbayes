package unbbayes.gui.mebn.extension.editor;

import java.util.Collection;

import javax.swing.ImageIcon;

import org.java.plugin.PluginManager;

import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This interface manages MEBN edition panel's plugins
 * for UnBBayes' MEBN module, using the default JPF plugin infrastructure.
 * @author Shou Matsumoto
 *
 */
public interface IMEBNEditionPanelPluginManager {

	/**
	 * This method will initialize this manager
	 */
	public abstract void initialize();

	/**
	 * This method reloads all plugins.
	 */
	public abstract void reloadPlugins();

	/**
	 * This is the base context holder for UnBBayes' plugin framework
	 * @return the pluginContextHolder
	 */
	public UnBBayesPluginContextHolder getPluginContextHolder();

	/**
	 * This is the base context holder for UnBBayes' plugin framework
	 * @param pluginContextHolder the pluginContextHolder to set
	 */
	public void setPluginContextHolder(UnBBayesPluginContextHolder pluginContextHolder) ;

	/**
	 * @return the corePluginID
	 */
	public abstract String getCorePluginID();

	/**
	 * @param corePluginID the corePluginID to set
	 */
	public abstract void setCorePluginID(String corePluginID);

	/**
	 * @return the mebnEditionPanelExtensionPointID
	 */
	public abstract String getMebnEditionPanelExtensionPointID();

	/**
	 * @param mebnEditionPanelExtensionPointID the mebnEditionPanelExtensionPointID to set
	 */
	public abstract void setMebnEditionPanelExtensionPointID(
			String mebnEditionPanelExtensionPointID);

	/**
	 * @return the paramNameMEBNEditionPanelBuilder
	 */
	public abstract String getParamNameMEBNEditionPanelBuilder();

	/**
	 * @param paramNameMEBNEditionPanelBuilder the paramNameMEBNEditionPanelBuilder to set
	 */
	public abstract void setParamNameMEBNEditionPanelBuilder(
			String paramNameMEBNEditionPanelBuilder);

	/**
	 * @return the paramNameName
	 */
	public abstract String getParamNameName();

	/**
	 * @param paramNameName the paramNameName to set
	 */
	public abstract void setParamNameName(String paramNameName);

	/**
	 * @return the paramNameIcon
	 */
	public abstract String getParamNameIcon();

	/**
	 * @param paramNameIcon the paramNameIcon to set
	 */
	public abstract void setParamNameIcon(String paramNameIcon);

	/**
	 * @return the paramNameDescription
	 */
	public abstract String getParamNameDescription();

	/**
	 * @param paramNameDescription the paramNameDescription to set
	 */
	public abstract void setParamNameDescription(String paramNameDescription);
	
	/**
	 * Obtains the result of {@link #reloadPlugins()}.
	 * @return an instance of {@link IMEBNEditionPanelPluginComponents}, which is usually
	 * a data transfer object containing a name, description, a builder and an image icon.
	 */
	public abstract Collection<IMEBNEditionPanelPluginComponents> getLoadedComponents();
	
	/**
	 * This is a data transfer object containing elements extracted from
	 * {@link IMEBNEditionPanelPluginManager#reloadPlugins()}.
	 * It also contains key-mapped property for general purpose objects.
	 * Usually, it is filled by a name, the JPanel builder,
	 * an icon and a tool tip text (description) in order to create
	 * @author Shou Matsumoto
	 * @see IMEBNEditionPanelPluginManager#reloadPlugins()
	 */
	public interface IMEBNEditionPanelPluginComponents {
		/** Obtains the name (title) of the panel*/
		public abstract String getName();
		/** Obtains the description (tool tip text) of the panel */
		public abstract String getDescription();
		/** Obtains a builder object to instantiate a JPanel for MEBN edition */
		public abstract IMEBNEditionPanelBuilder getPanelBuilder();
		/** Obtains an icon to be added to a tabbed panel */
		public abstract ImageIcon getIcon();
		/**
		 * Obtains an object from key.
		 * This is useful if you want to use this object to pass objects through methods
		 * (that is, a data transfer object).
		 * @param key : ID of the objects stored by this object
		 * @return object
		 * @see #setProperty(String, Object)
		 */
		public abstract Object getProperty(String key);
		
		/**
		 * Add a property setting a key.
		 * This is useful if you want to use this object to pass objects through methods
		 * (that is, a data transfer object).
		 * @param key : ID of the objects stored by this object
		 * @param obj : object to be stored
		 * @see #getProperty(String)
		 */
		public abstract void setProperty(String key, Object obj);
	}

}