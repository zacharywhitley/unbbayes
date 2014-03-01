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

	protected ImageIcon listAdd; 
	protected ImageIcon reuseAttribute; 
	
	protected ImageIcon requirementsIcon; 
	protected ImageIcon analysisIcon; 
	
	protected ImageIcon saveObjectIcon; 
	protected ImageIcon returnWithoutSaveIcon; 
	
	protected ImageIcon leftDoubleArrowIcon; 
	protected ImageIcon rigthDoubleArrowIcon; 
	
	protected ImageIcon relationshipIcon; 
	
	protected ImageIcon returnIcon; 
	
	protected ImageIcon cicleGoalIcon; 
	protected ImageIcon cicleHypothesisIcon; 
	protected ImageIcon cicleEntityIcon; 
	protected ImageIcon cicleAttributeIcon; 
	protected ImageIcon cicleRelationshipIcon; 
	
	protected ImageIcon cicleGoalPIcon; 
	
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
	
	public ImageIcon getListAddIcon() {
		if (listAdd != null) {
			return listAdd;
		} else {
			listAdd = new ImageIcon(getClass().getResource(
//					"/icons/addAtribute.gif"));
					"/icons/list-add-5.png"));
			return listAdd;
		}
	}
	
	

	public ImageIcon getReuseAttributeIcon() {
		if (reuseAttribute != null) {
			return reuseAttribute;
		} else {
			reuseAttribute = new ImageIcon(getClass().getResource(
//					"/icons/recicleAttribute.gif"));
		             "/icons/arrow-refresh.png"));
			return reuseAttribute;
		}
	}
	
	public ImageIcon getSearchIcon() {
		if (searchIcon != null) {
			return searchIcon;
		} else {
			searchIcon = new ImageIcon(getClass().getResource(
					"/icons/search_p.png"));
			return searchIcon;
		}
	}
	
	public ImageIcon getCancelSearchIcon() {
		if (cancelSearchIcon != null) {
			return cancelSearchIcon;
		} else {
			cancelSearchIcon = new ImageIcon(getClass().getResource(
					"/icons/cancelsearch_p.png"));
			return cancelSearchIcon;
		}
	}
	
	public ImageIcon getSaveObjectIcon() {
		if (saveObjectIcon != null) {
			return saveObjectIcon;
		} else {
			saveObjectIcon = new ImageIcon(getClass().getResource(
					"/icons/document-save-2.png"));
			return saveObjectIcon;
		}
	}
	
	public ImageIcon getReturnWithoutSaveIcon() {
		if (returnWithoutSaveIcon != null) {
			return returnWithoutSaveIcon;
		} else {
			returnWithoutSaveIcon = new ImageIcon(getClass().getResource(
					"/icons/document-revert-3.png"));
			return returnWithoutSaveIcon;
		}
	}
	
	public ImageIcon getLeftDoubleArrowIcon() {
		if (leftDoubleArrowIcon != null) {
			return leftDoubleArrowIcon;
		} else {
			leftDoubleArrowIcon = new ImageIcon(getClass().getResource(
					"/icons/arrow-left-double-3.png"));
			return leftDoubleArrowIcon;
		}
	}
	
	public ImageIcon getRigthDoubleArrowIcon() {
		if (rigthDoubleArrowIcon != null) {
			return rigthDoubleArrowIcon;
		} else {
			rigthDoubleArrowIcon = new ImageIcon(getClass().getResource(
					"/icons/arrow-right-double-3.png"));
			return rigthDoubleArrowIcon;
		}
	}
	
	public ImageIcon getRelationshipIcon() {
		if (relationshipIcon != null) {
			return relationshipIcon;
		} else {
			relationshipIcon = new ImageIcon(getClass().getResource(
					"/icons/layer-lower.png"));
			return relationshipIcon;
		}
	}
	
	public ImageIcon getReturnIcon() {
		if (returnIcon != null) {
			return returnIcon;
		} else {
			returnIcon = new ImageIcon(getClass().getResource(
					"/icons/edit-undo-3.png"));
			return returnIcon;
		}
	}
	
//	protected ImageIcon cicleGoalIcon; 
//	protected ImageIcon cicleHypothesisIcon; 
//	protected ImageIcon cicleEntityIcon; 
//	protected ImageIcon cicleAttributeIcon; 
//	protected ImageIcon cicleRelationshipIcon; 
	
	
	public ImageIcon getCicleGoalIcon() {
		if (cicleGoalIcon != null) {
			return cicleGoalIcon;
		} else {
			cicleGoalIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_blue_goal.png"));
			return cicleGoalIcon;
		}
	}
	
	public ImageIcon getCicleGoalPIcon() {
		if (cicleGoalPIcon != null) {
			return cicleGoalPIcon;
		} else {
			cicleGoalPIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_blue_goal-P.png"));
			return cicleGoalPIcon;
		}
	}
	
	
	
	
	public ImageIcon getCicleHypothesisIcon() {
		if (cicleHypothesisIcon != null) {
			return cicleHypothesisIcon;
		} else {
			cicleHypothesisIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_grey_hypothesis.png"));
			return cicleHypothesisIcon;
		}
	}
	
	public ImageIcon getCicleEntityIcon() {
		if (cicleEntityIcon != null) {
			return cicleEntityIcon;
		} else {
			cicleEntityIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_green_entity.png"));
			return cicleEntityIcon;
		}
	}
	
	public ImageIcon getCicleAttributeIcon() {
		if (cicleAttributeIcon != null) {
			return cicleAttributeIcon;
		} else {
			cicleAttributeIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_yellow_attribute.png"));
			return cicleAttributeIcon;
		}
	}
	
	public ImageIcon getCicleRelationshipIcon() {
		if (cicleRelationshipIcon != null) {
			return cicleRelationshipIcon;
		} else {
			cicleRelationshipIcon = new ImageIcon(getClass().getResource(
					"/icons/circle_orange_relationship.png"));
			return cicleRelationshipIcon;
		}
	}
	
	
	

}
