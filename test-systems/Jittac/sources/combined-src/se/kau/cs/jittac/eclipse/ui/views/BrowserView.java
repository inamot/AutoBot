package se.kau.cs.jittac.eclipse.ui.views;


import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class BrowserView
{
	private static boolean hasData()
	{		
		try
		{
			URL dataFileUrl = new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/data.json" );
			InputStream inputStream = dataFileUrl.openConnection().getInputStream();
	    	BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );
		    String inputLine = in.readLine();
		    
		    if( inputLine.equals( "no feature dependencies" ) )
		    {
		        return false;
		    }
		 
		    in.close();
		}
		catch( IOException e )
		{ 	
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static void openBrowser()
	{	
		String browserID = "JittacBrowser";
		ArrayList<URL> url = new ArrayList<URL>();

		try
		{
			if( hasData() )
			{
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerA.html" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerB.html" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerC.html" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/data.json" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/footer.html" ) );
			}
			else
			{
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerA.html" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerB.html" ) );
				url.add( new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/nullConnector.html" ) );
			}
			
			URL featureDependenciesUrl = new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/featureDependencies.html" );
			File featureDependenciesFile = new File( FileLocator.resolve( featureDependenciesUrl ).toURI() );
			featureDependenciesFile.delete();
			featureDependenciesFile.createNewFile();	
			BufferedWriter writer = new BufferedWriter( new FileWriter( featureDependenciesFile, true ) );
		    
		    for( int i = 0; i < url.size(); i++ )
		    {
		    	InputStream inputStream = url.get( i ).openConnection().getInputStream();
		    	BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );
			    String inputLine;
			    
			    while( ( inputLine = in.readLine() ) != null )
			    {
			        writer.append( "\n" );
			        writer.append( inputLine );
			    }
			 
			    in.close();
		    }
		    
		    writer.close();
		    
		    String featureDependenciesPage = "file:///" + featureDependenciesFile;
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser( browserID ).openURL( new URL ( featureDependenciesPage ) );	
		}
		catch( IOException | PartInitException | URISyntaxException e )
		{ 	
			e.printStackTrace();
		}
	}
	
}