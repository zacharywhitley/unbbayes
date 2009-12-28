/**
 * 
 */
package unbbayes.util.extension.dto.impl;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import unbbayes.draw.UShape;
import unbbayes.gui.table.extension.IPluginNodeProbabilityFunctionPanel;
import unbbayes.prs.INode;
import unbbayes.util.extension.dto.INodeDataTransferObject;

/**
 * A simple implementation of {@link INodeDataTransferObject}
 * @author Shou Matsumoto
 *
 */
public class NodeDto implements INodeDataTransferObject {

	private INode node;
	private UShape shape;
	private ImageIcon icon;
	private IPluginNodeProbabilityFunctionPanel panel;
	private Map<String, Object> map;
	private ImageIcon cursorIcon;
	
	/**
	 * The main constructor is not public. Use {@link #newInstance()} instead.
	 * This is kept protected in order to permit extension.
	 */
	protected NodeDto() {}
	
	public static NodeDto newInstance() {
		NodeDto ret = new NodeDto();
		ret.map = new HashMap<String, Object>();
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getIcon()
	 */
	public ImageIcon getIcon() {
		return this.icon;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getNode()
	 */
	public INode getNode() {
		return this.node;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getProbabilityFunctionPanelHolder()
	 */
	public IPluginNodeProbabilityFunctionPanel getProbabilityFunctionPanelHolder() {
		return this.panel;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getShape()
	 */
	public UShape getShape() {
		return this.shape;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setIcon(javax.swing.ImageIcon)
	 */
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setNode(unbbayes.prs.INode)
	 */
	public void setNode(INode node) {
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setProbabilityFunctionPanelHolder(unbbayes.gui.table.extension.IPluginNodeProbabilityFunctionPanel)
	 */
	public void setProbabilityFunctionPanelHolder(
			IPluginNodeProbabilityFunctionPanel panelHolder) {
		this.panel = panelHolder;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setShape(unbbayes.draw.UShape)
	 */
	public void setShape(UShape shape) {
		this.shape = shape;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getObject(java.lang.String)
	 */
	public Object getObject(String key) {
		return this.map.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setObject(java.lang.String, java.lang.Object)
	 */
	public void setObject(String key, Object object) {
		if (object == null) {
			this.map.remove(key);
		} else {
			this.map.put(key, object);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#getCursorIcon()
	 */
	public ImageIcon getCursorIcon() {
		return cursorIcon;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.dto.INodeDataTransferObject#setCursorIcon(javax.swing.ImageIcon)
	 */
	public void setCursorIcon(ImageIcon cursorIcon) {
		this.cursorIcon = cursorIcon;
	}

}
