/**
 * 
 */
package unbbayes.controller.oobn;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JInternalFrame;

import unbbayes.controller.NetworkController;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.gui.oobn.OOBNClassWindow;
import unbbayes.gui.oobn.OOBNWindow;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.io.oobn.impl.DefaultOOBNIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.compiler.IOOBNCompiler;
import unbbayes.prs.oobn.compiler.impl.OOBNToSingleAgentMSBNCompiler;
import unbbayes.prs.oobn.impl.DefaultOOBNClass;
import unbbayes.prs.oobn.impl.OOBNClassSingleEntityNetworkWrapper;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNController extends NetworkController {

	// the index of the very firs network/class in a project
	private static int FIRST_NETWORK_INDEX = 0;
	
//	private OOBNClassController classController = null;
	
	protected OOBNController() {
		// TODO Auto-generated constructor stub
		super((SingleEntityNetwork)null, null);
//		try{
//			this.classController = OOBNClassController.newInstance(null,null);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
	}
	
	private IObjectOrientedBayesianNetwork oobn;
	private OOBNWindow window;
	private OOBNClassWindow active;
	
	private IOOBNClass selectedClass = null;
	
	private UnBBayesFrame upperUnBBayesFrame = null;
	
	/**
	 * Creates a controller that controls a MSBNWindow.
	 * The MSBNWindows is created.
	 * @param oobn The oobn to display.
	 */
	protected OOBNController(IObjectOrientedBayesianNetwork oobn, UnBBayesFrame upperUnBBayesFrame) {
		super(oobn.getSingleEntityNetwork(),null);
		this.oobn = oobn;
		this.setUpperUnBBayesFrame(upperUnBBayesFrame);
		this.window = OOBNWindow.newInstance(oobn, this);
		this.init();	
//		upperUnBBayesFrame.addWindow(this.getPanel());
		
//		this.classController = OOBNClassController.newInstance(oobn.getSingleEntityNetwork(), this.getScreen());
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
//		active.getNetWindowEdition().getBtnCompile().setVisible(false);
//		active.getNetWindowCompilation().getEditMode().setVisible(false);
		window.getContentPane().add(active.getContentPane(), BorderLayout.CENTER);
		window.updateUI();
	}
	
	
	/**
	 * Adds a new oobn class to this project and updates the OOBN model representation.
	 * @param name: name/title of the new class
	 */
	public void addNewOOBNClass(String name) {
		this.addOOBNClass(DefaultOOBNClass.newInstance(name));
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
	 * Compiles the currently active OOBN class
	 * @see OOBNController#getActive()
	 */
	public AbstractMSBN compileActiveOOBNClassToMSBN() {
		IOOBNCompiler compiler = OOBNToSingleAgentMSBNCompiler.newInstance();
		AbstractMSBN msbn = (AbstractMSBN)compiler.compile(this.getOobn(),OOBNClassSingleEntityNetworkWrapper.newInstance((SingleEntityNetwork)this.getActive().getNetworkController().getNetwork()));
		return msbn;
	}
	
	
	/**
	 * Loads a new OOBN class from file
	 * @param file
	 */
	public Set<IOOBNClass> loadOOBNClassesFromFile(File file) {
		// TODO implement this
		Debug.println(this.getClass(), "File loader not yet implemented");
		Set<IOOBNClass> ret = new HashSet<IOOBNClass>();
		
		IObjectOrientedBayesianNetworkIO io = DefaultOOBNIO.newInstance();
		
		try {
//			IOOBNClass loadedOOBN = (IOOBNClass)io.load(file);		
			IObjectOrientedBayesianNetwork returnedOOBN = io.loadOOBN(file);
			for (IOOBNClass loadedOOBN : returnedOOBN.getOOBNClassList()) {
				ret.add(loadedOOBN);
			}			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
//		ret.add(DefaultOOBNClass.newInstance("STUB"));
//		RET.ADD(DEFAULTOOBNCLASS.NEWINSTANCE("STUB1"));
//		RET.ADD(DEFAULTOOBNCLASS.NEWINSTANCE("STUB2"));
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
	
	/**
	 * Searches for a OOBNNode by giving class name and node name
	 * @param className
	 * @param nodeName
	 * @return
	 */
	public OOBNNodeGraphicalWrapper getOOBNNodeFromNames(String className, String nodeName) {
		for (IOOBNClass oobnClass : this.getOobn().getOOBNClassList()) {
			if (nodeName.equals(oobnClass.getClassName())) {
				for (IOOBNNode node : oobnClass.getAllNodes()) {
					if (nodeName.equals(node.getName())) {
						return OOBNNodeGraphicalWrapper.newInstance(node);
					}
				}
			}
		}
		return null;
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
	public OOBNClassWindow getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(OOBNClassWindow active) {
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

//	/**
//	 * @return the classController
//	 */
//	public OOBNClassController getClassController() {
//		return classController;
//	}
//
//	/**
//	 * @param classController the classController to set
//	 */
//	public void setClassController(OOBNClassController classController) {
//		this.classController = classController;
//	}

	/**
	 * @return the selectedClass
	 */
	public IOOBNClass getSelectedClass() {
		return selectedClass;
	}

	/**
	 * @param selectedClass the selectedClass to set
	 */
	public void setSelectedClass(IOOBNClass selectedClass) {
		this.selectedClass = selectedClass;
	}



	
	
	
	
}
