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
	protected ImageIcon searchIcon;
	protected ImageIcon cancelSearchIcon; 
	protected ImageIcon addIconP; 

	protected ImageIcon addAttribute; 
	protected ImageIcon reuseAttribute; 
	
	protected ImageIcon requirementsIcon; 
	protected ImageIcon analysisIcon; 
	
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
//					"/icons/middle.gif"));
		            "/icons/ump.ico"));
			return umpstIcon;
		}
	}	
	
	public ImageIcon getRequirementsIcon() {
		if (requirementsIcon != null) {
			return requirementsIcon;
		} else {
			requirementsIcon = new ImageIcon(getClass().getResource(
					"/icons/requirements.png"));
			return requirementsIcon;
		}
	}	
	
	public ImageIcon getAnalysisDesignIcon() {
		if (analysisIcon != null) {
			return analysisIcon;
		} else {
			analysisIcon = new ImageIcon(getClass().getResource(
					"/icons/analysis.png"));
			return analysisIcon;
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

	public ImageIcon getAddIconP() {
		if (addIconP != null) {
			return addIconP;
		} else {
			addIconP = new ImageIcon(getClass().getResource(
					"/icons/add_p.png"));
			return addIconP;
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
	
//	public ImageIcon getEditIcon() {
//	if (editIcon != null) {
//		return editIcon;
//	} else {
//		editIcon = new ImageIcon(getClass().getResource(
//				"/icons/edit.png"));
//		return editIcon;
//	}
//}	
	
	public ImageIcon getAddAttribute() {
		if (addAttribute != null) {
			return addAttribute;
		} else {
			addAttribute = new ImageIcon(getClass().getResource(
//					"/icons/addAtribute.gif"));
					"/icons/list-add-5.png"));
			return addAttribute;
		}
	}

	public ImageIcon getReuseAttribute() {
		if (reuseAttribute != null) {
			return reuseAttribute;
		} else {
			reuseAttribute = new ImageIcon(getClass().getResource(
//					"/icons/recicleAttribute.gif"));
		             "/icons/arrow-refresh.png"));
			return reuseAttribute;
		}
	}
	
	public ImageIcon getSearch() {
		if (searchIcon != null) {
			return searchIcon;
		} else {
			searchIcon = new ImageIcon(getClass().getResource(
					"/icons/search_p.png"));
			return searchIcon;
		}
	}
	
	public ImageIcon getCancelSearch() {
		if (cancelSearchIcon != null) {
			return cancelSearchIcon;
		} else {
			cancelSearchIcon = new ImageIcon(getClass().getResource(
					"/icons/cancelsearch_p.png"));
			return cancelSearchIcon;
		}
	}
	
	

}
