package de.hanneseilers.jgithubloader.splash;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QMainWindow;

import de.hanneseilers.jgithubloader.UpdateProgressChangedListener;

public class SplashScreen implements UpdateProgressChangedListener {

	private QApplication mQApplication;
	private QMainWindow mQMainWindow;
	private Ui_frmMain mUi;
	
	/**
	 * Constructor
	 */
	public SplashScreen(){
		mQApplication = new QApplication( new String[]{} );
		mUi = new Ui_frmMain();
		mQMainWindow = new QMainWindow();
		mUi.setupUi(mQMainWindow);
		mQMainWindow.show();
	}
	
	/**
	 * Shows splash screen.
	 * Blocking call until screen gets closed.
	 */
	public void show(){
		mQApplication.exec();
	}
	
	/**
	 * Sets application name.
	 * @param appName	{@link String} of application name.
	 */
	public void setAppName(final String appName){
		QApplication.invokeLater( new Runnable() {			
			@Override
			public void run() {
				mUi.lblAppName.setText( appName );
			}
		} );
	}
	
	/**
	 * Exits splash screen
	 */
	public void exit(){
		if( mQMainWindow != null ){			
			QApplication.invokeLater(new Runnable() {			
				@Override
				public void run() {		
					QApplication.quit();
					mQMainWindow = null;
				}
			});			
		}		
	}

	@Override
	public void onUpdateProgressChanged(final UpdateProgress progress) {
		QApplication.invokeLater( new Runnable() {			
			@Override
			public void run() {
				String vMessage = "";
				
				switch (progress) {
				case BUILD:
					vMessage = "building application ...";
					break;
				case CHECK:
					vMessage = "checking for update ...";
					break;
				case DONE:
					vMessage = "application up to date";
					break;
				case DOWNLOAD:
					vMessage = "downloading updates ...";
					break;
				case FAILED:
					vMessage = "failed  ...";
					break;
				case RESTART:
					vMessage = "restarting application ...";
					break;
				case UNPACK:
					vMessage = "unpacking updates ...";
					break;
				case UPDATE:
					vMessage = "updating application ...";
					break;
				default:
					break;		
				}
				
				mUi.lblProgress.setText( vMessage );
			}
		} );
	}
	
}
