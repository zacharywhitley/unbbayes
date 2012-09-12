/*
 * $Id: JTableRenderer.java,v 1.6 2012-07-29 09:30:47 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package unbbayes.prm.view.graphicator.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.util.RelSchemaConsult;
import unbbayes.prm.view.graphicator.IGraphicTableListener;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.view.mxGraph;

/**
 * @author Administrator
 * 
 */
public class TableRenderer extends JComponent {
	Logger log = Logger.getLogger(TableRenderer.class);
	/**
     *
     */
	private static final long serialVersionUID = 2106746763664760745L;
	/**
     *
     */
	public static final String IMAGE_PATH = "/unbbayes/prm/view/graphicator/editor/images/";
	/**
     *
     */
	protected static TableRenderer dragSource = null;
	/**
     *
     */
	protected static int sourceRow = 0;
	/**
     *
     */
	protected Object cell;
	/**
     *
     */
	protected mxGraphComponent graphContainer;
	/**
     *
     */
	protected mxGraph graph;
	/**
     *
     */
	public GraphicRelTable table;

	/**
     *
     */
	@SuppressWarnings("serial")
	public TableRenderer(final Object cell,
			final mxGraphComponent graphContainer, RelSchemaConsult consult,
			IGraphicTableListener tableListener) {
		this.cell = cell;
		this.graphContainer = graphContainer;
		this.graph = graphContainer.getGraph();

		String cellName = String.valueOf(graph.getLabel(cell));

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(
				ShadowBorder.getSharedInstance(),
				BorderFactory.createBevelBorder(BevelBorder.RAISED)));

		// Cell Title (Table title)
		JPanel title = new JPanel();
		title.setBackground(new Color(149, 173, 239));
		title.setOpaque(true);
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
		title.setLayout(new BorderLayout());
		// Icon for the title
		JLabel icon = new JLabel(
				new ImageIcon(TableRenderer.class.getResource(IMAGE_PATH
						+ "preferences.gif")));
		icon.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 1));
		title.add(icon, BorderLayout.WEST);
		// Title label.
		JLabel labelTitle = new JLabel(cellName);
		labelTitle.setForeground(Color.WHITE);
		labelTitle.setFont(title.getFont().deriveFont(Font.BOLD, 11));
		labelTitle.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
		title.add(labelTitle, BorderLayout.CENTER);

		// Minimize button on the top
		JButton buttonMinimize = new JButton(new AbstractAction("",
				new ImageIcon(TableRenderer.class.getResource(IMAGE_PATH
						+ "minimize.gif"))) {
			public void actionPerformed(ActionEvent e) {
				graph.foldCells(!graph.isCellCollapsed(cell), false,
						new Object[] { cell });
				((JButton) e.getSource()).setIcon(new ImageIcon(
						TableRenderer.class.getResource(IMAGE_PATH
								+ ((graph.isCellCollapsed(cell)) ? "maximize.gif"
										: "minimize.gif"))));
			}
		});
		buttonMinimize.setPreferredSize(new Dimension(16, 16));
		buttonMinimize.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		buttonMinimize.setToolTipText("Collapse/Expand");
		buttonMinimize.setOpaque(false);
		// A panel for minimizing button.
		JPanel panelMinimize = new JPanel();
		panelMinimize.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
		panelMinimize.setOpaque(false);
		panelMinimize.add(buttonMinimize);

		title.add(panelMinimize, BorderLayout.EAST);
		add(title, BorderLayout.NORTH);

		// CellStyle style =
		// graph.getStylesheet().getCellStyle(graph.getModel(),
		// cell);
		// if (style.getStyleClass() == null) {

		// Get table
		Table tableByName = consult.getTableByName(cellName);

		if (tableByName != null) {
			// Table
			table = new GraphicRelTable(tableByName, tableListener);
		} else {
			log.warn("Table " + cellName + " not found");
		}
		// Scroll
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		scrollPane.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						graphContainer.refresh();
					}
				});
		// Without rows it looks white.
		if (graph.getModel().getChildCount(cell) == 0) {
			scrollPane.getViewport().setBackground(Color.WHITE);
			setOpaque(true);
			add(scrollPane, BorderLayout.CENTER);
		}

		setMinimumSize(new Dimension(20, 30));
	}

	/**
	 * Implements an event redirector for the specified handle index, where 0 is
	 * the top right, and 1-7 are the top center, rop right, middle left, middle
	 * right, bottom left, bottom center and bottom right, respectively. Default
	 * index is 7 (bottom right).
	 */
	public class ResizeHandler implements MouseListener, MouseMotionListener {

		protected int index;

		public ResizeHandler() {
			this(7);
		}

		public ResizeHandler(int index) {
			this.index = index;
		}

		public void mouseClicked(MouseEvent e) {
			// ignore
		}

		public void mouseEntered(MouseEvent e) {
			// ignore
		}

		public void mouseExited(MouseEvent e) {
			// ignore
		}

		public void mousePressed(MouseEvent e) {
			// Selects to create a handler for resizing
			if (!graph.isCellSelected(cell)) {
				graphContainer.selectCellForEvent(cell, e);
			}

			// Initiates a resize event in the handler
			mxCellHandler handler = graphContainer.getSelectionCellsHandler()
					.getHandler(cell);

			if (handler != null) {
				// Starts the resize at index 7 (bottom right)
				handler.start(
						SwingUtilities.convertMouseEvent(
								(Component) e.getSource(), e,
								graphContainer.getGraphControl()), index);
				e.consume();
			}
		}

		public void mouseReleased(MouseEvent e) {
			graphContainer.getGraphControl().dispatchEvent(
					SwingUtilities.convertMouseEvent((Component) e.getSource(),
							e, graphContainer.getGraphControl()));
		}

		public void mouseDragged(MouseEvent e) {
			graphContainer.getGraphControl().dispatchEvent(
					SwingUtilities.convertMouseEvent((Component) e.getSource(),
							e, graphContainer.getGraphControl()));
		}

		public void mouseMoved(MouseEvent e) {
			// ignore
		}
	}

	/**
     *
     */
	public static TableRenderer getVertex(Component component) {
		while (component != null) {
			if (component instanceof TableRenderer) {
				return (TableRenderer) component;
			}
			component = component.getParent();
		}

		return null;
	}

	public void enableCPDFor(Column c) {
		table.enableCPDFor(c);
	}
	
	public void disableCPDFor(Column c) {
		table.disableCPDFor(c);
	}
}
