package de.hanneseilers.jgithubloader;

public class Test {

	public static void main(String[] args) {		
		try {
			
			GithubLoader loader = new GithubLoader("hanneseilers", "jGithubLoader");
			loader.update( "v0.0.0" );
			
		} catch (UpdateFailedException e) {
			e.printStackTrace();
		}
	}

}
