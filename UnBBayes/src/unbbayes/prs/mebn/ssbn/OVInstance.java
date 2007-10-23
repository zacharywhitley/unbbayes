/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;

/**
 * @author Shou Matsumoto
 *
 */
public class OVInstance {
	private OrdinaryVariable ov = null;
	private LiteralEntityInstance entity = null;
	
	private OVInstance() {
		super();
	}
	
	public static OVInstance getInstance(OrdinaryVariable ov) {
		OVInstance ovi = new OVInstance();
		ovi.setOv(ov);
		ovi.setEntity(LiteralEntityInstance.getInstance(ov.getName(), ov.getValueType()));
		return ovi;
	}
	
	public static OVInstance getInstance(OrdinaryVariable ov , LiteralEntityInstance ei) {
		OVInstance ovi = new OVInstance();
		ovi.setOv(ov);
		ovi.setEntity(ei);
		return ovi;
	}
	
	public static OVInstance getInstance(OrdinaryVariable ov , String entityName , Type entityType) {
		OVInstance ovi = new OVInstance();
		ovi.setOv(ov);
		ovi.setEntity(LiteralEntityInstance.getInstance(entityName, entityType));
		return ovi;
	}
	
	/**
	 * @return the entity
	 */
	public LiteralEntityInstance getEntity() {
		return entity;
	}
	/**
	 * @param entity the entity to set
	 */
	public void setEntity(LiteralEntityInstance entity) {
		this.entity = entity;
	}
	/**
	 * @return the ov
	 */
	public OrdinaryVariable getOv() {
		return ov;
	}
	/**
	 * @param ov the ov to set
	 */
	public void setOv(OrdinaryVariable ov) {
		this.ov = ov;
	}
	
	/* 
	 * devera ser criado um metodo para retornar a OVInstance para uma data OV, 
	 * em uma lista de OVInstance...
	 * 
	 *  Objeto "Parameters"? 
	 */
	
	private OVInstance getOVInstanceForOV(OrdinaryVariable ov, List<OVInstance> list){
		
		for(OVInstance ovi: list){
			if (ovi.getOv() == ov){
				return ovi; 
			}
		}
		
		return null; 
	}
	
}
