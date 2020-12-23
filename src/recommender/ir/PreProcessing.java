package recommender.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PreProcessing
{	
	public void clean( String programSources, String stopWords [], String methodPrefixes [], String sourceFileExt, String packageExclusion [] )
	{
		// taken from https://en.wikipedia.org/wiki/List_of_Java_keywords and https://en.wikipedia.org/wiki/Java_annotation
		final String javaKeyWords [] = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", 
				"continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "if", "goto", 
				"implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", 
				"public", "return", "short", "static", "strictfp", "string", "super", "switch", "synchronized", "this", "throw", "throws", 
				"transient", "try", "void", "volatile", "while", "true", "false", "null", "var", "const", "goto", "@Override", 
				"@SuppressWarnings", "@Retention", "@Documented", "@Target", "@Inherited", "@SafeVarargs", "@FunctionalInterface", 
				"@Repeatable", "@param", "@author", ".INSTANCE" };
		
		final String chars [] = { "\\{", "\\}", "\\[", "\\]", "\\(", "\\)", ";", "\\*", "/", "\\+", "&", "@", "\\|", "<", ">", 
				"=", ":", ",", "\"", "\\.", "!" };
		
		File folder = new File( programSources );
		
		Stack<File> stack = new Stack<File>();
		stack.push( folder );
		
		// delete pre-processed files
		while( !stack.isEmpty() )
	    {
			File childFolder = stack.pop();
	        File[] files = childFolder.listFiles();
	        ArrayList<File> filesList = new ArrayList<File>();
	        
	        for( int j = 0; j < files.length; j++ )
		    {
			    filesList.add( files[ j ] );
		    }
	        
	        for( File file : filesList )
	        {
	        	if( file.isFile() && file.getPath().contains( sourceFileExt ) )
	        	{
		    		file.delete();		// delete all existing pre-processed files before cleaning
	            }
	        	else if( file.isDirectory() )
	            {
	        		stack.push( file );
	            }
	        }            
	    }
		
		// clean filtered source files
		Stack<File> filteredStack = new Stack<File>();
		filteredStack.push( folder );
		
		while( !filteredStack.isEmpty() )
	    {
			File childFolder = filteredStack.pop();
	        File[] files = childFolder.listFiles();
	        ArrayList<File> filesList = new ArrayList<File>();
	        ArrayList<File> filteredFilesList = new ArrayList<File>();
	        
	        for( int j = 0; j < files.length; j++ )
		    {
			    filesList.add( files[ j ] );
			    filteredFilesList.add( files[ j ] );
		    }
	      
	        // remove files that are not being considered	        
	        for( File file : filesList )
	        {
		    	if( file.isFile() && packageExclusion.length > 0 )
	 			{
	 				for( int k = 0; k < packageExclusion.length; k++ )
	 				{
	 					String excludedPackage = packageExclusion[ k ];
	 					String fileName = file.toString().replaceAll( "\\\\", "." );
	 					fileName = fileName.replaceAll( ".java$", "" );
	 					
	 					if( excludedPackage.contains( "*" ) )
	 					{
	 						excludedPackage = excludedPackage.substring( 0 , excludedPackage.indexOf( "*" ) );
	 						
	 						if( fileName.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
	 						{
	 							filteredFilesList.remove( file );
	 						}
	 					}
	 					else
	 					{
	 						if( fileName.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
	 						{
	 							filteredFilesList.remove( file );
	 						}
	 					}
	 				}
	 			}
	        }
	        
	        // clean files
	    	for( File file : filteredFilesList ) 
	        {
	    		  
	    		if( file.isFile() && file.getPath().endsWith( ".java" ) )
	            {
	    			String progSrcs = programSources.toString().replaceAll( "/", "." );
	    			String className = file.toString().replaceAll( "\\\\", "." );
	    			className = className.toString().replaceAll( progSrcs, "" );
	    			className = className.toString().replaceAll( ".java", "" );
	            	
	            	
	            	try
					{
	        			// extract code and comments from file separately and clean them
	        			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "ISO-8859-1") );
	                    String commentsRegex = "((/\\*+)|(//)).*";
	                    Pattern commentsPattern = Pattern.compile( commentsRegex );
	                    String comments = "";
	                    String code = "";
	                    String codeOnly = "";
	                    String line = null;
	                    boolean longComment = false;
	        			
	                    while( ( line = bufferedReader.readLine() ) != null )
	                    {
	                	    // remove import statements
	                    	line = line.replaceAll( "^(\\s)*import(.*);" , " " );
	                    	
	                    	// remove private statements: intended to remove private method names to exclude them from searches
	                    	line = line.replaceAll( "^(\\s)*private(.*);" , " " );
	                    	
	                    	code = line;
	                	    
	                    	// split code and comments
	                    	if( !longComment )
                	    	{
	                    		code = code.replaceAll( commentsRegex , " " );
	                    		codeOnly = codeOnly + " \n" + code;
	                    		Matcher matcher = commentsPattern.matcher( line );
	                	    	
	                	    	if( matcher.find( ) )
		                	    {
		                	    	comments = comments + " \n" + matcher.group( 0 ).toString();	
		                	    	
		                	    	if( matcher.group( 0 ).toString().contains( "/*" ) )
		                	    	{
		                	    		longComment = true;
		                	    	}
		                	    	
		                	    	if( matcher.group( 0 ).toString().contains( "*/" ) )
		                	    	{
		                	    		longComment = false;
		                	    	}
		                	    }
                	    	}
	                	    else
	                	    {
	                	    	comments = comments + " \n" + line;
	                	    	
	                	    	if( line.contains( "*/" ) )
	                	    	{
	                	    		longComment = false;
	                	    	}
	                	    }
		    		    }
	                    
	                    // split code and comments
	                    comments = comments.toLowerCase();
	                    codeOnly = codeOnly.toLowerCase();

	                    // remove chars from comments and code
	                    for( int j = 0; j < chars.length; j++ )
					    {
	                    	comments = comments.replaceAll( chars[ j ].toLowerCase(), " " );
	                    	codeOnly = codeOnly.replaceAll( chars[ j ].toLowerCase(), " " );
	                    	className = className.replaceAll( chars[ j ].toLowerCase(), " " );
					    }
	                    
	                    // remove stop words from comments
	                    for( int j = 0; j < stopWords.length; j++ )
					    {
	                    	comments = comments.replaceAll( "\\W+" + stopWords[ j ].toLowerCase() + "\\W+", " " );
					    }
	                    // remove keywords from code
	                    for( int j = 0; j < javaKeyWords.length; j++ )
					    {
	                    	codeOnly = codeOnly.replaceAll( "\\W+" + javaKeyWords[ j ].toLowerCase() + "\\W+", " " );
					    }
	                    
	                    // adding full name of class to preserve important keywords in the package name
	                    comments = comments + className;		
	                    codeOnly = codeOnly + className;
	                                               			
	                    bufferedReader.close();
	                    
	                    String cleanedSource = comments + codeOnly;			// comments & code
	                    //String cleanedSource = codeOnly;					// code only
	                    //String cleanedSource = comments;					// comments only 
	                    
	                    // remove method prefixes from code and comments
	                    /*for( int j = 0; j < methodPrefixes.length; j++ ) 
					    {
	                    	String methodPrefixRegex = "(^|\\s+)(" + methodPrefixes[ j ].toLowerCase() + ")\\w+";
	                    	Pattern methodPrefixPattern = Pattern.compile( methodPrefixRegex );
	                    	Matcher methodPrefixMatcher = methodPrefixPattern.matcher( cleanedSource );
	                    	
	                    	if( methodPrefixMatcher.find( ) )
	                	    {
	                    		cleanedSource = cleanedSource.replaceAll( methodPrefixMatcher.group( 2 ).toString(), " " );
	                	    }
					    }*/
	                    
		    		    String stripedFile = file.getPath(); 
		    		    stripedFile = stripedFile.replaceAll( ".java$", sourceFileExt );
		    		    File cleanedFile = new File( stripedFile ); 
		    		    BufferedWriter writeOut = new BufferedWriter( new FileWriter( cleanedFile, true ) );
		    		    writeOut.append( cleanedSource );
		    			writeOut.close();
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
	            }
	        	else if( file.isDirectory() )
	            {
	        		filteredStack.push( file );
	            }
	        }            
	    }
	}
}