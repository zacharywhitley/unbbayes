package unbbayes.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class GUIUtils {

	public static Point getCenterPositionForComponent(int width, int heigth){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int positionX = screenSize.width - (screenSize.width/2) - (width/2);
        int positionY = screenSize.height - (screenSize.height/2) - (heigth/2);
        return new Point(positionX, positionY);
	}
	
	public static JFrame getWaitScreen(){
		JFrame jDialog = new JFrame();
		JPanel panelWait = new JPanel(); 
		JButton label = new JButton("Aguarde...");
		panelWait.setLayout(new GridLayout(1,1)); 
		panelWait.add(label);
		jDialog.setContentPane(panelWait);
		jDialog.setPreferredSize(new Dimension(200, 100));
		jDialog.setLocation(GUIUtils.getCenterPositionForComponent(200, 100)); 
		jDialog.pack();
		jDialog.validate();
		jDialog.repaint();
		return jDialog;
	}
	
}
