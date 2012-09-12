package unbbayes.prm.view.graphicator;

import java.util.HashMap;

import javax.swing.ImageIcon;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.PrmProcessState;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.RelSchemaConsult;
import unbbayes.prm.view.graphicator.editor.BasicGraphEditor;
import unbbayes.prm.view.graphicator.editor.EditorPalette;
import unbbayes.prm.view.graphicator.editor.SchemaGraphComponent;
import unbbayes.prm.view.graphicator.editor.TableRenderer;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

public class RelationalGraphicator extends BasicGraphEditor {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(RelationalGraphicator.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_COLUMNS = 4;
	private Database dbSchema;

	private PrmProcessState prmState = PrmProcessState.ProbModel;

	/**
	 * Indexed object of tables. key: unique table name object: graphic table
	 * object.
	 */
	HashMap<String, Object> indexedTables;
	private SchemaGraphComponent schemaGraphComponent;

	public RelationalGraphicator(Database dbSchema,
			IGraphicTableListener tableListener) {
		// Get DB schema.
		this.dbSchema = dbSchema;

		schemaGraphComponent = new SchemaGraphComponent(new mxGraph() {
			/**
			 * Allows expanding tables
			 */
			public boolean isCellFoldable(Object cell, boolean collapse) {
				return model.isVertex(cell);
			}
		}, new RelSchemaConsult(dbSchema), tableListener);

		initGraphEditor("PRM Plugin", schemaGraphComponent);

		//

		addGraphicTables();

		graphOutline.setVisible(false);

		// Palette
		// addPalete();

	}

	public void showPalette() {
		EditorPalette shapesPalette = insertPalette("Dependency structure");

		mxCell tableTemplate = new mxCell("New Table", new mxGeometry(0, 0,
				200, 280), null);
		tableTemplate.getGeometry().setAlternateBounds(
				new mxRectangle(0, 0, 140, 25));
		tableTemplate.setVertex(true);

		shapesPalette.addEdgeTemplate(
				"Arrow",
				new ImageIcon(RelationalGraphicator.class
						.getResource(TableRenderer.IMAGE_PATH + "arrow.png")),
				"arrow", 120, 120, "");

		// Show outline.
		graphOutline.setVisible(true);

	}

	private void addGraphicTables() {
		mxGraph graph = getGraphComponent().getGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		Table[] tables = dbSchema.getTables();

		try {
			indexedTables = new HashMap<String, Object>();

			// Add graphic tables.
			for (int i = 0; i < tables.length; i++) {
				int x = (i % NUM_COLUMNS) * 220 + 20;// Only 5 Columns
				int y = (i / NUM_COLUMNS) * 200 + 20;
				String tableName = tables[i].getName();
				// Add cell
				Object v1 = graph.insertVertex(parent, null, tableName, x, y,
						180, 150);
				indexedTables.put(tableName, v1);
			}

			// References
			// For each Table
			for (int i = 0; i < tables.length; i++) {
				ForeignKey[] foreignKeys = tables[i].getForeignKeys();
				String tableName = tables[i].getName();
				// For each foreign key
				for (ForeignKey foreignKey : foreignKeys) {
					String referenced = foreignKey.getForeignTable().getName();
					Object v1 = indexedTables.get(tableName);
					Object v2 = indexedTables.get(referenced);

					// For each reference
					String local = foreignKey.getFirstReference()
							.getLocalColumnName();
					String foreign = foreignKey.getFirstReference()
							.getForeignColumnName();
					String edgeTitle = local + " -> " + foreign;
					graph.insertEdge(parent, null, edgeTitle, v1, v2);
				}

			}
		} finally {
			graph.getModel().endUpdate();
		}

	}

	/**
	 * @return the prmState
	 */
	public PrmProcessState getPrmState() {
		return prmState;
	}

	/**
	 * @param prmState
	 *            the prmState to set
	 */
	public void setPrmState(PrmProcessState prmState) {
		this.prmState = prmState;
	}

	public void drawRelationShip(ParentRel newRel) {
		log.debug("Drawing relationship");
		// FIXME change arrow type.
		mxGraph graph = getGraphComponent().getGraph();
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try {
			// Graphic tables
			Object v1 = indexedTables.get(newRel.getParent().getTable()
					.getName());
			Object v2 = indexedTables.get(newRel.getChild().getTable()
					.getName());

			String edgeTitle = "Pa["
					+ newRel.getChild().getAttribute().getName() + "]="
					+ newRel.getParent().getAttribute().getName();

			graph.insertEdge(parent, null, edgeTitle, v1, v2);
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public TableRenderer getGraphicTable(String name) {
		return schemaGraphComponent.getGraphicTable(name);
	}
}
