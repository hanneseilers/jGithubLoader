package de.hanneseilers.jgithubloader;

/**
 * Listener to nofity about {@link UpdateProgress} changes.
 * @author eilers
 *
 */
public interface UpdateProgressChangedListener {

	public void onUpdateProgressChanged(UpdateProgress progress);	
	
	/**
	 * Status for update progress.
	 */
	public enum UpdateProgress {
		CHECK,			// checking for new application version
		DOWNLOAD,		// downloading updates
		UNPACK,			// unpacking updates 
		BUILD,			// building updated application
		UPDATE,			// update application binaries
		RESTART,		// restart application
		DONE,			// update done
		FAILED			// updateing failed 
	}
	
}
