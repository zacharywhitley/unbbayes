package unbbayes.controller;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

/** This class shows a screen that initializes a loading and shows
 *  it's status for the FileController instance
 *
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *  @version $1.0 $ (07/04/2003)
 */
public class ProgressDialog extends JDialog
{
	/** Create an instance of this class  */
	public ProgressDialog(File file, IProgress progress)
	{
		setTitle("Loading...");
		setSize(280,130);
		setModal(true);
		setResizable(false);
		
		//center screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   		Dimension frameSize = getSize();
   		if (frameSize.height > screenSize.height) 
   		{
      		frameSize.height = screenSize.height;
    	}
    	if (frameSize.width > screenSize.width) 
    	{
     		 frameSize.width = screenSize.width;
    	}
		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		Container contentPane = getContentPane();
		
		//label
		JPanel panelLabel = new JPanel();
		contentPane.add(panelLabel, "North");
		JLabel label = new JLabel("Carregando "+file.getName());
		panelLabel.add(label);
		
		//progress bar
		JPanel panelProgress = new JPanel();
		contentPane.add(panelProgress, "Center");
		///////////
		min = 0;
		max = progress.maxCount();
		this.progress = progress;
		///////////
		progressBar = new JProgressBar(min,max);
		panelProgress.add(progressBar);
						
		//button
		JPanel panelButton = new JPanel();
		contentPane.add(panelButton, "South");
		JButton cancelButton = new JButton("Cancel");
		panelButton.add(cancelButton);
		cancelButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					activity.requestCancel();
					activity.interrupt();
					activityMonitor.stop();
					hide();		
				}
			});
		
		//startActivity
		activity = new LoadingActivity(progress,max);
					
		//activityMonitor
		activityMonitor = new Timer(500,
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int current = activity.getCurrent();
					progressBar.setValue(current);
					progressBar.setStringPainted(true);
					progressBar.setString(current+" de "+max);
					if(current==activity.getTarget())
					{	
						activityMonitor.stop();
						hide();
					}
				}
			});
	}
	
	/** start the loading 
	 * @return boolean value indication if activity was terminated successfully or not
	 * */
	public boolean load()
	{
		if(max>NEED_DIALOG_VALUE)
		{
			activity.start();
			activityMonitor.start();
			show();
			return !activity.wasActivityCancelled();
		}
		else
		{
			while (progress.next());
			return true;
		}
	}
	
	//--------------------------------------------------------------------//
	
	public final int NEED_DIALOG_VALUE = 1000;
	private IProgress progress;	
	private Timer activityMonitor;
	private LoadingActivity activity;
	private JProgressBar progressBar;
	private int min;
	private int max;
}
