/**
 * 
 */
package unbbayes.datamining.gui.extension;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.gui.GlobalOptions;
import unbbayes.datamining.gui.UnBMinerFrame;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.prs.Graph;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;

/**
 * @author Shou Matsumoto
 *
 */
public class UnBMinerJPFModule extends UnBBayesModule implements
		UnBBayesModuleBuilder {

	private static final long serialVersionUID = -6973696481752699006L;
	
	private UnBMinerFrame frame;
	
	/**
	 * 
	 */
	public UnBMinerJPFModule() {
		this.setFrame(new UnBMinerFrame(false));
		this.getFrame().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setJMenuBar(this.getFrame().getJMenuBar());

		this.add(this.getFrame().getContentPane());
		
		// hiding inactive, unnecessary and/or broken tool bars and/or menus
		this.getFrame().getJtbView().setVisible(false);
		this.getFrame().getLafMenu().setVisible(false);
		this.getFrame().getTbView().setVisible(false);
//		this.getFrame().getHelpMenu().setVisible(false);
//		this.getFrame().getTbHelp().setVisible(false);
//		this.getFrame().getJtbHelp().setVisible(false);
		
		// resetting view menu, in order to show only the tool bar menu
		this.getFrame().getViewMenu().removeAll();
		this.getFrame().getViewMenu().add(this.getFrame().getTbMenu());
		
		// adding action listeners for toolbars' enable/disable menu, in order to repaint the correct container
		for (Component comp : this.getFrame().getTbMenu().getMenuComponents()) {
			if (comp instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)comp;
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateUI();
						repaint();
					}
				});
			}
		}
		
		// fixing global options and its creation routines, in order to hide the look'n'feel options
		
		// removing the old action listener
		this.getFrame().getPreferences().removeActionListener(this.getFrame().getAlPreferences());
		this.getFrame().getOptionsItem().removeActionListener(this.getFrame().getAlPreferences());
		
		// creating new action listener for our context (no Look'n'feel options)
		this.getFrame().setAlPreferences(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (getFrame().getCurrentGlobalOption( ) == null) {
					// initialize
					getFrame().setCurrentGlobalOption(new GlobalOptions());
					getFrame().getCurrentGlobalOption().setDefaultOptions(
								Options.getInstance().getNumberStatesAllowed(), 
								Options.getInstance().getConfidenceLimit(), 
								getFrame().getDefaultLanguage(), // inherit from the UnBMineFrame
								UIManager.getLookAndFeel().getID()	// set as currently selected Look'n'feel
							);
				}
				
				// removing/hiding/fixing unwanted options
				getFrame().getCurrentGlobalOption().getLookNFeelComboBox().setEnabled(false);
				getFrame().getCurrentGlobalOption().getDefaultLookNFeelLabel().setEnabled(false);
				
				// forcing the current look and feel as the displayed one.
				getFrame().getCurrentGlobalOption().getLookNFeelComboBox().setSelectedItem(UIManager.getLookAndFeel().getID());
				
				// try to add the window and repeat it if it fails
				try {
					getFrame().addWindow(getFrame().getCurrentGlobalOption());
				} catch (Exception x) {
					Debug.println(this.getClass(), x.getMessage(), x);
					try {
						getFrame().addWindow(getFrame().getCurrentGlobalOption());
					} catch (Exception x2) {
						Debug.println(this.getClass(), x2.getMessage(), x2);
						try {
							getFrame().addWindow(getFrame().getCurrentGlobalOption());
						} catch (Exception x3) {
							// the 3rd attempt failed
							x3.printStackTrace();
						}
					}
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		// setting the new action listener
		this.getFrame().getPreferences().addActionListener(this.getFrame().getAlPreferences());
		this.getFrame().getOptionsItem().addActionListener(this.getFrame().getAlPreferences());
		
		
		
		// overwriting the UnBMinerFrame's property that was not allowing us to drag JTB
		this.getFrame().getJtbPreferences().setFloatable(true);
		this.getFrame().getJtbWindow().setFloatable(true);
		this.getFrame().getJtbHelp().setFloatable(true);
		
		this.setTitle(this.getModuleName());
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#setUnbbayesFrame(unbbayes.gui.UnBBayesFrame)
	 */
	public void setUnbbayesFrame(UnBBayesFrame unbbayesFrame) {
		super.setUnbbayesFrame(unbbayesFrame);
		try {
			this.getFrame().setUpperFrame(unbbayesFrame);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}




	/**
	 * @param title
	 */
	public UnBMinerJPFModule(String title) {
		super(title);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "UnBMiner";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		throw new IOException("UnBMiner does not support file management from UnBBayes.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return null;
	}

	/**
	 * @return the frame
	 */
	public UnBMinerFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	protected void setFrame(UnBMinerFrame frame) {
		this.frame = frame;
	}

}
