package unbbayes.io.umpst;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

import unbbayes.model.umpst.project.UMPSTProject;

public class FileLoadObject {


	public  UMPSTProject loadUbf(File file,UMPSTProject _umpstProject) 
			throws InvalidClassException, IOException, ClassNotFoundException {

		//File file = new File("images/file.ump");
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fis);
		
		UMPSTProject umpstProject = (UMPSTProject) in.readObject(); 
		
		return umpstProject; 
	}
	
	
}
