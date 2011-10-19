/**
 * 
 */
package unbbayes.gui.mebn.extension.node;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;

import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeDiamond;
import unbbayes.draw.UShapeResidentNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.prs.Node;

/**
 * @author Shou Matsumoto
 *
 */
public class UShapeContinuousResidentNode extends UShapeResidentNode implements IPluginUShape {

	public UShapeContinuousResidentNode() {
		super(null,null, 0,0,100,50);
	}
//	/**
//	 * @param c
//	 * @param x
//	 * @param y
//	 * @param w
//	 * @param h
//	 */
//	public UShapeContinuousResidentNode(UCanvas c, int x, int y, int w, int h) {
//		super(c, x, y, w, h);
//		// TODO Auto-generated constructor stub
//	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShape#getUShape(unbbayes.prs.Node, unbbayes.draw.UCanvas)
	 */
	public UShape getUShape(Node node, UCanvas canvas) {
		this.setNode(node);
		this.setCanvas(canvas);
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.draw.UShapeResidentNode#InitShape()
	 */
	@Override
	public void InitShape() {
		// TODO Auto-generated method stub
		super.InitShape();
		this.setBackColor(Color.GREEN);
	}
	

}
