package de.hanneseilers.jgithubloader;

/**
 * {@link Exception} class for failed updates.
 * @author eilers
 *
 */
public class UpdateFailedException extends Exception {

	private static final long serialVersionUID = 4380239028992455143L;

	public UpdateFailedException(String msg) {
		super(msg);
	}
	
}
