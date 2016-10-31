/**
 * 
 */
package unbbayes.gui.option.extension;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import unbbayes.gui.NetworkWindow;
import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.UniformMarkovNetJunctionTreeAlgorithm;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class UniformMarkovNetJunctionTreeOptionPanel extends JunctionTreeOptionPanel {

	private static final long serialVersionUID = 7309239877237682988L;
	
	/** Default component listener that will remove the direction of arcs in network when component is shown, and re-add direction when component is hidden.
	 *  It's used in {@link #commitChanges()} in order to remove direction of arcs when {@link NetworkWindow#getNetWindowCompilation()} is shown.*/
	private ComponentListener defaultArcDirectionComponentListener  = new ComponentListener() {
		
		public void componentShown(ComponentEvent e) {
			// only make change if currently selected algorithm is the one used by this algorithm
			if (getMediator() != null) {
				if (getMediator().getInferenceAlgorithm() != getInferenceAlgorithm()) {
					Debug.println(getClass(), "Current algorithm is not the one managed by this plug-in");
					NetworkWindow networkWindow = getMediator().getScreen();
					if (networkWindow == null) {
						return;
					}
					// make sure component listener is not invoked again
					networkWindow.getNetWindowCompilation().removeComponentListener(getDefaultArcDirectionComponentListener());
					return;
				}
			}
			Debug.println(getClass(), "Removing direction of arcs");
			if (getInferenceAlgorithm() != null 
					&& getInferenceAlgorithm().getNetwork() != null
					&& getInferenceAlgorithm().getNetwork().getEdges() != null) {
				for (Edge edge : getInferenceAlgorithm().getNetwork().getEdges()) {
					edge.setDirection(false);
				}
			}
		}
		public void componentHidden(ComponentEvent e) {
			// only make change if currently selected algorithm is the one used by this algorithm
			if (getMediator() != null) {
				if (getMediator().getInferenceAlgorithm() != getInferenceAlgorithm()) {
					Debug.println(getClass(), "Current algorithm is not the one managed by this plug-in");
					return;
				}
			}
			Debug.println(getClass(), "Including direction of arcs");
			if (getInferenceAlgorithm() != null 
					&& getInferenceAlgorithm().getNetwork() != null
					&& getInferenceAlgorithm().getNetwork().getEdges() != null) {
				for (Edge edge : getInferenceAlgorithm().getNetwork().getEdges()) {
					edge.setDirection(true);
				}
			}
		}
		public void componentResized(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
	};

	/**
	 * 
	 */
	public UniformMarkovNetJunctionTreeOptionPanel() {
		super();
		UniformMarkovNetJunctionTreeAlgorithm algorithmToSet = new UniformMarkovNetJunctionTreeAlgorithm();
		algorithmToSet.setOptionPanel(this);
		this.setInferenceAlgorithm(algorithmToSet);
		this.setName("UniformMarkovNetJunctionTree");
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.option.JunctionTreeOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		super.commitChanges();
		if (getMediator() != null) {
			NetworkWindow networkWindow = getMediator().getScreen();
			if (networkWindow == null) {
				return;
			}
			// make sure component listener is not included twice
			networkWindow.getNetWindowCompilation().removeComponentListener(getDefaultArcDirectionComponentListener());
			networkWindow.getNetWindowCompilation().addComponentListener(getDefaultArcDirectionComponentListener());
		}
	}
	
	

	/**
	 * @return the defaultArcDirectionComponentListener
	 */
	public ComponentListener getDefaultArcDirectionComponentListener() {
		return defaultArcDirectionComponentListener;
	}

	/**
	 * @param defaultArcDirectionComponentListener the defaultArcDirectionComponentListener to set
	 */
	public void setDefaultArcDirectionComponentListener(
			ComponentListener defaultArcDirectionComponentListener) {
		this.defaultArcDirectionComponentListener = defaultArcDirectionComponentListener;
	}

	
	

}
