/**
 * 
 */
package unbbayes.gui.pomc;

import java.io.File;
import java.io.IOException;

import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.swing.JLabel;

import unbbayes.io.BaseIO;
import unbbayes.prs.Graph;
import unbbayes.util.extension.UnBBayesModule;

/**
 * @author rafaelmezzomo
 *
 */
public class PomcModule extends UnBBayesModule {

	protected void initComponents(){
		
		this.add(new JLabel("Hello Word!"));
	}
	
	
	/**
	 * 
	 */
	public PomcModule() {
		try {
			this.initComponents();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		
		// TODO Auto-generated method stub
		return "Pomc";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		// TODO Auto-generated method stub
		
		//retornar ProjetoPOMC
		return null;
	}

}
