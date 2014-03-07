/**
 * 
 */
package unbbayes.gui.umpst;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import unbbayes.io.BaseIO;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.prs.Graph;
import unbbayes.util.extension.UnBBayesModule;

/**
 * @author rafaelmezzomo
 *
 */
public class UmpstModule extends UnBBayesModule {
	
	private JTabbedPane topTabbedPane;
	
	private MainPanel menuPanel;
	
	public MainPanel getMenuPanel(){
		if(menuPanel == null){
			menuPanel = new MainPanel(this);
		}
		return menuPanel;
	}

	protected void initComponents(){
		
		//this.setTopTabbedPane(new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
		
		this.setContentPane(getMenuPanel());
	}
	
	
	/**
	 * 
	 */
	public UmpstModule() {
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
		return "umpst";
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
	
	
	/**
	 * this is the top level container where all MEBN edition panels (tabs) will be placed
	 * @return the topTabbedPane
	 */
	public JTabbedPane getTopTabbedPane() {
		return topTabbedPane;
	}

	/**
	 * this is the top level container where all MEBN edition panels (tabs) will be placed
	 * @param topTabbedPane the topTabbedPane to set
	 */
	public void setTopTabbedPane(JTabbedPane topTabbedPane) {
		this.topTabbedPane = topTabbedPane;
	}

	public static void main(String... args){
		UmpstModule umpstModule = new UmpstModule(); 
		JFrame jframe = new JFrame(); 
		jframe.setContentPane(umpstModule.getContentPane());
		jframe.pack();
		jframe.setVisible(true); 
	}
	
	
}
