package de.hanneseilers.jgithubloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.service.RepositoryService;

import de.hanneseilers.jgithubloader.UpdateProgressChangedListener.UpdateProgress;
import de.hanneseilers.jgithubloader.splash.SplashScreen;

/**
 * GithubLoader
 * @author eilers
 *
 */
public class GithubLoader {
	
	private static final String TMP_DIR = "tmp";
	private static final String TMP_ZIP = "source.zip";
	
	private String mGitUser;
	private String mGitRepository;
	private boolean mUseSplash = false;
	private SplashScreen mSplashScreen;
	private List<UpdateProgressChangedListener> mListener = new ArrayList<UpdateProgressChangedListener>();
	
	/**
	 * Constructor without splash screen.
	 * @param gitUser			{@link String} username of applications repository
	 * @param gitRepository		{@link String} repository name
	 */
	public GithubLoader( String gitUser, String gitRepository ){
		this( gitUser, gitRepository, false );
	}
	
	/**
	 * Constructor with slapsh screen.
	 * @param gitUser			{@link String} username of applications repository
	 * @param gitRepository		{@link String} repository name
	 * @param useSlpash			If {@code true} splash screen is used
	 */
	public GithubLoader( String gitUser, String gitRepository, boolean useSlpash ){
		mGitUser = gitUser;
		mGitRepository = gitRepository;
		mUseSplash = useSlpash;
	}
	
	/**
	 * Sets application nameon splash screen.
	 * @param appName	{@link String} of application name.
	 */
	public void setAppName(String appName){
		if( mSplashScreen != null )
			mSplashScreen.setAppName( appName );
	}
	
	/**
	 * Adds a {@link UpdateProgressChangedListener}.
	 * @param listener	{@link UpdateProgressChangedListener} to add.
	 */
	public void addUpdateProgressChangedListener(UpdateProgressChangedListener listener){
		if( !mListener.contains(listener) )
			mListener.add( listener );
	}
	
	/**
	 * Removes a {@link UpdateProgressChangedListener}.
	 * @param listener	{@link UpdateProgressChangedListener} to remove.
	 */
	public void remoteUpdateProgressChangedListener(UpdateProgressChangedListener listener){
		mListener.remove( listener );
	}
	
	/**
	 * Notifies all listeners about {@link UpdateProgress} changes.
	 * @param progress	Current {@link UpdateProgress}.
	 */
	private void notifyUpdateProgressChanged(final UpdateProgress progress){
		for( final UpdateProgressChangedListener vListener : mListener ){
			new Thread( new Runnable() {				
				@Override
				public void run() {
					vListener.onUpdateProgressChanged( progress );
				}
			} ).start();
		}
	}
	
	/**
	 * Updates the application, if new tag is available in repository.
	 * @param currentTag	{@link String} name of currently installed tag.
	 */
	public void update(final String currentTag){
		
			if( mUseSplash ){
				mSplashScreen = new SplashScreen();
				addUpdateProgressChangedListener( mSplashScreen );
			}
		
			new Thread( new Runnable() {				
				@Override
				public void run() {
					try{
						
						updateIntern( currentTag );
						notifyUpdateProgressChanged( UpdateProgress.DONE );
						
					} catch(UpdateFailedException e){
						notifyUpdateProgressChanged( UpdateProgress.FAILED );
					}
					
					remoteUpdateProgressChangedListener( mSplashScreen );
					
					if( mSplashScreen != null )
						mSplashScreen.exit();
				}
			} ).start();
			
			mSplashScreen.show();
			
	}
	
	/**
	 * Updates the application, if new tag is available in repository. (Internal version)
	 * @param currentTag	{@link String} name of currently installed tag.
	 * @throws UpdateFailedException 
	 */
	private void updateIntern(String currentTag) throws UpdateFailedException{
		if( hasUpdate(currentTag) ){
			RepositoryTag vUpdateTag = getLatestTag();
			
			// download
			File vTmpZipFile = download( vUpdateTag.getZipballUrl() );
//			File vTmpZipFile = new File( "tmp/source.zip" );
			if( vTmpZipFile == null )
				throw new UpdateFailedException( "Failed to download source code from " + vUpdateTag.getZipballUrl() );
					

			// unzip
			File vTmpDir = new File( TMP_DIR );
			try {
				if( !unzip(vTmpZipFile, vTmpDir) )
					throw new UpdateFailedException( "Failed to unzip downloaded file " + vTmpZipFile.getPath() );
			} catch (FileNotFoundException e) {
				throw new UpdateFailedException( e.getMessage() );
			}
			
			// build
			File vJarFile = build( vTmpDir );
			if( vJarFile == null )
				throw new UpdateFailedException( "Cannot build jar file from project source at " + vTmpDir.getPath() );
				
			// replace old jar with new one
			if( !replace(vJarFile) )
				throw new UpdateFailedException( "Cannot update application using " + vJarFile.getPath() );
				
			// reboot
			if( !restartApplication() )
				throw new UpdateFailedException( "Cannot restart application" );
		}
	}
	
	/**
	 * Checks if new release tag is available
	 * @param currentTag	{@link String} name of currently installed tag.
	 * @return				{@code true} if new tag is available, {@code false} otherwise.
	 */
	private boolean hasUpdate(String currentTag){		
		notifyUpdateProgressChanged( UpdateProgress.CHECK );
		RepositoryTag vLatestTag = getLatestTag();
		if( vLatestTag != null && !vLatestTag.getName().equals(currentTag) )
			return true;
		
		return false;
	}
	
	/**
	 * Gets latest tag from repository
	 * @param gitUser
	 * @param gitRepository
	 * @return
	 */
	private RepositoryTag getLatestTag(){		
		try {
			
			RepositoryService vService = new RepositoryService();
			Repository vRepository;		
			vRepository = vService.getRepository( mGitUser, mGitRepository);
			final List<RepositoryTag> vTags = vService.getTags(vRepository);
			
			if( vTags.size() > 0 )
				return vTags.get(0);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;		
	}
	
	/**
	 * Downloads a file from url.
	 * @param url	{@link String} of download url.
	 * @return		Downloaded {@link File} if successful, {@code false} otherwise.
	 */
	private File download( String url ){
		
		try{
			
			notifyUpdateProgressChanged( UpdateProgress.DOWNLOAD );
			
			// create temporary directory
			File vTmpDir = new File( TMP_DIR );
			if( !vTmpDir.exists() )
				vTmpDir.mkdir();
			
			// create url and streams
			URL vUrl = new URL( url );
			File vTmpZipFile = new File( TMP_DIR + File.separator + TMP_ZIP );
			
			ReadableByteChannel vByteChannel = Channels.newChannel( vUrl.openStream() );
			FileOutputStream vFileOutputStream = new FileOutputStream( vTmpZipFile );
			
			// transfer data from url to tmp directory
			vFileOutputStream.getChannel().transferFrom( vByteChannel, 0, Long.MAX_VALUE );
			
			// close streams			
			vFileOutputStream.close();
			vByteChannel.close();
			
			return vTmpZipFile;
			
		} catch(IOException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * Unzips a source file to a destination directory
	 * @param source		{@link String} name of source file.
	 * @param destination	{@link String} of destination directory.
	 * @return				{@code true} if unzipping was successful, {@code false} otherwise.
	 * @throws FileNotFoundException 
	 */
	private boolean unzip(File source, File destination) throws FileNotFoundException{
		
		byte[] vBuffer = new byte[1024];
		
		try {
			
			notifyUpdateProgressChanged( UpdateProgress.UNPACK );
			
			// create destination directory		
			if( !destination.exists() )
				destination.mkdir();
			
			// get zip source file
			if( !source.exists() )
				throw new FileNotFoundException( "Zip file " + source + " not found." );
				
			// get the zip file input stream
			ZipInputStream vZipInputStream = new ZipInputStream( new FileInputStream(source) );
			
			// get every zip file entry
			ZipEntry vZipEntry;
			while( (vZipEntry = vZipInputStream.getNextEntry()) != null ){
				
				// create new file at destination directory
				File vNewFile = new File( destination + File.separator + vZipEntry.getName() );
				
				// create all non exists directories
				new File( vNewFile.getParent() ).mkdirs();
				
				// check if entry is file or directory
				if( vZipEntry.isDirectory() ){
					
					vNewFile.mkdir();
					
				} else {
				
					// get file stream
					FileOutputStream vFileOutputStream = new FileOutputStream(vNewFile);
					
					// read file from input stream and write to output stream
					int vLength;
					while( (vLength = vZipInputStream.read(vBuffer)) > 0 ){
						vFileOutputStream.write(vBuffer, 0, vLength);
					}
					
					// close output file
					vFileOutputStream.close();
					
				}
				
			}
			
			// close input stream
			vZipInputStream.close();
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	/**
	 * tries to find ant build directory.
	 * @param directory	{@link File} directory to search in.
	 * @return			{@link File} directory where ant build file is located,
	 * 					{@code null} if no ant build file was found.
	 */
	private File getAntBuildDirectory(File directory){
		if( directory.isDirectory() ){
			for( File vFile : directory.listFiles() ){

				// check if file is directory or normal file
				if( vFile.isDirectory() ){					
					File vRetDeepSearch = getAntBuildDirectory( vFile );
					if( vRetDeepSearch != null )
						return vRetDeepSearch;					
				} else if ( vFile.getName().equals("build.xml") ){
					return directory;					
				}
				
			}
		}
		
		return null;
	}
	
	/**
	 * Builds a projects jar file.
	 * @param source	{@link File} source directory
	 * @return			Jar {@link File} if build successfuly, {@code null} otherwise.
	 */
	private File build(File source){
		
		try{
			
			notifyUpdateProgressChanged( UpdateProgress.BUILD );
			
			String vCommand = "";
			
			// check if to build with ant
			File vDirectory = null;
			if( (vDirectory = getAntBuildDirectory(source)) != null )
				vCommand = "ant";
			
			// run build command
			if( vCommand.length() > 0 ){
				
				// start process
				ProcessBuilder vProcessBuilder = new ProcessBuilder( vCommand );
				vProcessBuilder.directory( vDirectory );
				Process vProcess = vProcessBuilder.start();
				
				BufferedReader vBufferedReader = new BufferedReader(
						new InputStreamReader( vProcess.getInputStream() ) );
				String vLine;
				while( (vLine = vBufferedReader.readLine()) != null ){
					System.out.println(vLine);
				}
				
				// wait util build process finished
				vProcess.waitFor();
				
				// find jar file
				for( File vFile : vDirectory.listFiles() ){
					if( vFile.getName().endsWith(".jar") )
						return vFile;
				}
				
			}
			
		} catch( InterruptedException e ){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Replaces current jar file with another jar file
	 * @param jarFile	{@link File} to use for replacing current jar file
	 * @return
	 */
	private boolean replace(File jarFile){
		
		try{
			
			notifyUpdateProgressChanged( UpdateProgress.UPDATE );
			
			File vOldFile = new File( GithubLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
			
			if( vOldFile.getPath().endsWith(".jar")
					&& jarFile.getPath().endsWith(".jar") ){
				
				Files.copy( jarFile.toPath(), vOldFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
				return true;
				
			}
			
		} catch( IOException e ){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Retsrats application
	 * @return	{@code false} if restart failed.
	 */
	public boolean restartApplication(){
		try{
				
			notifyUpdateProgressChanged( UpdateProgress.RESTART );
			
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File( GithubLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI() );
	
			/* is it a jar file? */
			if(!currentJar.getName().endsWith(".jar"))
				return false;
			
			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());
			
			if( mSplashScreen != null )
				mSplashScreen.exit();
			
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();			
			System.exit(0);
			
		} catch(IOException e){
			e.printStackTrace();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}
