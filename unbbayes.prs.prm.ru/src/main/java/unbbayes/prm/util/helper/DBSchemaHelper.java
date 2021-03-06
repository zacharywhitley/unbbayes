package unbbayes.prm.util.helper;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;

public class DBSchemaHelper {
	private static Logger log = Logger.getLogger(DBSchemaHelper.class);

	public static Column getUniqueIndex(Table t) {
		Index[] indices = t.getUniqueIndices();

		// we assume there is only one index.
		if (indices.length == 1) {
			return t.findColumn(indices[0].getName());
		} else if (indices.length > 1) {
			log.error("More than one unique index for table is not supported yet.");
		}
		return null;
	}

	/**
	 * Invert parent to child in a Parent relationship.
	 * 
	 * @param rel
	 *            relationship to invert
	 * @return inverted relationship
	 * @deprecated not in use.
	 */
	public static ParentRel invertParentRelationship(ParentRel rel) {
		ParentRel inverted = new ParentRel(rel.getChild(), rel.getParent());

		Attribute[] path = rel.getPath();
		Attribute[] invertedPath = new Attribute[path.length];

		for (int i = 0; i < path.length; i++) {
			invertedPath[path.length - i - 1] = path[i];
		}
		inverted.setPath(invertedPath);

		return inverted;
	}

	/**
	 * Evaluate if the attribute is a foreign key.
	 * 
	 * @param a
	 *            attribute.
	 * @return if the attribute is a foreign key.
	 */
	public static boolean isAttributeFK(Attribute a) {
		ForeignKey[] foreignKeys = a.getTable().getForeignKeys();

		for (ForeignKey foreignKey : foreignKeys) {
			Reference[] refs = foreignKey.getReferences();
			for (Reference reference : refs) {
				if (reference.getLocalColumn().getName().equals(
						a.getAttribute().getName())) {
					return true;
				}
			}	
		}
		return false;
	}
}
