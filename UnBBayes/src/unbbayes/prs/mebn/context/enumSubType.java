package unbbayes.prs.mebn.context;

public enum enumSubType{

	/* DON'T CARE */
	NOTHING, 
	
	/* OPERANDO */
	OVARIABLE, 
	NODE, 
	ENTITY, 
	VARIABLE, 
	SKOLEN, 
			
	/* SIMPLE OPERATOR */
	AND, 
	OR, 
	NOT, 
	EQUALTO, 
	IMPLIES, 
	IFF, 
	
	/* QUANTIFIER	 */
	FORALL, 
	EXISTS
	
}