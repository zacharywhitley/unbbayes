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
  protected ImageIcon arcIcon;
  protected ImageIcon printNetIcon;
  protected ImageIcon printPreviewNetIcon;
  protected ImageIcon saveNetIcon;
  protected ImageIcon folderSmallDisabledIcon;
  protected ImageIcon yellowBallIcon;
  protected ImageIcon greenBallIcon;
  protected ImageIcon arffFileIcon = new ImageIcon(getClass().getResource("/icons/arff-file.gif"));
  protected ImageIcon txtFileIcon = new ImageIcon(getClass().getResource("/icons/txt-file.gif"));
  protected ImageIcon netFileIcon = new ImageIcon(getClass().getResource("/icons/net-file.gif"));
  protected ImageIcon deleteFolderIcon;
  protected ImageIcon renameFolderIcon;
  protected ImageIcon addFolderIcon;
  protected ImageIcon editIcon;
  protected ImageIcon newIcon;
  protected ImageIcon informationIcon;
  protected ImageIcon initializeIcon;
  protected ImageIcon ellipsisIcon;
  protected ImageIcon decisionNodeIcon;
  protected ImageIcon utilityNodeIcon;
  protected ImageIcon selectionIcon;
  protected ImageIcon saveTableIcon;
  protected ImageIcon hierarchyIcon;

  public static IconController getInstance()
    {   if (singleton == null)
        {   singleton = new IconController();
        }
        return singleton;
    }


  protected IconController() {
  }

  public ImageIcon getMetalIcon()
  {
    if (metalIcon!=null)
    {
      return metalIcon;
    }
    else
    {
      metalIcon = new ImageIcon(getClass().getResource("/icons/metal.gif"));
      return metalIcon;
    }
  }
  public ImageIcon getMotifIcon()
  {
    if (motifIcon!=null)
    {
      return motifIcon;
    }
    else
    {
      motifIcon = new ImageIcon(getClass().getResource("/icons/motif.gif"));
      return motifIcon;
    }
  }
  public ImageIcon getWindowsIcon()
  {
    if (windowsIcon!=null)
    {
      return windowsIcon;
    }
    else
    {
      windowsIcon = new ImageIcon(getClass().getResource("/icons/windows.gif"));
      return windowsIcon;
    }
  }
  public ImageIcon getCascadeIcon()
  {
    if (cascadeIcon!=null)
    {
      return cascadeIcon;
    }
    else
    {
      cascadeIcon = new ImageIcon(getClass().getResource("/icons/cascade.gif"));
      return cascadeIcon;
    }
  }
  public ImageIcon getTileIcon()
  {
    if (tileIcon!=null)
    {
      return tileIcon;
    }
    else
    {
      tileIcon = new ImageIcon(getClass().getResource("/icons/tile.gif"));
      return tileIcon;
    }
  }
  public ImageIcon getHelpIcon()
  {
    if (helpIcon!=null)
    {
      return helpIcon;
    }
    else
    {
      helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
      return helpIcon;
    }
  }
  public ImageIcon getGlobalOptionIcon()
  {
    if (globalOptionIcon!=null)
    {
      return globalOptionIcon;
    }
    else
    {
      globalOptionIcon = new ImageIcon(getClass().getResource("/icons/global-options.gif"));
      return globalOptionIcon;
    }
  }
  public ImageIcon getPrintIcon()
  {
    if (printIcon!=null)
    {
      return printIcon;
    }
    else
    {
      printIcon = new ImageIcon(getClass().getResource("/icons/print.gif"));
      return printIcon;
    }
  }
  public ImageIcon getVisualizeIcon()
  {
    if (visualizeIcon!=null)
    {
      return visualizeIcon;
    }
    else
    {
      visualizeIcon = new ImageIcon(getClass().getResource("/icons/visualize.gif"));
      return visualizeIcon;
    }
  }
  public ImageIcon getCompileIcon() {
    if (compileIcon!=null)
    {
      return compileIcon;
    }
    else
    {
      compileIcon = new ImageIcon(getClass().getResource("/icons/learn.gif"));
      return compileIcon;
    }
  }
  public ImageIcon getOpenIcon() {
    if (openIcon!=null)
    {
      return openIcon;
    }
    else
    {
      openIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
      return openIcon;
    }
  }
  public ImageIcon getSaveIcon() {
    if (saveIcon!=null)
    {
      return saveIcon;
    }
    else
    {
      saveIcon = new ImageIcon(getClass().getResource("/icons/save.gif"));
      return saveIcon;
    }
  }
  public ImageIcon getReturnIcon() {
    if (returnIcon!=null)
    {
      return returnIcon;
    }
    else
    {
      returnIcon = new ImageIcon(getClass().getResource("/icons/return.gif"));
      return returnIcon;
    }
  }
  public ImageIcon getDiagnosticMetaphorIcon() {
    if (diagnosticMetaphorIcon!=null)
    {
      return diagnosticMetaphorIcon;
    }
    else
    {
      diagnosticMetaphorIcon = new ImageIcon(getClass().getResource("/icons/diagnostic-metaphor.gif"));
      return diagnosticMetaphorIcon;
    }
  }
  public ImageIcon getDiagnosticMetaphorRollOverIcon() {
    if (diagnosticMetaphorRollOverIcon!=null)
    {
      return diagnosticMetaphorRollOverIcon;
    }
    else
    {
      diagnosticMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/diagnostics-metaphor.gif"));
      return diagnosticMetaphorRollOverIcon;
    }
  }
  public ImageIcon getOpenMetaphorIcon() {
    if (openMetaphorIcon!=null)
    {
      return openMetaphorIcon;
    }
    else
    {
      openMetaphorIcon = new ImageIcon(getClass().getResource("/icons/open-metaphor.gif"));
      return openMetaphorIcon;
    }
  }
  public ImageIcon getOpenMetaphorRollOverIcon() {
    if (openMetaphorRollOverIcon!=null)
    {
      return openMetaphorRollOverIcon;
    }
    else
    {
      openMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/opens-metaphor.gif"));
      return openMetaphorRollOverIcon;
    }
  }
  public ImageIcon getSaveMetaphorIcon() {
    if (saveMetaphorIcon!=null)
    {
      return saveMetaphorIcon;
    }
    else
    {
      saveMetaphorIcon = new ImageIcon(getClass().getResource("/icons/save-metaphor.gif"));
      return saveMetaphorIcon;
    }
  }
  public ImageIcon getSaveMetaphorRollOverIcon() {
    if (saveMetaphorRollOverIcon!=null)
    {
      return saveMetaphorRollOverIcon;
    }
    else
    {
      saveMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icons/saves-metaphor.gif"));
      return saveMetaphorRollOverIcon;
    }
  }
  public ImageIcon getEmptyStateIcon() {
    if (emptyStateIcon!=null)
    {
      return emptyStateIcon;
    }
    else
    {
      emptyStateIcon = new ImageIcon(getClass().getResource("/icons/empty-state.gif"));
      return emptyStateIcon;
    }
  }
  public ImageIcon getFolderSmallIcon() {
    if (folderSmallIcon!=null)
    {
      return folderSmallIcon;
    }
    else
    {
      folderSmallIcon = new ImageIcon(getClass().getResource("/icons/folder-small.gif"));
      return folderSmallIcon;
    }
  }
  public ImageIcon getMoreIcon() {
    if (moreIcon!=null)
    {
      return moreIcon;
    }
    else
    {
      moreIcon = new ImageIcon(getClass().getResource("/icons/more.gif"));
      return moreIcon;
    }
  }
  public ImageIcon getNoStateIcon() {
    if (noStateIcon!=null)
    {
      return noStateIcon;
    }
    else
    {
      noStateIcon = new ImageIcon(getClass().getResource("/icons/no-state.gif"));
      return noStateIcon;
    }
  }
  public ImageIcon getYesStateIcon() {
    if (yesStateIcon!=null)
    {
      return yesStateIcon;
    }
    else
    {
      yesStateIcon = new ImageIcon(getClass().getResource("/icons/yes-state.gif"));
      return yesStateIcon;
    }
  }
  public ImageIcon getColapseIcon() {
    if (colapseIcon!=null)
    {
      return colapseIcon;
    }
    else
    {
      colapseIcon = new ImageIcon(getClass().getResource("/icons/contract-nodes.gif"));
      return colapseIcon;
    }
  }
  public ImageIcon getExpandIcon() {
    if (expandIcon!=null)
    {
      return expandIcon;
    }
    else
    {
      expandIcon = new ImageIcon(getClass().getResource("/icons/expand-nodes.gif"));
      return expandIcon;
    }
  }
  public ImageIcon getPropagateIcon() {
    if (propagateIcon!=null)
    {
      return propagateIcon;
    }
    else
    {
      propagateIcon = new ImageIcon(getClass().getResource("/icons/propagate.gif"));
      return propagateIcon;
    }
  }
  public ImageIcon getPrintPreviewTableIcon() {
    if (printPreviewTableIcon!=null)
    {
      return printPreviewTableIcon;
    }
    else
    {
      printPreviewTableIcon = new ImageIcon(getClass().getResource("/icons/preview-table.gif"));
      return printPreviewTableIcon;
    }
  }
  public ImageIcon getPrintTableIcon() {
    if (printTableIcon!=null)
    {
      return printTableIcon;
    }
    else
    {
      printTableIcon = new ImageIcon(getClass().getResource("/icons/print-table.gif"));
      return printTableIcon;
    }
  }
  public ImageIcon getArcIcon() {
    if (arcIcon!=null)
    {
      return arcIcon;
    }
    else
    {
      arcIcon = new ImageIcon(getClass().getResource("/icons/arc.gif"));
      return arcIcon;
    }
  }
  public ImageIcon getPrintNetIcon() {
    if (printNetIcon!=null)
    {
      return printNetIcon;
    }
    else
    {
      printNetIcon = new ImageIcon(getClass().getResource("/icons/print-net.gif"));
      return printNetIcon;
    }
  }
  public ImageIcon getPrintPreviewNetIcon() {
    if (printPreviewNetIcon!=null)
    {
      return printPreviewNetIcon;
    }
    else
    {
      printPreviewNetIcon = new ImageIcon(getClass().getResource("/icons/preview-print.gif"));
      return printPreviewNetIcon;
    }
  }
  public ImageIcon getSaveNetIcon() {
    if (saveNetIcon!=null)
    {
      return saveNetIcon;
    }
    else
    {
      saveNetIcon = new ImageIcon(getClass().getResource("/icons/save-net.gif"));
      return saveNetIcon;
    }
  }
  public ImageIcon getFolderSmallDisabledIcon() {
    if (folderSmallDisabledIcon!=null)
    {
      return folderSmallDisabledIcon;
    }
    else
    {
      folderSmallDisabledIcon = new ImageIcon(getClass().getResource("/icons/folder-small-disabled.gif"));
      return folderSmallDisabledIcon;
    }
  }
  public ImageIcon getGreenBallIcon() {
    if (greenBallIcon!=null)
    {
      return greenBallIcon;
    }
    else
    {
      greenBallIcon = new ImageIcon(getClass().getResource("/icons/green-ball.gif"));
      return greenBallIcon;
    }
  }
  public ImageIcon getYellowBallIcon() {
    if (yellowBallIcon!=null)
    {
      return yellowBallIcon;
    }
    else
    {
      yellowBallIcon = new ImageIcon(getClass().getResource("/icons/yellow-ball.gif"));
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
    if (addFolderIcon!=null)
    {
      return addFolderIcon;
    }
    else
    {
      addFolderIcon = new ImageIcon(getClass().getResource("/icons/add-folder.gif"));
      return addFolderIcon;
    }
  }
  public ImageIcon getDeleteFolderIcon() {
    if (deleteFolderIcon!=null)
    {
      return deleteFolderIcon;
    }
    else
    {
      deleteFolderIcon = new ImageIcon(getClass().getResource("/icons/delete-folder.gif"));
      return deleteFolderIcon;
    }
  }
  public ImageIcon getEditIcon() {
    if (editIcon!=null)
    {
      return editIcon;
    }
    else
    {
      editIcon = new ImageIcon(getClass().getResource("/icons/edit.gif"));
      return editIcon;
    }
  }
  public ImageIcon getRenameFolderIcon() {
    if (renameFolderIcon!=null)
    {
      return renameFolderIcon;
    }
    else
    {
      renameFolderIcon = new ImageIcon(getClass().getResource("/icons/rename-folder.gif"));
      return renameFolderIcon;
    }
  }
  public ImageIcon getNewIcon() {
    if (newIcon!=null)
    {
      return newIcon;
    }
    else
    {
      newIcon = new ImageIcon(getClass().getResource("/icons/new.gif"));
      return newIcon;
    }

  }
  public ImageIcon getLessIcon() {
    if (lessIcon!=null)
    {
      return lessIcon;
    }
    else
    {
      lessIcon = new ImageIcon(getClass().getResource("/icons/less.gif"));
      return lessIcon;
    }
  }
  public ImageIcon getInformationIcon() {
    if (informationIcon!=null)
    {
      return informationIcon;
    }
    else
    {
      informationIcon = new ImageIcon(getClass().getResource("/icons/information.gif"));
      return informationIcon;
    }
  }
  public ImageIcon getInitializeIcon() {
    if (initializeIcon!=null)
    {
      return initializeIcon;
    }
    else
    {
      initializeIcon = new ImageIcon(getClass().getResource("/icons/initialize.gif"));
      return initializeIcon;
    }
  }
  public ImageIcon getDecisionNodeIcon() {
    if (decisionNodeIcon!=null)
    {
      return decisionNodeIcon;
    }
    else
    {
      decisionNodeIcon = new ImageIcon(getClass().getResource("/icons/decision-node.gif"));
      return decisionNodeIcon;
    }
  }
  public ImageIcon getEllipsisIcon() {
    if (ellipsisIcon!=null)
    {
      return ellipsisIcon;
    }
    else
    {
      ellipsisIcon = new ImageIcon(getClass().getResource("/icons/ellipsis.gif"));
      return ellipsisIcon;
    }
  }
  public ImageIcon getHierarchyIcon() {
    if (hierarchyIcon!=null)
    {
      return hierarchyIcon;
    }
    else
    {
      hierarchyIcon = new ImageIcon(getClass().getResource("/icons/hierarchy.gif"));
      return hierarchyIcon;
    }
  }
  public ImageIcon getSaveTableIcon() {
    if (saveTableIcon!=null)
    {
      return saveTableIcon;
    }
    else
    {
      saveTableIcon = new ImageIcon(getClass().getResource("/icons/save-table.gif"));
      return saveTableIcon;
    }
  }
  public ImageIcon getSelectionIcon() {
    if (selectionIcon!=null)
    {
      return selectionIcon;
    }
    else
    {
      selectionIcon = new ImageIcon(getClass().getResource("/icons/selection.gif"));
      return selectionIcon;
    }
  }
  public ImageIcon getUtilityNodeIcon() {
    if (utilityNodeIcon!=null)
    {
      return utilityNodeIcon;
    }
    else
    {
      utilityNodeIcon = new ImageIcon(getClass().getResource("/icons/utility-node.gif"));
      return utilityNodeIcon;
    }
  }
}