package unbbayes.prm.view.graphicator.editor;

import java.awt.Component;

import javax.swing.ImageIcon;

import unbbayes.prm.util.RelSchemaConsult;
import unbbayes.prm.view.graphicator.IGraphicTableListener;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class SchemaGraphComponent extends mxGraphComponent {

	/**
     *
     */
	private static final long serialVersionUID = -1152655782652932774L;
	private RelSchemaConsult relSchemaConsult;
	private IGraphicTableListener tableListener;

	/**
	 * 
	 * @param graph
	 * @param relSchemaConsult
	 */
	public SchemaGraphComponent(mxGraph graph, RelSchemaConsult relSchemaConsult, IGraphicTableListener tableListener) {
		super(graph);
		this.relSchemaConsult = relSchemaConsult;
		this.tableListener = tableListener;

		// mxGraphView graphView = new mxGraphView(graph) {
		// /**
		// *
		// */
		// public void updateFloatingTerminalPoint(mxCellState edge,
		// mxCellState start, mxCellState end, boolean isSource) {
		// int col = getColumn(edge, isSource);
		//
		// if (col >= 0) {
		// double y = getColumnLocation(edge, start, col);
		// boolean left = start.getX() > end.getX();
		//
		// if (isSource) {
		// double diff = Math.abs(start.getCenterX()
		// - end.getCenterX())
		// - start.getWidth() / 2 - end.getWidth() / 2;
		//
		// if (diff < 40) {
		// left = !left;
		// }
		// }
		//
		// double x = (left) ? start.getX() : start.getX()
		// + start.getWidth();
		// double x2 = (left) ? start.getX() - 20 : start.getX()
		// + start.getWidth() + 20;
		//
		// int index2 = (isSource) ? 1
		// : edge.getAbsolutePointCount() - 1;
		// edge.getAbsolutePoints().add(index2, new mxPoint(x2, y));
		//
		// int index = (isSource) ? 0
		// : edge.getAbsolutePointCount() - 1;
		// edge.setAbsolutePoint(index, new mxPoint(x, y));
		// } else {
		// super.updateFloatingTerminalPoint(edge, start, end,
		// isSource);
		// }
		// }
		// };
		//
		// graph.setView(graphView);
	}

	/**
	 * 
	 * @param edge
	 * @param isSource
	 * @return the column number the edge is attached to
	 */
	public int getColumn(mxCellState state, boolean isSource) {
		if (state != null) {
			if (isSource) {
				return mxUtils.getInt(state.getStyle(), "sourceRow", -1);
			} else {
				return mxUtils.getInt(state.getStyle(), "targetRow", -1);
			}
		}

		return -1;
	}

	/**
     *
     */
	public int getColumnLocation(mxCellState edge, mxCellState terminal,
			int column) {
		// Component[] c = components.get(terminal.getCell());
		int y = 0;

		// if (c != null) {
		// for (int i = 0; i < c.length; i++) {
		// if (c[i] instanceof JTableRenderer) {
		// JTableRenderer vertex = (JTableRenderer) c[i];
		//
		// JTable table = vertex.table;
		// JViewport viewport = (JViewport) table.getParent();
		// double dy = -viewport.getViewPosition().getY();
		// y = (int) Math.max(terminal.getY() + 22, terminal.getY()
		// + Math.min(terminal.getHeight() - 20, 30 + dy
		// + column * 16));
		// }
		// }
		// }

		return y;
	}

	/**
     *
     */
	public Component[] createComponents(mxCellState state) {
		if (getGraph().getModel().isVertex(state.getCell())) {
			return new Component[] { new TableRenderer(state.getCell(), this,
					relSchemaConsult,tableListener) };
		}

		return null;
	}

	/**
	 * Disables folding icons.
	 */
	public ImageIcon getFoldingIcon(mxCellState state) {
		return null;
	}
}
