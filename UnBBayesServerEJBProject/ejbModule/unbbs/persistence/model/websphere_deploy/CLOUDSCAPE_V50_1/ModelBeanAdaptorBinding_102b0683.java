package unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1;
/**
 * ModelBeanAdaptorBinding_102b0683
 * @generated
 */
public class ModelBeanAdaptorBinding_102b0683
	implements com.ibm.ws.ejbpersistence.beanextensions.BeanAdaptorBinding {
	/**
	 * getExtractor
	 * @generated
	 */
	public com.ibm.ws.ejbpersistence.dataaccess.EJBExtractor getExtractor() {
		int beanChunkLength = 5;
		// extractor for unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanExtractor_102b0683
		com.ibm.ws.ejbpersistence.dataaccess.AbstractEJBExtractor extractor =
			new unbbs
				.persistence
				.model
				.websphere_deploy
				.CLOUDSCAPE_V50_1
				.ModelBeanExtractor_102b0683();
		extractor.setChunkLength(beanChunkLength);
		extractor.setPrimaryKeyColumns(new int[] { 1 });
		extractor.setDataColumns(new int[] { 1, 2, 3, 4, 5 });
		return extractor;
	}
	/**
	 * getInjector
	 * @generated
	 */
	public com.ibm.ws.ejbpersistence.beanextensions.EJBInjector getInjector() {
		return new unbbs
			.persistence
			.model
			.websphere_deploy
			.CLOUDSCAPE_V50_1
			.ModelBeanInjectorImpl_102b0683();
	}
	/**
	 * getAdapter
	 * @generated
	 */
	public com.ibm.websphere.ejbpersistence.EJBToRAAdapter getAdapter() {
		return com.ibm.ws.rsadapter.cci.WSRelationalRAAdapter.createAdapter();
	}
	/**
	 * getMetadata
	 * @generated
	 */
	public Object[] getMetadata() {

		java.lang.String[] primarykey, subhomes, composedObjs, composedObjImpls;
		com.ibm.ObjectQuery.metadata.OSQLExternalCatalogEntry[] cat;
		com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef[] fields;
		cat = new com.ibm.ObjectQuery.metadata.OSQLExternalCatalogEntry[4];
		//-------------------------------------------------------------------------
		cat[0] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalCatalogRDBAlias(
				"Model",
				"Model1_Alias",
				"CLOUDSCAPE",
				"MODEL",
				"Model_Model1_Table");

		//-------------------------------------------------------------------------
		fields = new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef[5];

		fields[0] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"ID",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._INTEGER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				true,
				0,
				-1,
				0);
		fields[1] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"NAME",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				250,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				true,
				0,
				-1,
				0);
		fields[2] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"DESCRIPTION",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				250,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				true,
				0,
				-1,
				0);
		fields[3] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"MODEL",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				250,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				true,
				0,
				-1,
				0);
		fields[4] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"DOMAIN_ID",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._INTEGER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				true,
				0,
				-1,
				0);
		primarykey = new String[1];
		primarykey[0] = "ID";
		cat[1] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalCatalogType(
				"Model",
				"Model1_Table",
				null,
				fields,
				primarykey);

		//-------------------------------------------------------------------------
		fields = new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef[5];

		fields[0] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"id",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._INTEGER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				false,
				0,
				-1,
				0);
		fields[1] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"name",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				false,
				0,
				-1,
				0);
		fields[2] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"description",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				false,
				0,
				-1,
				0);
		fields[3] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"model",
				new String(),
				com.ibm.ObjectQuery.engine.OSQLSymbols._CHARACTER,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				false,
				0,
				-1,
				0);
		fields[4] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalColumnDef(
				"domain",
				"Domain",
				com.ibm.ObjectQuery.engine.OSQLSymbols.OOSQL_TABLE,
				0,
				com.ibm.ObjectQuery.engine.OSQLConstants.NO_TYPE,
				false,
				0,
				-1,
				com.ibm.ObjectQuery.engine.OSQLSymbols.SQL_BO);
		primarykey = new String[1];
		primarykey[0] = "id";
		cat[2] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalCatalogType(
				"Model",
				"Model_BO",
				"unbbs.persistence.model.ModelBean",
				fields,
				primarykey);

		//-------------------------------------------------------------------------
		composedObjs = null;
		composedObjImpls = null;
		subhomes = null;
		cat[3] =
			new com.ibm.ObjectQuery.metadata.OSQLExternalCatalogView(
				"Model",
				"Model_Model_BO",
				"Model_Model1_Alias",
				composedObjs,
				composedObjImpls,
				"select t1.ID,t1.NAME,t1.DESCRIPTION,t1.MODEL,(select t2 from Domain_Domain1_Alias t2 where t2.ID = t1.DOMAIN_ID) from _this t1",
				null,
				subhomes,
				0,
				null);

		return cat;
	}
	/**
	 * createDataAccessSpecs
	 * @generated
	 */
	public java.util.Collection createDataAccessSpecs()
		throws javax.resource.ResourceException {
		com.ibm.ws.ejbpersistence.beanextensions.EJBDataAccessSpec daSpec;
		com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl iSpec;
		java.util.Collection result = new java.util.ArrayList(10);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("Create");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("Create");
		daSpec.setInputRecordName("Create");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.CREATE_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("Remove");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("Remove");
		daSpec.setInputRecordName("Remove");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.REMOVE_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("Store");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("Store");
		daSpec.setInputRecordName("Store");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.STORE_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("StoreUsingOCC");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("Store");
		daSpec.setInputRecordName("Store");
		daSpec.setOptimistic(true);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.STORE_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("FindByNameOrDescription");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("FindByNameOrDescription");
		daSpec.setInputRecordName("FindByNameOrDescription");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.FIND_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(true);
		daSpec.setAllowDuplicates(true);
		daSpec.setContainsDuplicates(true);
		daSpec.setSingleResult(false);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("FindByNameOrDescriptionForUpdate");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("FindByNameOrDescription");
		daSpec.setInputRecordName("FindByNameOrDescription");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.FIND_BEAN);
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(false);
		daSpec.setAllowDuplicates(true);
		daSpec.setContainsDuplicates(true);
		daSpec.setSingleResult(false);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("FindByPrimaryKey");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("FindByPrimaryKey");
		daSpec.setInputRecordName("FindByPrimaryKey");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com.ibm.ws.ejbpersistence.beanextensions.EJBDataAccessSpec.FIND_PK);
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(true);
		daSpec.setAllowDuplicates(false);
		daSpec.setContainsDuplicates(false);
		daSpec.setSingleResult(true);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("FindByPrimaryKeyForUpdate");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("FindByPrimaryKey");
		daSpec.setInputRecordName("FindByPrimaryKey");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com.ibm.ws.ejbpersistence.beanextensions.EJBDataAccessSpec.FIND_PK);
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(false);
		daSpec.setAllowDuplicates(false);
		daSpec.setContainsDuplicates(false);
		daSpec.setSingleResult(true);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("findModelsByDomainKey_Local");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("findModelsByDomainKey_Local");
		daSpec.setInputRecordName("findModelsByDomainKey_Local");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.FIND_BEAN);
		{
			com.ibm.ws.ejbpersistence.dataaccess.CompleteAssociationList cal =
				new com
					.ibm
					.ws
					.ejbpersistence
					.dataaccess
					.CompleteAssociationList(
					1);
			cal.add(new String[] { "domain" });
			daSpec.setCompleteAssociationList(cal);
		}
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(true);
		daSpec.setAllowDuplicates(true);
		daSpec.setContainsDuplicates(true);
		daSpec.setSingleResult(false);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		daSpec =
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.DataAccessSpecFactory
				.getDataAccessSpec();
		iSpec = new com.ibm.ws.rsadapter.cci.WSInteractionSpecImpl();
		iSpec.setFunctionSetName(
			"unbbs.persistence.model.websphere_deploy.CLOUDSCAPE_V50_1.ModelBeanFunctionSet_102b0683");
		iSpec.setFunctionName("findModelsByDomainKey_LocalForUpdate");
		daSpec.setInteractionSpec(iSpec);
		daSpec.setSpecName("findModelsByDomainKey_Local");
		daSpec.setInputRecordName("findModelsByDomainKey_Local");
		daSpec.setOptimistic(false);
		daSpec.setType(
			com
				.ibm
				.ws
				.ejbpersistence
				.beanextensions
				.EJBDataAccessSpec
				.FIND_BEAN);
		{
			com.ibm.ws.ejbpersistence.dataaccess.CompleteAssociationList cal =
				new com
					.ibm
					.ws
					.ejbpersistence
					.dataaccess
					.CompleteAssociationList(
					1);
			cal.add(new String[] { "domain" });
			daSpec.setCompleteAssociationList(cal);
		}
		daSpec.setQueryScope(new String[] { "Model" });
		daSpec.setReadAccess(false);
		daSpec.setAllowDuplicates(true);
		daSpec.setContainsDuplicates(true);
		daSpec.setSingleResult(false);
		daSpec.setExtractor(
			new com.ibm.ws.ejbpersistence.dataaccess.WholeRowExtractor(
				getExtractor()));
		result.add(daSpec);

		return result;

	}
}
