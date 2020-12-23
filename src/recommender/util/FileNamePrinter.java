package recommender.util;
import java.io.File;
import java.util.HashSet;
import java.util.Stack;

import recommender.da.DependencyAnalysis;

public class FileNamePrinter
{
	public static void main( String[] args )
	{
		String packageExclusion [] = 
			{ 
					"java.*",
					"javax.*"
			};
		
		String programSources = "test-systems/teammates/src/main/java/";
		String programUrl = "test-systems/teammates/bin/";	
		
		File folder = new File( programSources );
		Stack<File> stack = new Stack<File>();
		Stack<File> folders = new Stack<File>();
		stack.push( folder );
		
		HashSet<String> files = new HashSet<String>();
		
		System.out.println( "\nSOURCE CODE CLASS NAMES:" );
		System.out.println( "--------------------------" );
		
		//int i = 0;
		
		while( !stack.isEmpty() )
	    {
	        File child = stack.pop();
	        File[] listFiles = child.listFiles();
	        folders.push( child );

	        for( File file : listFiles )
	        {
	        	if( file.isFile() && file.getPath().endsWith( ".java" ) )
	            {
	        		//i++;
	        		String path = file.getPath();
	        		programSources = programSources.replaceAll( "/", "\\\\\\\\" );
	        		path = path.replaceAll( programSources, "" );
	        		path = path.replaceAll( ".java$", "" );
	        		path = path.replaceAll( "\\\\", "." );
	        		files.add( path );
	            }
	        	else if( file.isDirectory() )
	            {
	        		stack.push( file );
	            }
	        }            
	    }
		
		HashSet<String> filteredFiles = new HashSet<String>();
		filteredFiles.addAll( files );
		
		for( String file : files )
		{
			// remove excluded packages
			if( packageExclusion.length > 0 )
			{
				for( int j = 0; j < packageExclusion.length; j++ )
				{
					String excludedPackage = packageExclusion[ j ];
					
					// remove packages that are not being considered
					if( excludedPackage.contains( "*" ) )
					{
						excludedPackage = excludedPackage.substring( 0 , excludedPackage.indexOf( "*" )  );
						
						if( file.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
						{
							filteredFiles.remove( excludedPackage );
						}
					}
					else
					{
						if( file.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
						{
							filteredFiles.remove( excludedPackage );
						}
					}
				}
			}
		}
		
		for( String file : filteredFiles )
		{
			System.out.println( "\t\t\"" + file + "\"" );
		}
		
		System.out.println( "------\n" + filteredFiles.size() + " files" );
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		
		DependencyAnalysis depAnalysis = new DependencyAnalysis( programUrl, packageExclusion );
		HashSet<String> classes = depAnalysis.getClassesWithDependencies();
		
		System.out.println( "\nBINARY CODE CLASS NAMES:" );
		System.out.println( "--------------------------" );
		
		int j = 0;
		
		for( String className : classes )
        {
			j++;
			System.out.println( "\t\t\"" + className + "\"" );
        	
        } 
		
		System.out.println( "------\n" + j + " classes" );
	}
}
