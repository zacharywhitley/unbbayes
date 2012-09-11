package unbbayes.prm.view.graphicator.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.view.graphicator.IGraphicTableListener;

import com.mxgraph.swing.util.mxGraphTransferable;

/**
 * Graphic relational table that extends of JTtable.
 * 
 * @author David Saldana.
 * 
 */
public class GraphicRelTable extends JTable implements DropTargetListener,
		MouseListener {
	Logger log = Logger.getLogger(GraphicRelTable.class);

	/**
	 * Table to show
	 */
	private Table relationalTable;
	/**
	 * Columns or attributes that must be shown.
	 */
	private Column[] tableColumns;

	/**
    *
    */
	private static final long serialVersionUID = 5841175227984561071L;
	Object[][] data;
	String[] colNames = new String[] { "FK", "Name", "Apply" };

	private IGraphicTableListener tableListener;

	@SuppressWarnings("serial")
	public GraphicRelTable(Table t, IGraphicTableListener tableListener) {
		relationalTable = t;
		this.tableListener = tableListener;
		this.tableColumns = t.getColumns();

		data = new Object[tableColumns.length][3];

		for (int i = 0; i < tableColumns.length; i++) {
			Column c = tableColumns[i];

			data[i][0] = c.isPrimaryKey() ? "ID " : "";
			data[i][1] = c.getName();
			// data[i][2] = (Math.random() > 0.5) ? new ImageIcon(
			// JTableRenderer.class.getResource(JTableRenderer.IMAGE_PATH
			// + "preferences.gif")) : null;
		}
		setModel(createModel());
		setTableHeader(null);
		setAutoscrolls(true);
		setGridColor(Color.WHITE);
		TableColumn column = getColumnModel().getColumn(0);
		column.setMaxWidth(20);
		column = getColumnModel().getColumn(2);
		column.setMaxWidth(12);
		// column = getColumnModel().getColumn(3);
		// column.setMaxWidth(12);
		// column = getColumnModel().getColumn(4);
		// column.setMaxWidth(12);

		setTransferHandler(new TransferHandler() {

			/*
			 * @see
			 * javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent
			 * )
			 */
			@Override
			public int getSourceActions(JComponent c) {
				return COPY_OR_MOVE;
			}

			/*
			 * 
			 * @see
			 * javax.swing.TransferHandler#createTransferable(javax.swing.JComponent
			 * )
			 */
			protected Transferable createTransferable(JComponent c) {
				// sourceRow = getSelectedRow();
				// dragSource = JTableRenderer.this;
				// mxRectangle bounds = new mxRectangle(0, 0, MyTable.this
				// .getWidth(), 20);
				return new mxGraphTransferable(null, null, null);
			}
		});

		setDragEnabled(true);
		setDropTarget(new DropTarget(this, // component
				DnDConstants.ACTION_COPY_OR_MOVE, // actions
				this));
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		this.addMouseListener(this);
	}

	/**
    *
    */
	public DropTarget getDropTarget() {
		// if (!((mxGraphTransferHandler) graphContainer.getTransferHandler())
		// .isLocalDrag()) {
		// return super.getDropTarget();
		// }
		return null;
	}

	/*
	 * @see
	 * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	public void dragEnter(DropTargetDragEvent e) {
	}

	/*
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	public void dragOver(DropTargetDragEvent e) {
		// if (!((mxGraphTransferHandler) graphContainer.getTransferHandler())
		// .isLocalDrag()
		// && JTableRenderer.this != dragSource) {
		// Point p = e.getLocation();
		// int row = rowAtPoint(p);
		// getSelectionModel().setSelectionInterval(row, row);
		// }
	}

	/*
	 * 
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.
	 * DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/*
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent e) {
		// if (dragSource != null) {
		// e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		// Point p = e.getLocation();
		// int targetRow = rowAtPoint(p);
		//
		// Object edge = graph.insertEdge(null, null, null,
		// dragSource.cell, JTableRenderer.this.cell, "sourceRow="
		// + sourceRow + ";targetRow=" + targetRow);
		// graph.setSelectionCell(edge);
		//
		// // System.out.println("clearing drag source");
		// dragSource = null;
		// e.dropComplete(true);
		// } else {
		// e.rejectDrop();
		// }
	}

	/*
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte) {
	}

	/**
	 * 
	 * @return the created table model
	 */
	public TableModel createModel() {
		return new AbstractTableModel() {
			/**
            *
            */
			private static final long serialVersionUID = -3642207266816170738L;

			public int getColumnCount() {
				return colNames.length;
			}

			public int getRowCount() {
				return data.length;
			}

			public String getColumnName(int col) {
				return colNames[col];
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}

			public Class<? extends Object> getColumnClass(int c) {
				Object value = getValueAt(0, c);
				return (value != null) ? value.getClass() : ImageIcon.class;
			}

			/*
			 * The table is not editable.
			 */
			public boolean isCellEditable(int row, int col) {
				return col == 0;
			}

			/*
			 * Don't need to implement this method unless your table's data can
			 * change.
			 */
			public void setValueAt(Object value, int row, int col) {
				data[row][col] = value;
				fireTableCellUpdated(row, col);
			}
		};

	}

	public void mouseClicked(MouseEvent e) {
		// if (e.getClickCount() == 2) {
		int row = this.getSelectedRow();
		Column selectedCol = tableColumns[row];
		log.debug("Selected collumn " + selectedCol.getName());

		// Notify to listener.
		tableListener.selectedAttribute(new Attribute(relationalTable,
				selectedCol));
		
		// TODO consultar si ahora es 
		// }

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
}
