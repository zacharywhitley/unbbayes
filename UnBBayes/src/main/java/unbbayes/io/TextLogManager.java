/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Responsible for generating network compilation log
 * 
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @author Laecio L. dos Santos
 * 
 * @version 2.0
 */
public class TextLogManager implements ILogManager, java.io.Serializable {
    
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	public static final int DEFAULT_BUFFER_SIZE = 10 * 1024;
    public static final String DEFAULT_FILENAME = "aj.txt";

    public static final int numColumn = 80; 
    public static final String separator = "*"; 
    public static final String identationString = "  "; 
    
    private StringBuffer log;
    
    /** Load resource file from this package */
  	protected static ResourceBundle resource = ResourceBundle.getBundle(
  			"unbbayes.io.resources.IoResources");

    public TextLogManager(int bufferSize) {
        log = new StringBuffer(bufferSize);
        reset();
    }

    public TextLogManager() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public void clear() {
        log.setLength(0);
    }

    public void reset() {
        clear();
    }

    public void append(String text) {
        log.append(text);
        
        System.out.print(text);
    }

    public void appendIfTrue(boolean debug, String text) {
    	if(debug){
    		append(text);
    	}
    }
    
    public void appendln(String text) {
        log.append(text);
        log.append("\n");

        System.out.println(text);
    }

    public void appendlnIfTrue(boolean debug, String text) {
    	if(debug){
    		appendln(text); 
    	}
    }
    
	public void appendln(int identation, String text) {
		
		for(int i= 0; i < identation; i++){
			log.append(identationString);
		}
		
        log.append(text);
        log.append("\n");
	}

	public void appendlnIfTrue(int identation, boolean debug, String text) {
    	if(debug){
    		appendln(identation, text); 
    	}
	}
    
    public void appendSeparator(){
    	for(int i = 0; i < numColumn; i++){
    		this.append(separator); 
    	}
    	this.append("\n\n"); 
    }
    
    public String getLog() {
        return log.toString();
    }

    public void writeToDisk(String fileName, boolean append) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, append));
        out.write(getLog());
        out.flush();
        out.close();
    }

	public void addTitle(String text) {
		appendSeparator(); 
		appendln(text); 
		appendSeparator(); 
		appendln(" "); 
	}

	public void appendSectionTitle(String text) {
		append("\n" + text.toUpperCase() + "\n"); 
	}

	public void appendSpecialTitle(String text) {
		 appendln("."); 
		 appendln("."); 
		 appendln("................................................................................"); 
		 appendln(". " + text); 
		 appendln("................................................................................"); 
		 appendln("."); 
		 appendln("."); 
		 appendln(""); 
	}



}