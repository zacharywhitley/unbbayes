package unbbayes.util;

import java.util.ResourceBundle;

import unbbayes.controller.resources.ControllerResources;
import unbbayes.gui.resources.GuiResources;

/**
 * This class is used to facilite the use of the 
 * diverses files of resources of the project. It
 * contains methods to get this resources. 
 * 
 * ...only an experience...
 * 
 * @author Laecio
 */
public class ResourceController {

	public static ResourceBundle RS_CONTROLLER = ResourceBundle
		.getBundle(ControllerResources.class.getName());

	public static ResourceBundle RS_GUI = ResourceBundle
	.getBundle(GuiResources.class.getName());
	
	//TODO change the name of the repetitives resouces
	
	public static ResourceBundle RS_COMPILER = ResourceBundle
	.getBundle(unbbayes.prs.mebn.compiler.resources.Resources.class.getName());

	public static ResourceBundle RS_MEBN = ResourceBundle
	.getBundle(unbbayes.prs.mebn.resources.Resources.class.getName());
	
	public static ResourceBundle RS_SSBN = ResourceBundle
	.getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	 
}
