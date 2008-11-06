/**
 * 
 */
package unbbayes.controller;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JInternalFrame;

import unbbayes.gui.NetworkWindow;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.gui.oobn.OOBNClassWindow;
import unbbayes.gui.oobn.OOBNWindow;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.impl.BasicOOBNClass;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNController {

	// the index of the very firs network/class in a project
	private static int FIRST_NETWORK_INDEX = 0;
	
	private OOBNController() {
		// TODO Auto-generated constructor stub
	}
	
	private IObjectOrientedBayesianNetwork oobn;
	private OOBNWindow window;
	private NetworkWindow active;
	
	private UnBBayesFrame upperUnBBayesFrame = null;
	
	/**
	 * Creates a controller that controls a MSBNWindow.
	 * The MSBNWindows is created.
	 * @param oobn The oobn to display.
	 */
	private OOBNController(IObjectOrientedBayesianNetwork oobn, UnBBayesFrame upperUnBBayesFrame) {
		this.oobn = oobn;
		this.setUpperUnBBayesFrame(upperUnBBayesFrame);
		this.window = new OOBNWindow(oobn, this);
		this.init();	

		upperUnBBayesFrame.addWindow(this.getPanel());
	}
	
	/**
	 * Creates a controller that controls a MSBNWindow.
	 * The MSBNWindows is created.
	 * @param oobn The oobn to display.
	 * @param upperContainer: the container which is going to be hosting the OOBN's internal window
	 */
	public static OOBNController newInstance(IObjectOrientedBayesianNetwork oobn, UnBBayesFrame upperContainer){
		return new OOBNController(oobn, upperContainer);
	}
	
	/**
	 * Gets the OOBNWindow associated with this controller.
	 * @return JInternalFrame
	 */
	public JInternalFrame getPanel() {
		return this.window;	
	}
	
	
	
	/**
	 * Initializes the controller
	 */
	private void init() {
		this.window.setController(this);
		this.window.getNetList().setSelectedIndex(0);
		if (this.getOobn().getOOBNClassCount() > 0) {
			this.changeActiveOOBNClass(OOBNClassWindow.newInstance(this.getOobn().getOOBNClassList().get(FIRST_NETWORK_INDEX)));
		}	
	}

	
	
	/**
	 * Sets the active OOBN class window as 
	 * @param newWindow
	 */
	public void changeActiveOOBNClass(OOBNClassWindow newWindow) {				
		if (active != null) {
			SingleEntityNetwork net = active.getSingleEntityNetwork();
			for (int i = 0; i < net.getNodeCount(); i++) {
				net.getNodeAt(i).setSelected(false);			            	
			}
			window.getContentPane().remove(active.getContentPane());			
		}	
		
		active = newWindow;
		if (newWindow == null) {
			return;			
		}
		active.getNetWindowEdition().getBtnCompile().setVisible(false);
		active.getNetWindowCompilation().getEditMode().setVisible(false);
		window.getContentPane().add(active.getContentPane(), BorderLayout.CENTER);
		window.updateUI();
	}
	
	
	/**
	 * Adds a new oobn class to this project and updates the OOBN model representation.
	 * @param name: name/title of the new class
	 */
	public void addNewOOBNClass(String name) {
		this.addOOBNClass(BasicOOBNClass.newInstance(name));
	}
	
	/**
	 * Remove a class from currently controlled oobn
	 * @param index: index from oobnClassList
	 */
	public void removeClassAt(int index){
		
		try{
			this.getOobn().getOOBNClassList().remove(index);
		} catch (Exception e) {
			// it is probably NullPointerException or ArrayIndexOutOfBoundException
			// ignore it
			Debug.println(this.getClass(), "Trying to remove invalid OOBN class", e);
		}
		
		if (this.getOobn().getOOBNClassCount() <= 0) {
			// if  we are removing the only class within project...
			// select nothing as active (since no class is present anymore)
			this.changeActiveOOBNClass(null);
		} else {
			// if there are more elements, select the first as active
			this.changeActiveOOBNClass(OOBNClassWindow.newInstance(getOobn().getOOBNClassList().get(0)));
		}
		
		
	}
	
	
	
	
	
	/**
	 * Compilers the currently active OOBN class
	 * @see OOBNController#getActive()
	 */
	public void compileActiveOOBNClass() {
		// TODO implement this
		Debug.println(this.getClass(), "Compiler not yet implemented");
	}
	
	
	/**
	 * Loads a new OOBN class from file
	 * @param file
	 */
	public Set<IOOBNClass> loadOOBNClassesFromFile(File file) {
		// TODO implement this
		Debug.println(this.getClass(), "File loader not yet implemented");
		Set<IOOBNClass> ret = new HashSet<IOOBNClass>();
		ret.add(BasicOOBNClass.newInstance("Stub"));
		ret.add(BasicOOBNClass.newInstance("Stub1"));
		ret.add(BasicOOBNClass.newInstance("Stub2"));
		return ret;
	}
	
	/**
	 * Adds a new oobn class to this project and updates the OOBN model representation.
	 * @param newClass:new oobn class to be added
	 */
	public void addOOBNClass(IOOBNClass newClass) {
		this.getOobn().getOOBNClassList().add(newClass);
		if (getOobn().getOOBNClassList().size() == 1) {
			// if this is the first time we add a classs, change the active class to it.
			this.changeActiveOOBNClass(OOBNClassWindow.newInstance(getOobn().getOOBNClassList().get(0)));					
		}
	}
	
	/**
	 * Searches for classes and detects if there is a class with same name
	 * @param className: a name to find
	 * @return: true if there is a class with such name already. False otherwise.
	 * @throws NullPointerException if className == null
	 */
	public boolean containsOOBNClassByName(String className) {
		for (IOOBNClass oobnClass : this.getOobn().getOOBNClassList()) {
			if (className.equals(oobnClass.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	
	// getters and setters
	
	
	/**
	 * @return the oobn
	 */
	public IObjectOrientedBayesianNetwork getOobn() {
		return oobn;
	}

	/**
	 * @param oobn the oobn to set
	 */
	public void setOobn(IObjectOrientedBayesianNetwork oobn) {
		this.oobn = oobn;
	}

	/**
	 * @return the window
	 */
	public OOBNWindow getWindow() {
		return window;
	}

	/**
	 * @param window the window to set
	 */
	public void setWindow(OOBNWindow window) {
		this.window = window;
	}

	/**
	 * @return the active
	 */
	public NetworkWindow getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(NetworkWindow active) {
		this.active = active;
	}

	/**
	 * @return the upperUnBBayesFrame, container of the OOBNWindow
	 */
	public UnBBayesFrame getUpperUnBBayesFrame() {
		return upperUnBBayesFrame;
	}

	/**
	 * @param upperUnBBayesFrame the upperUnBBayesFrame to set, container of the OOBNWindow
	 */
	public void setUpperUnBBayesFrame(UnBBayesFrame upperUnBBayesFrame) {
		this.upperUnBBayesFrame = upperUnBBayesFrame;
	}



	
	
	
	
}
