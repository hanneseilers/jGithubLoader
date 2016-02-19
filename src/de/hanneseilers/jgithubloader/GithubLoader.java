package de.hanneseilers.jgithubloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.service.RepositoryService;

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
	
	public GithubLoader( String gitUser, String gitRepository ){
		mGitUser = gitUser;
		mGitRepository = gitRepository;		
	}
	
	/**
	 * Updates the application, if new tag is available in repository.
	 * @param currentTag	{@link String} name of currently installed tag.
	 * @throws UpdateFailedException 
	 */
	public void update(String currentTag) throws UpdateFailedException{
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
			
			System.out.println( "build new jar " + vJarFile.getPath() );
				
			// TODO: replace
				
			// TODO: reboot
		}
	}
	
	/**
	 * Checks if new release tag is available
	 * @param currentTag	{@link String} name of currently installed tag.
	 * @return				{@code true} if new tag is available, {@code false} otherwise.
	 */
	private boolean hasUpdate(String currentTag){
		
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
			List<RepositoryTag> vTags = vService.getTags(vRepository);
			
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
	
}
