/**
 * 
 */
package unbbayes.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * SplitToggleButton class provides a drop down menu when the right side arrow
 * is clicked.
 * @author Shou Matsumoto
 */
public class SplitToggleButton extends JToggleButton implements ActionListener{
  
	private static final long serialVersionUID = -7324436021762703938L;
  
    private JToggleButton mainButton; 
    private JButton dropDownButton;
    private JPopupMenu dropDownMenu;
    

  /**
   * Default Constructor that creates a blank button with a down facing arrow.
   */
  public SplitToggleButton() {
    this(" ");
  }

  /**
   * Creates a button with the specified text  and a down facing arrow.
   * @param text String
   */
  public SplitToggleButton(String text) {
    this(new JToggleButton(text), SwingConstants.SOUTH);
  }

  /**
   * Creates a button with the specified text 
   * and a arrow in the specified direction.
   * @param text String
   * @param orientation int : use values such as {@link SwingConstants.SOUTH}
   */
  public SplitToggleButton(String text, int orientation) {
    this(new JToggleButton(text), orientation);
  }

  /**
   * Passes in the button to use in the left hand side, with the specified 
   * orientation for the arrow on the right hand side.
   * @param mainButton JButton
   * @param orientation int : use values such as {@link SwingConstants.SOUTH}
   */
  public SplitToggleButton(JToggleButton mainButton, int orientation) {
    super();
    
    
    this.dropDownButton  = new BasicArrowButton(orientation);
    dropDownButton.addActionListener(this);
    dropDownButton.setPreferredSize(new Dimension(15, 40));
    dropDownButton.setMaximumSize(dropDownButton.getPreferredSize());

    this.setBorderPainted(false);
//    this.dropDownButton.setBorderPainted(false);
//    this.mainButton.setBorderPainted(false);

//    this.setPreferredSize(new Dimension(75, 34));
//    this.setMaximumSize(new Dimension(75, 34));
//    this.setMinimumSize(new Dimension(200, 34));

    this.setPreferredSize(new Dimension(55, 40));
    this.setMaximumSize(this.getPreferredSize());
    
//    this.setBorder(BorderFactory.createEmptyBorder());
    
    this.setLayout(new BorderLayout());
//    this.setMargin(new Insets(-3, -3,-3,-3));

    
    this.add(dropDownButton, BorderLayout.EAST);
    
    this.setMainButton(mainButton);
    
    this.setEnabled(false);	// keep it unselectable

	this.setFocusPainted(false);
	this.setContentAreaFilled(false);// hide background
	this.setRolloverEnabled(false);	
    
  }

  /**
   * Sets the popup menu to show when the arrow is clicked.
   * @param menu JPopupMenu
   */
  public void setMenu(JPopupMenu menu) {
    this.dropDownMenu = menu;
  }

  /**
   * returns the main (left hand side) button.
   * @return JButton
   */
  public JToggleButton getMainButton() {
    return mainButton;
  }

  /**
   * gets the drop down button (with the arrow)
   * @return JButton
   */
  public JButton getDropDownButton() {
    return dropDownButton;
  }

  /**
   * gets the drop down menu
   * @return JPopupMenu
   */
  public JPopupMenu getMenu() {
    return dropDownMenu;
  }

  /**
   * action listener for the arrow button- shows / hides the popup menu.
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e){
    if(this.dropDownMenu == null){
      return;
    }
    if(!dropDownMenu.isVisible()){
      Point p = this.getLocationOnScreen();
      dropDownMenu.setLocation( (int) p.getX(),
                                (int) p.getY() + this.getHeight());
      dropDownMenu.setVisible(true);
    }else{
      dropDownMenu.setVisible(false);
    }
  }

  /**
   * adds a action listener to this button (actually to the left hand side 
   * button, and any left over surrounding space.  the arrow button will not
   * be affected.
   * @param al ActionListener
   */
  public void addActionListener(ActionListener al){
    this.mainButton.addActionListener(al);
    this.addActionListener(al);
  }

	/**
	 * @param mainButton the mainButton to set. Must not be null
	 * @throws NullPointerException if mainButton == null
	 */
	public void setMainButton(JToggleButton mainButton) {
		if (this.mainButton != null) {
			this.remove(this.mainButton);
		}
		this.mainButton = mainButton;
		this.mainButton.setContentAreaFilled(false);
	    this.add(mainButton, BorderLayout.CENTER);
	    this.repaint();
	}
	
	/**
	 * @param dropDownButton the dropDownButton to set
	 */
	public void setDropDownButton(JButton dropDownButton) {
		this.dropDownButton = dropDownButton;
	}

//	/**
//	 * @return
//	 * @see java.awt.Component#isCursorSet()
//	 */
//	public boolean isCursorSet() {
//		return mainButton.isCursorSet();
//	}

//	/**
//	 * @return
//	 * @see java.awt.Component#isEnabled()
//	 */
//	public boolean isEnabled() {
//		return mainButton.isEnabled();
//	}

	/**
	 * @return
	 * @see javax.swing.AbstractButton#isSelected()
	 */
	public boolean isSelected() {
		return this.getMainButton().isSelected();
	}
	
	

//	/* (non-Javadoc)
//	 * @see javax.swing.AbstractButton#getModel()
//	 */
//	public ButtonModel getModel() {
//		return this.getMainButton().getModel();
//	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean b) {
		this.getMainButton().setSelected(b);
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#repaint()
	 */
	public void repaint() {
		super.repaint();
		if (this.getMainButton() != null) {
			this.getMainButton().repaint();
		}
		if (this.getMenu() != null) {
			this.getMenu().repaint();
		}
		if (this.getDropDownButton() != null) {
			this.getDropDownButton().repaint();
		}
	}
	
	

}
