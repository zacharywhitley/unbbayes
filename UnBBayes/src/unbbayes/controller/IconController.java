package unbbayes.controller;

import javax.swing.ImageIcon;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0 13/04/2003
 */

public class IconController
{
  private static IconController singleton;
  protected ImageIcon metalIcon = new ImageIcon(getClass().getResource("/icons/metal.gif"));
  protected ImageIcon motifIcon = new ImageIcon(getClass().getResource("/icons/motif.gif"));
  protected ImageIcon windowsIcon = new ImageIcon(getClass().getResource("/icons/windows.gif"));
  protected ImageIcon cascadeIcon = new ImageIcon(getClass().getResource("/icons/cascade.gif"));
  protected ImageIcon tileIcon = new ImageIcon(getClass().getResource("/icons/tile.gif"));
  protected ImageIcon helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
  protected ImageIcon globalOptionIcon = new ImageIcon(getClass().getResource("/icons/global-options.gif"));
  protected ImageIcon printIcon = new ImageIcon(getClass().getResource("/icons/print.gif"));
  protected ImageIcon visualizeIcon = new ImageIcon(getClass().getResource("/icons/visualize.gif"));
  protected ImageIcon openIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
  protected ImageIcon compileIcon = new ImageIcon(getClass().getResource("/icons/learn.gif"));
  protected ImageIcon saveIcon = new ImageIcon(getClass().getResource("/icons/save.gif"));
  protected ImageIcon returnIcon = new ImageIcon(getClass().getResource("/icons/return.gif"));
  protected ImageIcon openMetaphorIcon = new ImageIcon(getClass().getResource("/icons/open-metaphor.gif"));
  protected ImageIcon saveMetaphorIcon = new ImageIcon(getClass().getResource("/icons/save-metaphor.gif"));
  protected ImageIcon diagnosticMetaphorIcon = new ImageIcon(getClass().getResource("/icons/diagnostic-metaphor.gif"));
  protected ImageIcon openMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/opens-metaphor.gif"));
  protected ImageIcon saveMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/saves-metaphor.gif"));
  protected ImageIcon diagnosticMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/diagnostics-metaphor.gif"));
  protected ImageIcon yesStateIcon = new ImageIcon(getClass().getResource("/icons/yes-state.gif"));
  protected ImageIcon noStateIcon = new ImageIcon(getClass().getResource("/icons/no-state.gif"));
  protected ImageIcon emptyStateIcon = new ImageIcon(getClass().getResource("/icons/empty-state.gif"));
  protected ImageIcon moreIcon = new ImageIcon(getClass().getResource("/icons/more.gif"));
  protected ImageIcon lessIcon = new ImageIcon(getClass().getResource("/icons/less.gif"));
  protected ImageIcon folderSmallIcon = new ImageIcon(getClass().getResource("/icons/folder-small.gif"));
  protected ImageIcon colapseIcon = new ImageIcon(getClass().getResource("/icons/contract-nodes.gif"));
  protected ImageIcon expandIcon = new ImageIcon(getClass().getResource("/icons/expand-nodes.gif"));
  protected ImageIcon propagateIcon = new ImageIcon(getClass().getResource("/icons/propagate.gif"));
  protected ImageIcon printTableIcon = new ImageIcon(getClass().getResource("/icons/print-table.gif"));
  protected ImageIcon printPreviewTableIcon = new ImageIcon(getClass().getResource("/icons/preview-table.gif"));
  protected ImageIcon arcIcon = new ImageIcon(getClass().getResource("/icons/arc.gif"));
  protected ImageIcon printNetIcon = new ImageIcon(getClass().getResource("/icons/print-net.gif"));
  protected ImageIcon printPreviewNetIcon = new ImageIcon(getClass().getResource("/icons/preview-print.gif"));
  protected ImageIcon saveNetIcon = new ImageIcon(getClass().getResource("/icons/save-net.gif"));
  protected ImageIcon folderSmallDisabledIcon = new ImageIcon(getClass().getResource("/icons/folder-small-disabled.gif"));
  protected ImageIcon yellowBallIcon = new ImageIcon(getClass().getResource("/icons/yellow-ball.gif"));
  protected ImageIcon greenBallIcon = new ImageIcon(getClass().getResource("/icons/green-ball.gif"));
  protected ImageIcon arffFileIcon = new ImageIcon(getClass().getResource("/icons/arff-file.gif"));
  protected ImageIcon txtFileIcon = new ImageIcon(getClass().getResource("/icons/txt-file.gif"));
  protected ImageIcon netFileIcon = new ImageIcon(getClass().getResource("/icons/net-file.gif"));
  protected ImageIcon deleteFolderIcon = new ImageIcon(getClass().getResource("/icons/delete-folder.gif"));
  protected ImageIcon renameFolderIcon = new ImageIcon(getClass().getResource("/icons/rename-folder.gif"));
  protected ImageIcon addFolderIcon = new ImageIcon(getClass().getResource("/icons/add-folder.gif"));
  protected ImageIcon editIcon = new ImageIcon(getClass().getResource("/icons/edit.gif"));
  protected ImageIcon newIcon = new ImageIcon(getClass().getResource("/icons/new.gif"));
  protected ImageIcon informationIcon = new ImageIcon(getClass().getResource("/icons/information.gif"));
  protected ImageIcon initializeIcon = new ImageIcon(getClass().getResource("/icons/initialize.gif"));
  protected ImageIcon ellipsisIcon = new ImageIcon(getClass().getResource("/icons/ellipsis.gif"));
  protected ImageIcon decisionNodeIcon = new ImageIcon(getClass().getResource("/icons/decision-node.gif"));
  protected ImageIcon utilityNodeIcon = new ImageIcon(getClass().getResource("/icons/utility-node.gif"));
  protected ImageIcon selectionIcon = new ImageIcon(getClass().getResource("/icons/selection.gif"));
  protected ImageIcon saveTableIcon = new ImageIcon(getClass().getResource("/icons/save-table.gif"));
  protected ImageIcon hierarchyIcon = new ImageIcon(getClass().getResource("/icons/hierarchy.gif"));

  public static IconController getInstance()
    {   if (singleton == null)
        {   singleton = new IconController();
        }
        return singleton;
    }


  protected IconController() {
  }

  public ImageIcon getMetalIcon(){ return metalIcon;}
  public ImageIcon getMotifIcon(){ return motifIcon;}
  public ImageIcon getWindowsIcon(){ return windowsIcon;}
  public ImageIcon getCascadeIcon(){ return cascadeIcon;}
  public ImageIcon getTileIcon(){ return tileIcon;}
  public ImageIcon getHelpIcon(){ return helpIcon;}
  public ImageIcon getGlobalOptionIcon(){ return globalOptionIcon;}
  public ImageIcon getPrintIcon(){ return printIcon;}
  public ImageIcon getVisualizeIcon() { return visualizeIcon;}
  public ImageIcon getCompileIcon() {
    return compileIcon;
  }
  public ImageIcon getOpenIcon() {
    return openIcon;
  }
  public ImageIcon getSaveIcon() {
    return saveIcon;
  }
  public ImageIcon getReturnIcon() {
    return returnIcon;
  }
  public ImageIcon getDiagnosticMetaphorIcon() {
    return diagnosticMetaphorIcon;
  }
  public ImageIcon getDiagnosticMetaphorRollOverIcon() {
    return diagnosticMetaphorRollOverIcon;
  }
  public ImageIcon getOpenMetaphorIcon() {
    return openMetaphorIcon;
  }
  public ImageIcon getOpenMetaphorRollOverIcon() {
    return openMetaphorRollOverIcon;
  }
  public ImageIcon getSaveMetaphorIcon() {
    return saveMetaphorIcon;
  }
  public ImageIcon getSaveMetaphorRollOverIcon() {
    return saveMetaphorRollOverIcon;
  }
  public ImageIcon getEmptyStateIcon() {
    return emptyStateIcon;
  }
  public ImageIcon getFolderSmallIcon() {
    return folderSmallIcon;
  }
  public ImageIcon getMoreIcon() {
    return moreIcon;
  }
  public ImageIcon getNoStateIcon() {
    return noStateIcon;
  }
  public ImageIcon getYesStateIcon() {
    return yesStateIcon;
  }
  public ImageIcon getColapseIcon() {
    return colapseIcon;
  }
  public ImageIcon getExpandIcon() {
    return expandIcon;
  }
  public ImageIcon getPropagateIcon() {
    return propagateIcon;
  }
  public ImageIcon getPrintPreviewTableIcon() {
    return printPreviewTableIcon;
  }
  public ImageIcon getPrintTableIcon() {
    return printTableIcon;
  }
  public ImageIcon getArcIcon() {
    return arcIcon;
  }
  public ImageIcon getPrintNetIcon() {
    return printNetIcon;
  }
  public ImageIcon getPrintPreviewNetIcon() {
    return printPreviewNetIcon;
  }
  public ImageIcon getSaveNetIcon() {
    return saveNetIcon;
  }
  public ImageIcon getFolderSmallDisabledIcon() {
    return folderSmallDisabledIcon;
  }
  public ImageIcon getGreenBallIcon() {
    return greenBallIcon;
  }
  public ImageIcon getYellowBallIcon() {
    return yellowBallIcon;
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
    return addFolderIcon;
  }
  public ImageIcon getDeleteFolderIcon() {
    return deleteFolderIcon;
  }
  public ImageIcon getEditIcon() {
    return editIcon;
  }
  public ImageIcon getRenameFolderIcon() {
    return renameFolderIcon;
  }
  public ImageIcon getNewIcon() {
    return newIcon;
  }
  public ImageIcon getLessIcon() {
    return lessIcon;
  }
  public ImageIcon getInformationIcon() {
    return informationIcon;
  }
  public ImageIcon getInitializeIcon() {
    return initializeIcon;
  }
  public ImageIcon getDecisionNodeIcon() {
    return decisionNodeIcon;
  }
  public ImageIcon getEllipsisIcon() {
    return ellipsisIcon;
  }
  public ImageIcon getHierarchyIcon() {
    return hierarchyIcon;
  }
  public ImageIcon getSaveTableIcon() {
    return saveTableIcon;
  }
  public ImageIcon getSelectionIcon() {
    return selectionIcon;
  }
  public ImageIcon getUtilityNodeIcon() {
    return utilityNodeIcon;
  }

}
