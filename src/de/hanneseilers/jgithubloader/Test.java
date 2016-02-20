package de.hanneseilers.jgithubloader;

public class Test implements UpdateProgressChangedListener {
	
	@Override
	public void onUpdateProgressChanged(UpdateProgress progress) {
		System.out.println( "update progress: " + progress.name() );
	}
	

	/**
	 * Main function
	 * @param args
	 */
	public static void main(String[] args) {		
		try {
			
			Test vTest = new Test();			
			GithubLoader loader = new GithubLoader("hanneseilers", "jGithubLoader");
			loader.addUpdateProgressChangedListener( vTest );
			loader.update( "v0.0.0" );
			
		} catch (UpdateFailedException e) {
			e.printStackTrace();
		}
	}

}
