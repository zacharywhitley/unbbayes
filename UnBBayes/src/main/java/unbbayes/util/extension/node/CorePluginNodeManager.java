/**
 * 
 */
package unbbayes.util.extension.node;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import unbbayes.controller.IconController;
import unbbayes.draw.extension.impl.ClassInstantiationPluginUShapeBuilder;
import unbbayes.draw.extension.impl.DefaultPluginUShape;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.builder.extension.impl.ClassInstantiationPluginNodeBuilder;
import unbbayes.prs.extension.impl.ProbabilisticNodePluginStub;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;
import unbbayes.util.extension.dto.INodeClassDataTransferObject;
import unbbayes.util.extension.dto.impl.NodeDto;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;


/**
 * This class manages the plugin nodes
 * (nodes loaded by plugins)
 * for the UnBBayes' core module.
 * @author Shou Matsumoto
 *
 */
public class CorePluginNodeManager {
	
	// this is a map to store the informations associated to classes extending IPluginNode (which are plugin nodes)
	private Map<Class, INodeClassDataTransferObject> nodeClassToDtoMap = new HashMap<Class, INodeClassDataTransferObject>();
	
	// ID of the plugin where we can find new node declarations
	private static String pluginNodeExtensionPointID;
	static {
		pluginNodeExtensionPointID = ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.node.CorePluginNodeManager.pluginNodeExtensionPointID");
		if (pluginNodeExtensionPointID == null) {
			System.err.println("Error reading PluginNode extension point from application.properties. Using default.");
			pluginNodeExtensionPointID = "PluginNode";
		}
	}

	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of compiler
     */
    private static class SingletonHolder { 
    	private static final CorePluginNodeManager INSTANCE = new CorePluginNodeManager();
    }
	
	/**
	 * the default constructor is made protected in order to make
	 * it easy to extend
	 */
	protected CorePluginNodeManager() {
		this.setNodeClassToDtoMap(new HashMap<Class, INodeClassDataTransferObject>());
	}
	
	/**
	 * Obtains a singleton instance of CorePluginNodeManager
	 * @return a instance of CorePluginNodeManager
	 */
	public static CorePluginNodeManager newInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * this is a map to store the informations associated to classes extending IPluginNode (which are plugin nodes)
	 * @return the nodeClassToDtoMap
	 */
	protected Map<Class, INodeClassDataTransferObject> getNodeClassToDtoMap() {
		return nodeClassToDtoMap;
	}

	/**
	 * this is a map to store the informations associated to classes extending IPluginNode (which are plugin nodes)
	 * @param nodeClassToDtoMap the nodeClassToDtoMap to set
	 */
	protected void setNodeClassToDtoMap(
			Map<Class, INodeClassDataTransferObject> nodeClassToDtoMap) {
		this.nodeClassToDtoMap = nodeClassToDtoMap;
	}
	
	/**
	 * Associates a plugin node's class to its set of aditional information.
	 * This information can be restored by calling {@link #getPluginNodeInformation(Class)}
	 * @param nodeClass
	 * @param dto
	 * @see #getPluginNodeInformation(Class)
	 */
	public void registerNodeClass(Class nodeClass, INodeClassDataTransferObject dto) {
		this.getNodeClassToDtoMap().put(nodeClass, dto);
	}
	
	/**
	 * Obtains a plugin node information registered by {@link #registerNodeClass(Class, INodeClassDataTransferObject)}.
	 * If not found, the plugins will be reloaded once and tried again.
	 * @param nodeClass
	 * @return
	 */
	public INodeClassDataTransferObject getPluginNodeInformation(Class nodeClass) {
		INodeClassDataTransferObject ret = this.getNodeClassToDtoMap().get(nodeClass);
		if (ret == null) {
			// retry reloading plugins
			try {
				this.reloadPluginNode();	
			} catch (Exception e) {
				Debug.println(this.getClass(), "Error reloading plugin at getPluginNodeInformation", e);
			}
			ret = this.getNodeClassToDtoMap().get(nodeClass);
		}
		return ret;
	}
	
	/**
	 * {@link UnBBayesPluginContextHolder} will be used to load the plugin nodes
	 * and fill the plugin informations that can be obtained by
	 * {@link #getPluginNodeInformation(Class)}.
	 */
	public void reloadPluginNode() {
		System.err.println("Plugin nodes functionality is not implemented yet.");
		
		// TODO the below code is a stub
		IconController iconController = IconController.getInstance();
		INodeClassDataTransferObject nodeDto = NodeDto.newInstance();
		nodeDto.setIcon(iconController.getBlueNodeIcon());
		ClassInstantiationPluginNodeBuilder builder = new ClassInstantiationPluginNodeBuilder(ProbabilisticNodePluginStub.class);
		nodeDto.setNodeBuilder(builder);
		ClassInstantiationPluginUShapeBuilder ushapeBuilder = new ClassInstantiationPluginUShapeBuilder(DefaultPluginUShape.class);
		nodeDto.setShapeBuilder(ushapeBuilder);
		IProbabilityFunctionPanelBuilder panelBuilder = new IProbabilityFunctionPanelBuilder () {
			private Node node;
			public JPanel buildProbabilityFunctionEditionPanel() {
				JPanel panel =  new JPanel();
				panel.setLayout(new BorderLayout());
				panel.setBorder(new TitledBorder("This is a panel displayed when a plugin node is selected"));
				panel.add(new JLabel("Name: "), BorderLayout.WEST);
				JLabel field = new JLabel();
				if (node != null) {
					field.setText(node.getName());
				}
				panel.add(field, BorderLayout.CENTER);
				return panel;
			}
			public void setProbabilityFunctionOwner(
					Node node) {
				this.node = node;
			}
			public Node getProbabilityFunctionOwner() {
				return this.node;
			}
		};
		nodeDto.setProbabilityFunctionPanelBuilder(panelBuilder);
		nodeDto.setCursorIcon(iconController.getContextNodeCursor());
		nodeDto.setName("Stub plugin node");
		nodeDto.setDescription("This is just a stub added in order to test plugin functionality...");
		
		this.registerNodeClass(	nodeDto.getNodeBuilder().getNodeClass(), nodeDto);
	}
	
	/**
	 * This is equivalent to {@link #reloadPluginNode()}
	 * @see #reloadPluginNode()
	 */
	public void loadPluginNode() {
		this.reloadPluginNode();
	}
	
	/**
	 * Calls {@link #reloadPluginNode()} and then returns
	 * a set of all loaded plugin node's informations.
	 * @return a collection of all loaded plugin nodes. A change
	 * in this collection should not affect the plugin manager's state.
	 */
	public Collection<INodeClassDataTransferObject> getAllLoadedPluginNodes() {
		try {
			this.reloadPluginNode();
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failed to reload plugin node while getting all loaded plugin nodes", e);
		}
		
		if (this.getNodeClassToDtoMap() == null) {
			System.err.println("Unexpected situation at " + this.getClass().getName() + ": nodeClassToDtoMap == null.");
			// returning an empty collection
			return Collections.EMPTY_SET;
		}
		
		// returning as a hash set in order to avoid duplicate items
		return new HashSet<INodeClassDataTransferObject>(this.getNodeClassToDtoMap().values());
	}

	/**
	 * @return the pluginNodeExtensionPointID
	 */
	protected static String getPluginNodeExtensionPointID() {
		return pluginNodeExtensionPointID;
	}

	/**
	 * @param pluginNodeExtensionPointID the pluginNodeExtensionPointID to set
	 */
	protected static void setPluginNodeExtensionPointID(String pluginNodeID) {
		CorePluginNodeManager.pluginNodeExtensionPointID = pluginNodeID;
	}

}
