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

package unbbayes.gui.umpst;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;

/**
 * Adapted from source code of javax.swing.DefaultCellEditor
 */
public class TableTree extends AbstractCellEditor implements
		TableCellEditor {

	private static final long serialVersionUID = 1L;

	JTree tree;
	

	protected int clickCountToStart = 1;

	public TableTree() {
		/**tree = new JTree();
		tree.setHorizontalAlignment(JTree.CENTER);
		tree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// do not allow de-selection of radioButton
				// this is provided for in the method
				// resetNonSelectedValues
				// of the CustomTableModel class
				if (!tree.isSelected())
					cancelCellEditing();
				stopCellEditing();
			}s
		});*/
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int col) {
		
		tree = new JTree();
		

		/**Criar e retornar todos os filhos do Goal que eu passei*/
		
		return tree;
	}

	public Component getComponent() {
		return tree;
	}

	public int getClickCountToStart() {
		return clickCountToStart;
	}

	public Object getCellEditorValue() {
		return Boolean.valueOf(tree.isSelectionEmpty());
	}

	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent)
			return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
		return true;
	}

	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	public boolean stopCellEditing(EventObject anEvent) {
		fireEditingStopped();
		return true;
	}

	public void cancelCellEditing() {
		fireEditingCanceled();
	}
}
