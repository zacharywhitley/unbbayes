package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;

import unbbayes.draw.DrawEllipse;
import unbbayes.prs.Node;

// FIXME We have to refactor the Node inheritance to separate discrete from continuous and other messy things!
// FIXME GATO no continuous node
public class ContinuousNode extends TreeVariable implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Color color = Color.BLUE;
    private DrawEllipse drawEllipse;
	
	private CNNormalDistribution cnNormalDistribution;
	
	@Override
	public int getType() {
		return Node.CONTINUOUS_NODE_TYPE;
	}
	
	public ContinuousNode() {
		cnNormalDistribution = new CNNormalDistribution(this);
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

}
