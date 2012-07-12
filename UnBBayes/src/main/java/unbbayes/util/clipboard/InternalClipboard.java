package unbbayes.util.clipboard;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper to put objects on the clipboard. It is a singleton.
 * 
 * @author David Saldana
 * 
 */
public class InternalClipboard {
	/**
	 * Singleton instance.
	 */
	private static InternalClipboard instance;

	private List<IInternalClipboardListener> listeners;

	/**
	 * Shared object in clipboard.
	 */
	private Object sharedObject;

	/**
	 * Private constructor for clipboard
	 */
	private InternalClipboard() {
		listeners = new ArrayList<IInternalClipboardListener>();
	}

	public static InternalClipboard getInstance() {
		if (instance == null) {
			instance = new InternalClipboard();
		}
		return instance;
	}

	public void putToClipboard(Object obj) {
		sharedObject = obj;

		notify(obj);
	}

	/**
	 * Notify to all listeners.
	 * 
	 * @param obj new object added to clipboard. 
	 */
	private void notify(Object obj) {
		for (IInternalClipboardListener listener : listeners) {
			listener.newElementInClipboard(obj);
		}		
	}

	/**
	 * Add a new listener.
	 * 
	 * @param listener listener who  pays attention to clipboard.
	 */
	public void addListener(IInternalClipboardListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Get shared object from clilpboard.
	 * @return shared object
	 */
	public Object getFromClipboard() {
		return sharedObject;
	}
}
