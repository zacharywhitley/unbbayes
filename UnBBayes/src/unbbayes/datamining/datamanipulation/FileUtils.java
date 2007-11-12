package unbbayes.datamining.datamanipulation;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;

import unbbayes.controller.FileController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 12/08/2007
 */
public class FileUtils {

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.datamanipulation.resources." +
			"DataManipulationResource");

	public static final byte OPEN_FILE_DIALOG = 0;
	public static final byte SAVE_FILE_DIALOG = 1;

	private static SimpleFileFilter bevFileFilter() {
		String[] filter = {"BEV"};
		String filterText = resource.getString("besFilterText");

		return new SimpleFileFilter(filter, filterText);
	}
	
	private static SimpleFileFilter getArffFileFilter() {
		String[] filter = {"ARFF"};
		String filterText = resource.getString("arffFilterText");

		return new SimpleFileFilter(filter, filterText);
	}

	public static File openBevFile(JInternalFrame window, String[] returnStatus) {
		String dialogTitle;
		String successText;
		SimpleFileFilter fileFilter;
		
		dialogTitle = resource.getString("openBevDialog");
		successText = resource.getString("openBevSuccess");
		fileFilter = bevFileFilter();
		
		File file = null;
		file = openFile(
				dialogTitle,
				successText,
				fileFilter,
				SAVE_FILE_DIALOG,
				window,
				returnStatus
		);
		
		return file;
	}
	
	public static File saveBevFile(JInternalFrame window, String[] returnStatus) {
		String dialogTitle;
		String successText;
		SimpleFileFilter fileFilter;
		
		dialogTitle = resource.getString("saveBevDialog");
		successText = resource.getString("saveBevSuccess");
		fileFilter = bevFileFilter();
		
		File file = null;
		file = openFile(
				dialogTitle,
				successText,
				fileFilter,
				SAVE_FILE_DIALOG,
				window,
				returnStatus
		);

		return file;
	}
	
	public static File openArffFile(JInternalFrame window, String[] returnStatus) {
		String dialogTitle;
		String successText;
		SimpleFileFilter fileFilter;
		
		dialogTitle = resource.getString("openArffDialog");
		successText = resource.getString("openArffSuccess");
		fileFilter = getArffFileFilter();
		
		File file;
		file = openFile(
				dialogTitle,
				successText,
				fileFilter,
				OPEN_FILE_DIALOG,
				window,
				returnStatus
		);
		
		return file;
	}
	
	public File saveArffFile(JInternalFrame window, String[] returnStatus) {
		String dialogTitle;
		String successText;
		SimpleFileFilter fileFilter;
		
		dialogTitle = resource.getString("saveArffDialog");
		successText = resource.getString("saveArffSuccess");
		fileFilter = getArffFileFilter();
		
		File file;
		file = openFile(
				dialogTitle,
				successText,
				fileFilter,
				SAVE_FILE_DIALOG,
				window,
				returnStatus
		);
		
		return file;
	}
	
	/**
	 * Show open/save dialog window and return the chosen file.
	 *  
	 * @param dialogTitle
	 * @param successText 
	 * @param fileFilter
	 * @param type OPEN_FILE_DIALOG|SAVE_FILE_DIALOG
	 * @param returnStatus 
	 * @return
	 */
	public static File openFile(String dialogTitle, String successText,
			SimpleFileFilter fileFilter, byte type, JInternalFrame window,
			String[] returnStatus) {
		File selectedFile = null;
		JFileChooser fileChooser;
		File currentPath = FileController.getInstance().getCurrentDirectory();
		fileChooser = new JFileChooser(currentPath);
		fileChooser.setDialogTitle(dialogTitle);
		fileChooser.setMultiSelectionEnabled(false);
		
		/* Add FileView to FileChooser for drawing file icons */
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileView(new FileIcon(window));

		/* Choose between save and open dialog */
		window.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		int returnVal = -1;
		switch(type) {
			case OPEN_FILE_DIALOG:
				returnVal = fileChooser.showOpenDialog(window);
				break;
			case SAVE_FILE_DIALOG:
				returnVal = fileChooser.showSaveDialog(window);
				break;
		}
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			currentPath = fileChooser.getCurrentDirectory();
			FileController.getInstance().setCurrentDirectory(currentPath);
			returnStatus[0] = new String(successText);
		} else {
			String cancelText = resource.getString("canceledDialog");
			returnStatus[0] = new String(cancelText);
		}
		window.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		return selectedFile;
	}

	/**
	 * Reads and return the names of all attributes in an ARFF file.
	 * @return An arraylist containing the following results: An arraylist with
	 * 	       all attributes' names, the likely counter attribute's name and
	 *         the relation's name.
	 * @exception IOException if the information is not read successfully.
	 */
	public static ArrayList<Object> getFileHeaderInfo(File file) throws IOException {
		Loader loader;
		String fileName = file.getName();

		/* Constructs a preliminary header */
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
			/* Read the arff header */
			loader = new ArffLoader(file, 0);
		} else if (fileName.regionMatches(true, fileName.length() - 4, ".txt",
				0, 4)) {
			/*
			 * Read header of the txt file. All attributes will be read as
			 * nominal (String values)
			 */
			loader = new TxtLoader(file, 1);
		} else {
			throw new IOException(resource.getString("fileExtensionException"));
		}

		return loader.getHeaderInfo();
	}
	
}

