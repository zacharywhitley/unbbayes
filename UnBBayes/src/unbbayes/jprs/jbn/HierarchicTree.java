package unbbayes.jprs.jbn;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * @author M�rio Henrique Paes Vieira
 * @version 1.0
 */

public class HierarchicTree extends JTree
{ private ImageIcon folderSmallIcon;
  private ImageIcon yellowBallIcon;

  public HierarchicTree(DefaultTreeModel model)
  {   super(model);

      yellowBallIcon = new ImageIcon(getClass().getResource("/icones/yellow-ball.gif"));
      folderSmallIcon = new ImageIcon(getClass().getResource("/icones/folderSmall.gif"));

      // set up node icons
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setClosedIcon(folderSmallIcon);
      renderer.setOpenIcon(folderSmallIcon);
      renderer.setLeafIcon(yellowBallIcon);
      this.setCellRenderer(renderer);

      this.setRootVisible(false);
      this.setEditable(true);
  }
}