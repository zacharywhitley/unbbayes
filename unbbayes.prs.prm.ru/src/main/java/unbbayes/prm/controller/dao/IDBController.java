package unbbayes.prm.controller.dao;

import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prm.model.ParentRel;

/**
 * Load a relational schema from a data source (database or xml). The model is
 * based on the ddlutils library. The model can be seen here:
 * http://db.apache.org
 * /ddlutils/api-usage.html#Reading+the+model+from+a+live+database
 * 
 * @author David Saldaña
 * 
 */
public interface IDBController {

	/**
	 * Initialize the controller.
	 * 
	 * @throws Exception
	 */
	void init(String URL) throws Exception;

	/**
	 * End the controller.
	 */
	void end();

	/**
	 * Get a relational schema from a datasource.
	 * 
	 * @param URL
	 * @return
	 */
	Database getRelSchema();

	/**
	 * Get the possible values of some columns of a table. This is only
	 * supported for types VARCHAR.
	 * 
	 * @param t
	 *            table to search.
	 * @param cols
	 *            interested columns
	 * @return possible values.
	 */
	String[] getPossibleValues(Table t, Column[] cols);

	/**
	 * Get the possible values of some columns of a table. This is only
	 * supported for types VARCHAR.
	 * 
	 * @param t
	 *            table to search.
	 * @param cols
	 *            interested columns
	 * @return possible values.
	 */
	String[] getPossibleValues(Attribute attribute);

	/**
	 * Get all table values from database.
	 * 
	 * @param db
	 *            database
	 * @param t
	 *            table.
	 * @return table values.
	 */
	Iterator<DynaBean> getTableValues(Table t);

	/**
	 * Get every related instance to one instance.
	 * 
	 * @param db
	 * @param path
	 *            path to achieve Example: PERSON.BLOODTYPE -> PERSON.MOTHER ->
	 *            PERSON.ID -> PERSON.BLOODTYPE
	 * @param queryIndex
	 *            FK value to search.
	 * @return value of the instances.
	 */
	String[][] getParentRelatedInstances(ParentRel path, String queryIndex);

	/**
	 * Get a specific value of an instance.
	 * 
	 * @param queryColumn
	 *            query column
	 * @param idColum
	 *            unique index column.
	 * @param idValue
	 *            instance id.
	 * @return the specific attribute queryColumn of the table idColumn.table.
	 */
	String getSpecificValue(Column queryColumn, Attribute idColum,
			String instanceId);

	/**
	 * Get a specific value of an instance, but it also must be related to the
	 * foreign key.
	 * 
	 * @param queryColumn
	 *            query column
	 * @param idColum
	 *            unique index column.
	 * @param idValue
	 *            instance id.
	 * @param fkColum
	 *            foreign key of another table.
	 * @return the specific attribute queryColumn of the table idColumn.table.
	 */
//	String getSpecificValue(Column queryColumn, Attribute idColum,
//			Attribute fkColum, Attribute origIdAttribute,
//			String originInstanceId);

	double[] getStateProbability(AttributeStates childStates);
}
