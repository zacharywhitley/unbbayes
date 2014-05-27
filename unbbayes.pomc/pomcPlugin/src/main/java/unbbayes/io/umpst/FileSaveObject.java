package unbbayes.io.umpst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import unbbayes.model.umpst.project.UMPSTProject;

public class FileSaveObject {

		public static final String NULL = "null";

		private UMPSTProject umpstProject;

		private File file; 
		
		public void saveUbf(File _file,
				UMPSTProject _umpstProject) throws IOException{


			this.umpstProject = _umpstProject;
			
			file = _file; 
			
			String fileName = file.getName(); 
			
			int index = fileName.lastIndexOf(".");

			String fileExtension = null; 
			
			if (index >= 0) {
				fileExtension = fileName.substring(index + 1);
			}
			
			if ((fileExtension == null) || (!fileExtension.equals("ump"))){
				file = new File(file.getPath()+".ump");
			}
			
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			out.writeObject(umpstProject); 
			
			out.close(); 
			fileOut.flush(); 
			fileOut.close(); 
			
			System.out.println("Serialization succesfull");
			
			
		}

}
