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

package recommender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import recommender.da.DependencyAnalysis;
import recommender.ir.IRAnalysis;
import recommender.util.DoubleComparator;

import static java.util.stream.Collectors.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class AutoMap
{
	//final String CONSOLE_GREEN = "\u001B[32m";
	//final String CONSOLE_WHITE = "\u001B[37m";
	//final String CONSOLE_RED = "\u001B[31m";
	boolean runMapping;
	int numberOfAcceptedClasses;
	int numberOfRecommendations;
	int totalNumOfAcceptedClasses;
	int runCount;
	int resultsPerPage;
	int numOfClasses;
	int pageNumber;
	double threshold;
	double thresholdFilter;
	double pageThreshold;
	double penaltyFactor;
	double mnBoostFactor;
	double mdBoostFactor;
	double initialMappingFraction;
	double techniqueWeighting;
	double maxTechniqueWeighting;
	double minTechniqueWeighting;
	String programName;
	String moduleNames [];
	String weightingType;
	String normalizationType;
	String thresholdType;
	String evalutionTimeStamp;
	Map<Map<String, String>, Double> classModuleSimilarity_AffinityScores = new HashMap<>();
	Map<Map<String, String>, Double> classModuleDependency_AffinityScores = new HashMap<>();
	Map<Map<String, String>, Double> normalizedModuleSimilarity_AffinityScores = new HashMap<>();
	Map<Map<String, String>, Double> normalizedModuleDependency_AffinityScores = new HashMap<>();
	Map<Map<String, String>, Double> classModule_AffinityScores = new HashMap<>();
	Map<Map<String, String>, Double> highestClassScoreMappings = new HashMap<>();
	Map<Map<String, String>, Double> recommendedMappings = new HashMap<>();
	Map<String, HashSet<String>> acceptedMappings = new HashMap<>();
	Map<String, HashSet<String>> rejectedMappings = new HashMap<>();
	//Map<String, Double> percentageOfMappedNeighboursMap = new HashMap<>();
	HashSet<String> mappedClasses = new HashSet<String>();
	HashSet<String> unMappedClasses = new HashSet<String>();
	public ArrayList<Map<String, String>> runResults = new ArrayList<>();
	Map<String, HashSet<String>> classMethodsMap;
	IRAnalysis irAnalysis;
	Program program;
	boolean searchOnly;
	boolean runComplete;
	long runStartTime;

	DependencyAnalysis depAnalysis;
	boolean initalMapping;
	int numOfInitialMappedClassesSet;
	int numOfInitialMappedClasses;
	
	public AutoMap( Program program, int runCount, String weightingType, String normalizationType, String thresholdType, double maxTechniqueWeighting, 
			double minTechniqueWeighting, double threshold, double penaltyFactor, double mnBoostFactor, double mdBoostFactor, double initialMappingFraction,
			int resultsPerPage, String evalutionTimeStamp, boolean createIndex )
	{	
		this.weightingType = weightingType;
		this.thresholdType = thresholdType;
		this.resultsPerPage = resultsPerPage;
		this.mnBoostFactor = mnBoostFactor;
		this.mdBoostFactor = mdBoostFactor;

		this.normalizationType = normalizationType;
		this.threshold = threshold;
		this.thresholdFilter = threshold;
		
		this.maxTechniqueWeighting = maxTechniqueWeighting;
		this.minTechniqueWeighting = minTechniqueWeighting;
		this.penaltyFactor = penaltyFactor;
		this.initialMappingFraction = initialMappingFraction;
		
		this.runCount = runCount;
		this.program = program;												
		this.programName = program.getProgramName();	
		this.moduleNames = program.getModuleNames();
		this.irAnalysis = new IRAnalysis( program, runCount, createIndex ); 
		this.depAnalysis = new DependencyAnalysis( program, irAnalysis );	
		this.numOfClasses = depAnalysis.numOfClasses();					
		//this.numOfClasses = irAnalysis.numOfClasses();
		this.irAnalysis.setNumOfRequestedDocs( numOfClasses );				
		this.classMethodsMap = depAnalysis.getFliteredClassMethods();		
		this.irAnalysis.setClassMethods( classMethodsMap );
		this.evalutionTimeStamp = evalutionTimeStamp;
		
		flushCollections();
		
		/*Map<String, ArrayList<String>> programMap = program.getProgramMap();
		
		for( int i = 1; i <= numOfIntialMappedClasses; i++ )
		{
			ArrayList<String> moduleList = new ArrayList<String>( programMap.keySet() );
			Random rM = new Random();		
			String randomModule = moduleList.get( rM.nextInt( moduleList.size() ) );
			
			ArrayList<String> classList = new ArrayList<String>( programMap.get( randomModule ) );
			Random rC = new Random();		
			String randomClass = classList.get( rC.nextInt( classList.size() ) );
			
			HashSet<String> moduleClasses = new HashSet<String>();
			
			if( acceptedMappings.get( randomModule ) != null )
			{
				moduleClasses = acceptedMappings.get( randomModule );
				moduleClasses.add( randomClass );
			}
			else
				moduleClasses.add( randomClass );
			
			acceptedMappings.put( randomModule, moduleClasses );
		}*/
	}
	
	private void flushCollections()
	{
		mappedClasses.clear();
		unMappedClasses.clear();
		acceptedMappings.clear();
		rejectedMappings.clear();
		//percentageOfMappedNeighboursMap.clear();
		runResults.clear();
	}
	
	public ArrayList<Map<String, String>> call() 
	{
		// If we need to have a set of classes pre-mapped before algorithm begins. This is needed especially for 
		// use by a class-dependency only technique
		
		/*BigDecimal roundedNumOfInitialMappedClasses = new BigDecimal( initialMappingFraction * numOfClasses );
		roundedNumOfInitialMappedClasses = roundedNumOfInitialMappedClasses.setScale( 0, RoundingMode.HALF_UP );
		numOfInitialMappedClassesSet = roundedNumOfInitialMappedClasses.intValue();
		runMapping = true;
		initalMapping = true;
		double thresholdTemp = threshold;
		threshold = 0.00;
		thresholdFilter = 0.00;
		
		while( mappedClasses.size() < numOfInitialMappedClassesSet )
		{	
			getSimilarityAffinityScores();
			getDependencyAffinityScores();
			
			if( classModuleSimilarity_AffinityScores.size() != classModuleDependency_AffinityScores.size() )
			{
				System.out.println( "\n *** CLASS COUNT MISMATCH : Textual Search Analysis Recommendations ( " + classModuleSimilarity_AffinityScores.size() + 
						" ) are not equal to Call-Dependency Analysis Recommendations ( " + classModuleDependency_AffinityScores.size() + " )\n" );
				break;
			}
	
			removeRejectedMappings();
		    normalizeAffinityScores();
			calculateClassModuleAffinityScores();
			sortMappings();
			produceRecommendedMappings();
			
			if( !runMapping )
				break;
			
			acceptRejectMappings( numOfClasses );
		}
		
		initalMapping = false;
		numOfInitialMappedClasses = mappedClasses.size();
		rejectedMappings.clear();
		threshold = thresholdTemp;
		thresholdFilter = thresholdTemp; */
		
		// Algorithm begins here
		
		runStartTime = System.currentTimeMillis();
		BigDecimal runRoundedTW = new BigDecimal( Double.toString( maxTechniqueWeighting ) );
		BigDecimal runRoundedDW = new BigDecimal( 1 - maxTechniqueWeighting );
		BigDecimal runRoundedThreshold = new BigDecimal( thresholdFilter );
		BigDecimal runRoundedrunInitialMappingFraction = new BigDecimal( initialMappingFraction );
		runRoundedTW = runRoundedTW.setScale( 2, RoundingMode.HALF_UP );
		runRoundedDW = runRoundedDW.setScale( 2, RoundingMode.HALF_UP );
		runRoundedThreshold = runRoundedThreshold.setScale( 2, RoundingMode.HALF_UP );
		runRoundedrunInitialMappingFraction = runRoundedrunInitialMappingFraction.setScale( 2, RoundingMode.HALF_UP );
		
		System.out.println( "\n RUN CONFIG: " + runCount );
		System.out.println( " ---------------" );
		System.out.println( " Program: " + programName );
		System.out.println( " Weighting Type: " + weightingType );
		System.out.println( " Threshold Type: " + thresholdType );
		System.out.println( " Results per Page: " + resultsPerPage );
		System.out.println( " Module Name Boost Factor: " + mnBoostFactor );
		System.out.println( " Module Description Boost Factor: " + mdBoostFactor );
		System.out.println( " Threshold: " + runRoundedThreshold );
		System.out.println( " Normalization Type: " + normalizationType );
		
		//System.out.println( " Search Technique Weighting: " + runRoundedTW );
		//System.out.println( " DA Technique Weighting: " + runRoundedDW );
		//System.out.println( " Penalty Factor: " + penaltyFactor );
		//System.out.println( " Initial Mapping Fraction: " + runRoundedrunInitialMappingFraction );
		
	    this.pageNumber = 1;
	    this.runMapping = true;
	    this.searchOnly = true;
	    this.runComplete = false;
	    totalNumOfAcceptedClasses = 0;
	 	
		while( runMapping )
		{	
			System.out.print( pageNumber + "." );
			//System.out.println( "------\n...Calculating Similarity Affinity Scores" );
			getSimilarityAffinityScores();
			
			//System.out.println( "...Calculating Dependency Affinity Scores" );
			// TODO: for Dependency Analysis
			//getDependencyAffinityScores();
			
			//t.join();
			
			// TODO: for Dependency Analysis
			/*if( classModuleSimilarity_AffinityScores.size() != classModuleDependency_AffinityScores.size() )
			{
				System.out.println( "\n *** CLASS COUNT MISMATCH : Textual Search Analysis Recommendations ( " + classModuleSimilarity_AffinityScores.size() + 
						" ) are not equal to Call-Dependency Analysis Recommendations ( " + classModuleDependency_AffinityScores.size() + " )\n" );
				break;
			}*/
			
			//System.out.println( "...Filtering Scores" );
			removeRejectedMappings();
			
			//System.out.println( "...Normalizing Scores" );
			// TODO: for Dependency Analysis
		    //normalizeAffinityScores();
		    
		    //System.out.println( "...Computing Class-Module Affinity Scores " );
			// TODO: for Dependency Analysis
			//calculateClassModuleAffinityScores();
			
			//System.out.println( "...Picking Highest Score of a Class for each Class Module Pair" );
			pickHighestClassScore();
			
			//System.out.println( "...Generating Recommendations" );
			produceRecommendedMappings( resultsPerPage );
			
			if( !runMapping )
				break;
			
			//System.out.println( "...Checking Recommendations Against Intended Architecture" );
			acceptRejectMappings();
			
			//System.out.println( "...Printing Results" );
			printAffinityScores( resultsPerPage );
			
			pageNumber++;
		}
		
		generateRunResultsJSONData();
		generateGraphData();
		generateRunResultsWebPage( runResults.get( runResults.size() - 1 ) );
		endProgram();
		
		return runResults;
	}
	
	private int numOfRejectedMappings()
	{
		int numOfRejectedMappings = 0;
		
		for( HashSet<String> rejectedMappings : rejectedMappings.values() )
		{
			numOfRejectedMappings = numOfRejectedMappings + rejectedMappings.size();
		}
		
		return numOfRejectedMappings;
	}
	
	private void getSimilarityAffinityScores()
	{
		// Search the source code for module keyword-terms taken from the module name and informal descriptions of the software architecture
		// and retrieve similarity affinity scores for each class in relation to each given module.	
		classModuleSimilarity_AffinityScores.clear();
		classModuleSimilarity_AffinityScores.putAll( irAnalysis.getSimilarityAffinityScores( acceptedMappings, mnBoostFactor, mdBoostFactor ) );
	}
	
	private void getDependencyAffinityScores()
	{
		// Calculate dependency affinity scores of a class to a module based on class dependencies between mapped and unmapped classes taking 
		// module dependency architectural violations as provided
		classModuleDependency_AffinityScores.clear();
		classModuleDependency_AffinityScores.putAll( depAnalysis.calculateDependencyAffinityScores( acceptedMappings, penaltyFactor ) );
	}
	
	private void removeRejectedMappings()
	{	
		// remove rejected mappings
		for( int i = 0; i < moduleNames.length; i++ )
		{
			String module = moduleNames[ i ];
			HashSet<String> rejectedClasses = rejectedMappings.get( module );
			
			if( rejectedClasses != null )
			{
				for( String rejectedClass : rejectedClasses )
				{
					Map<String, String> rejectedClassModuleMapping = new HashMap<>();
					rejectedClassModuleMapping.put( rejectedClass, module );
					classModuleSimilarity_AffinityScores.remove( rejectedClassModuleMapping );
					classModuleDependency_AffinityScores.remove( rejectedClassModuleMapping );
				}	
			}
		}
		
		// remove classes with low number of mapped neighbours
		/*if( pageNumber > 1 )
		{
			for( int i = 0; i < moduleNames.length; i++ )
			{
				String module = moduleNames[ i ];
				int numOfMappedClasses = mappedClasses.size();
				double percentageOfMappedClasses =  ( ( double ) numOfMappedClasses / ( double ) numOfClasses ) ;
				//Map<String, Double> percentageOfCallsToMappedNeighboursMap = depAnalysis.getPercentageOfCallsToMappedNeighboursMap();
				Map<String, Double> percentageOfMappedNeighboursMap = depAnalysis.getPercentageOfMappedNeighboursMap();
				
				for( String lowMappedCallsClass : percentageOfMappedNeighboursMap.keySet() )
				{
					double percentOfCallsToMappedNeighbours = percentageOfMappedNeighboursMap.get( lowMappedCallsClass );
					
					if( percentOfCallsToMappedNeighbours < ( thresholdFilter * percentageOfMappedClasses ) ) 
					{
						Map<String, String> lowMappedCallsClassModule = new HashMap<>();
						lowMappedCallsClassModule.put( lowMappedCallsClass, module );
						classModuleSimilarity_AffinityScores.remove( lowMappedCallsClassModule );
						classModuleDependency_AffinityScores.remove( lowMappedCallsClassModule );
					}
				}
			}
		}*/
	}
	
	private void normalizeAffinityScores()
	{
		normalizedModuleSimilarity_AffinityScores.clear();
		normalizedModuleDependency_AffinityScores.clear();
		
		if( normalizationType.equals( "None" ) )
		{
			normalizedModuleSimilarity_AffinityScores.putAll( classModuleSimilarity_AffinityScores );
			normalizedModuleDependency_AffinityScores.putAll( classModuleDependency_AffinityScores );
		}
		else
		{
			double maxSimilarityAffinityScore = ( double ) 0.00;
			double minSimilarityAffinityScore = ( double ) 0.00;
			double sumSimilarityAffinityScore = ( double ) 0.00;
			
			for( Map<String, String> classModule : classModuleSimilarity_AffinityScores.keySet() )
			{
				double similarityAffinityScore = classModuleSimilarity_AffinityScores.get( classModule );
				sumSimilarityAffinityScore = sumSimilarityAffinityScore + similarityAffinityScore;
				
				if( similarityAffinityScore > maxSimilarityAffinityScore )
				{
					maxSimilarityAffinityScore = similarityAffinityScore;
				}
				
				if( similarityAffinityScore < minSimilarityAffinityScore )
				{
					minSimilarityAffinityScore = similarityAffinityScore;
				}
			}
				
			int numOfClasses = ( classModuleSimilarity_AffinityScores.size() );
			double avgSimilarityAffinityScore = sumSimilarityAffinityScore / numOfClasses;
			double normalizedSimilarityAffinityScore = ( double ) 0.00;
			double similarityNormalizationFactor = ( double ) 0.00;
			double similarityStandardDeviation = ( double ) 0.00;
			
			if( normalizationType.equals( "Min Max" ) || normalizationType.equals( "Bounded" ) )
			{
				similarityNormalizationFactor = minSimilarityAffinityScore;
			}
			else if( normalizationType.equals( "Mean" ) )
			{
				similarityNormalizationFactor = avgSimilarityAffinityScore;
			}
			else if( normalizationType.equals( "Standardization" ) )
			{
				Map<Map<String, String>, Double> similaritySquaredDeviations = new HashMap<>(); 
				
				for( Map<String, String> classModule : classModuleSimilarity_AffinityScores.keySet() )
				{
					double similarityAffinityScore = classModuleSimilarity_AffinityScores.get( classModule );
					double deviation = similarityAffinityScore - avgSimilarityAffinityScore;
					double squaredDeviation = deviation * deviation;
					similaritySquaredDeviations.put( classModule, squaredDeviation );
				}
				
				double sumSimilaritySquaredDeviation = ( double ) 0.00;
				
				for( double similaritySquaredDeviation : similaritySquaredDeviations.values() )
				{
					sumSimilaritySquaredDeviation = sumSimilaritySquaredDeviation + similaritySquaredDeviation;
				}
				
				double similarityDeviationUnrooted = sumSimilaritySquaredDeviation / ( numOfClasses - 1 );
				similarityStandardDeviation = Math.sqrt( similarityDeviationUnrooted );
			}
			
			for( Map<String, String> classModule : classModuleSimilarity_AffinityScores.keySet() )
			{
				double similarityAffinityScore = classModuleSimilarity_AffinityScores.get( classModule );
				
				if( maxSimilarityAffinityScore - minSimilarityAffinityScore == 0 )
				{
					normalizedSimilarityAffinityScore = ( double ) 0.00;
				}
				else
				{
					if( normalizationType.equals( "Bounded" ) )
					{
						double lowerBound = -1.00;
						double upperBound = 1.00;
								
						normalizedSimilarityAffinityScore = lowerBound + ( 
							( ( similarityAffinityScore - similarityNormalizationFactor ) * ( upperBound - lowerBound ) )
							/ ( maxSimilarityAffinityScore - minSimilarityAffinityScore ) );
					}
					else if( normalizationType.equals( "Standardization" ) )
					{
						normalizedSimilarityAffinityScore = ( similarityAffinityScore - avgSimilarityAffinityScore ) / similarityStandardDeviation ;
					}
					else if( normalizationType.equals( "Equal Unity" ) )
					{
						normalizedSimilarityAffinityScore = similarityAffinityScore / maxSimilarityAffinityScore; 
					}
					else if( normalizationType.equals( "Division Total" ) )
					{
						normalizedSimilarityAffinityScore = similarityAffinityScore / sumSimilarityAffinityScore; 
					}
					else
						normalizedSimilarityAffinityScore = ( similarityAffinityScore - similarityNormalizationFactor ) / 
							( maxSimilarityAffinityScore - minSimilarityAffinityScore );
				}
				
				normalizedModuleSimilarity_AffinityScores.put( classModule, normalizedSimilarityAffinityScore );
			}
			
			double maxDependencyAffinityScore = ( double ) 0.00;
			double minDependencyAffinityScore= ( double ) 0.00;
			double sumDependencyAffinityScore = ( double ) 0.00;
			
			for( Map<String, String> classModule : classModuleDependency_AffinityScores.keySet() )
			{
				double dependencyAffinityScore = classModuleDependency_AffinityScores.get( classModule );
				sumDependencyAffinityScore = ( double ) sumDependencyAffinityScore + dependencyAffinityScore;
				
				if( dependencyAffinityScore > maxDependencyAffinityScore )
				{
					maxDependencyAffinityScore = ( double ) dependencyAffinityScore;
				}
				
				if( dependencyAffinityScore < minDependencyAffinityScore )
				{
					minDependencyAffinityScore = ( double ) dependencyAffinityScore;
				}
			}
			
			double avgDependencyAffinityScore = sumDependencyAffinityScore / numOfClasses;
			double normalizedDependencyAffinityScore = ( double ) 0.00;
			double dependencyNormilazationFactor = ( double ) 0.00;
			double dependencyStandardDeviation = ( double ) 0.00;
			
			if( normalizationType.equals( "Min Max" ) || normalizationType.equals( "Bounded" ) )
			{
				dependencyNormilazationFactor = minDependencyAffinityScore;
			}
			else if( normalizationType.equals( "Mean" ) )
			{
				dependencyNormilazationFactor = avgDependencyAffinityScore;
			}
			else if( normalizationType.equals( "Standardization" ) )
			{
				Map<Map<String, String>, Double> dependencySquaredDeviations = new HashMap<>(); 
				
				for( Map<String, String> classModule : classModuleDependency_AffinityScores.keySet() )
				{
					double dependencyAffinityScore = classModuleDependency_AffinityScores.get( classModule );
					double deviation = dependencyAffinityScore - avgDependencyAffinityScore;
					double squaredDeviation = deviation * deviation;
					dependencySquaredDeviations.put( classModule, squaredDeviation );
				}
				
				double sumDependencySquaredDeviation = ( double ) 0.00;
				
				for( double dependencySquaredDeviation : dependencySquaredDeviations.values() )
				{
					sumDependencySquaredDeviation = sumDependencySquaredDeviation + dependencySquaredDeviation;
				}
				
				double dependencyDeviationUnrooted = sumDependencySquaredDeviation / ( numOfClasses - 1 );
				dependencyStandardDeviation = Math.sqrt( dependencyDeviationUnrooted );
			}
			
			for( Map<String, String> classModule : classModuleDependency_AffinityScores.keySet() )
			{
				double dependencyAffinityScore = ( double ) classModuleDependency_AffinityScores.get( classModule );
				
				if( maxDependencyAffinityScore - minDependencyAffinityScore == 0 )
				{
					normalizedDependencyAffinityScore = ( double ) 0.00;
				}
				else
				{
					if( normalizationType.equals( "Bounded" ) )
					{
						double lowerBound = -1.00;
						double upperBound = 1.00;
								
						normalizedDependencyAffinityScore = lowerBound + ( 
							( ( dependencyAffinityScore - dependencyNormilazationFactor ) * ( upperBound - lowerBound ) )
							/ ( maxDependencyAffinityScore - minDependencyAffinityScore ) );
					}
					else if( normalizationType.equals( "Standardization" ) )
					{
						normalizedDependencyAffinityScore = ( dependencyAffinityScore - avgDependencyAffinityScore ) / dependencyStandardDeviation ;
					}
					else if( normalizationType.equals( "Equal Unity" ) )
					{
						normalizedDependencyAffinityScore = dependencyAffinityScore / maxDependencyAffinityScore; 
					}
					else if( normalizationType.equals( "Division Total" ) )
					{
						normalizedDependencyAffinityScore = dependencyAffinityScore / sumDependencyAffinityScore; 
					}
					else
						normalizedDependencyAffinityScore = ( dependencyAffinityScore - dependencyNormilazationFactor ) / 
							( maxDependencyAffinityScore - minDependencyAffinityScore );
				}
				
				normalizedModuleDependency_AffinityScores.put( classModule, normalizedDependencyAffinityScore );
			}
		}
	}
	
	private void calculateClassModuleAffinityScores()
	{	
		classModule_AffinityScores.clear(); 
		
		double ogTW = techniqueWeighting;
		techniqueWeighting = 0.0f;
		int numOfMappedClasses = mappedClasses.size();
		int numOfUnMappedClasses = numOfClasses - numOfMappedClasses;
		double percentageOfUnMappedClasses =  ( ( double ) numOfUnMappedClasses / ( double ) numOfClasses ) ;
		Map<String, Double> percentageOfCallsToMappedNeighboursMap = depAnalysis.getPercentageOfCallsToMappedNeighboursMap();
		Map<String, Double> percentageOfMappedNeighboursMap = depAnalysis.getPercentageOfMappedNeighboursMap();
		
		// match recommendations and compute score and store
		for( Map<String, String> classModuleSimilarity : normalizedModuleSimilarity_AffinityScores.keySet() )
		{
			double classModuleSimilarityAffinityScore = ( double ) normalizedModuleSimilarity_AffinityScores. get( classModuleSimilarity );
			
			if( normalizedModuleSimilarity_AffinityScores.entrySet().iterator().hasNext() )
			{
				for( Map<String, String> classModuleDependency : normalizedModuleDependency_AffinityScores.keySet() )
				{
					double classModuleDependencyAffinityScore = normalizedModuleDependency_AffinityScores.get( classModuleDependency );
					
					if( classModuleSimilarity.toString().equals( classModuleDependency.toString() ) )
					{
						Map<String, String> classModuleAffinity = classModuleSimilarity;
						String unMappedClass = classModuleSimilarity.entrySet().iterator().next().getKey();
						
						if( weightingType.equals( "% of Unmapped Classes" ) )
						{
							//techniqueWeighting = percentageOfUnMappedClasses;
							techniqueWeighting = minTechniqueWeighting + ( percentageOfUnMappedClasses * ( maxTechniqueWeighting - minTechniqueWeighting ) );
						}
						else if( weightingType.equals( "Search Only" ) )
						{
							techniqueWeighting = 1;
						}
						else if( weightingType.equals( "Dependency Only" ) )
						{
							techniqueWeighting = 0;
						}
						else if( weightingType.equals( "Search Once Then Dependency Only" ) )
						{
							if( pageNumber < 2 )
							{
								techniqueWeighting = 1;
							}
							else
							{
								techniqueWeighting = 0;
							}
						}
						else if( weightingType.equals( "Search Once Then High Dependency + Low Search" ) )
						{
							if( pageNumber < 2 )
							{
								techniqueWeighting = 1;
							}
							else
							{
								//techniqueWeighting = 1 - percentageOfUnMappedClasses;
								techniqueWeighting = minTechniqueWeighting + ( ( 1 - percentageOfUnMappedClasses ) * ( maxTechniqueWeighting - minTechniqueWeighting ) );
							}
						}
						else if( weightingType.equals( "Search Only Then Dependency Only" ) )
						{
							if( searchOnly )
							{
								techniqueWeighting = 1;
							}
							else
							{
								techniqueWeighting = 0;
							}
						}
						else if( weightingType.equals( "Search Only Then High Dependency + Low Search" ) )
						{
							if( searchOnly )
							{
								techniqueWeighting = 1;
							}
							else
							{
								//techniqueWeighting = 1 - percentageOfUnMappedClasses;
								techniqueWeighting = minTechniqueWeighting + ( ( 1 - percentageOfUnMappedClasses ) * ( maxTechniqueWeighting - minTechniqueWeighting ) );
							}
						}
						else if( weightingType.equals( "Search Only Then % of Unmapped Classes" ) )
						{
							if( searchOnly )
							{
								techniqueWeighting = 1.0;
							}
							else
							{
								//techniqueWeighting = percentageOfUnMappedClasses;
								techniqueWeighting = minTechniqueWeighting + ( percentageOfUnMappedClasses * ( maxTechniqueWeighting - minTechniqueWeighting ) );
							}
						}
						else if( weightingType.equals( "Equal Weighting" ) )
						{	
							//double classModuleAffinityScore = classModuleSimilarityAffinityScore + classModuleDependencyAffinityScore;
							double classModuleAffinityScore;
							
							if( pageNumber == 1 )
								classModuleAffinityScore = classModuleSimilarityAffinityScore;
							else
								classModuleAffinityScore = classModuleSimilarityAffinityScore * classModuleDependencyAffinityScore;
							
							classModule_AffinityScores.put( classModuleAffinity, classModuleAffinityScore );
						}
						else if( weightingType.equals( "% of Calls To Mapped Neighbours" ) )
						{
							techniqueWeighting = percentageOfCallsToMappedNeighboursMap.get( unMappedClass );
							
							//double classModuleAffinityScore = ( ( 1 - techniqueWeighting ) * classModuleSimilarityAffinityScore ) + 	
								//	( techniqueWeighting * classModuleDependencyAffinityScore );
							
							double classModuleAffinityScore = Math.pow( classModuleSimilarityAffinityScore, ( 1 - techniqueWeighting ) ) * 
									Math.pow( classModuleDependencyAffinityScore,  techniqueWeighting );
							
							classModule_AffinityScores.put( classModuleAffinity, classModuleAffinityScore );
						}
						else if( weightingType.equals( "% of Mapped Neighbours" ) )
						{
							techniqueWeighting = percentageOfMappedNeighboursMap.get( unMappedClass );
							
							//double classModuleAffinityScore = ( ( 1 - techniqueWeighting ) * classModuleSimilarityAffinityScore ) + 	
								//	( techniqueWeighting * classModuleDependencyAffinityScore );
							
							double classModuleAffinityScore = Math.pow( classModuleSimilarityAffinityScore, ( 1 - techniqueWeighting ) ) * 
									Math.pow( classModuleDependencyAffinityScore,  techniqueWeighting );
							
							classModule_AffinityScores.put( classModuleAffinity, classModuleAffinityScore );
						}
						
						
						if( !weightingType.contains( "Equal Weighting" ) )
						{
							if( !weightingType.contains( "Mapped Neighbours" ) )
							{
								if( initalMapping )
								{
									techniqueWeighting = 1;			// Apply search technique only to get the inital mapping
								}
								
								//double classModuleAffinityScore = ( techniqueWeighting * classModuleSimilarityAffinityScore ) + 	
									//	( ( 1 - techniqueWeighting ) * ( classModuleDependencyAffinityScore ) );
								
								double classModuleAffinityScore = Math.pow( classModuleSimilarityAffinityScore, techniqueWeighting ) * 
										Math.pow( classModuleDependencyAffinityScore,  ( 1 - techniqueWeighting ) );
								
								classModule_AffinityScores.put( classModuleAffinity, classModuleAffinityScore );
							}
						}
						
						techniqueWeighting = ogTW;
					}
				}
			}
		}
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
		ArrayList<String> filteredClasses = irAnalysis.getFilteredClasses();
		unMappedClasses.clear();
		unMappedClasses.addAll( filteredClasses );
		
		for( String mappedClass : mappedClasses )
		{
			unMappedClasses.remove( mappedClass );
		}
	}
	
	private void pickHighestClassScore() 
	{	
		// TODO: remove for dependency analysis
		classModule_AffinityScores.clear();
		classModule_AffinityScores.putAll( classModuleSimilarity_AffinityScores );
		
		// get the highest affinity score of a class to a module and store it, discard all lower scores
		highestClassScoreMappings.clear();
		retrieveUnMappedClasses();
		
		Map<Map<String, String>, Double> similarScoreMappings = new HashMap<>();
		
		for( String unMappedClass : unMappedClasses )
		{	
			Map<String, String> oldMap = new HashMap<>();
			// TODO: Figure out oldhighscore value to use for dependency analysis...an extreme negative score?
			double oldHighScore = -1.0f;
			
			for( int i = 0; i < moduleNames.length; i++ )
			{
				String module = moduleNames[ i ];
				Map<String, String> newMap = new HashMap<>();
				newMap.put( unMappedClass, module );
				
				if( classModule_AffinityScores.get( newMap ) != null )
				{
					double newHighScore = classModule_AffinityScores.get( newMap );
					
					// store mapping with similar score
					if( newHighScore == oldHighScore )
					{
						similarScoreMappings.put( oldMap, oldHighScore );
					}
					else if( newHighScore > oldHighScore )
					{
						highestClassScoreMappings.remove( oldMap, oldHighScore );
						highestClassScoreMappings.put( newMap, newHighScore );
						oldMap.putAll( newMap );
						oldHighScore = newHighScore;
					}
				}	
			}
		}
		
		// remove mappings with similar scores
		for( Map<String, String> map : similarScoreMappings.keySet() )
		{
			highestClassScoreMappings.remove( map );
		}
	}
	
	private void produceRecommendedMappings( int maxResultsPerPage ) 
	{	
		double sumAffinityScore = ( double ) 0.00;
		
		for( double affinityScore : highestClassScoreMappings.values() )
		{
			sumAffinityScore = sumAffinityScore + affinityScore;
		}
		
		if( thresholdType.equals( "Recommendation Average" ) )
		{
			if( highestClassScoreMappings.size() > 0 )
				threshold = sumAffinityScore / highestClassScoreMappings.size();
			else 
				threshold = 0;
			
			pageThreshold = threshold;
		}
		
		// get affinity scores above given threshold
		recommendedMappings.clear();
		recommendedMappings.putAll( highestClassScoreMappings );
		
		for( Map<String, String> mapping : highestClassScoreMappings.keySet() )
		{	
			double score = highestClassScoreMappings.get( mapping );
			
			if( score < threshold ) // if( score < ( threshold * ( 1.0 + pageThreshold ) ) )
			{
				recommendedMappings.remove( mapping );
			}
		}
		
		if( recommendedMappings.isEmpty() )
		{
			if( weightingType.contains( "Search Only Then" ) && !runComplete )
			{
				searchOnly = false;
				runComplete = true;
			}
			else
				runMapping = false;
		}
		
		// sort the recommended mappings in descending order before first selection
		recommendedMappings = recommendedMappings.entrySet()
			.stream()
	        .sorted( Collections.reverseOrder( Map.Entry.comparingByValue( new DoubleComparator() ) ) )
	        .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2 ) -> e2, LinkedHashMap::new ) );
		
		int count = 1;
		Map<Map<String, String>, Double> recommendedMappingsTemp = new HashMap<>();
		
		for( Map<String, String> classModule : recommendedMappings.keySet() ) 
		{
			double affnityScore = recommendedMappings.get( classModule );
			recommendedMappingsTemp.put( classModule, affnityScore );
			
			if( count >= maxResultsPerPage )
				break;
			else
				count++;
		}
		
		recommendedMappings.clear();
		recommendedMappings.putAll( recommendedMappingsTemp );
		//System.out.println( "recommendedMappings: " + recommendedMappings );
	}
	
	private void acceptRejectMappings()
	{
		Map<String, ArrayList<String>> programMap = program.getProgramMap(); 
		
		/*if( recommendedMappings.size() < maxResultsPerPage )
		{
			maxResultsPerPage = recommendedMappings.size();
		}*/
		
		numberOfAcceptedClasses = 0;
		numberOfRecommendations = 0;
		
		for( Map<String, String> classModule : recommendedMappings.keySet() ) 
		{ 
			String unMappedClass = classModule.entrySet().iterator().next().getKey();
			String recommendedModule = classModule.entrySet().iterator().next().getValue();
			double affnityScore = recommendedMappings.get( classModule );
			ArrayList<String> moduleMap = programMap.get( recommendedModule );
			HashSet<String> acceptedClasses = new HashSet<String>();
			HashSet<String> rejectedClasses = new HashSet<String>();
				
			if( affnityScore >= threshold )
			{
				boolean mapped = false;
				
				for( String validClass : moduleMap )
				{
					if( initalMapping )
					{
						if( mappedClasses.size() == numOfInitialMappedClassesSet )
						{
							break;
						}
					}
					
					if( unMappedClass.toString().equals( validClass.toString() ) )
				    {	
				    	if( acceptedMappings.get( recommendedModule ) != null )
				    	{
				    		acceptedClasses = acceptedMappings.get( recommendedModule );
				    	}
				    	
				    	acceptedClasses.add( unMappedClass );
				    	acceptedMappings.put( recommendedModule, acceptedClasses );
				    	mappedClasses.add( unMappedClass );
				    	mapped = true;
				    	numberOfAcceptedClasses++;
				    }
				}
				
				if( mapped == false )
			    {	  
			    	if( rejectedMappings.get( recommendedModule ) != null )
			    	{
			    		rejectedClasses = rejectedMappings.get( recommendedModule );
			    	}
			    	
			    	rejectedClasses.add( unMappedClass );
			    	rejectedMappings.put( recommendedModule, rejectedClasses );
			    }
			}
			else
			{
				break;
			}
			
			numberOfRecommendations++;
			
			/*if( numberOfRecommendations == maxResultsPerPage )
			{
		    	break;
			}*/
		}
		
		totalNumOfAcceptedClasses = totalNumOfAcceptedClasses + numberOfAcceptedClasses;
	}
	
	private void printAffinityScores( int maxResultsPerPage )
	{	
		int numOfRecommendations = recommendedMappings.size();
		int numOfResults = 0;
		int resultsPerPage = 0;
		
		if( numOfRecommendations < maxResultsPerPage )
		{
			resultsPerPage = numOfRecommendations;
		}
		else
		{
			resultsPerPage = maxResultsPerPage;
		}
		
		for( Map<String, String> classModule : recommendedMappings.keySet() )
		{
			double affinityScore = recommendedMappings.get( classModule );
		    
		    if( affinityScore >= threshold )
		    {
		    	if( numOfResults == resultsPerPage )
				{
			    	break;
				}
				
		    	numOfResults++;
		    }
		    else
			{
				break;
			}
		}
		
		if( numOfResults >= 1 )
		{
			//System.out.println( "\n\n - Page Recommendations: " + numOfResults + " out of " + numOfRecommendations + " Recommendations" );
			//System.out.println( " - System Settings:  Similarity Affinity Weighting  => " + irTechniqueWeighting  + "  |  Dependency Affinity Weighting => " + ( 1 - irTechniqueWeighting) );
			//System.out.println( " - User Settings:  Max Results/Page => " + maxResultsPerPage + "  |  Results Threshold => " + thresholdFilter );
			//System.out.println( " - Correct Recommendations (for Page) => " + numberOfAcceptedClasses + " out of " + numberOfRecommendations + "  |  Precision => " + roundedPrecision );
			//System.out.println( " - Correct Recommendations (for System) => " + numberOfAcceptedClasses + " out of " + numOfClasses + "  |  Recall => " + roundedRecall );
			//System.out.println( " - Number of Unmapped Classes => " + ( numOfClasses - numOfMappedClasses ) + "  |  Total Number of Rejected Mapping Recommendations => " + numOfRejectedMappings() );
			//System.out.println( " - Mapping Coverage => " + numOfMappedClasses + " out of " + numOfClasses + "  |  Mapping Coverage Percentage => " + roundedPercent + "%" );
			//System.out.println( "\n-------------- ");
			
			int numOfMappedClasses = mappedClasses.size();
			double precision = ( double ) numberOfAcceptedClasses / ( double ) numberOfRecommendations;
			double recall = ( double ) numberOfAcceptedClasses / ( double ) numOfClasses;
			double percentageOfMappedClasses = ( double ) numOfMappedClasses / ( double ) numOfClasses * 100.00f;
			
			BigDecimal roundedPrecision = new BigDecimal( Double.toString( precision ) );		
			BigDecimal roundedRecall = new BigDecimal( Double.toString( recall ) );
			BigDecimal roundedPercentOfMappedClasses = new BigDecimal( Double.toString( percentageOfMappedClasses ) );
			BigDecimal roundedIRTechniqueWeighting = new BigDecimal( Double.toString( techniqueWeighting ) );
			BigDecimal roundedDependencyTechniqueWeighting = new BigDecimal( Double.toString( 1 - techniqueWeighting ) );
			BigDecimal roundedThreshold = new BigDecimal( Double.toString( thresholdFilter ) );
			roundedPrecision = roundedPrecision.setScale( 2, RoundingMode.HALF_UP );
			roundedRecall = roundedRecall.setScale( 2, RoundingMode.HALF_UP );
			roundedPercentOfMappedClasses = roundedPercentOfMappedClasses.setScale( 2, RoundingMode.HALF_UP );
			roundedIRTechniqueWeighting = roundedIRTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
			roundedDependencyTechniqueWeighting = roundedDependencyTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
			roundedThreshold = roundedThreshold.setScale( 2, RoundingMode.HALF_UP );
			
			Map<String, String> pageResults = new HashMap<>();
			pageResults.put( "pageNumber", Integer.toString( pageNumber ) );
			pageResults.put( "irTechniqueWeighting", roundedIRTechniqueWeighting.toString() ); 
			pageResults.put( "dependencyTechniqueWeighting", roundedDependencyTechniqueWeighting.toString() );
			pageResults.put( "numberOfAcceptedClasses", Integer.toString( numberOfAcceptedClasses ) );
			pageResults.put( "totalNumOfAcceptedClasses", Integer.toString( totalNumOfAcceptedClasses ) );
			pageResults.put( "precision", roundedPrecision.toString() );
			pageResults.put( "numOfMappedClasses", Integer.toString( numOfMappedClasses ) );
			pageResults.put( "percentOfMappedClasses", roundedPercentOfMappedClasses.toString() );
			pageResults.put( "numOfRejectedMappings", Integer.toString( numOfRejectedMappings() ) );
			pageResults.put( "numOfClasses", Integer.toString( numOfClasses ) );
			pageResults.put( "runCount", Integer.toString( runCount ) );
			pageResults.put( "numberOfRecommendations", Integer.toString( numberOfRecommendations ) );
			pageResults.put( "weightingType", weightingType );
			pageResults.put( "normalizationType", normalizationType );
			pageResults.put( "thresholdType", thresholdType );
			pageResults.put( "threshold", roundedThreshold.toString() );
			pageResults.put( "resultsPerPage", Integer.toString( resultsPerPage ) );
			pageResults.put( "runCount", Integer.toString( runCount ) );
			runResults.add( pageResults );
		}
		else 
		{
			if( !weightingType.contains( "Search Only Then" ) && !runComplete )
				runMapping = false;
		}
	}
	
	private void endProgram()
	{
		//in.close();
		
		//System.out.println( "unMappedClasses: " + unMappedClasses );
		//System.out.println( "\n\nNo Recommendations. Exiting Mapping..." );
		//System.out.println( "\n\n\n\nMODULE CALL DEPENDENCY GRAPH DIAGRAM\n------------------------------------" );
		//System.out.println( "\n\n\n\nRUN ENDED\n-------------" );
		
		long runEndTime = System.currentTimeMillis();
		int runSeconds = ( int ) ( runEndTime - runStartTime ) / 1000;
		double runMinutes = ( double ) runSeconds / 60;
		BigDecimal runRoundedMinutes = new BigDecimal( Double.toString( runMinutes ) );
		runRoundedMinutes = runRoundedMinutes.setScale( 2, RoundingMode.HALF_UP );
		
		System.out.println( "\n Completed -  " + runSeconds + " seconds ( " + runRoundedMinutes + " minutes )\n" );
	}
	
	private void generateRunResultsJSONData()
	{		
		if( runResults.isEmpty() )
		{
			Map<String, String> pageResults = new HashMap<>();
			
			int numOfMappedClasses = mappedClasses.size();
			double precision = 0.00f;
			double recall = ( double ) numberOfAcceptedClasses / ( double ) numOfClasses;
			double percentageOfMappedClasses = ( double ) numOfMappedClasses / ( double ) numOfClasses * 100.00f;
			double f1score = 2 * ( ( precision * recall ) / ( precision + recall ) );
			
			BigDecimal roundedPrecision = new BigDecimal( Double.toString( precision ) );		
			BigDecimal roundedRecall = new BigDecimal( Double.toString( recall ) );
			BigDecimal roundedF1Score = new BigDecimal( Double.toString( f1score ) );
			BigDecimal roundedPercentOfMappedClasses = new BigDecimal( Double.toString( percentageOfMappedClasses ) );
			BigDecimal roundedIRTechniqueWeighting = new BigDecimal( Double.toString( maxTechniqueWeighting ) );
			BigDecimal roundedDependencyTechniqueWeighting = new BigDecimal( Double.toString( 1 - maxTechniqueWeighting ) );
			BigDecimal roundedPageThreshold = new BigDecimal( Double.toString( pageThreshold ) );
			roundedPrecision = roundedPrecision.setScale( 2, RoundingMode.HALF_UP );
			roundedRecall = roundedRecall.setScale( 2, RoundingMode.HALF_UP );
			roundedF1Score = roundedF1Score.setScale( 2, RoundingMode.HALF_UP );
			roundedPercentOfMappedClasses = roundedPercentOfMappedClasses.setScale( 2, RoundingMode.HALF_UP );
			roundedIRTechniqueWeighting = roundedIRTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
			roundedDependencyTechniqueWeighting = roundedDependencyTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
			roundedPageThreshold = roundedPageThreshold.setScale( 2, RoundingMode.HALF_UP );
			
			System.out.println( " F1-Score: " + roundedF1Score );
			System.out.println( " Recall: " + roundedRecall );
			System.out.println( " Precision: " + roundedPrecision );
			
			pageResults.put( "pageNumber", Integer.toString( pageNumber ) );
			pageResults.put( "irTechniqueWeighting", roundedIRTechniqueWeighting.toString() ); 
			pageResults.put( "dependencyTechniqueWeighting", roundedDependencyTechniqueWeighting.toString() );
			pageResults.put( "threshold", roundedPageThreshold.toString() );
			pageResults.put( "numberOfAcceptedClasses", Integer.toString( numberOfAcceptedClasses ) );
			pageResults.put( "totalNumOfAcceptedClasses", Integer.toString( totalNumOfAcceptedClasses ) );
			pageResults.put( "precision", roundedPrecision.toString() );
			pageResults.put( "numOfMappedClasses", Integer.toString( numOfMappedClasses ) );
			pageResults.put( "percentOfMappedClasses", roundedPercentOfMappedClasses.toString() );
			pageResults.put( "numOfRejectedMappings", Integer.toString( numOfRejectedMappings() ) );
			pageResults.put( "numOfClasses", Integer.toString( numOfClasses ) );
			pageResults.put( "runCount", Integer.toString( runCount ) );
			pageResults.put( "numberOfRecommendations", Integer.toString( numberOfRecommendations ) );
			
			runResults.add( pageResults );
		}
		
		String data;
	    BufferedWriter writer;
	    
		try 
		{
			if( runCount == 1 )
				new File( "eval-results/results/" + evalutionTimeStamp + "/run-results/" + programName ).mkdirs();
			
			writer = new BufferedWriter( new FileWriter( "eval-results/results/" + evalutionTimeStamp + "/run-results/" + programName + "/" + programName + "-run-" + runCount + "-results-data.json" ) );

			data = "{ \n\t\"data\": [ ";
			
			for( Map<String, String> pageResults : runResults )
			{	
				data = data + "\n\t\t[";
				data = data + "\n\t\t\t\"" + pageResults.get( "pageNumber" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "irTechniqueWeighting" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "dependencyTechniqueWeighting" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "threshold" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "numberOfAcceptedClasses" ) + " / " + pageResults.get( "numberOfRecommendations" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "precision" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "numOfMappedClasses" ) + "\",";
				data = data + "\n\t\t\t\"" + pageResults.get( "percentOfMappedClasses" ) + "\",";
				data = data.substring( 0, data.lastIndexOf( "," ) );
				data = data + "\n\t\t],";
			}

			data = data.substring( 0, data.lastIndexOf( "," ) );
			data = data + "\n\t]\n}";
			
		    writer.write( data );
		    writer.close();
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
		}
	}
	
	private void generateRunResultsWebPage( Map<String, String> lastPage )
	{	
		try
		{
			String pageHTML = "";
			
			String runHeaderA = runHeaderA();
		    pageHTML = pageHTML + runHeaderA;
		    pageHTML = pageHTML + "\t\t\t\t<br><a href=\"../../" + programName + "-algo-eval-results.html\"><strong>Algorithm Results</a> > Run " 
		    + runCount + " Results</strong><br><br>\n";
		    
		    String runHeaderB = runHeaderB();
		    pageHTML = pageHTML + runHeaderB;
		    
		    int numOfMappedClasses = Integer.parseInt( lastPage.get( "numOfMappedClasses" ) );
			int numOfRejectedMappings = Integer.parseInt( lastPage.get( "numOfRejectedMappings" ) );
			int numOfUnMappedClasses = numOfClasses - numOfMappedClasses;
			int totalNumOfAcceptedClasses = Integer.parseInt( lastPage.get( "totalNumOfAcceptedClasses" ) );
			double recall = ( double ) numOfMappedClasses / ( double ) numOfClasses;
			double precision;
			
			if( totalNumOfAcceptedClasses == 0 && numOfRejectedMappings == 0 )
			{
				precision = 0.00f;
			}
			else 
			{
				precision = ( double ) totalNumOfAcceptedClasses / ( double ) ( totalNumOfAcceptedClasses + numOfRejectedMappings );
			}
			
			BigDecimal roundedRecall = new BigDecimal( Double.toString( recall ) );
			BigDecimal roundedRunThreshold = new BigDecimal( Double.toString( thresholdFilter ) );
			BigDecimal roundedPrecision = new BigDecimal( Double.toString( precision ) );
			BigDecimal runRoundedrunInitialMappingFraction = new BigDecimal( initialMappingFraction );
			roundedRunThreshold = roundedRunThreshold.setScale( 2, RoundingMode.HALF_UP );
			roundedRecall = roundedRecall.setScale( 2, RoundingMode.HALF_UP );
			roundedPrecision = roundedPrecision.setScale( 2, RoundingMode.HALF_UP );
			runRoundedrunInitialMappingFraction = runRoundedrunInitialMappingFraction.setScale( 2, RoundingMode.HALF_UP );
		    
			pageHTML = pageHTML + "\t\t\t\t<h6><strong>Run Parameters: </strong>"
					+ "<span class=\"blue\">Weighting Type: </span>" + weightingType
					+ " | <span class=\"blue\">Normalization Type: </span>" + normalizationType 
		    		+ " | <span class=\"blue\">Threshold Type: </span>" + thresholdType
		    		+ " | <span class=\"blue\">Penalty Factor: </span>" + penaltyFactor
		    		+ " | <span class=\"blue\">Module Name Boost Factor: </span>" + mnBoostFactor
		    		+ " | <span class=\"blue\">Module Description Boost Factor: </span>" + mdBoostFactor
		    		+ " | <span class=\"blue\">Initial Mapping Fraction Set: </span>" + runRoundedrunInitialMappingFraction
		    		+ " | <span class=\"blue\">Num of Initial Mapped Classes: </span>" + numOfInitialMappedClasses
		    		+ " | <span class=\"blue\">Results per page: </span>" + resultsPerPage 
		    		+ " | <span class=\"blue\"># of Classes: </span>" + numOfClasses + "</h6>\n";
		    pageHTML = pageHTML + "\t\t\t\t<h6><strong>Run Results: </strong>"
		    		+ "<span class=\"blue\">Recall: </span>" + roundedRecall 
		    		+ " | <span class=\"blue\">Precision: </span>" + roundedPrecision 
		    		+ " | <span class=\"blue\"># of Unmapped Classes: </span>" + numOfUnMappedClasses 
		    		+ " | <span class=\"blue\"># of Rejected Recommendations: </span>" + numOfRejectedMappings + "</h6><br>\n";
		    
		    String runFooterA = runFooterA();
		    pageHTML = pageHTML + runFooterA;
		    pageHTML = pageHTML + "\n\t\t\t\tajax: '" + programName + "-run-" + runCount + "-results-data.json',\n";
		    
		    String runFooterB = runFooterB();
		    pageHTML = pageHTML + runFooterB;
		    pageHTML = pageHTML + "\t\td3.json( \"" + programName + "-run-" + runCount + "-graph-data.json\", function( error, graph ) {";
		    
		    String runFooterC = runFooterC();
		    pageHTML = pageHTML + runFooterC;
		    
		    File resultsPageFile = new File( "eval-results/results/" + evalutionTimeStamp + "/run-results/" + programName + "/" + programName + "-run-" + runCount + ".html"  );
			resultsPageFile.delete();
			resultsPageFile.createNewFile();	
			
			BufferedWriter writer = new BufferedWriter( new FileWriter( resultsPageFile, true ) );
		    writer.append( pageHTML );
		    writer.close();
		}
		catch( IOException e )
		{ 	
			e.printStackTrace();
		}
	}
	
	private void generateGraphData()
	{		
		String data;
	    BufferedWriter writer;
	    
		try 
		{
			writer = new BufferedWriter( new FileWriter( "eval-results/results/" + evalutionTimeStamp + "/run-results/" + programName + "/" + programName + "-run-" + runCount + "-graph-data.json" ) );
			data = "{ \n\t\"nodes\": [ ";
			
			int moduleNumber = 1;
			
			for( String module : acceptedMappings.keySet() )
			{
				HashSet<String> mappedClasses = acceptedMappings.get( module );
				
				for( String mappedClass : mappedClasses )
				{
					data = data + "\n\t\t{ \"id\": \"" + mappedClass + " : " + module.toUpperCase() + "\", \"group\": " + moduleNumber + " },";
				}
				
				moduleNumber++;
			}
			
			for( String unMappedClass : unMappedClasses )
			{
				data = data + "\n\t\t{ \"id\": \"" + unMappedClass + " : UNMAPPED\" },";
			}

			data = data.substring( 0, data.lastIndexOf( "," ) );
			data = data + " \n\t],\n\t\"links\": [ ";
			
			Map<Map<String, String>, Integer> classDependency_Count = depAnalysis.getFilteredClassDependency_Counts();

			for( Map<String, String> classDependency : classDependency_Count.keySet() )
			{
				if( classDependency.entrySet().iterator().hasNext() )
				{
					String sourceClass = classDependency.entrySet().iterator().next().getKey();
					String targetClass = classDependency.entrySet().iterator().next().getValue();
					int numOfCalls = classDependency_Count.get( classDependency );
					String sourceModule = "UNMAPPED";
					String targetModule = "UNMAPPED";
					
					for( String module : acceptedMappings.keySet() )
					{
						HashSet<String> mappedClasses = acceptedMappings.get( module );
						
						for( String mappedClass : mappedClasses )
						{
							if( sourceClass.equals( mappedClass ) )
							{
								sourceModule = module;
							}
							if( targetClass.equals( mappedClass ) )
							{
								targetModule = module;
							}
						}
					}
					
					data = data + "\n\t\t{ \"source\": \"" + sourceClass + " : " + sourceModule.toUpperCase() + "\", \"target\": \"" 
					+ targetClass + " : " + targetModule.toUpperCase() + "\", \"value\": " + numOfCalls + " },";
				}
			}

			data = data.substring( 0, data.lastIndexOf( "," ) );
			data = data + " \n\t]\n}";
		    writer.write( data );
		    writer.close();
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
		}
	} 
	
	private String runHeaderA()
	{
		return "<!doctype html>\r\n" + 
				"<html class=\"no-js\" lang=\"en\">\r\n" + 
				"<head>\r\n" + 
				"	<meta charset=\"utf-8\" />\r\n" + 
				"	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\r\n" + 
				"	<title>Automated Mapping Results</title>\r\n" + 
				"	<link rel=\"stylesheet\" href=\"../../../../lib/foundation.min.css\">\r\n" + 
				"	<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../../lib/jquery.dataTables.css\">\r\n" + 
				"	<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../../lib/d3.forceLayout.css\">\r\n" + 
				"	<style>\r\n" + 
				"		.top-left \r\n" + 
				"		{\r\n" + 
				"		  	float: left;\r\n" + 
				"		}\r\n" + 
				"		.top-right \r\n" + 
				"		{\r\n" + 
				"		  	float: right;\r\n" + 
				"		}\r\n" + 
				"		.blue\r\n" + 
				"		{\r\n" + 
				"			color: #000080;\r\n" + 
				"		}\r\n" + 
				"	</style>\r\n" + 
				"</head>\r\n" + 
				"<body>\r\n" + 
				"	<div class=\"top-bar\">\r\n" + 
				"		<div class=\"row\">\r\n" + 
				"			<div class=\"top-bar-left\">";
	}
	
	private String runHeaderB()
	{
		return "			</div>\r\n" + 
				"		</div>\r\n" + 
				"	</div>\r\n" + 
				"	<br>\r\n" + 
				"	<div class=\"column\">\r\n" + 
				"		<ul class=\"tabs\" data-tabs id=\"results-tabs\">\r\n" + 
				"			<li class=\"tabs-title is-active\"><a href=\"#panel1\" aria-selected=\"true\">Results</a></li>\r\n" + 
				"			<li class=\"tabs-title\"><a href=\"#panel2\">Graph</a></li>\r\n" + 
				"		</ul>\r\n" + 
				"		<div class=\"tabs-content\" data-tabs-content=\"results-tabs\">\r\n" + 
				"			<div class=\"tabs-panel is-active\" id=\"panel1\">";
	}

	private String runFooterA()
	{
		return "				<div>\r\n" + 
				"					<table id=\"RunResultsTable\" class=\"display\" style=\"width:100%\">\r\n" + 
				"					    <thead>\r\n" + 
				"					        <tr>\r\n" + 
				"					        	<th>Page #</th>\r\n" + 
				"					            <th>Search Technique Weight</th>\r\n" + 
				"					            <th>Dependency Technique Weight</th>\r\n" + 
				"								<th>Threshold</th>\r\n" +
				"					            <th>Correct Recommendations</th>\r\n" +
				"					            <th>Page Precision</th>\r\n" + 
				"					            <th>Mapping Coverage</th>\r\n" + 
				"					            <th>Mapping Coverage (%)</th>\r\n" + 
				"					        </tr>\r\n" + 
				"					    </thead>\r\n" + 
				"					</table>\r\n" + 
				"				</div>\r\n" + 
				"			</div>\r\n" + 
				"			<div class=\"tabs-panel\" id=\"panel2\">\r\n" + 
				"				<div class=\"svg-container\">\r\n" + 
				"	 				<svg version=\"1.1\" viewBox=\"-2000 -2000 4000 4000\" preserveAspectRatio=\"xMinYMin meet\" class=\"svg-content\"></svg>\r\n" + 
				"				</div>\r\n" + 
				"			</div>\r\n" + 
				"		</div>\r\n" + 
				"	</div>\r\n" + 
				"	<script type=\"text/javascript\" src=\"../../../../lib/jquery-2.1.4.min.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" src=\"../../../../lib/foundation.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" charset=\"utf8\" src=\"../../../../lib/jquery.dataTables.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" > \r\n" + 
				"		$( document ).foundation();\r\n" + 
				"		$( document ).ready( function () {\r\n" + 
				"			$( '#RunResultsTable').DataTable( {\r\n" + 
				"				// see https://datatables.net for more options";
	}

	private String runFooterB()
	{
		return "				dom: '<\"top-left\"fl><\"top-right\"i>rt<\"bottom\"p>',\r\n" + 
				"				//stateSave: true,\r\n" + 
				"				pagingType: \"full_numbers\",\r\n" + 
				"				scrollX: true,\r\n" + 
				"				language: {\r\n" + 
				"		            lengthMenu: \"Entries per page _MENU_\",\r\n" + 
				"		        }\r\n" + 
				"			} );\r\n" + 
				"		} );\r\n" + 
				"	</script>\r\n" + 
				"	<script type=\"text/javascript\" src=\"../../../../lib/d3.v4.min.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" id=\"d3-force-layout\">\r\n" + 
				"\r\n" + 
				"		var svg = d3.select( \"svg\" ),\r\n" + 
				"			color = d3.scaleOrdinal( d3.schemeCategory20 );		//***\r\n" + 
				"\r\n" + 
				"		var simulation = d3.forceSimulation()\r\n" + 
				"		    .force( \"link\", d3.forceLink().id( function( d ) { return d.id; } ) )\r\n" + 
				"		    .force( \"charge\", d3.forceManyBody() );				//***";
	}

	private String runFooterC()
	{
		return "			if ( error ) \r\n" + 
				"				throw error;\r\n" + 
				"\r\n" + 
				"			var link = svg.append( \"g\" )\r\n" + 
				"			    .attr( \"class\", \"links\" )\r\n" + 
				"			    .selectAll( \"line\" )\r\n" + 
				"			    .data( graph.links )\r\n" + 
				"			    .enter().append( \"line\" )\r\n" + 
				"			    .attr( \"stroke-width\", function( d ) { return Math.sqrt( d.value ); } );	//***\r\n" + 
				"\r\n" + 
				"			var node = svg.append( \"g\" )\r\n" + 
				"			    .attr( \"class\", \"nodes\" )\r\n" + 
				"			    .selectAll( \"g\" )\r\n" + 
				"			    .data( graph.nodes )\r\n" + 
				"			    .enter().append( \"g\" )\r\n" + 
				"			    \r\n" + 
				"			var circles = node.append( \"circle\" )\r\n" + 
				"			    .attr( \"r\", 15 )						//**\r\n" + 
				"			    .attr( \"fill\", function( d ) { return color( d.group ); } )\r\n" + 
				"			    .call( d3.drag()\r\n" + 
				"			        .on( \"start\", dragstarted )\r\n" + 
				"			        .on( \"drag\", dragged )\r\n" + 
				"			        .on( \"end\", dragended ) \r\n" + 
				"			    );\r\n" + 
				"\r\n" + 
				"			var lables = node.append( \"text\" )\r\n" + 
				"			    .text( function( d ) { return null; } )\r\n" + 
				"			    .attr( 'x', 6 )\r\n" + 
				"			    .attr( 'y', 3 );\r\n" + 
				"\r\n" + 
				"			node.append( \"title\" )\r\n" + 
				"			    .text( function( d ) { return d.id; } );\r\n" + 
				"\r\n" + 
				"			simulation\r\n" + 
				"			    .nodes( graph.nodes )\r\n" + 
				"			    .on( \"tick\", ticked )\r\n" + 
				"			    .force( \"link\" )\r\n" + 
				"			    .links( graph.links );\r\n" + 
				"\r\n" + 
				"			function ticked() {\r\n" + 
				"			    link\r\n" + 
				"			        .attr( \"x1\", function( d ) { return d.source.x; } )\r\n" + 
				"			        .attr( \"y1\", function( d ) { return d.source.y; } )\r\n" + 
				"			        .attr( \"x2\", function( d ) { return d.target.x; } )\r\n" + 
				"			        .attr( \"y2\", function( d ) { return d.target.y; } );\r\n" + 
				"\r\n" + 
				"			    node\r\n" + 
				"			        .attr( \"transform\", function( d ) {\r\n" + 
				"			          	return \"translate(\" + d.x + \",\" + d.y + \")\";\r\n" + 
				"			        } )\r\n" + 
				"			}\r\n" + 
				"		} );\r\n" + 
				"\r\n" + 
				"		function dragstarted( d ) {\r\n" + 
				"		  	if( !d3.event.active )\r\n" + 
				"		  		simulation.alphaTarget( 0.3 ).restart();\r\n" + 
				"		  	d.fx = d.x;\r\n" + 
				"		  	d.fy = d.y;\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"		function dragged( d ) {\r\n" + 
				"		  	d.fx = d3.event.x;\r\n" + 
				"		  	d.fy = d3.event.y;\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"		function dragended( d ) {\r\n" + 
				"		  	if( !d3.event.active ) \r\n" + 
				"		  		simulation.alphaTarget( 0 );\r\n" + 
				"		  	d.fx = null;\r\n" + 
				"		  	d.fy = null;\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"	</script>\r\n" + 
				"</body>\r\n" + 
				"</html>";
	}

}