/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.bn.continuous;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ResourceBundle;

import unbbayes.draw.DrawEllipse;
import unbbayes.prs.Node;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.ResourceController;

// FIXME We have to refactor the Node inheritance to separate discrete from continuous and other messy things!
// FIXME GATO no continuous node
public class ContinuousNode extends TreeVariable implements Serializable {
	
	public final static int MEAN_MARGINAL_INDEX = 0;
	public final static int VARIANCE_MARGINAL_INDEX = 1;

	private static final long serialVersionUID = 1L;
	
	private static Color color = Color.GREEN;
    private DrawEllipse drawEllipse;
	
	private CNNormalDistribution cnNormalDistribution;
	
	private ResourceBundle resource = ResourceController.RS_BN;
	
	@Override
	public int getType() {
		return Node.CONTINUOUS_NODE_TYPE;
	}
	
	public ContinuousNode() {
		cnNormalDistribution = new CNNormalDistribution(this);
		this.appendState(resource.getString("meanName"));
		this.appendState(resource.getString("varianceName"));
		drawEllipse = new DrawEllipse(position, size);
        drawElement.add(drawEllipse);
	}
	
	public CNNormalDistribution getCnNormalDistribution() {
		return cnNormalDistribution;
	}
	
	public static Color getColor() {
		return color;
	}

	public static void setColor(Color color) {
		ContinuousNode.color = color;
	}
	
	public static void setColor(int c) {
		ContinuousNode.color = new Color(c);
	}
	
	@Override
	public void setSelected(boolean b) {
		// Update the DrawEllipse selection state
		drawEllipse.setSelected(b);
		super.setSelected(b);
	}
	
	@Override
	public void paint(Graphics2D graphics) {
    	drawEllipse.setFillColor(getColor());
		super.paint(graphics);
	}

	@Override
	protected void marginal() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addParent(Node parent) throws InvalidParentException {
		if (parent.getType() != Node.PROBABILISTIC_NODE_TYPE && parent.getType() != Node.CONTINUOUS_NODE_TYPE) {
			throw new InvalidParentException(resource.getString("continuousNodeInvalidParentException"));
		}
		super.addParent(parent);
		cnNormalDistribution.refreshParents();
	}
	
	@Override
	public void removeParent(Node parent) {
		super.removeParent(parent);
		cnNormalDistribution.refreshParents();
	}
	
	@Override
	public void addChild(Node child) throws InvalidParentException {
		if (child.getType() != Node.CONTINUOUS_NODE_TYPE) {
			throw new InvalidParentException(resource.getString("continuousNodeInvalidParentException"));
		}
		super.addChild(child);
		cnNormalDistribution.refreshParents();
	}
	
	@Override
	public void removeChild(Node child) {
		// TODO Auto-generated method stub
		super.removeChild(child);
		cnNormalDistribution.refreshParents();
	}

}
