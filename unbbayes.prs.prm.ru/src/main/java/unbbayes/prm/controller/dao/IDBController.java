package unbbayes.prm.controller.dao;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import unbbayes.prm.model.Attribute;

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
	 */
	void init(String URL);
	
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
	 * @param t table to search.
	 * @param cols interested columns
	 * @return possible values.
	 */
	String[] getPossibleValues(Database db, Table t, Column[] cols);
	
	
	/**
	 * Get the possible values of some columns of a table. This is only
	 * supported for types VARCHAR.
	 * 
	 * @param t table to search.
	 * @param cols interested columns
	 * @return possible values.
	 */
	String[] getPossibleValues(Database db, Attribute attribute);
}
