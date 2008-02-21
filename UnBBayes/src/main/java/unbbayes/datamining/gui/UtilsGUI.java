/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.miginfocom.layout.PlatformDefaults;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/08/2007
 */
public class UtilsGUI implements Serializable {

	private static final long serialVersionUID = 1L;

	private static boolean buttonOpaque = true;
	private static final Color LABEL_COLOR = new Color(0, 70, 213);

	public static JLabel createLabel(String text) {
		return createLabel(text, SwingConstants.LEADING);
	}

	public static JLabel createLabel(String text, int align) {
		final JLabel b = new JLabel(text, align);

		return b;
	}

	public static JComboBox createCombo(String[] items) {
		JComboBox combo = new JComboBox(items);

		if (PlatformDefaults.getCurrentPlatform() == PlatformDefaults.MAC_OSX)
			combo.setOpaque(false);

		return combo;
	}

	public static JTextField createTextField(int cols) {
		return createTextField("", cols);
	}

	public static JTextField createTextField(String text) {
		return createTextField(text, 0);
	}

	public static JTextField createTextField(String text, int cols) {
		final JTextField b = new JTextField(text, cols);

		return b;
	}

	public static JButton createButton() {
		return createButton("");
	}

	public static JButton createButton(String text) {
		return createButton(text, false);
	}

	public static JButton createButton(String text, boolean bold) {
		JButton b = new JButton(text);

		if (bold) {
			b.setFont(b.getFont().deriveFont(Font.BOLD));
		}
		b.setOpaque(buttonOpaque); // Or window's buttons will have strange border
		if (PlatformDefaults.getCurrentPlatform() == PlatformDefaults.MAC_OSX) {
			b.setContentAreaFilled(false);
		}

		return b;
	}

	public static JToggleButton createToggleButton(String text) {
		JToggleButton b = new JToggleButton(text);
		b.setOpaque(buttonOpaque); // Or window's buttons will have strange border
		
		return b;
	}

	public static JCheckBox createCheck(String text) {
		JCheckBox b = new JCheckBox(text);
		b.setOpaque(buttonOpaque); // Or window's checkboxes will have strange border

		return b;
	}

	public static JPanel createTabPanel(LayoutManager lm) {
		JPanel panel = new JPanel(lm);
		panel.setOpaque(buttonOpaque);

		return panel;
	}

	public static JComponent createPanel() {
		return createPanel("");
	}

	public static JComponent createPanel(String s) {
		JLabel panel = new JLabel(s, SwingConstants.CENTER);

		panel.setBorder(new EtchedBorder());
		panel.setOpaque(true);

		return panel;
	}

	public static JTextArea createTextArea(String text, int rows, int cols) {
		JTextArea ta = new JTextArea(text, rows, cols);

		ta.setBorder(UIManager.getBorder("TextField.border"));
		ta.setFont(UIManager.getFont("TextField.font"));
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);

		return ta;
	}

	public static JScrollPane createTextAreaScroll(String text, int rows, int cols,
			boolean hasVerScroll) {
		JTextArea ta = new JTextArea(text, rows, cols);

		ta.setFont(UIManager.getFont("TextField.font"));
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);

		JScrollPane scroll = new JScrollPane(
			    ta,
			    hasVerScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
			    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		return scroll;
	}

	public static void addSeparator(JPanel panel, String text) {
		JLabel l = createLabel(text);
		l.setForeground(LABEL_COLOR);

		panel.add(l, "gapbottom 1, span, split 2");
		panel.add(new JSeparator(), "gapleft rel, growx");
	}

	
	
	public class RowHeaderRenderer extends JLabel implements ListCellRenderer,
	Serializable {
		private static final long serialVersionUID = 1L;

		public RowHeaderRenderer(JTable table, boolean border) {
			JTableHeader header = table.getTableHeader();
			setOpaque(true);
			setHorizontalAlignment(RIGHT);
			setForeground(header.getForeground());
			setBackground(header.getBackground());
			setFont(header.getFont());
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	public class EachRowRenderer implements TableCellRenderer,
	Serializable {
		private static final long serialVersionUID = 1L;
		protected ArrayList<TableCellRenderer> renderers;
		protected TableCellRenderer renderer, defaultRenderer;

		public EachRowRenderer() {
			renderers = new ArrayList<TableCellRenderer>();
			defaultRenderer = new DefaultTableCellRenderer();
		}

		public void add(int row, TableCellRenderer renderer) {
			renderers.add(row, renderer);
		}

		public void remove(int row) {
			renderers.remove(row);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			renderer = renderers.get(row);
			if (renderer == null) {
				renderer = defaultRenderer;
			}
			return renderer.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
	}

	public class EachRowEditor implements TableCellEditor,
	Serializable {
		private static final long serialVersionUID = 1L;
		protected ArrayList<TableCellEditor> editors;
		protected TableCellEditor editor, defaultEditor;

		JTable table;

		public EachRowEditor(JTable table) {
			this.table = table;
			editors = new ArrayList<TableCellEditor>();
			defaultEditor = new DefaultCellEditor(new JTextField());
		}

		public void setEditorAt(int row, TableCellEditor editor) {
			editors.add(row, editor);
		}

		public void remove(int row) {
			editors.remove(row);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			editor = editors.get(row);
			if (editor == null) {
				editor = defaultEditor;
			}
			return editor.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

		public Object getCellEditorValue() {
			return editor.getCellEditorValue();
		}

		public boolean stopCellEditing() {
			return editor.stopCellEditing();
		}

		public void cancelCellEditing() {
			editor.cancelCellEditing();
		}

		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				selectEditor((MouseEvent) anEvent);
			}
			
			return editor.isCellEditable(anEvent);
		}

		public void addCellEditorListener(CellEditorListener l) {
			editor.addCellEditorListener(l);
		}

		public void removeCellEditorListener(CellEditorListener l) {
			editor.removeCellEditorListener(l);
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				selectEditor((MouseEvent) anEvent);
			}
			
			return editor.shouldSelectCell(anEvent);
		}

		protected void selectEditor(MouseEvent e) {
			int row;
			if (e == null) {
				row = table.getSelectionModel().getAnchorSelectionIndex();
			} else {
				row = table.rowAtPoint(e.getPoint());
			}
			editor = (TableCellEditor) editors.get(row);
			if (editor == null) {
				editor = defaultEditor;
			}
		}
	}

	public class RadioButtonRenderer extends JCheckBox implements TableCellRenderer,
	Serializable {
		private static final long serialVersionUID = 1L;

		private JRadioButton button = new JRadioButton();

		public RadioButtonRenderer() {
			super("");
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null) {
				return null;
			}
			button.setSelected((Boolean) value);
			button.setHorizontalAlignment(JRadioButton.CENTER);
			
			return (Component) button;
		}
	}

	public class RadioButtonEditor extends DefaultCellEditor implements ItemListener,
	Serializable {
		private static final long serialVersionUID = 1L;

		private JRadioButton button;

		public RadioButtonEditor() {
			super(new JCheckBox(""));
			button = new JRadioButton("");
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (value == null) {
				return null;
			}
			button.setSelected((Boolean) value);
			button.addItemListener(this);
			button.setHorizontalAlignment(JRadioButton.CENTER);
			
			return (Component) button;
		}

		public Object getCellEditorValue() {
			return button;
		}

		public void itemStateChanged(ItemEvent e) {
			super.fireEditingStopped();
		}
	}

	public class ComboBoxRenderer extends JComboBox implements TableCellRenderer,
			Serializable {
		private static final long serialVersionUID = 1L;

		public ComboBoxRenderer(String[] items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setSelectedItem(value);
			
			return this;
		}

	}

	public class ComboBoxEditor extends DefaultCellEditor
	implements Serializable {
		private static final long serialVersionUID = 1L;

		public ComboBoxEditor(String[] items) {
			super(new JComboBox(items));
		}
	}

	public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer,
	Serializable {

		private static final long serialVersionUID = -8068756382226198363L;

		public CheckBoxRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setSelected((value != null && ((Boolean) value).booleanValue()));
			
			return this;
		}
	}

	public static class CheckBoxEditor extends DefaultCellEditor
	implements Serializable {
		private static final long serialVersionUID = 1L;

		private static final JCheckBox checkBox = new JCheckBox();

		public CheckBoxEditor() {
			super(checkBox);
			checkBox.setHorizontalAlignment(JCheckBox.CENTER);
		}
	}

	public class JTableButtonRenderer extends JButton implements TableCellRenderer,
	Serializable {
		private static final long serialVersionUID = 1L;
		private JButton button;
		private String label;
		 
		public JTableButtonRenderer(JButton button) {
			this.button = button;
			setOpaque(true);
		}
			
		public Component getTableCellRendererComponent(JTable table, Object value,
										 boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else{
				button.setForeground(table.getForeground());
				button.setBackground(UIManager.getColor("Button.background"));
			}
			if (value != null && value instanceof JButton) {
				label = ((JButton) value).getText();
				button.setText(label);
			}
			return button;
		}
	}


	public class JTableButtonEditor extends DefaultCellEditor
	implements Serializable {
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private String label;
	 
		public JTableButtonEditor(JButton button) {
			super(new JCheckBox());
			this.button = button;
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}
	 
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else{
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			if (value != null && value instanceof JButton) {
				label = ((JButton) value).getText();
				button.setText(label);
			}

			return button;
		}
	 
		public Object getCellEditorValue() {
			return button;
		}
		 
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}
	 
		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
	
	public class JTableButtonMouseListener implements MouseListener,
	Serializable {
		private JTable table;

		public JTableButtonMouseListener(JTable table) {
			this.table = table;
		}

		private void forwardEventToButton(MouseEvent e) {
			TableColumnModel columnModel = table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row	= e.getY() / table.getRowHeight();
			Object value;
			JButton button;
			MouseEvent buttonEvent;

			if (row >= table.getRowCount() || row < 0 ||
				 column >= table.getColumnCount() || column < 0) {
				return;
			}

			value = table.getValueAt(row, column);

			if (!(value instanceof JButton)) {
				return;
			}
			
			button = (JButton) value;

			buttonEvent =
				(MouseEvent) SwingUtilities.convertMouseEvent(table, e, button);
			button.dispatchEvent(buttonEvent);
			// This is necessary so that when a button is pressed and released
			// it gets rendered properly.	Otherwise, the button may still appear
			// pressed down when it has been released.
			table.repaint();
		}

		public void mouseClicked(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseEntered(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseExited(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mousePressed(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseReleased(MouseEvent e) {
			forwardEventToButton(e);
		}
	}

}

