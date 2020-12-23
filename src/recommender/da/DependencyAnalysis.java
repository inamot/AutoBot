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

package recommender.da;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import recommender.Program;
import recommender.ir.IRAnalysis;
import recommender.util.DoubleComparator;
import recommender.util.IntComparator;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DependencyAnalysis
{
	int moduleDependencies [][];
	String moduleNames [];
	String cdgPath;
	String packageExclusion [];
	double penaltyFactor;
	Map<Map<String, String>, Integer> classDependency_Count = new HashMap<>();
	Map<Map<String, String>, Integer> sanitizedInnerClassesDependency_Count = new HashMap<>();
	Map<Map<String, String>, Integer> filteredClassDependency_Count = new HashMap<>();
	
	Map<String, HashSet<String>> classMethods = new HashMap<>();
	Map<String, HashSet<String>> sanitizedClassMethods = new HashMap<>();
	Map<String, HashSet<String>> filteredClassMethods = new HashMap<>();
	
	Map<Map<String, String>, Double> classModuleDependency_AffinityScores = new HashMap<>();
	//Map<Map<String, String>, Integer> filteredClassModuleDependency_AffinityScores = new HashMap<>();
	
	HashSet<String> classes = new HashSet<String>();
	HashSet<String> mappedClasses = new HashSet<String>();
	HashSet<String> unMappedClasses = new HashSet<String>();
	Map<String, HashSet<String>> acceptedMappings = new HashMap<>();
	HashSet<Map<String, String>> noDependencies = new HashSet<Map<String, String>>();
	Map<Map<String, String>, Integer> mappedClassDependency_Count = new HashMap<>();
	Map<Map<String, String>, Integer> unMappedClassModuleDependency_Count = new HashMap<>();		// stores calls from class to module
	Map<Map<String, String>, Integer> moduleUnMappedClassDependency_Count = new HashMap<>();		// stores calls from module to class
	Map<String, Integer> mappedNeighboursCallsCount = new HashMap<>();
	Map<String, Integer> totalNeighboursCallsCount = new HashMap<>();
	Map<String, Integer> mappedNeighboursCount = new HashMap<>();
	Map<Map<String, String>, Integer> mappedNeighboursModule_Count = new HashMap<>();
	Map<String, Integer> totalNeighboursCount = new HashMap<>();
	Program program;
	IRAnalysis irAnalysis;
	
	public DependencyAnalysis( Program program, IRAnalysis irAnalysis )
	{
		this.program = program;
		this.cdgPath = program.getProgramUrl() + "bin/static-cdg.txt";
		this.moduleNames = program.getModuleNames();
		this.moduleDependencies = program.getModuleDependencies();
		this.packageExclusion = program.getPackageExclusion();
		this.irAnalysis = irAnalysis;
        extractProgDependencies();			
        sanitizeProgDependencies();			
        filterProgDependencies();			
        
        extractClassMethodsKeywords();		
        sanitizeClassMethodsKeywords();		
        filterClassMethodsKeywords();		
        
        retriveClassesfromIR();				
        //countTotalDependencyCalls();		// TODO: Fix! Very slow Operation
		//countTotalNeighbours();				
	}
	
	public DependencyAnalysis( String cdgPath, String packageExclusion [] )
	{
		this.cdgPath = cdgPath + "static-cdg.txt";
		this.packageExclusion = packageExclusion;
        extractProgDependencies(); 
        sanitizeProgDependencies();
        filterProgDependencies();
        extractClassMethodsKeywords();
        sanitizeClassMethodsKeywords();
        filterClassMethodsKeywords();
	}
	
	/* obtain program call graph using by java-callgraph (https://github.com/gousiosg/java-callgraph)
    jcg runs on class files not source files
	   bundle class file into jar archive:   
	        jar cvf Jittac.jar code/bin
    generate program call-graph:   
	        java -jar ../../../lib/java-callgraph-master/target/javacg-0.1-SNAPSHOT-static.jar Jittac.jar > static-cdg.txt
	   make sure # of source and class files are equal and source file names are equal to class file names */
	private void extractProgDependencies() 
	{   
		// all types of dependencies
		/*String jarPath = program.getProgramUrl() + "bin/" + program.getProgramName() + ".jar";
		String rootPackage = program.getProgramJarRootPackage();	
		LoadJar jar = new LoadJar( jarPath, rootPackage );
		
        try
        {
        	classDependency_Count.putAll( jar.getClasses( rootPackage ) );
        }
        catch ( IOException e )
        {
            java.lang.System.out.println( e );
        }*/
		
		// method call dependencies only
    	try
        {	//System.out.println( "cdgPath: " + cdgPath );
            BufferedReader bufferedReader = new BufferedReader( new FileReader( cdgPath ) );
            String nonMethodCallRegex = "^C:(\\S*)\\s+(\\S*)";
            String methodCallRegex = "^M:(\\S*):(\\S*)\\s+\\(\\S*\\)([a-zA-Z0-9_\\.]*)(\\$\\S*)*:(\\S*)";
            Pattern nonMethodCallPattern = Pattern.compile( nonMethodCallRegex ); 
            Pattern methodCallPattern = Pattern.compile( methodCallRegex ); 
            String line = null;
            
            // get program dependencies from output generated by java-callgraph
            while( ( line = bufferedReader.readLine() ) != null )
            {	
            	Matcher nonMethodCallMatcher = nonMethodCallPattern.matcher( line );
            	Matcher methodCallMatcher = methodCallPattern.matcher( line );
        	    Map<String, String> classDependency = new HashMap<>();
        	    
        	    // adding non method call dependencies i.e. class to class dependencies such as "extends" or "implements"
        	    if( nonMethodCallMatcher.find( ) )
        	    {
        	    	String class1Name = nonMethodCallMatcher.group( 1 ).toString();
        	    	String class2Name = nonMethodCallMatcher.group( 2 ).toString();
        	    	classDependency.put( class1Name, class2Name ); 
                   
        	    	int numOfCalls = 1;
        	    	
        			if ( classDependency_Count.get( classDependency ) != null ) 
        			{
        				numOfCalls = classDependency_Count.get( classDependency ) + 1;
                	}
        			
        			classDependency_Count.put( classDependency, numOfCalls );
                }
        	    
        	    // adding method call dependencies 
        	    if( methodCallMatcher.find( ) )
        	    {
        	    	String class1Name = methodCallMatcher.group( 1 ).toString();
        	    	String class2Name = methodCallMatcher.group( 3 ).toString();
        	    	classDependency.put( class1Name, class2Name );
                   
        	    	int numOfCalls = 1;
        	    	
        			if ( classDependency_Count.get( classDependency ) != null ) 
        			{
        				numOfCalls = classDependency_Count.get( classDependency ) + 1;
                	}
        			
        			classDependency_Count.put( classDependency, numOfCalls );
                }
            }   
            
            bufferedReader.close();
        }
        catch( FileNotFoundException ex )
        {
            System.out.println( "Unable to open file '" + cdgPath + "'" );                
        }
        catch( IOException ex )
        {
        	ex.printStackTrace(); 
        }
	}
	
	private void sanitizeProgDependencies()
	{
		// checks if a class has an inner class if it does it adds its inner class' calls to its calls
		sanitizedInnerClassesDependency_Count.putAll( classDependency_Count );
		
		for( Map<String, String> classDependency : classDependency_Count.keySet() )
		{
			if( classDependency.entrySet().iterator().hasNext() )
			{
				String sourceClass = classDependency.entrySet().iterator().next().getKey();
				String targetClass  = classDependency.entrySet().iterator().next().getValue();
				int count = classDependency_Count.get( classDependency );
				
				// i$j <-> k
				if( sourceClass.contains( "$" ) && !targetClass.contains( "$" ) )
				{
					sanitizedInnerClassesDependency_Count.remove( classDependency );	
					
					int trimEnd = sourceClass.indexOf( "$" );
					String sanitizedSourceClass = sourceClass.substring( 0, trimEnd );
					Map<String, String> sanitizedClassDependency = new HashMap<>();
					sanitizedClassDependency.put( sanitizedSourceClass,  targetClass );
					
					if( sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency ) != null )
					{
						int sanitizedCount = count + sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.remove( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, sanitizedCount );
					}
					else
					{
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, count );
					}
				}
				
				// i <-> k$l
				else if( !sourceClass.contains( "$" ) && targetClass.contains( "$" ) )
				{
					sanitizedInnerClassesDependency_Count.remove( classDependency );
					
					int trimEnd = targetClass.indexOf( "$" );
					String sanitizedTargetClass = targetClass.substring( 0, trimEnd );
					Map<String, String> sanitizedClassDependency = new HashMap<>();
					sanitizedClassDependency.put( sourceClass, sanitizedTargetClass );
					
					if( sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency ) != null )
					{
						int sanitizedCount = count + sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.remove( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, sanitizedCount );
					}
					else
					{
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, count );
					}
				}
				
				// i$j <-> k$l
				else if( sourceClass.contains( "$" ) && targetClass.contains( "$" ) )
				{
					sanitizedInnerClassesDependency_Count.remove( classDependency );
					
					int sourceTrimEnd = sourceClass.indexOf( "$" );
					int targetTrimEnd = targetClass.indexOf( "$" );
					String sanitizedSourceClass = sourceClass.substring( 0, sourceTrimEnd );
					String sanitizedTargetClass = targetClass.substring( 0, targetTrimEnd );
					Map<String, String> sanitizedClassDependency = new HashMap<>();
					sanitizedClassDependency.put( sanitizedSourceClass, sanitizedTargetClass );
					
					if( sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency ) != null )
					{
						int sanitizedCount = count + sanitizedInnerClassesDependency_Count.get( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.remove( sanitizedClassDependency );
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, sanitizedCount );
					}
					else
					{
						sanitizedInnerClassesDependency_Count.put( sanitizedClassDependency, count );
					}
				}
			}
		}
	}
	
	private void filterProgDependencies()
	{
		filteredClassDependency_Count.putAll( sanitizedInnerClassesDependency_Count );
		
		if( packageExclusion.length > 0 )
		{
			for( Map<String, String> classDependency : sanitizedInnerClassesDependency_Count.keySet() )
			{
				if( classDependency.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependency.entrySet().iterator().next().getKey();
					String targetClass  = classDependency.entrySet().iterator().next().getValue();
					
					// remove class recursive dependencies
					// TODO: Find a better way for this when doing depAnlysis as it removes classes without methods calls such as interfaces
					if( sourceClass.equals( targetClass) )	
					{	//System.out.println( "classDependency: " + classDependency );
						filteredClassDependency_Count.remove( classDependency );	
					}
					else 
					{
						for( int j = 0; j < packageExclusion.length; j++ )
						{
							String excludedClass = packageExclusion[ j ];
							
							// remove dependencies to packages that are not being considered e.g. java & javax
							if( excludedClass.contains( "*" ) )
							{
								excludedClass = excludedClass.substring( 0 , excludedClass.indexOf( "*" ) ) ;

								if( ( sourceClass.startsWith( excludedClass ) || targetClass.startsWith( excludedClass ) ) && !excludedClass.equals( "" ) )	
								{	
									filteredClassDependency_Count.remove( classDependency );
								}
							}
							else
							{
								if( ( sourceClass.startsWith( excludedClass ) || targetClass.startsWith( excludedClass ) ) && !excludedClass.equals( "" ) )	
								{	
									filteredClassDependency_Count.remove( classDependency );
								}
							}
						}
					}
				}
			} 
		} 
	}

	public Map<Map<String, String>, Integer> getFilteredClassDependency_Counts()
	{
		return filteredClassDependency_Count;
	}

	private void extractClassMethodsKeywords()
	{	
		try
        {
            FileReader fileReader = new FileReader( cdgPath );
            BufferedReader bufferedReader = new BufferedReader( fileReader );
            String classRegex = "^C:(\\S*)\\s+(\\S*)";
            String classMethodRegex = "^M:(\\S*):(\\S*)\\(\\S*\\)\\s+\\(\\S*\\)([a-zA-Z0-9_\\.]*)((\\S*):)*(\\S*)\\(\\S*\\)";
            Pattern classPattern = Pattern.compile( classRegex );
            Pattern classMethodPattern = Pattern.compile( classMethodRegex ); 
            String line = null;
            
            // get calls to methods from output generated by java-callgraph
            while( ( line = bufferedReader.readLine() ) != null )
            {
            	Matcher classMatcher = classPattern.matcher( line );
            	Matcher classMethodMatcher = classMethodPattern.matcher( line );
        	    HashSet<String> methods = new HashSet<>();
        	    
        	    // adding classes that have no methods
        	    if( classMatcher.find( ) )
        	    {
        	    	String class1Name = classMatcher.group( 1 ).toString();
        	    	String class2Name = classMatcher.group( 2 ).toString();
        	    	
        	    	classMethods.put( class1Name, methods );
        	    	classMethods.put( class2Name, methods );
                }
        	    
        	    // adding classes with methods
        	    if( classMethodMatcher.find( ) )
        	    {
        	    	// first pair
        	    	String className = classMethodMatcher.group( 1 ).toString();
        	    	String method = classMethodMatcher.group( 2 ).toString();

        	    	if( !methods.isEmpty() || methods != null )
        	    	{
        	    		methods = classMethods.get( className );  
        	    	}
        	    	
        	    	if( methods == null )
        	    	{
        	    		methods = new HashSet<>(); 
        	    	}
        	    	
        	    	if( !method.contains( "$" ) )
        	    	{
        	    		if( !method.contains( "<" ) )
            	    	{
            	    		methods.add( method );
                	    	classMethods.put( className, methods );
            	    	}
        	    	}

        	    	// second pair
        	    	className = classMethodMatcher.group( 3 ).toString();
        	    	method = classMethodMatcher.group( 6 ).toString();
        	    	methods = new HashSet<>();
        	    	
        	    	if( !methods.isEmpty() || methods != null )
        	    	{
        	    		methods = classMethods.get( className );  
        	    	}
        	    	
        	    	if( methods == null )
        	    	{
        	    		methods  = new HashSet<>(); 
        	    	}
        	    	
        	    	if( !method.contains( "$" ) )
        	    	{
        	    		if( !method.contains( "<" ) )
            	    	{
            	    		methods.add( method );
                	    	classMethods.put( className, methods );
            	    	}
        	    	}
                }
            }   
            
            bufferedReader.close();
        }
        catch( FileNotFoundException ex )
        {
            System.out.println( "Unable to open file '" + cdgPath + "'" );                
        }
        catch( IOException ex )
        {
        	ex.printStackTrace(); 
        } 	//System.out.println( "classMethods: " + classMethods);
	} 
	
	private void sanitizeClassMethodsKeywords()
	{
		// checks if a class has an inner class if it does it adds its inner class' calls to its calls
		sanitizedClassMethods.putAll( classMethods );
		
		for( String className : classMethods.keySet() )
		{
			if( classMethods.entrySet().iterator().hasNext() )
			{
				HashSet<String> methods = classMethods.entrySet().iterator().next().getValue();
				
				if( className.contains( "$" ) )
				{
					sanitizedClassMethods.remove( className );	
					
					int trimEnd = className.indexOf( "$" );
					String sanitizedClassName = className.substring( 0, trimEnd );
					
					if( sanitizedClassMethods.get( sanitizedClassName ) != null )
					{
						HashSet<String> sanitizedMethods = sanitizedClassMethods.get( sanitizedClassName );
						
						for( String method : methods )
						{
							sanitizedMethods.add( method );
						}
						
						sanitizedClassMethods.remove( sanitizedClassName );
						sanitizedClassMethods.put( sanitizedClassName, sanitizedMethods );
					}
					else
					{
						sanitizedClassMethods.put( sanitizedClassName, methods );
					}
				}
			}
		}
	}
	
	private void filterClassMethodsKeywords()
	{
		// remove classes that exist in packages that are not being considered e.g. external libraries such as java & javax
		filteredClassMethods.putAll( sanitizedClassMethods );
		
		if( packageExclusion.length > 0 )
		{
			for( String className : sanitizedClassMethods.keySet() )
			{
				for( int j = 0; j < packageExclusion.length; j++ )
				{
					String excludedClass = packageExclusion[ j ];
					
					// remove dependencies to packages that are not being considered e.g. java & javax
					if( excludedClass.contains( "*" ) )
					{
						excludedClass = excludedClass.substring( 0 , excludedClass.lastIndexOf( "." ) + 1 );
						
						if( className.contains( excludedClass ) && !excludedClass.equals( "" ) )
						{
							filteredClassMethods.remove( className );
						}
					}
					else
					{
						if( className.equals( excludedClass ) && !excludedClass.equals( "" ) )
						{
							filteredClassMethods.remove( className );
						}
					}
				}
			}
		}
	}
	
	public Map<String, HashSet<String>> getFliteredClassMethods()
	{	
		return filteredClassMethods;
	}
	
	private void retriveClassesfromIR() 
	{
		classes.clear();
		classes.addAll( irAnalysis.getFilteredClasses() ); 
		
		//System.out.println( "classes: " + classes ); getClassesWithDependencies();
	}
	
	public HashSet<String> getClassesWithDependencies() 
	{
		HashSet<String> depClasses = new HashSet<String>();
		
		/*for( String className : filteredClassMethods.keySet() )
		{
			classes.add( className );
		}*/
		
		for( Map<String, String> classDependency : filteredClassDependency_Count.keySet() )
		{
			String sourceClass = classDependency.entrySet().iterator().next().getKey();
			String targetClass  = classDependency.entrySet().iterator().next().getValue();
			depClasses.add( sourceClass );
			depClasses.add( targetClass );
		}	
		
		return depClasses;
	}
	
	public int numOfClasses()
	{
		return classes.size();
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
		unMappedClasses.clear();
		unMappedClasses.addAll( classes );
		
		for( String mappedClass : mappedClasses )
		{
			unMappedClasses.remove( mappedClass );
		}
	}

	public HashSet<String> getUnMappedClasses()
	{
		return unMappedClasses;
	}

	private void retrieveMappedProgDependencies()
	{	
		retrieveMappedClasses();
		mappedClassDependency_Count.clear();
		
		// narrow down to dependencies that have the source already mapped or the target class already mapped, not both		
		for( Map<String, String> classDependencies : filteredClassDependency_Count.keySet() )
		{
			for( String mappedClass: mappedClasses ) 
			{
				for( String sourceClass : classDependencies.keySet() )
				{
					Map<String, String> mappedClassDependencies = new HashMap<>();
					String targetClass = classDependencies.get( sourceClass ); 
					
					if( sourceClass.equals( mappedClass ) && !targetClass.equals( mappedClass ) || !sourceClass.equals( mappedClass ) && targetClass.equals( mappedClass ) )
					{						
						int numOfCalls = filteredClassDependency_Count.get( classDependencies );
						mappedClassDependencies.put( sourceClass, targetClass );
						mappedClassDependency_Count.put( mappedClassDependencies, numOfCalls );
					}
				}
			}
		}
	}

	private void addClassModuleDependencies()
	{	
		unMappedClassModuleDependency_Count.clear();
		moduleUnMappedClassDependency_Count.clear();
		
		// for each module get the total number of
		// 	calls from all classes in the given module to an unmapped class, plus
		// 	calls from an unmapped class to all classes in the given module		
		for( int i = 0; i < moduleNames.length; i++ )
		{
			for( Map<String, String> classDependencies : mappedClassDependency_Count.keySet() )
			{	
				if( classDependencies.entrySet().iterator().hasNext() )
				{	
					for( String unMappedClass : unMappedClasses )
					{
						String module = moduleNames[ i ];
						String sourceClass = classDependencies.entrySet().iterator().next().getKey();
						String targetClass = classDependencies.entrySet().iterator().next().getValue();					
						int classModuleCalls = 0;
						int moduleClassCalls = 0;
						HashSet<String> moduleMappedClasses = acceptedMappings.get( module );
						Map<String, String> unMappedDependency = classDependencies;
						Map<String, String> moduleClassDependencies = new HashMap<>();		// stores module to class dependency
						Map<String, String> classModuleDependencies = new HashMap<>();		// stores class to module dependency
						
						if( moduleMappedClasses != null )
						{
							int classCalls = mappedClassDependency_Count.get( unMappedDependency );
							
							for( String moduleMappedClass : moduleMappedClasses ) 
							{
								// adding calls from unmapped class to mapped classes of a given module
								if( sourceClass.toString().equals( unMappedClass.toString() ) && targetClass.toString().equals( moduleMappedClass.toString() ) )
								{
									classModuleCalls = classModuleCalls + classCalls;
								}
								
								// adding calls from mapped classes of a given module to an unmapped class
								else if( sourceClass.toString().equals( moduleMappedClass.toString() ) && targetClass.toString().equals( unMappedClass.toString() ) )
								{
									moduleClassCalls = moduleClassCalls + classCalls;
								}
							}
						}
						
						if( classModuleCalls > 0  )
						{
							classModuleDependencies.put( unMappedClass, module );
							
							if( unMappedClassModuleDependency_Count.get( classModuleDependencies ) != null ) {
								classModuleCalls = classModuleCalls + unMappedClassModuleDependency_Count.get( classModuleDependencies );
							}
							
							unMappedClassModuleDependency_Count.put( classModuleDependencies, classModuleCalls );
						}
						else if( moduleClassCalls  > 0  )
						{
							moduleClassDependencies.put( module, unMappedClass );
							
							if( moduleUnMappedClassDependency_Count.get( moduleClassDependencies ) != null ) {
								moduleClassCalls = moduleClassCalls + moduleUnMappedClassDependency_Count.get( moduleClassDependencies );
							}
							
							moduleUnMappedClassDependency_Count.put( moduleClassDependencies, moduleClassCalls );
						}
					}
				}
			}
		}
	}
	
	public Map<Map<String, String>, Double> calculateDependencyAffinityScores( Map<String, HashSet<String>> acceptedMaps, double penaltyFactor ) 
	{
		this.acceptedMappings.clear();
		this.acceptedMappings.putAll( acceptedMaps );
		this.penaltyFactor = penaltyFactor;
		retrieveMappedProgDependencies();
		retrieveUnMappedClasses();
		addClassModuleDependencies();
		classModuleDependency_AffinityScores.clear();
		noDependencies.clear();
		
		for( String unMappedClass : unMappedClasses )
		{	
			for( int row = 0; row  < moduleNames.length; row ++ )
			{	
				Map<String, String> classModuleDependency = new HashMap<>();
				double classModuleAffinityScore = 0.0;
				double classModuleAffinityAbsoluteScore = 0.0;
				String module = moduleNames[ row ];
				
				for( int column = 0; column < moduleNames.length; column++ )
				{	
					if( unMappedClassModuleDependency_Count.entrySet().iterator().hasNext() || moduleUnMappedClassDependency_Count.entrySet().iterator().hasNext() )
					{
						Map<String, String> unMappedClassModuleDependency = new HashMap<>();
						Map<String, String> moduleUnMappedClassDependency = new HashMap<>();
						String candidateModule = moduleNames[ column ];
						unMappedClassModuleDependency.put( unMappedClass, candidateModule );
						moduleUnMappedClassDependency.put( candidateModule, unMappedClass );
						int moduleDependencyDirection = moduleDependencies[ row  ][ column ];
						int inverseModuleDependencyDirection = moduleDependencies[ column ][ row ];
						double unMappedClassModuleCalls = 0.0;
						double moduleUnMappedClassCalls = 0.0;
						
						if( unMappedClassModuleDependency_Count.get( unMappedClassModuleDependency ) != null )
						{
							unMappedClassModuleCalls = unMappedClassModuleDependency_Count.get( unMappedClassModuleDependency );
						}
						
						if( moduleUnMappedClassDependency_Count.get( moduleUnMappedClassDependency ) != null )
						{
							moduleUnMappedClassCalls = moduleUnMappedClassDependency_Count.get( moduleUnMappedClassDependency );
						}
						
						classModuleAffinityAbsoluteScore = classModuleAffinityAbsoluteScore + ( unMappedClassModuleCalls + moduleUnMappedClassCalls );
						
						if( moduleDependencyDirection == -1 )
						{
							unMappedClassModuleCalls = unMappedClassModuleCalls * penaltyFactor;
						}
						
						if( inverseModuleDependencyDirection == -1 )
						{
							moduleUnMappedClassCalls = moduleUnMappedClassCalls * penaltyFactor;
						}
						
						classModuleAffinityScore = classModuleAffinityScore + ( unMappedClassModuleCalls + moduleUnMappedClassCalls );
					}
				}
				
				classModuleDependency.put( unMappedClass, module );				
				classModuleDependency_AffinityScores.put( classModuleDependency, classModuleAffinityScore );
				
				if( classModuleAffinityAbsoluteScore == 0.0 )
				{
					noDependencies.add( classModuleDependency );
				}
			}
		}
		
		//if( pageNumber > 1 )
			//rangeShift();
		
		return classModuleDependency_AffinityScores;
	}	
	
	private void rangeShift()
	{	     
		Map<Map<String, String>, Double> rangeShiftScores = new HashMap<>();
		
		double rangeShift = Math.abs( Collections.min( classModuleDependency_AffinityScores.values() ) + 1 );
		
		for( Map<String, String> classModule : classModuleDependency_AffinityScores.keySet() )
		{
			double score = 0.0;
			
			for( Map<String, String> classModuleNoDep : noDependencies )
			{
				if( classModule != classModuleNoDep )
				{
					score = classModuleDependency_AffinityScores.get( classModule ) + rangeShift;
					break;
				}
			}
			
			rangeShiftScores.put( classModule, score );
		}
		
		classModuleDependency_AffinityScores.clear();
		classModuleDependency_AffinityScores.putAll( rangeShiftScores );
	}
	
	private void countTotalDependencyCalls()
	{	
		totalNeighboursCallsCount.clear();
		
		// for each class get the total number of calls to and from that class
		for( String filteredClass : classes )
		{	
			int fromClassCalls = 0;
			int toClassCalls = 0;
			
			for( Map<String, String> classDependencies : classDependency_Count.keySet() )
			{	
				if( classDependencies.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependencies.entrySet().iterator().next().getKey();
					String targetClass = classDependencies.entrySet().iterator().next().getValue();	
					int classCalls = classDependency_Count.get( classDependencies );
					
					// adding calls from a class
					if( sourceClass.toString().equals( filteredClass.toString() ) )
					{
						fromClassCalls = fromClassCalls + classCalls;
					}
					
					// adding calls to a class
					else if( targetClass.toString().equals( filteredClass.toString() ) )
					{
						toClassCalls = toClassCalls + classCalls;
					}
				}
			}
			
			int totalClassCalls = fromClassCalls + toClassCalls;
			totalNeighboursCallsCount.put( filteredClass, totalClassCalls );
		}
	}
	
	private void countMappedDependencyCalls()
	{	
		mappedNeighboursCallsCount.clear();
		
		// for each class get the total number of calls to and from mapped classes
		for( String filteredClass : classes )
		{	
			int fromClassCalls = 0;
			int toClassCalls = 0;
				
			for( Map<String, String> classDependencies : mappedClassDependency_Count.keySet() )
			{	
				if( classDependencies.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependencies.entrySet().iterator().next().getKey();
					String targetClass = classDependencies.entrySet().iterator().next().getValue();
					int classCalls = mappedClassDependency_Count.get( classDependencies );
					
					// adding calls from a class
					if( sourceClass.toString().equals( filteredClass.toString() ) )
					{
						fromClassCalls = fromClassCalls + classCalls;
					}
					
					// adding calls to a class
					else if( targetClass.toString().equals( filteredClass.toString() ) )
					{
						toClassCalls = toClassCalls + classCalls;
					}
				}
			}
			
			int totalClassCalls = fromClassCalls + toClassCalls;
			mappedNeighboursCallsCount.put( filteredClass, totalClassCalls );
		}
	}	

	private void countTotalNeighbours()
	{	
		totalNeighboursCount.clear();
		
		// for each class get the total number of neighbours i.e. classes that have calls to and from the specified class
		for( String filteredClass : classes )
		{	
			int toNeighbours = 0;
			int fromNeighbours = 0;
			
			for( Map<String, String> classDependencies : classDependency_Count.keySet() )
			{	
				if( classDependencies.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependencies.entrySet().iterator().next().getKey();
					String targetClass = classDependencies.entrySet().iterator().next().getValue();	

					// counting neighbours with calls from a class
					if( sourceClass.toString().equals( filteredClass.toString() ) )
					{
						toNeighbours++;
					}
					
					// counting neighbours with calls to a class
					else if( targetClass.toString().equals( filteredClass.toString() ) )
					{
						fromNeighbours++;
					}
				}
			}
			
			int neighbours = toNeighbours + fromNeighbours;
			totalNeighboursCount.put( filteredClass, neighbours );
		}
	}
	
	private void countMappedNeighbours()
	{	
		mappedNeighboursCount.clear();
		
		// for each class get the total number of neighbours that are mapped
		for( String filteredClass : classes )
		{	
			int fromNeighbours = 0;
			int toNeighbours = 0;
				
			for( Map<String, String> classDependencies : mappedClassDependency_Count.keySet() )
			{	
				if( classDependencies.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependencies.entrySet().iterator().next().getKey();
					String targetClass = classDependencies.entrySet().iterator().next().getValue();	
					
					// counting mapped neighbours with calls from a class
					if( sourceClass.toString().equals( filteredClass.toString() ) )
					{
						toNeighbours++;
					}
					
					// counting mapped neighbours with calls to a class
					else if( targetClass.toString().equals( filteredClass.toString() ) )
					{
						fromNeighbours++;
					}
				}
			}
			
			int neighbours = toNeighbours + fromNeighbours;
			mappedNeighboursCount.put( filteredClass, neighbours );
		}
	}
	
	private void countModuleMappedNeighbours()
	{	
		/*mappedNeighboursCount.clear();
		
		// for each class get the total number of neighbours that are mapped
		for( String filteredClass : classes )
		{	
			//int fromNeighbours = 0;
			//int toNeighbours = 0;
			
			for( HashSet<String> moduleClasses : acceptedMappings.values() )
			{
				String module = acceptedMappings.keySet().iterator().next();
				
				for( Map<String, String> classDependencies : mappedClassDependency_Count.keySet() )
				{	
					if( classDependencies.entrySet().iterator().hasNext() )
					{
						String sourceClass = classDependencies.entrySet().iterator().next().getKey();
						String targetClass = classDependencies.entrySet().iterator().next().getValue();	
						
						if( moduleClasses.contains( filteredClass.toString() ) )
						{
							// counting mapped neighbours with calls from a class
							if( sourceClass.toString().equals( filteredClass.toString() ) )
							{
								//toNeighbours++;
							}
							
							// counting mapped neighbours with calls to a class
							else if( targetClass.toString().equals( filteredClass.toString() ) )
							{
								//fromNeighbours++;
							}
						}
					}
				}
				
				int neighbours = toNeighbours + fromNeighbours;
				Map<String, String> mappedModuleNeighbour = new HashMap<>();
				mappedModuleNeighbour.put( filteredClass, module );
				mappedNeighboursModule_Count.put( mappedModuleNeighbour, neighbours );
			}
		}*/
	}
	
	private double getPercentageOfCallsToMappedNeighbours( String candidateClass )
	{
		double callsToMappedNeighbours = ( double ) mappedNeighboursCallsCount.get( candidateClass );
		double callsToAllNeighbours = ( double ) totalNeighboursCallsCount.get( candidateClass );
		double percentageOfCallsToMappedNeighbours = callsToMappedNeighbours / callsToAllNeighbours;
		
		return percentageOfCallsToMappedNeighbours;
	}
	
	private double getPercentageOfMappedNeighbours( String candidateClass )
	{
		double mappedNeighbours = ( double ) mappedNeighboursCount.get( candidateClass );
		double allNeighbours = ( double ) totalNeighboursCount.get( candidateClass );
		double percentageOfMappedNeighbours = mappedNeighbours / allNeighbours;
		
		return percentageOfMappedNeighbours;
	}
	
	public Map<String, Double> getPercentageOfCallsToMappedNeighboursMap()
	{
		countMappedDependencyCalls();		
		
		Map<String, Double> percentageOfCallsToMappedNeighboursMap = new HashMap<>();
		
		for( String unMappedClass : unMappedClasses )
		{	
			percentageOfCallsToMappedNeighboursMap.put( unMappedClass, getPercentageOfCallsToMappedNeighbours( unMappedClass ) );
		}
				
		return percentageOfCallsToMappedNeighboursMap;
	}
	
	public Map<String, Double> getPercentageOfMappedNeighboursMap()
	{
		countMappedNeighbours();
		
		Map<String, Double> percentageOfMappedNeighboursMap = new HashMap<>();
		
		for( String unMappedClass : unMappedClasses )
		{	
			percentageOfMappedNeighboursMap.put( unMappedClass, getPercentageOfMappedNeighbours( unMappedClass ) );
		}
				
		return percentageOfMappedNeighboursMap;
	}

}