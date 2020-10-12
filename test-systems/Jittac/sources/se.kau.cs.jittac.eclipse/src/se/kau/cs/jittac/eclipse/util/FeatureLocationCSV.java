package se.kau.cs.jittac.eclipse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import se.kau.cs.jittac.model.feature.Feature;
import se.kau.cs.jittac.model.feature.FeatureLocation;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;

public class FeatureLocationCSV
{
	public static void load( File csvPath )
	{
        try
        {
        	// TODO use more robust CSV reader e.g. OpenCSV (http://opencsv.sourceforge.net/)
        	BufferedReader bufferedReader = new BufferedReader( new FileReader( csvPath ) );	            
            String line = null;
            String cvsSplitBy = ",";
            
            FeatureLocationRegistry.INSTANCE.clearAll();
            
            while( ( line = bufferedReader.readLine() ) != null )
            {
            	String[] featLoc = line.split( cvsSplitBy );
        	    String sourcePath = featLoc[0];
        	    String featureName = featLoc[1];
        	    int startOffSet = Integer.parseInt( featLoc[2] );
        	    int length = Integer.parseInt( featLoc[3] );
        	    Feature feature = Feature.getFeature( featureName );
        	    
        	    String projectName = sourcePath.split( "/" )[1];
        	    IProject eProject = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
        	    IJittacProject jProject = EclipseJittacProject.get( ( IProject ) eProject );
        	    IProject ejProject = ( ( EclipseJittacProject ) jProject ).getWrappedProject();
        	    
        	    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( sourcePath );

				if( ejProject.equals( resource.getProject() ) )
				{ 
					IJittacResource jResource = ( ( EclipseJittacProject ) jProject ).getResource( resource );
					FeatureLocationRegistry.INSTANCE.registerFeatureLocation( new FeatureLocation( feature, jResource, startOffSet, length ) );
				}
            }   
            
            bufferedReader.close();
        }
        catch( FileNotFoundException ex )
        {
            System.out.println( "Unable to open file '" + csvPath + "'" );                
        }
        catch( IOException ex )
        {
        	ex.printStackTrace(); 
        }
	}
}
