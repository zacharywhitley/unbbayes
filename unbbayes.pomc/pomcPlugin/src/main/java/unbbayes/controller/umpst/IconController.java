package unbbayes.controller.umpst;

import javax.swing.ImageIcon;

/**
 * Created this UMPST specific icon controller in order to allow the addition 
 * of new icons without having to change UnBBayes core.
 * 
 * @author Laecio Santos (laecio@gmail.com)
 *
 */
public class IconController extends unbbayes.controller.IconController {
	
	private static final long serialVersionUID = -8636074503907649076L;

	private static IconController singleton;
	
	protected ImageIcon umpstIcon; 
	protected ImageIcon addIcon; 
	protected ImageIcon deleteIcon; 
	protected ImageIcon editIcon; 
	
	public static IconController getInstance() {
		if (singleton == null) {
			singleton = new IconController();
		}
		return singleton;
	}
	
	public ImageIcon getUmpstIcon() {
		if (umpstIcon != null) {
			return umpstIcon;
		} else {
			umpstIcon = new ImageIcon(getClass().getResource(
					"/icons/middle.gif"));
			return umpstIcon;
		}
	}	
	
	public ImageIcon getAddIcon() {
		if (addIcon != null) {
			return addIcon;
		} else {
			addIcon = new ImageIcon(getClass().getResource(
					"/icons/add.gif"));
			return addIcon;
		}
	}	

	public ImageIcon getDeleteIcon() {
		if (deleteIcon != null) {
			return deleteIcon;
		} else {
			deleteIcon = new ImageIcon(getClass().getResource(
					"/icons/del.gif"));
			return deleteIcon;
		}
	}	
	
	public ImageIcon getEditIcon() {
		if (editIcon != null) {
			return editIcon;
		} else {
			editIcon = new ImageIcon(getClass().getResource(
					"/icons/edit.gif"));
			return editIcon;
		}
	}	
}
