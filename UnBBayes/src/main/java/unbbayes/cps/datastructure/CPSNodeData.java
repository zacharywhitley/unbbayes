package unbbayes.cps.datastructure;

import java.io.Serializable;

public class CPSNodeData implements Serializable 
{ 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8750318313771683265L;
	
	/**
	 * Local Class
	 */
	
	public String name;
	public String value;
	public String type;
	public String number;
	public String origin;
	public String index;
	 
	public CPSNodeData() {
        super();
    }
	     
	public CPSNodeData(String n, String v) {
		this();
	    setName(n);
	    setValue(v);
	}
	 
	public String getType() {
	    return this.type;
	}
	 
	public void setType(String v) {
	        this.type = v;
	}
	
	public String getValue() {
	    return this.value;
	}
	 
	public void setNumber(String v) {
        this.number = v;
	}
	
	
	public String getNumber() {
	    return this.number;
	}
	
	public void setValue(String v) {
	        this.value = v;
	}
 
	public String getOrigin() {
	    return this.origin;
	}
	 
	public void setOrigin(String v) {
	        this.origin = v;
	}
	
	public String getName() {
	    return this.name;
	}
	 
	public void setName(String v) {
	        this.name = v;
	}

	
	public String getIndex() {
	    return this.index;
	}
	 
	public void setIndex(String v) {
	        this.index = v;
	}
	
	
	
	public void assign(CPSNodeData d)
	{
		name 	= d.name;
		value 	= d.value ;
		type	= d.type;
		number	= d.number;
		origin	= d.origin;
		index   = d.index;
	}
	 
}