package unbbayes.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonDataUtil {

	private static CommonDataUtil  singleton;

	private String authorName = null;
	private SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
	
	private CommonDataUtil(){}
		
	public static CommonDataUtil getInstance(){		
		if (singleton == null){
			singleton = new CommonDataUtil(); 
		}		
		return singleton;		
	}
	
	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getActualDate() {
		Date date = new Date(System.currentTimeMillis());		
		return formatador.format(date);
	}
	
//	public Date convertToDate(String _date) {
//		Date date = new Date();
//		try {
//			date = formatador.parse(_date);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return date;
//	}
//	
//	public int compareDate(Date one, Date two) {
//		int value;
//		if(one.after(two)) {
//			value = 1;
//		} else if(one.before(two)) {
//			value = 0;
//		} else {
//			value = -1;
//		}		
//		return value;
//	}

//	public void setActualDate(String actualDate) {
//		this.actualDate = actualDate;
//	}
	
}
