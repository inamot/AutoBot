/*Copyright 2019 Zipani Tom Sinkala (tom.sinkala@kau.se)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package recommender.ir;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import recommender.Program;
import recommender.da.DependencyAnalysis;
import recommender.ir.IndexFiles;
import recommender.util.IntComparator;

public class IRAnalysis
{
	final String stopWords [] = { "a", "able", "about", "after", "all", "almost", "also", "am", "among", "an", "and", "are", "as", "at", "be", "because", 
			"been", "but", "by", "can", "could", "dear", "did", "do", "does", "else", "ever", "for", "from", "get", "got", "had", "has", 
			"have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "let", "like", "me", 
			"more", "my", "neither", "no", "nor", "of", "off", "on", "or", "other", "our", "own", "rather", "said", "say", "says", 
			"she", "should", "since", "so", "such", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", 
			"wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your" };
	
	final String methodPrefixes [] = { "get", "set", "init", "is", "has", "from", "can", "show", "hide", "update", "create", "find", "add", "start", "as", "add",
			"end", "close", "open", "next" }; // 
	
	String sourceFileExt = ".jvtp";
	int moduleDependencies [][];
	String moduleNames [];
	String moduleDescriptions [];
	String packageExclusion [];
	String indexPath;
	String docsPath;
	int numOfRequestedDocs;
	double mnBoostFactor;
	double mdBoostFactor;
	ArrayList<String> documents = new ArrayList<>();
	ArrayList<String> classes = new ArrayList<>();
	ArrayList<String> filteredClasses = new ArrayList<>();
	Map<Map<String, String>, Double> documentModuleSimilarity_AffinityScores = new HashMap<>();	
	HashSet<String> mappedClasses = new HashSet<String>();
	HashSet<String> unMappedClasses = new HashSet<String>();
	Map<String, HashSet<String>> acceptedMappings = new HashMap<>();
	Map<String, HashSet<String>> classMethodsMap = new HashMap<>();
	IndexFiles index = new IndexFiles();
	PreProcessing files = new PreProcessing();
	//DependencyAnalysis depAnalysis;
	Program program;
	
	public IRAnalysis( Program program, int runCount, boolean createIndex )
	{
		this.program = program;
		this.indexPath = program.getProgramUrl() + "index";
		this.docsPath = program.getProgramSourceUrl();
		this.moduleNames = program.getModuleNames();
		this.moduleDescriptions = program.getModuleDescriptions();
		this.moduleDependencies = program.getModuleDependencies();
		this.packageExclusion = program.getPackageExclusion();
		
		if( createIndex && runCount == 1 )
		{
			this.files.clean( docsPath, stopWords, methodPrefixes, sourceFileExt, packageExclusion );
			this.index.indexFiles( indexPath, docsPath, sourceFileExt );
		}
		
		this.documents = index.getDocuments(); 
		getIRAnalysisDocuments();
	}
	
	private void getIRAnalysisDocuments()
	{			
		classes.clear();
		
		// for use with java source files
		for( String document : documents )
		{   
			document = document.replaceAll( "\\\\", "." );
			document = document.replaceAll( "/", "." );
			String strip = docsPath.replaceAll( "/", "." );
        	String className = document.replaceAll( strip, "" );
			className = className.replaceAll( sourceFileExt, "" );		// rewrite source filenames to match class packages names
			classes.add( className );
		}
		
		// for use with class binaries
		/*File folder = new File( docsPath );
		Stack<File> stack = new Stack<File>();
		Stack<File> folders = new Stack<File>();
		stack.push( folder );
		
		while( !stack.isEmpty() )
	    {
	        File child = stack.pop();
	        File[] listFiles = child.listFiles();
	        folders.push( child );

	        for( File file : listFiles )
	        {
	        	if( file.isFile() && file.getPath().endsWith( ".java" ) )
	            {
	        		String path = file.getPath();
	        		docsPath = docsPath.replaceAll( "/", "\\\\\\\\" );
	        		path = path.replaceAll( docsPath, "" );
	        		path = path.replaceAll( ".java$", "" );
	        		path = path.replaceAll( "\\\\", "." );
	        		classes.add( path );
	            }
	        	else if( file.isDirectory() )
	            {
	        		stack.push( file );
	            }
	        }            
	    }*/
		
		filterClasses(); 
	}
	
	private void filterClasses()
	{
		filteredClasses.clear();
		filteredClasses.addAll( classes );
		
		if( packageExclusion.length > 0 )
		{
			for( String className : classes )
			{	
				for( int j = 0; j < packageExclusion.length; j++ )
				{
					String excludedPackage = packageExclusion[ j ];
					
					// remove classes & packages that are not being considered
					if( excludedPackage.contains( "*" ) )
					{
						excludedPackage = excludedPackage.substring( 0 , excludedPackage.indexOf( "*" ) );
						
						if( className.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
						{
							filteredClasses.remove( className );
						}
					}
					else
					{
						if( className.startsWith( excludedPackage ) && !excludedPackage.equals( "" ) )
						{
							filteredClasses.remove( className );
						}
					}
				}
			}
		}
		
		System.out.println( "# of documents: " + filteredClasses.size() );
	}
	
	public ArrayList<String> getFilteredClasses()
	{
		return filteredClasses; 
	}
	
	public int numOfClasses()
	{
		return filteredClasses.size();
	}
	
	private void retrieveMappedClasses()
	{
		mappedClasses.clear();
		
		for( int i = 0; i < moduleNames.length; i++ )
		{
			String module = moduleNames[ i ];
			
			if( acceptedMappings != null )
			{
				HashSet<String> moduleClasses = acceptedMappings.get( module );
				
				if( moduleClasses != null )
				{
					for( String mappedClass : moduleClasses ) 
					{
						mappedClasses.add( mappedClass );
					}
				}
			}
		}
	}
	
	private void retrieveUnMappedClasses()
	{
		retrieveMappedClasses();
		unMappedClasses.clear();
		unMappedClasses.addAll( filteredClasses );
		
		for( String mappedClass : mappedClasses )
		{
			unMappedClasses.remove( mappedClass );
		}
	}
	
	public HashSet<String> getUnMappedClasses()
	{
		retrieveMappedClasses();
		return unMappedClasses;
	}
	
	public void setClassMethods( Map<String, HashSet<String>> classMethodsMap )
	{
		this.classMethodsMap = classMethodsMap;
	}
	
	public void setNumOfRequestedDocs( int numOfRequestedDocs )
	{
		this.numOfRequestedDocs = numOfRequestedDocs;
	}
	
	private String cleanText( String str ) 
	{
		str = str.replaceAll( "[^a-zA-Z0-9_\\s-]", "" );
		return str;
	}
	
	public Map<Map<String, String>, Double> getSimilarityAffinityScores( Map<String, HashSet<String>> acceptedMaps, double mnBoostFactor, double mdBoostFactor )
	{	
		this.acceptedMappings.clear();
		this.acceptedMappings.putAll( acceptedMaps );
		this.mnBoostFactor = mnBoostFactor;
		this.mdBoostFactor = mdBoostFactor;
		retrieveUnMappedClasses();
		
		try 
		{	
			documentModuleSimilarity_AffinityScores.clear();
			
			for( int i = 0; i < moduleNames.length; i++ )
			{
				IndexReader reader = DirectoryReader.open( FSDirectory.open( Paths.get( indexPath ) ) );
			    IndexSearcher searcher = new IndexSearcher( reader );
			    String field = "contents";
			    Analyzer analyzer = new StandardAnalyzer();
			    QueryParser parser = new QueryParser( field, analyzer );
			    SnowballStemmer stemmer = ( SnowballStemmer ) new englishStemmer();	
			    String module = moduleNames[ i ];
			    String moduleSearchKeywords = "";
			    
			    // ADDING MODULE NAMES
			    ArrayList<String> moduleNameList = new ArrayList<String>();
			    module = module.replaceAll( "[^a-zA-Z0-9_\\s-]", "" );
			    
		    	String moduleNameWords [] = module.toLowerCase().split( " " );				// for multi-word module names e.g. “architecture model”
				   
			    for( int j = 0; j < moduleNameWords.length; j++ )
			    {
			    	String word = cleanText( moduleNameWords[ j ] );
			    	
			    	if( !word.equals( "" ) )
			    	{
			    		stemmer.setCurrent( word.toLowerCase() );
						//stemmer.stem();  										// stem each word
			    		moduleNameList.add( stemmer.getCurrent() );
			    	}
			    }
			    
			    if( module.contains( "-" ) || module.contains( "_" ) )			// for multi-word module names that have characters e.g. “architecture-model”
			    {	
			    	String moduleCleaned = module.replaceAll( "-", " " );
			    	moduleCleaned = moduleCleaned.replaceAll( "_", " " );
			    	moduleNameWords = moduleCleaned.split( " " );				
					   
				    for( int j = 0; j < moduleNameWords.length; j++ )
				    {
				    	String word = cleanText( moduleNameWords[ j ] );
				    	
				    	if( !word.equals( "" ) )
				    	{
				    		stemmer.setCurrent( word.toLowerCase() );
							//stemmer.stem();  										// stem each word
				    		moduleNameList.add( stemmer.getCurrent() );
				    	}
				    }							
			    }
			    
			    moduleSearchKeywords = moduleSearchKeywords + "(";
			    
			    for( String moduleNameWord : moduleNameList )
		    	{
			    	//moduleSearchKeywords = moduleSearchKeywords + "/\\w*" + moduleNameWord + "\\w*/";		// using regex for further research
			    	moduleSearchKeywords = moduleSearchKeywords + " " + moduleNameWord + "*~";		// add module name words (with * wildcards and fuzzy searching) to search keywords
		    	}
			    
			    moduleSearchKeywords = moduleSearchKeywords + " )^" + mnBoostFactor;
			    
			    // ADDING MODULE DESCRIPTIONS
			    String moduleDescriptionWords [] = moduleDescriptions[ i ].split( " " );		
			    ArrayList<String> moduleDescriptionList = new ArrayList<String>(); 
			    
			    // remove characters
			    for( int j = 0; j < moduleDescriptionWords.length; j++ )
			    {
			    	String word = cleanText( moduleDescriptionWords[ j ] );
			    	
			    	if( !word.equals( "" ) )
			    	{
			    		stemmer.setCurrent( word.toLowerCase() );
						//stemmer.stem();  										// stem each word
						moduleDescriptionList.add( stemmer.getCurrent() );
			    	}
			    }
			    
			    // removes stop words from module descriptions
			    ArrayList<String> filteredModuleDescriptionList = new ArrayList<String>();
			    filteredModuleDescriptionList.addAll( moduleDescriptionList );
			    
			    for( int j = 0; j < stopWords.length; j++ )
			    {
			    	for( String word : moduleDescriptionList )
			    	{
			    		word = word.toLowerCase();
			    		
			    		if( word.equals( stopWords[ j ] ) )
			    		{
			    			filteredModuleDescriptionList.remove( word );
			    		}
			    	}
			    }

			   moduleSearchKeywords = moduleSearchKeywords + " (";
			    
			    for( String moduleDescriptionWord : filteredModuleDescriptionList )
		    	{
			    	//moduleSearchKeywords = moduleSearchKeywords + "/\\w*" + moduleDescriptionWord + "\\w* /";		// using regex for further research
			    	moduleSearchKeywords = moduleSearchKeywords + " " + moduleDescriptionWord + "*~";			// add module description words (with * wildcards) to search keywords
		    	}
			    
			    moduleSearchKeywords = moduleSearchKeywords + ")^" + mdBoostFactor;
			    
			    
			    // getting class names and method names of mapped classes as module keywords
			    HashSet<String> acceptedClasses = acceptedMappings.get( module );
			    int numOfModuleClasses = 0;
			    HashSet<String> classNamesKeywords = new HashSet<>();
			    Map<String, Integer> methodCountMap = new HashMap<>();
			    
			    if( acceptedClasses != null )
			    {	
			    	numOfModuleClasses = acceptedClasses.size();
			    	
			    	for( String acceptedClass : acceptedClasses )
				    {
			    		classNamesKeywords.add( acceptedClass );
			    		
			    		if( classMethodsMap.get( acceptedClass ) != null )
			    		{
				    		for( String method : classMethodsMap.get( acceptedClass ) )			// count the number of times a method occurs within the module
				    		{
					    		if( methodCountMap.get( method ) == null )
					    		{
					    			methodCountMap.put( method, 1 );						
					    		}
					    		else 
					    		{
						    		int methodCount = methodCountMap.get( method ) + 1;
						    		methodCountMap.put( method, methodCount );
					    		}
				    		}
			    		}
				    }
			    	
			    	// sort method map in descending order
			    	methodCountMap = methodCountMap.entrySet()
							.stream()
					        .sorted( Collections.reverseOrder( Map.Entry.comparingByValue( new IntComparator() ) ) )
					        .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2 ) -> e2, LinkedHashMap::new) );
			    																								
			    	
			    	// ADDING CLASS NAMES
			    	int classNum = 1;
			    	
			    	for( String classNameKeyword : classNamesKeywords )
				    {
			    		if( classNum == 1 )
				    	{
			    			moduleSearchKeywords = moduleSearchKeywords + " (";				// begin class name search keywords: '+' indicates must contain class names
				    	}
			    					    		
			    		int classNameIndex = classNameKeyword.lastIndexOf( "." ) + 1;
				    	classNameKeyword = classNameKeyword.substring( classNameIndex );
				    	
				    	//moduleSearchKeywords = moduleSearchKeywords + " /.*" + classNameKeyword.toLowerCase() + ".* /";		// using regex for further research
				    	moduleSearchKeywords = moduleSearchKeywords + " " + classNameKeyword.toLowerCase();		// Adding class names (without * wildcards) to search keywords
				    	
				    	//String moduleDescriptionWords [] = classNameKeyword.split( "[A-Z]" );
				    	
				    	if( classNum == classNamesKeywords.size() )
			    		{
				    		moduleSearchKeywords = moduleSearchKeywords + ")^1.0";								// end class name search keywords
			    		}
			    		
				    	classNum++;
				    }
			    	
			    	// ADDING METHOD NAMES
			    	int numOfMethodsToAdd = 1024 - filteredModuleDescriptionList.size() - numOfModuleClasses;
			    	//int numOfMethodsToAdd = 1024 - numOfModuleClasses;
			    	int methodNum = 1;
			    	//float methodCount = 0.0f;
			    	//float highestCount = 0.0f;
			    	
				    if( !methodCountMap.isEmpty() )
				    {
				    	for( String methodNameKeyword : methodCountMap.keySet() )
					    {
				    		//methodCount = methodCountMap.get( methodNameKeyword );
				    		//System.out.println( "Methods: " + methodNameKeyword );
				    		if( methodNum == 1 )
					    	{
				    			moduleSearchKeywords = moduleSearchKeywords + " (";								// begin method name search keywords:
				    			//highestCount = methodCountMap.get( methodNameKeyword );
					    	}
					    			
					    	if( methodNum < numOfMethodsToAdd && methodNum != 0 )					// Adding method names (without * wildcards) to search keywords but making sure keywords do not exceed lucene limit of 1,024
				    		{
					    		
					    		// remove method prefixes
			                    for( int j = 0; j < methodPrefixes.length; j++ )
							    {
			                    	//methodNameKeyword = methodNameKeyword.replaceAll( methodPrefixes[ j ].toLowerCase() + "\\w+", "" );
			                    	String methodPrefixRegex = "(" + methodPrefixes[ j ].toLowerCase() + ")\\w+";
			                    	Pattern methodPrefixPattern = Pattern.compile( methodPrefixRegex );
			                    	Matcher methodPrefixMatcher = methodPrefixPattern.matcher( methodNameKeyword );
			                    	
			                    	if( methodPrefixMatcher.find( ) )
			                	    {
			                    		methodNameKeyword = methodNameKeyword.replaceAll( methodPrefixMatcher.group( 1 ).toString(), "" );
			                	    }
							    }
					    		// Adding method names (without * wildcards) to search keywords. Give each method a boost proportionate to the most frequently occurring method
					    		//moduleSearchKeywords = moduleSearchKeywords + " " + methodNameKeyword.toLowerCase() + "^" + methodCount / highestCount; 		
					    		
					    		// Adding method names (without * wildcards) to search keywords. 
					    		if( !methodNameKeyword.equals( "" ) )
					    		{
					    			//moduleSearchKeywords = moduleSearchKeywords + " /.*" + methodNameKeyword.toLowerCase() + ".* /";		// using regex for further research
					    			moduleSearchKeywords = moduleSearchKeywords + " " + methodNameKeyword.toLowerCase();
					    		}
				    		}
					    	
				    		if( methodNum == methodCountMap.size() )
				    		{
				    			moduleSearchKeywords = moduleSearchKeywords + ")^1.0";							// end method name search keywords
				    		}
				    		
				    		methodNum++; 
					    } //System.out.println( "" );
				    }
			    }
			    
			    //System.out.println( moduleSearchKeywords  );
			    
			    Query query = parser.parse( moduleSearchKeywords );
			    TopDocs results = searcher.search( query, numOfRequestedDocs );
			    ScoreDoc[] hits = results.scoreDocs;
			    int numOfDocHits = Math.toIntExact( results.totalHits.value );
			    int end = Math.min( numOfDocHits, numOfRequestedDocs );

			    for( int j = 0; j < end; j++ ) 
		    	{
			        Document doc = searcher.doc( hits[ j ].doc );
			        String path = doc.get( "path" );
			        String strip = docsPath.replaceAll( "/", "\\\\\\\\" ); 
			        
			        if( path.contains( "/" ) ) // for unix systems
			        	strip = docsPath;
			        
			        String document = path.replaceAll( strip, "" ); 
					document = document.replaceAll( "\\\\", "." );
					document = document.replaceAll( "/", "." );
	        		document = document.replaceAll( sourceFileExt, "" );		// rewrite source filenames to match class packages names
	        		double documentModuleAffinityScore = hits[ j ].score;
	        		boolean containsPackageName = false;
	        		
	        		for( String moduleNameWord : moduleNameList )
			    	{
				        if( document.contains( moduleNameWord ) )			// explore adding fuzzy check https://code-high.blogspot.com/2015/10/fuzzy-string-matching-in-java.html
				        {
				        	containsPackageName = true;
				        }
			    	}
	        		
	        		if( containsPackageName )	
			        {
			        	documentModuleAffinityScore = documentModuleAffinityScore * mnBoostFactor;	// increase score of low scoring documents that have module name in package name
			        }
			        else
			        {
			        	documentModuleAffinityScore = documentModuleAffinityScore / mnBoostFactor;	// reduce score of high scoring documents that do not have module name in package name
			        }
	        		
			        Map<String, String> documentModuleSimilarity = new HashMap<>();
			        documentModuleSimilarity.put( document, module );
		    		documentModuleSimilarity_AffinityScores.put( documentModuleSimilarity, documentModuleAffinityScore );
		    	}
			    
			    reader.close(); 
			}
		}
		catch( Exception ex ) 
		{	
			ex.printStackTrace();
		}
		
		//System.out.println( "documentModuleSimilarity_AffinityScores" + documentModuleSimilarity_AffinityScores.size()  );
		
		// TODO: add zeros back for depanlysis? but decide how to deal with the fact that we dont have any information about them to make a decision
		//addMissingEntries(); 
		filterEntries();
		checkArchitectureViolations( false );
		//rangeShift();
		
		//System.out.println( "documentModuleSimilarity_AffinityScores" + documentModuleSimilarity_AffinityScores.size()  );
		return documentModuleSimilarity_AffinityScores;
	}
	
	private void rangeShift()
	{	     
		Map<Map<String, String>, Double> rangeShiftScores = new HashMap<>();
		
		double rangeShift = Math.abs( Collections.min( documentModuleSimilarity_AffinityScores.values() ) + 1 );
		
		for( Map<String, String> classModule : documentModuleSimilarity_AffinityScores.keySet() )
		{
			if( documentModuleSimilarity_AffinityScores.get( classModule ) != 0 )
			{
				double score = documentModuleSimilarity_AffinityScores.get( classModule ) + rangeShift;				
				rangeShiftScores.put( classModule, score );
			}
		}
		
		documentModuleSimilarity_AffinityScores.clear();
		documentModuleSimilarity_AffinityScores.putAll( rangeShiftScores );
	}
	
	private void addMissingEntries()
	{	
		// If a module keyword term returned no hits for a given class set score to zero to avoid nulls
		for( String className : filteredClasses )
		{	
			for( int i = 0; i < moduleNames.length; i++ )
			{
				String module = moduleNames[ i ];
				Map<String, String> classModule = new HashMap<>();
				classModule.put( className, module);
				
				if( documentModuleSimilarity_AffinityScores.get( classModule ) == null )
				{	
					double documentModuleAffinityScore = 0.0;
					documentModuleSimilarity_AffinityScores.put( classModule, documentModuleAffinityScore );
				}
			}
		}
	}
	
	private void checkArchitectureViolations( boolean conform ) 
	{
		double mappingPercent = mappedClasses.size() / classes.size(); 
		
		if( conform == true )
		{
			Map<Map<String, String>, Double> conformedScores = new HashMap<>();
			
			for( String unMappedClass : unMappedClasses )
			{
				for( int row = 0; row  < moduleNames.length; row ++ )
				{
					Map<String, String> classModule = new HashMap<>();
					double classModuleConformedAffinityScore = 0;
					String module = moduleNames[ row ];
					
					for( int column = 0; column < moduleNames.length; column++ )
					{	
						if( documentModuleSimilarity_AffinityScores.entrySet().iterator().hasNext() )
						{
							Map<String, String> unMappedClassModule = new HashMap<>();
							String candidateModule = moduleNames[ column ];
							unMappedClassModule.put( unMappedClass, candidateModule );
							int moduleDependencyDirection = moduleDependencies[ row  ][ column ];
							int inverseModuleDependencyDirection = moduleDependencies[ column ][ row ];
							double unMappedClassAffScore = 0;
							
							if( documentModuleSimilarity_AffinityScores.get( unMappedClassModule ) != null )
							{
								unMappedClassAffScore = documentModuleSimilarity_AffinityScores.get( unMappedClassModule );
							}
							
							if( moduleDependencyDirection == 1 && inverseModuleDependencyDirection == -1 )
							{
								unMappedClassAffScore = unMappedClassAffScore * -0.25;		// 75% of similarity penalized based on the assumption 75% of similarity score derived from method name keywords
							}
							else if( moduleDependencyDirection == -1 && inverseModuleDependencyDirection == -1 )
							{
								unMappedClassAffScore = unMappedClassAffScore * -1.0;
							}
							
							classModuleConformedAffinityScore = classModuleConformedAffinityScore + unMappedClassAffScore;
						}
					}
					
					classModule.put( unMappedClass, module );
					conformedScores.put( classModule, classModuleConformedAffinityScore );
				}
			}
			
			documentModuleSimilarity_AffinityScores.clear();
			documentModuleSimilarity_AffinityScores.putAll( conformedScores );
		}
	}
		
	private void filterEntries()
	{	
		Map<Map<String, String>, Double> unfilteredScores = new HashMap<>();
		unfilteredScores.putAll( documentModuleSimilarity_AffinityScores );
		
		// remove classes that are mapped
		for( Map<String, String> classModuleMap : unfilteredScores.keySet() )
		{
			if( classModuleMap.entrySet().iterator().hasNext() )
			{
				String className = classModuleMap.entrySet().iterator().next().getKey();
				
				for( String mappedClass : mappedClasses )
				{
					if( className.toString().equals( mappedClass.toString() ) )
					{
						documentModuleSimilarity_AffinityScores.remove( classModuleMap );
					}
				}
			}
		}
	}

}