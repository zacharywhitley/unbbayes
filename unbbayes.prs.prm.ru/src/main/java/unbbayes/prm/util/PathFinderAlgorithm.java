package unbbayes.prm.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
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

	/**
	 * 
	 */
	public PathFinderAlgorithm() {
	}

	public List<Attribute[]> getPossiblePaths(final Attribute parent,
			final Attribute child) {
		// Every possible path
		paths = new ArrayList<Attribute[]>();
		checkedFks = new ArrayList<ForeignKey>();

		// A single path to start recursively.
		List<Attribute> path = new ArrayList<Attribute>();
		// Add parent as a fist node of the path.
		path.add(parent);

		identifyPaths(parent, child, path);

		return paths;
	}

	/**
	 * 
	 * @param path
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	private void identifyPaths(Attribute tmpParent, Attribute tmpChild,
			List<Attribute> path) {
		// log.debug("IdentifyPath size=" + path.size());
		// log.debug("Path =" + pathToString(path));

		// Three search algorithm //

		// 1. Check every referenced table based on foreign keys.
		ForeignKey[] fKeys = tmpParent.getTable().getForeignKeys();
		for (ForeignKey foreignKey : fKeys) {
			// Referenced table
			Table refTable = foreignKey.getForeignTable();

			// Create a hard copy for this new path.
			List<Attribute> newPath = new ArrayList<Attribute>(path);
			// Add local and remote FK
			Attribute tmpFKLocal = new Attribute(refTable, foreignKey
					.getFirstReference().getLocalColumn());

			Attribute tmpFKRemote = new Attribute(refTable, foreignKey
					.getFirstReference().getForeignColumn());
			newPath.add(tmpFKLocal);
			newPath.add(tmpFKRemote);
			log.debug("Adding " + refTable.getName() + "."
					+ tmpFKLocal.getAttribute().getName());
			log.debug("Adding " + refTable.getName() + "."
					+ tmpFKRemote.getAttribute().getName());

			// 2. Check if the target (child) is inside this column.
			Column[] columns = refTable.getColumns();

			boolean found = false;

			for (Column column : columns) {
				Attribute tmpAtt = new Attribute(refTable, column);

				if (tmpAtt.equals(tmpChild)) {
					// The child is the last in the path.
					newPath.add(tmpChild);
					Attribute[] array = newPath.toArray(new Attribute[0]);
					log.debug("Final Path =" + pathToString(array));
					paths.add(array);

					newPath = null;
					found = true;
					break;
				}
			}

			if (found) {
				continue;
			}
			// Check if the FK was checked previously.
			if (!checkedFks.contains(foreignKey)) {

				// FK is checked.
				checkedFks.add(foreignKey);

				// 3. If the target is not inside this column, then apply recu
				identifyPaths(tmpParent, tmpChild, newPath);
			}
		}
	}

	public static String pathToString(Attribute[] possiblePath) {
		String path = "";

		for (Attribute attribute : possiblePath) {
			path = path
					+ " "
					+ (attribute.getTable().getName() + "."
							+ attribute.getAttribute().getName() + " ");
		}
		return path;

	}

}
