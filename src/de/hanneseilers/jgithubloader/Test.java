package de.hanneseilers.jgithubloader;

public class Test implements UpdateProgressChangedListener {
	
	public static final String TAGNAME = "v0.0.3a";
	
	@Override
	public void onUpdateProgressChanged(UpdateProgress progress) {
		System.out.println( "update progress: " + progress.name() );
	}
	

	/**
	 * Main function
	 * @param args
	 */
	public static void main(String[] args) {		
		System.out.println( "Current Version: " + TAGNAME );
		
		Test vTest = new Test();			
		GithubLoader loader = new GithubLoader("hanneseilers", "jGithubLoader", true);
		loader.setAppName( "jGithubLoader - Test" );
		loader.addUpdateProgressChangedListener( vTest );
		loader.update( TAGNAME );
	}

}
