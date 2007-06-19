package unbbayes.controller;

import java.io.Serializable;

import javax.swing.ImageIcon;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0 13/04/2003
 */

public class IconController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static IconController singleton;

	protected ImageIcon metalIcon;

	protected ImageIcon motifIcon;

	protected ImageIcon windowsIcon;

	protected ImageIcon cascadeIcon;

	protected ImageIcon tileIcon;

	protected ImageIcon helpIcon;

	protected ImageIcon globalOptionIcon;

	protected ImageIcon printIcon;

	protected ImageIcon visualizeIcon;

	protected ImageIcon openIcon;

	protected ImageIcon compileIcon;

	protected ImageIcon saveIcon;

	protected ImageIcon returnIcon;

	protected ImageIcon openMetaphorIcon;

	protected ImageIcon saveMetaphorIcon;

	protected ImageIcon diagnosticMetaphorIcon;

	protected ImageIcon openMetaphorRollOverIcon;

	protected ImageIcon saveMetaphorRollOverIcon;

	protected ImageIcon diagnosticMetaphorRollOverIcon;

	protected ImageIcon yesStateIcon;

	protected ImageIcon noStateIcon;

	protected ImageIcon emptyStateIcon;

	protected ImageIcon moreIcon;

	protected ImageIcon lessIcon;

	protected ImageIcon folderSmallIcon;

	protected ImageIcon colapseIcon;

	protected ImageIcon expandIcon;

	protected ImageIcon propagateIcon;

	protected ImageIcon printTableIcon;

	protected ImageIcon printPreviewTableIcon;

	protected ImageIcon edgeIcon;

	protected ImageIcon printNetIcon;

	protected ImageIcon printPreviewNetIcon;

	protected ImageIcon saveNetIcon;

	protected ImageIcon folderSmallDisabledIcon;

	protected ImageIcon yellowBallIcon;

	protected ImageIcon greenBallIcon;
	
	protected ImageIcon grayBorderIcon; 
	
	protected ImageIcon grayBoxIcon; 	

	protected ImageIcon arffFileIcon = new ImageIcon(getClass().getResource(
			"/icons/arff-file.gif"));

	protected ImageIcon txtFileIcon = new ImageIcon(getClass().getResource(
			"/icons/txt-file.gif"));

	protected ImageIcon netFileIcon = new ImageIcon(getClass().getResource(
			"/icons/net-file.gif"));

	protected ImageIcon deleteFolderIcon;

	protected ImageIcon renameFolderIcon;

	protected ImageIcon addFolderIcon;

	protected ImageIcon editIcon;

	protected ImageIcon newIcon;
	
	protected ImageIcon newBNIcon; 
	
	protected ImageIcon newMSBNIcon; 
	
	protected ImageIcon newMEBNIcon; 

	protected ImageIcon informationIcon;

	protected ImageIcon initializeIcon;

	protected ImageIcon ellipsisIcon;

	protected ImageIcon decisionNodeIcon;

	protected ImageIcon utilityNodeIcon;

	protected ImageIcon selectionIcon;

	protected ImageIcon saveTableIcon;

	protected ImageIcon hierarchyIcon;

	protected ImageIcon gridIcon;

	protected ImageIcon fillIcon;

	protected ImageIcon resetSizeIcon;

	protected ImageIcon openModelIcon;
	
	protected ImageIcon learningIcon;
	
	
	/* ---------- MEBN Icons -------------- */

	protected ImageIcon contextNodeIcon;

	protected ImageIcon inputNodeIcon;
	
	protected ImageIcon residentNodeIcon;	
	
	protected ImageIcon mfragIcon; 
	
	protected ImageIcon borderResidentNodeIcon; 
	
	protected ImageIcon borderInputNodeIcon; 
	
	protected ImageIcon borderContextNodeIcon; 
	
	protected ImageIcon borderMfragIcon; 
	
	protected ImageIcon boxResidentNodeIcon; 
	
	protected ImageIcon boxInputNodeIcon; 
	
	protected ImageIcon boxContextNodeIcon; 
	
	protected ImageIcon boxMfragIcon; 	
	
	protected ImageIcon eyeIcon; 
	
	protected ImageIcon xIcon; 
	
	protected ImageIcon orangeNodeIcon; 
	
	protected ImageIcon greenNodeIcon; 
	
	protected ImageIcon blueNodeIcon; 
	
	protected ImageIcon grayNodeIcon; 
	
	protected ImageIcon yellowNodeIcon; 
	
	protected ImageIcon mTheoryNodeIcon; 
	
	protected ImageIcon setBoxIcon; 
	
	protected ImageIcon functIcon; 
	
	protected ImageIcon stateIcon; 
	
	//operators

	protected ImageIcon andIcon; 
	
	protected ImageIcon orIcon; 
	
	protected ImageIcon notIcon; 
	
	protected ImageIcon equalIcon; 
	
	protected ImageIcon impliesIcon; 
	
	protected ImageIcon forallIcon; 
	
	protected ImageIcon existsIcon; 
	
	protected ImageIcon iffIcon; 
	
	protected ImageIcon entityNodeIcon; 
	
	protected ImageIcon ovariableNodeIcon; 
	
	protected ImageIcon nodeNodeIcon; 
	
	protected ImageIcon skolenNodeIcon; 
		
	protected ImageIcon emptyNodeIcon; 	
	
	protected ImageIcon downIcon; 	
	
	protected ImageIcon upIcon; 
	
	protected ImageIcon boxVariablesIcon; 		
	
	protected ImageIcon booleanIcon; 	
	
	
	public static IconController getInstance() {
		if (singleton == null) {
			singleton = new IconController();
		}
		return singleton;
	}

	protected IconController() {
	}

	public ImageIcon getMetalIcon() {
		if (metalIcon != null) {
			return metalIcon;
		} else {
			metalIcon = new ImageIcon(getClass()
					.getResource("/icons/metal.gif"));
			return metalIcon;
		}
	}

	public ImageIcon getMotifIcon() {
		if (motifIcon != null) {
			return motifIcon;
		} else {
			motifIcon = new ImageIcon(getClass()
					.getResource("/icons/motif.gif"));
			return motifIcon;
		}
	}

	public ImageIcon getWindowsIcon() {
		if (windowsIcon != null) {
			return windowsIcon;
		} else {
			windowsIcon = new ImageIcon(getClass().getResource(
					"/icons/windows.gif"));
			return windowsIcon;
		}
	}

	public ImageIcon getCascadeIcon() {
		if (cascadeIcon != null) {
			return cascadeIcon;
		} else {
			cascadeIcon = new ImageIcon(getClass().getResource(
					"/icons/cascade.gif"));
			return cascadeIcon;
		}
	}

	public ImageIcon getTileIcon() {
		if (tileIcon != null) {
			return tileIcon;
		} else {
			tileIcon = new ImageIcon(getClass().getResource("/icons/tile.gif"));
			return tileIcon;
		}
	}

	public ImageIcon getHelpIcon() {
		if (helpIcon != null) {
			return helpIcon;
		} else {
			helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
			return helpIcon;
		}
	}

	public ImageIcon getGlobalOptionIcon() {
		if (globalOptionIcon != null) {
			return globalOptionIcon;
		} else {
			globalOptionIcon = new ImageIcon(getClass().getResource(
					"/icons/preferences-system.png"));
			return globalOptionIcon;
		}
	}

	public ImageIcon getPrintIcon() {
		if (printIcon != null) {
			return printIcon;
		} else {
			printIcon = new ImageIcon(getClass()
					.getResource("/icons/document-print.png"));
			return printIcon;
		}
	}

	public ImageIcon getVisualizeIcon() {
		if (visualizeIcon != null) {
			return visualizeIcon;
		} else {
			visualizeIcon = new ImageIcon(getClass().getResource(
					"/icons/visualize.gif"));
			return visualizeIcon;
		}
	}

	public ImageIcon getCompileIcon() {
		if (compileIcon != null) {
			return compileIcon;
		} else {
			compileIcon = new ImageIcon(getClass().getResource(
					"/icons/compile.gif"));
			return compileIcon;
		}
	}

	public ImageIcon getOpenIcon() {
		if (openIcon != null) {
			return openIcon;
		} else {
			openIcon = new ImageIcon(getClass().getResource("/icons/document-open.png"));
			return openIcon;
		}
	}

	public ImageIcon getSaveIcon() {
		if (saveIcon != null) {
			return saveIcon;
		} else {
			saveIcon = new ImageIcon(getClass().getResource("/icons/document-save.png"));
			return saveIcon;
		}
	}

	public ImageIcon getReturnIcon() {
		if (returnIcon != null) {
			return returnIcon;
		} else {
			returnIcon = new ImageIcon(getClass().getResource(
					"/icons/return.gif"));
			return returnIcon;
		}
	}

	public ImageIcon getDiagnosticMetaphorIcon() {
		if (diagnosticMetaphorIcon != null) {
			return diagnosticMetaphorIcon;
		} else {
			diagnosticMetaphorIcon = new ImageIcon(getClass().getResource(
					"/icons/diagnostic-metaphor.gif"));
			return diagnosticMetaphorIcon;
		}
	}

	public ImageIcon getDiagnosticMetaphorRollOverIcon() {
		if (diagnosticMetaphorRollOverIcon != null) {
			return diagnosticMetaphorRollOverIcon;
		} else {
			diagnosticMetaphorRollOverIcon = new ImageIcon(getClass()
					.getResource("/icons/diagnostics-metaphor.gif"));
			return diagnosticMetaphorRollOverIcon;
		}
	}

	public ImageIcon getOpenMetaphorIcon() {
		if (openMetaphorIcon != null) {
			return openMetaphorIcon;
		} else {
			openMetaphorIcon = new ImageIcon(getClass().getResource(
					"/icons/open-metaphor.gif"));
			return openMetaphorIcon;
		}
	}

	public ImageIcon getOpenMetaphorRollOverIcon() {
		if (openMetaphorRollOverIcon != null) {
			return openMetaphorRollOverIcon;
		} else {
			openMetaphorRollOverIcon = new ImageIcon(getClass().getResource(
					"/icons/opens-metaphor.gif"));
			return openMetaphorRollOverIcon;
		}
	}

	public ImageIcon getSaveMetaphorIcon() {
		if (saveMetaphorIcon != null) {
			return saveMetaphorIcon;
		} else {
			saveMetaphorIcon = new ImageIcon(getClass().getResource(
					"/icons/save-metaphor.gif"));
			return saveMetaphorIcon;
		}
	}

	public ImageIcon getSaveMetaphorRollOverIcon() {
		if (saveMetaphorRollOverIcon != null) {
			return saveMetaphorRollOverIcon;
		} else {
			saveMetaphorRollOverIcon = new ImageIcon(getClass().getResource(
					"/icons/saves-metaphor.gif"));
			return saveMetaphorRollOverIcon;
		}
	}

	public ImageIcon getEmptyStateIcon() {
		if (emptyStateIcon != null) {
			return emptyStateIcon;
		} else {
			emptyStateIcon = new ImageIcon(getClass().getResource(
					"/icons/empty-state.gif"));
			return emptyStateIcon;
		}
	}

	public ImageIcon getFolderSmallIcon() {
		if (folderSmallIcon != null) {
			return folderSmallIcon;
		} else {
			folderSmallIcon = new ImageIcon(getClass().getResource(
					"/icons/folder-small.gif"));
			return folderSmallIcon;
		}
	}

	public ImageIcon getMoreIcon() {
		if (moreIcon != null) {
			return moreIcon;
		} else {
			moreIcon = new ImageIcon(getClass().getResource("/icons/list-add.png"));
			return moreIcon;
		}
	}

	public ImageIcon getNoStateIcon() {
		if (noStateIcon != null) {
			return noStateIcon;
		} else {
			noStateIcon = new ImageIcon(getClass().getResource(
					"/icons/no-state.gif"));
			return noStateIcon;
		}
	}

	public ImageIcon getYesStateIcon() {
		if (yesStateIcon != null) {
			return yesStateIcon;
		} else {
			yesStateIcon = new ImageIcon(getClass().getResource(
					"/icons/yes-state.gif"));
			return yesStateIcon;
		}
	}

	public ImageIcon getColapseIcon() {
		if (colapseIcon != null) {
			return colapseIcon;
		} else {
			colapseIcon = new ImageIcon(getClass().getResource(
					"/icons/contract-nodes.gif"));
			return colapseIcon;
		}
	}

	public ImageIcon getExpandIcon() {
		if (expandIcon != null) {
			return expandIcon;
		} else {
			expandIcon = new ImageIcon(getClass().getResource(
					"/icons/expand-nodes.gif"));
			return expandIcon;
		}
	}

	public ImageIcon getPropagateIcon() {
		if (propagateIcon != null) {
			return propagateIcon;
		} else {
			propagateIcon = new ImageIcon(getClass().getResource(
					"/icons/propagate.gif"));
			return propagateIcon;
		}
	}

	public ImageIcon getPrintPreviewTableIcon() {
		if (printPreviewTableIcon != null) {
			return printPreviewTableIcon;
		} else {
			printPreviewTableIcon = new ImageIcon(getClass().getResource(
					"/icons/preview-table.gif"));
			return printPreviewTableIcon;
		}
	}

	public ImageIcon getPrintTableIcon() {
		if (printTableIcon != null) {
			return printTableIcon;
		} else {
			printTableIcon = new ImageIcon(getClass().getResource(
					"/icons/print-table.gif"));
			return printTableIcon;
		}
	}

	public ImageIcon getEdgeIcon() {
		if (edgeIcon != null) {
			return edgeIcon;
		} else {
			edgeIcon = new ImageIcon(getClass().getResource("/icons/arc.gif"));
			return edgeIcon;
		}
	}

	public ImageIcon getPrintNetIcon() {
		if (printNetIcon != null) {
			return printNetIcon;
		} else {
			printNetIcon = new ImageIcon(getClass().getResource(
					"/icons/print-net.gif"));
			return printNetIcon;
		}
	}

	public ImageIcon getPrintPreviewNetIcon() {
		if (printPreviewNetIcon != null) {
			return printPreviewNetIcon;
		} else {
			printPreviewNetIcon = new ImageIcon(getClass().getResource(
					"/icons/preview-print.gif"));
			return printPreviewNetIcon;
		}
	}

	public ImageIcon getSaveNetIcon() {
		if (saveNetIcon != null) {
			return saveNetIcon;
		} else {
			saveNetIcon = new ImageIcon(getClass().getResource(
					"/icons/save-net.gif"));
			return saveNetIcon;
		}
	}

	public ImageIcon getFolderSmallDisabledIcon() {
		if (folderSmallDisabledIcon != null) {
			return folderSmallDisabledIcon;
		} else {
			folderSmallDisabledIcon = new ImageIcon(getClass().getResource(
					"/icons/folder-small-disabled.gif"));
			return folderSmallDisabledIcon;
		}
	}

	public ImageIcon getGreenBallIcon() {
		if (greenBallIcon != null) {
			return greenBallIcon;
		} else {
			greenBallIcon = new ImageIcon(getClass().getResource(
					"/icons/green-ball.gif"));
			return greenBallIcon;
		}
	}
	

	public ImageIcon getYellowBallIcon() {
		if (yellowBallIcon != null) {
			return yellowBallIcon;
		} else {
			yellowBallIcon = new ImageIcon(getClass().getResource(
					"/icons/yellow-ball.gif"));
			return yellowBallIcon;
		}
	}

	public ImageIcon getNetFileIcon() {
		return netFileIcon;
	}

	public ImageIcon getTxtFileIcon() {
		return txtFileIcon;
	}

	public ImageIcon getArffFileIcon() {
		return arffFileIcon;
	}

	public ImageIcon getAddFolderIcon() {
		if (addFolderIcon != null) {
			return addFolderIcon;
		} else {
			addFolderIcon = new ImageIcon(getClass().getResource(
					"/icons/add-folder.gif"));
			return addFolderIcon;
		}
	}

	public ImageIcon getDeleteFolderIcon() {
		if (deleteFolderIcon != null) {
			return deleteFolderIcon;
		} else {
			deleteFolderIcon = new ImageIcon(getClass().getResource(
					"/icons/delete-folder.gif"));
			return deleteFolderIcon;
		}
	}

	public ImageIcon getEditIcon() {
		if (editIcon != null) {
			return editIcon;
		} else {
			editIcon = new ImageIcon(getClass().getResource("/icons/edit-paste.png"));
			return editIcon;
		}
	}

	public ImageIcon getRenameFolderIcon() {
		if (renameFolderIcon != null) {
			return renameFolderIcon;
		} else {
			renameFolderIcon = new ImageIcon(getClass().getResource(
					"/icons/rename-folder.gif"));
			return renameFolderIcon;
		}
	}

	public ImageIcon getNewIcon() {
		if (newIcon != null) {
			return newIcon;
		} else {
			newIcon = new ImageIcon(getClass().getResource("/icons/document-new.png"));
			return newIcon;
		}
	}
	
	public ImageIcon getNewBNIcon() {
		if (newBNIcon != null) {
			return newBNIcon;
		} else {
			newBNIcon = new ImageIcon(getClass().getResource("/icons/new-bn.png"));
			return newBNIcon;
		}
	}	
	
	public ImageIcon getNewMSBNIcon() {
		if (newMSBNIcon != null) {
			return newMSBNIcon;
		} else {
			newMSBNIcon = new ImageIcon(getClass().getResource("/icons/new-msbn.png"));
			return newMSBNIcon;
		}
	}
	
	public ImageIcon getNewMEBNIcon() {
		if (newMEBNIcon != null) {
			return newMEBNIcon;
		} else {
			newMEBNIcon = new ImageIcon(getClass().getResource("/icons/new-mebn.png"));
			return newMEBNIcon;
		}
	}	

	public ImageIcon getLessIcon() {
		if (lessIcon != null) {
			return lessIcon;
		} else {
			lessIcon = new ImageIcon(getClass().getResource("/icons/list-remove.png"));
			return lessIcon;
		}
	}

	public ImageIcon getInformationIcon() {
		if (informationIcon != null) {
			return informationIcon;
		} else {
			informationIcon = new ImageIcon(getClass().getResource(
					"/icons/information.gif"));
			return informationIcon;
		}
	}

	public ImageIcon getInitializeIcon() {
		if (initializeIcon != null) {
			return initializeIcon;
		} else {
			initializeIcon = new ImageIcon(getClass().getResource(
					"/icons/initialize.gif"));
			return initializeIcon;
		}
	}

	public ImageIcon getDecisionNodeIcon() {
		if (decisionNodeIcon != null) {
			return decisionNodeIcon;
		} else {
			decisionNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/decision-node.gif"));
			return decisionNodeIcon;
		}
	}

	public ImageIcon getEllipsisIcon() {
		if (ellipsisIcon != null) {
			return ellipsisIcon;
		} else {
			ellipsisIcon = new ImageIcon(getClass().getResource(
					"/icons/ellipsis.gif"));
			return ellipsisIcon;
		}
	}

	public ImageIcon getHierarchyIcon() {
		if (hierarchyIcon != null) {
			return hierarchyIcon;
		} else {
			hierarchyIcon = new ImageIcon(getClass().getResource(
					"/icons/hierarchy.gif"));
			return hierarchyIcon;
		}
	}

	public ImageIcon getSaveTableIcon() {
		if (saveTableIcon != null) {
			return saveTableIcon;
		} else {
			saveTableIcon = new ImageIcon(getClass().getResource(
					"/icons/save-table.gif"));
			return saveTableIcon;
		}
	}

	public ImageIcon getSelectionIcon() {
		if (selectionIcon != null) {
			return selectionIcon;
		} else {
			selectionIcon = new ImageIcon(getClass().getResource(
					"/icons/selection.gif"));
			return selectionIcon;
		}
	}

	public ImageIcon getUtilityNodeIcon() {
		if (utilityNodeIcon != null) {
			return utilityNodeIcon;
		} else {
			utilityNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/utility-node.gif"));
			return utilityNodeIcon;
		}
	}

	public ImageIcon getFillIcon() {
		if (fillIcon != null) {
			return fillIcon;
		} else {
			fillIcon = new ImageIcon(getClass().getResource("/icons/fill.gif"));
			return fillIcon;
		}
	}

	public ImageIcon getResetSizeIcon() {
		if (resetSizeIcon != null) {
			return resetSizeIcon;
		} else {
			resetSizeIcon = new ImageIcon(getClass().getResource(
					"/icons/reset_size.gif"));
			return resetSizeIcon;
		}
	}

	public ImageIcon getGridIcon() {
		if (gridIcon != null) {
			return gridIcon;
		} else {
			gridIcon = new ImageIcon(getClass().getResource("/icons/grid.gif"));
			return gridIcon;
		}
	}

	public ImageIcon getOpenModelIcon() {
		if (openModelIcon != null) {
			return openModelIcon;
		} else {
			openModelIcon = new ImageIcon(getClass().getResource(
					"/icons/open4.gif"));
			return openModelIcon;
		}
	}
	
	public ImageIcon getLearningIcon() {
		if (learningIcon != null) {
			return learningIcon;
		} else {
			learningIcon = new ImageIcon(getClass().getResource(
					"/icons/learn.gif"));
			return learningIcon;
		}
	}
	
	public ImageIcon getGrayBorderBoxIcon() {
		if (grayBorderIcon != null) {
			return grayBorderIcon;
		} else {
			grayBorderIcon = new ImageIcon(getClass().getResource(
					"/icons/gray-border-box.gif"));
			return grayBorderIcon;
		}
	}
	
	public ImageIcon getGrayBoxBoxIcon() {
		if (grayBoxIcon != null) {
			return grayBoxIcon;
		} else {
			grayBoxIcon = new ImageIcon(getClass().getResource(
					"/icons/gray-box-box.gif"));
			return grayBoxIcon;
		}
	}	
	

	public ImageIcon getContextNodeIcon() {
		if (contextNodeIcon != null) {
			return contextNodeIcon;
		} else {
			contextNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/context-node.gif"));
			return contextNodeIcon;
		}
	}
	
	public ImageIcon getInputNodeIcon() {
		if (inputNodeIcon != null) {
			return inputNodeIcon;
		} else {
			inputNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/input-node.gif"));
			return inputNodeIcon;
		}
	}
	
	public ImageIcon getResidentNodeIcon() {
		if (residentNodeIcon != null) {
			return residentNodeIcon;
		} else {
			residentNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/resident-node.gif"));
			return residentNodeIcon;
		}
	}
	
	public ImageIcon getMFragIcon() {
		if (mfragIcon != null) {
			return mfragIcon;
		} else {
			mfragIcon = new ImageIcon(getClass().getResource(
					"/icons/mfrag.gif"));
			return mfragIcon;
		}
	}
	
	public ImageIcon getBoxContextIcon() {
		if (boxContextNodeIcon != null) {
			return boxContextNodeIcon;
		} else {
			boxContextNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/context-box.gif"));
			return boxContextNodeIcon;
		}
	}
	
	public ImageIcon getBoxInputIcon() {
		if (boxInputNodeIcon != null) {
			return boxInputNodeIcon;
		} else {
			boxInputNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/input-box.gif"));
			return boxInputNodeIcon;
		}
	}
	
	public ImageIcon getBoxResidentIcon() {
		if (boxResidentNodeIcon != null) {
			return boxResidentNodeIcon;
		} else {
			boxResidentNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/resident-box.gif"));
			return boxResidentNodeIcon;
		}
	}
	
	public ImageIcon getBoxMFragIcon() {
		if (boxMfragIcon != null) {
			return boxMfragIcon;
		} else {
			boxMfragIcon = new ImageIcon(getClass().getResource(
					"/icons/mfrag-box.gif"));
			return boxMfragIcon;
		}
	}
	
	public ImageIcon xIcon() {
		if (xIcon != null) {
			return xIcon;
		} else {
			xIcon = new ImageIcon(getClass().getResource(
					"/icons/x.png"));
			return xIcon;
		}
	}	
	
	public ImageIcon getBoxSetIcon() {
		if (setBoxIcon != null) {
			return setBoxIcon;
		} else {
			setBoxIcon = new ImageIcon(getClass().getResource(
					"/icons/set-box.gif"));
			return setBoxIcon;
		}
	}	
	
	public ImageIcon getEyeIcon() {
		if (eyeIcon != null) {
			return eyeIcon;
		} else {
			eyeIcon = new ImageIcon(getClass().getResource(
					"/icons/eye.gif"));
			return eyeIcon;
		}
	}		
	
	
	public ImageIcon getYellowNodeIcon() {
		if (yellowNodeIcon != null) {
			return yellowNodeIcon;
		} else {
			yellowNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/yellow-node.gif"));
			return yellowNodeIcon;
		}
	}	
	
	public ImageIcon getGreenNodeIcon() {
		if (greenNodeIcon != null) {
			return greenNodeIcon;
		} else {
			greenNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/green-node.gif"));
			return greenNodeIcon;
		}
	}	
	
	public ImageIcon getBlueNodeIcon() {
		if (blueNodeIcon != null) {
			return blueNodeIcon;
		} else {
			blueNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/blue-node.gif"));
			return blueNodeIcon;
		}
	}	
	
	public ImageIcon getGrayNodeIcon() {
		if (grayNodeIcon != null) {
			return grayNodeIcon;
		} else {
			grayNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/gray-node.gif"));
			return grayNodeIcon;
		}
	}		
	
	public ImageIcon getOrangeNodeIcon() {
		if (orangeNodeIcon != null) {
			return orangeNodeIcon;
		} else {
			orangeNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/orange-node.gif"));
			return orangeNodeIcon;
		}
	}	
	
	public ImageIcon getMTheoryNodeIcon() {
		if (mTheoryNodeIcon != null) {
			return mTheoryNodeIcon;
		} else {
			mTheoryNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/mtheory-node.gif"));
			return mTheoryNodeIcon;
		}
	}	
	
	public ImageIcon getFunctIcon() {
		if (functIcon != null) {
			return functIcon;
		} else {
			functIcon = new ImageIcon(getClass().getResource(
					"/icons/funct.gif"));
			return functIcon;
		}
	}	
	
	public ImageIcon getStateIcon() {
		if (stateIcon != null) {
			return stateIcon;
		} else {
			stateIcon = new ImageIcon(getClass().getResource(
					"/icons/state.gif"));
			return stateIcon;
		}
	}	
	
	// operators
	
	public ImageIcon getAndIcon() {
		if (andIcon != null) {
			return andIcon;
		} else {
			andIcon = new ImageIcon(getClass().getResource(
					"/icons/and.gif"));
			return andIcon;
		}
	}			
		

	public ImageIcon getOrIcon() {
		if (orIcon != null) {
			return orIcon;
		} else {
			orIcon = new ImageIcon(getClass().getResource(
					"/icons/or.gif"));
			return orIcon;
		}
	}
	

	public ImageIcon getNotIcon() {
		if (notIcon != null) {
			return notIcon;
		} else {
			notIcon = new ImageIcon(getClass().getResource(
					"/icons/not.gif"));
			return notIcon;
		}
	}
	

	public ImageIcon getEqualIcon() {
		if (equalIcon != null) {
			return equalIcon;
		} else {
			equalIcon = new ImageIcon(getClass().getResource(
					"/icons/equal.gif"));
			return equalIcon;
		}
	}
	

	public ImageIcon getIffIcon() {
		if (iffIcon != null) {
			return iffIcon;
		} else {
			iffIcon = new ImageIcon(getClass().getResource(
					"/icons/iff.gif"));
			return iffIcon;
		}
	}
	

	public ImageIcon getImpliesIcon() {
		if (impliesIcon != null) {
			return impliesIcon;
		} else {
			impliesIcon = new ImageIcon(getClass().getResource(
					"/icons/implies.gif"));
			return impliesIcon;
		}
	}
	

	public ImageIcon getForallIcon() {
		if (forallIcon != null) {
			return forallIcon;
		} else {
			forallIcon = new ImageIcon(getClass().getResource(
					"/icons/forall.gif"));
			return forallIcon;
		}
	}
	

	public ImageIcon getExistsIcon() {
		if (existsIcon != null) {
			return existsIcon;
		} else {
			existsIcon = new ImageIcon(getClass().getResource(
					"/icons/exists.gif"));
			return existsIcon;
		}
	}
	
	
	
	public ImageIcon getEntityNodeIcon() {
		if (entityNodeIcon != null) {
			return entityNodeIcon;
		} else {
			entityNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/entityNode.gif"));
			return entityNodeIcon;
		}
	}
	
	public ImageIcon getOVariableNodeIcon() {
		if (ovariableNodeIcon != null) {
			return ovariableNodeIcon;
		} else {
			ovariableNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/ovariableNode.gif"));
			return ovariableNodeIcon;
		}
	}
	
	public ImageIcon getNodeNodeIcon() {
		if (nodeNodeIcon != null) {
			return nodeNodeIcon;
		} else {
			nodeNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/nodeNode.gif"));
			return nodeNodeIcon;
		}
	}
	
	public ImageIcon getEmptyNodeIcon() {
		if (emptyNodeIcon != null) {
			return emptyNodeIcon;
		} else {
			emptyNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/emptyNode.gif"));
			return emptyNodeIcon;
		}
	}	
	
	public ImageIcon getSkolenNodeIcon() {
		if (skolenNodeIcon != null) {
			return skolenNodeIcon;
		} else {
			skolenNodeIcon = new ImageIcon(getClass().getResource(
					"/icons/skolenNode.gif"));
			return skolenNodeIcon;
		}
	}	
	
	public ImageIcon getDownIcon() {
		if (downIcon != null) {
			return downIcon;
		} else {
			downIcon = new ImageIcon(getClass().getResource(
					"/icons/go-down.png"));
			return downIcon;
		}
	}	
	
	public ImageIcon getUpIcon() {
		if (upIcon != null) {
			return upIcon;
		} else {
			upIcon = new ImageIcon(getClass().getResource(
					"/icons/go-up.png"));
			return upIcon;
		}
	}

	public ImageIcon getBoxVariablesIcon() {
		if (boxVariablesIcon != null) {
			return boxVariablesIcon;
		} else {
			boxVariablesIcon = new ImageIcon(getClass().getResource(
					"/icons/boxVariables.gif"));
			return boxVariablesIcon;
		}
	}	
	
	public ImageIcon getBooleanIcon() {
		if (booleanIcon != null) {
			return booleanIcon;
		} else {
			booleanIcon = new ImageIcon(getClass().getResource(
					"/icons/boolean.png"));
			return booleanIcon;
		}
	}		
}