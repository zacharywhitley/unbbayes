package unbbayes.prm.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

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

	private Attribute child;
	private Attribute parent;

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
	 * @param parent
	 * @param child
	 */
	public PathFinderAlgorithm(Attribute parent, Attribute child) {
		this.parent = parent;
		this.child = child;

		// Every possible path
		paths = new ArrayList<Attribute[]>();
		checkedFks = new ArrayList<ForeignKey>();
	}

	public List<Attribute[]> getPossiblePaths() {

		// A single path to start recursively.
		List<Attribute> path = new ArrayList<Attribute>();

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
		// The parent is the fist in the path
		path.add(tmpParent);

		// Three search algorithm //

		// 1. Check every referenced table based on foreign keys.
		ForeignKey[] fKeys = parent.getTable().getForeignKeys();
		for (ForeignKey foreignKey : fKeys) {
			Table refTable = foreignKey.getForeignTable();

			// 2. Check if the target (child) is inside this column.
			Column[] columns = refTable.getColumns();

			for (Column column : columns) {
				Attribute tmpAtt = new Attribute(refTable, column);
			
				if (tmpAtt.equals(tmpChild)) {
					// The child is the last in the path.
					path.add(child);
					paths.add(path.toArray(new Attribute[0]));
				}
			}

			// Check if the fk was checked previously.
			if (!checkedFks.contains(foreignKey)) {

				// FK is checked.
				checkedFks.add(foreignKey);

				// 3. If the target is not inside this column, then apply recu
				Attribute tmpAtt = new Attribute(refTable, foreignKey
						.getFirstReference().getForeignColumn());
				identifyPaths(tmpAtt, tmpChild, new ArrayList<Attribute>(path));
			}
		}
	}
}
