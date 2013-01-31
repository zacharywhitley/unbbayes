package unbbayes.prm.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.model.Attribute;

/**
 * This class is an algorithm to find the possible paths between two attributes
 * where the parent is the initial node and the child is the target. This is a
 * simple three search.
 * 
 * @author David Saldaña
 * 
 */
public class PathFinderAlgorithm {
	Logger log = Logger.getLogger(PathFinderAlgorithm.class);
	/**
	 * A collection to store every possible path.
	 */
	List<Attribute[]> paths;

	/**
	 * To check if a foreign key was checked previously. It is to avoid cyclic
	 * references.
	 */
	List<ForeignKey> checkedFks;
	private Database db;
	/**
	 * String: attribute name. This is unique
	 * 
	 */
	private Hashtable<String, List<ForeignKey>> pointingAttributes;
	private Hashtable<ForeignKey, Table> fkLocalTables;

	/**
	 * Default construct.
	 */
	public PathFinderAlgorithm() {
	}

	/**
	 * This method find every possible path between two attributes.
	 * 
	 * @param endAtt
	 *            Ending attribute, the end of the path.
	 * @param initAtt
	 *            The fist attribute.
	 * @return all possible paths. Every path starts with the child and ends
	 *         with the parent.
	 */
	public List<Attribute[]> getPossiblePaths(Database db,
			final Attribute endAtt, final Attribute initAtt) {
		this.db = db;
		// Every possible path
		paths = new ArrayList<Attribute[]>();
		checkedFks = new ArrayList<ForeignKey>();

		indexPointingAttributes();

		// A single path to start recursively.
		List<Attribute> path = new ArrayList<Attribute>();
		// Add parent as a fist node of the path.
		path.add(initAtt);

		// Identify possible paths based on DB schema.
		identifyPaths(initAtt, endAtt, path);

		// Add Local or intrinsic path.
		if (endAtt.getTable().equals(initAtt.getTable())) {
			List<Attribute> intrinsecPath = new ArrayList<Attribute>();
			intrinsecPath.add(initAtt);
			intrinsecPath.add(endAtt);
			paths.add(intrinsecPath.toArray(new Attribute[0]));
		}

		return paths;
	}

	private void indexPointingAttributes() {

		pointingAttributes = new Hashtable<String, List<ForeignKey>>();
		fkLocalTables = new Hashtable<ForeignKey, Table>();

		Table[] allTables = db.getTables();

		// Each table
		for (Table table : allTables) {
			ForeignKey[] foreignKeys = table.getForeignKeys();

			// Each foreign key
			for (ForeignKey foreignKey : foreignKeys) {

				// Index
				Table foreignTable = foreignKey.getForeignTable();
				Column foreignColumn = foreignKey.getFirstReference()
						.getForeignColumn();
				// Create the index attribute
				Attribute indexAttribute = new Attribute(foreignTable,
						foreignColumn);

				// get
				List<ForeignKey> list = pointingAttributes.get(indexAttribute
						.toString());

				if (list == null) {
					list = new ArrayList<ForeignKey>();
					list.add(foreignKey);
					pointingAttributes.put(indexAttribute.toString(), list);
				} else {
					list.add(foreignKey);
				}
				fkLocalTables.put(foreignKey, table);

			}

		}

	}

	/**
	 * Identify possible paths based on the Database schema and foreign keys. It
	 * is a recursive algorithm.
	 * 
	 * 
	 * @param tmpInitAtt
	 *            temporal initial attribute. This is temporal because the
	 *            algorithm is recursive.
	 * @param endAtt
	 *            end attribute.
	 * @param path
	 *            created path
	 */
	private void identifyPaths(Attribute tmpInitAtt, Attribute endAtt,
			List<Attribute> path) {
		// log.debug("IdentifyPath size=" + path.size());
		// log.debug("Path =" + pathToString(path));

		// Three search algorithm //

		// ////////////// Find by Foreign key ///////////////////

		// 1. Check every referenced table based on foreign keys.
		ForeignKey[] fKeys = tmpInitAtt.getTable().getForeignKeys();

		// Find tables which reference this table.
		for (ForeignKey foreignKey : fKeys) {
			// Referenced table
			Table refTable = foreignKey.getForeignTable();
			Column localColumn = foreignKey.getFirstReference()
					.getLocalColumn();

			// Create a hard copy for this new path.
			List<Attribute> newPath = new ArrayList<Attribute>(path);
			// Add local and remote FK
			Attribute tmpFKLocal = new Attribute(tmpInitAtt.getTable(),
					localColumn);

			Attribute tmpFKRemote = new Attribute(refTable, foreignKey
					.getFirstReference().getForeignColumn());
			newPath.add(tmpFKLocal);
			newPath.add(tmpFKRemote);
			log.debug("Adding by fk:" + tmpFKLocal + ", " + tmpFKRemote);

			// 2. Check if the target (child) is inside this column.
			boolean found = isAttributeInTable(endAtt, refTable);

			if (found) {
				// The child is the last in the path.
				newPath.add(endAtt);
				Attribute[] array = newPath.toArray(new Attribute[0]);
				log.debug("Final Path =" + pathToString(array));
				paths.add(array);

				newPath = null;
				continue;
			}

			// Check if the FK was checked previously.
			if (!checkedFks.contains(foreignKey)) {

				// FK is checked.
				checkedFks.add(foreignKey);

				// 3. If the target is not inside this column, then apply
				// recursively.
				identifyPaths(new Attribute(refTable, localColumn), endAtt,
						newPath);
			}
		}

		// //////////// Find by Unique ID ////////////////
		Table localTable = tmpInitAtt.getTable();
		Column[] columns = localTable.getColumns();

		for (Column localColumn : columns) {

			// Only primary keys.
			if (!localColumn.isPrimaryKey()) {
				continue;
			}

			// Local attribute
			Attribute indexAttribute = new Attribute(localTable, localColumn);

			// Find every attribute who is pointing to a unique index
			List<ForeignKey> pointingAtts = pointingAttributes
					.get(indexAttribute.toString());

			// Index without associated foreign key.
			if (pointingAtts == null) {
				continue;
			}

			for (ForeignKey pointingFK : pointingAtts) {
				List<Attribute> newPath = new ArrayList<Attribute>(path);
				// External column with FK.
				Column extColumn = pointingFK.getFirstReference()
						.getLocalColumn();
				Table extTable = fkLocalTables.get(pointingFK);
				Attribute pointingAtt = new Attribute(extTable, extColumn);

				// Add to path
				boolean idExists = newPath.get(newPath.size() - 1).equals(
						indexAttribute);
				if (!idExists) {
					newPath.add(indexAttribute);
				}

				newPath.add(pointingAtt);

				log.debug("Adding by index " + indexAttribute + ", "
						+ pointingAtt);

				// 2. Check if the target (child) is inside this column.
				boolean found = isAttributeInTable(endAtt, extTable);

				if (found) {
					// The child is the last in the path.
					newPath.add(endAtt);
					Attribute[] array = newPath.toArray(new Attribute[0]);
					log.debug("Final Path =" + pathToString(array));
					paths.add(array);

					newPath = null;
					continue;
				}

				// Check if the FK was checked previously.
				if (!checkedFks.contains(pointingFK)) {

					// FK is checked.
					checkedFks.add(pointingFK);

					// 3. If the target is not inside this column, then apply
					// recursively.
					identifyPaths(pointingAtt, endAtt, newPath);
				}
			}

		}

	}

	private boolean isAttributeInTable(Attribute endAtt, Table refTable) {

		Column[] columns = refTable.getColumns();

		boolean found = false;

		for (Column column : columns) {
			Attribute tmpAtt = new Attribute(refTable, column);

			if (tmpAtt.equals(endAtt)) {

				found = true;
				break;
			}
		}
		return found;
	}

	public static String pathToString(Attribute[] possiblePath) {
		String path = "";

		for (Attribute attribute : possiblePath) {
			path = path + " " + attribute;
		}
		return path;

	}

}
