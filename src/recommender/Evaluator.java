package recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class Evaluator
{	
	private static final DateTimeFormatter timeStampFormat = DateTimeFormatter.ofPattern( "yyyy.MM.dd-HH.mm.ss" );
	private static final LocalDateTime timeStamp = LocalDateTime.now();
	private static final String evalutionTimeStamp = timeStamp.format( timeStampFormat );
	
	private static int resultsCache = 1;
	private static int saveInterval;
	//private static int threadCount;
	private static String jabrefConfigFile = "test-systems/jabref/config-jabref.json";
	private static String promMConfigFile = "test-systems/prom/config-prom.json";
	private static String jittacConfigFile = "test-systems/jittac/config-jittac.json";
	private static String argoumlConfigFile = "test-systems/argouml/config-argouml.json";
	private static String antConfigFile = "test-systems/ant/config-ant.json";
	private static String teammatesConfigFile = "test-systems/teammates/config-teammates.json";
	private static boolean createIndex = true;
	private static String testingNote = "TESTING: ...";
	private static Program program = new Program();
	
	//private static ArrayList<Program> programs = new ArrayList<Program>();
	//private static Collection<AutoMap> autoMapRuns = new ArrayList<AutoMap>();
	//private static List<Future<ArrayList<Map<String, String>>>> results;
	private static Map<String, ArrayList<ArrayList<String>>> algorithmWebResults = new HashMap<>();

	
	private static void loadConfigSettings( String configFile )
	{   
		try
    	{
    		File resultsData  = new File( configFile );
    		BufferedReader in = new BufferedReader( new FileReader( resultsData ) );
    		String inputLine;
    		boolean arrayEntry = false;
    		String arrayKey = "";
    		ArrayList<String> arrayValues = new ArrayList<String>();
    		
    	    while( ( inputLine = in.readLine() ) != null )
    	    {		
				/*
				 * if( inputLine.startsWith( "****" ) ) { if( program.getProgramName() != null )
				 * { programs.add( program ); }
				 * 
				 * program = new Program(); }
				 */
	    		
	    		if( inputLine.contains( "{") )
    	    	{
	    			arrayEntry = true;
	    			String [] settingEntry = inputLine.split( ":" );
	    			arrayKey = settingEntry[ 0 ].trim().toLowerCase();
    	    	}
	    		else if( inputLine.contains( "}") )
    	    	{
	    			String [] avs = new String[ arrayValues.size() ];
	    			avs = arrayValues.toArray( avs );
	    			
	    			switch( arrayKey )
        	    	{
	        	    	case "modulenames" :
	                    	program.setModuleNames( avs ); 
	                        break;
	                        
	        	    	case "moduledescriptions" : 
	        	    		program.setModuleDescriptions( avs );  
	                        break;
	                    
	        	    	case "moduledependencies" : 
	        	    		int moduleDependencies[][] = new int[ avs.length ][ avs.length ];
	        	    		
	        	    		for( int row = 0; row < avs.length; row++ )
	        	    		{
	        	    			String rowValues[] = avs[ row ].split( "," );
	        	    			
	        	    			for( int column = 0; column < rowValues.length; column++ )
	        	    			{
	        	    				int value = Integer.parseInt( rowValues[ column ].trim() );
	        	    				moduleDependencies[ row ][ column ] = value;
	        	    			}
	        	    		}
	        	    		
	        	    		program.setModuleDependencies( moduleDependencies );  
	                        break;
	        	    	
	        	    	case "programmapping" : 
	        	    		Map<String, ArrayList<String>> programMap = new HashMap<>();
	        	    		String module = "";
	        	    		ArrayList<String> moduleClasses = new ArrayList<String>();
	        	    		
            	    		for( int i = 0; i < avs.length; i++  )
            	    		{
            	    			if( avs[ i ].contains( ":" ) )
            	    			{
            	    				String moduleLine [] = avs[ i ].split( ":" );
            	    				module = moduleLine[ 1 ].trim();
           
            	    				moduleClasses = new ArrayList<String>();
            	    			}
            	    			else
            	    			{
            	    				if( avs[ i ].contains( "." ) )
            	    					moduleClasses.add( avs[ i ] );
            	    			}
            	    			
            	    			programMap.put( module, moduleClasses );
            	    		}
	        	    		
            	    		program.setProgramMap( programMap );
                            break;
                            
	        	    	case "packageexclusion" : 
            	    		program.setPackageExclusion( avs ); 
                            break;
                            
	        	    	case "weightingtype" : 
            	    		program.setWeightingType( avs ); 
                            break;
                        
	        	    	case "normalizationtype" : 
            	    		program.setNormalizationType( avs ); 
                            break;
                            
	        	    	case "thresholdtype" : 
            	    		program.setThresholdType( avs ); 
                            break;
        	    	}
	    			
	    			arrayKey = "";
	    			arrayValues.clear();
	    			arrayEntry = false;
    	    	}
	    		
    			if( arrayEntry )
    			{
        	    	String arrayValue = inputLine.trim();
        	    	arrayValue = arrayValue.substring( 0, ( arrayValue.indexOf( "\"", arrayValue.indexOf( "\"" ) + 1 ) + 1 ) );
        	    	arrayValue = arrayValue.replace( "\"", "" );
        	    	
    				if( !inputLine.contains( "{" ) ) 
    				{
    					arrayValues.add( arrayValue );
    				}
    			}
    			else
	    		{
    				String [] settingEntry = inputLine.split( ":" );
    	    		
    	    		if( settingEntry.length > 1 )
    	    		{
	    				String settingKey = settingEntry[ 0 ].trim().toLowerCase();
            	    	String settingValue = settingEntry[ 1 ].trim();
            	    	settingValue = settingValue.substring( 0, ( settingValue.indexOf( "\"", settingValue.indexOf( "\"" ) + 1 ) + 1 ) );
            	    	settingValue = settingValue.replace( "\"", "" );
        	    		
        	    		switch( settingKey )
            	    	{
	            	    	case "programname" : 
	            	    		program.setProgramName( settingValue ); 
	                            break; 
	                        
	            	    	case "programurl" : 
	                        	program.setProgramUrl( settingValue ); 
	                            break; 
	                        
	            	    	case "programsourceurl" : 
	                        	program.setProgramSourceUrl( settingValue );
	                            break; 
	                            
	            	    	case "programjarrootpackage" : 
	                        	program.setProgramJarRootPackage( settingValue );
	                            break;
	                        
	            	    	case "maxtechniqueweighting" : 
	                        	program.setMaxTechniqueWeighting( Double.parseDouble( settingValue ));
	                            break;

	            	    	case "mintechniqueweighting" : 
	                        	program.setMinTechniqueWeighting( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "techniqueweightingstep" : 
	                        	program.setTechniqueWeightingStep( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "maxthreshold" : 
	                        	program.setMaxThreshold( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "minthreshold" : 
	                        	program.setMinThreshold( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "thresholdstep" : 
	                        	program.setThresholdStep( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "maxresultsperpage" : 
	                        	program.setMaxResultsPerPage( Integer.parseInt( settingValue ));
	                            break;

	            	    	case "minresultsperpage" : 
	                        	program.setMinResultsPerPage( Integer.parseInt( settingValue ) );
	                            break;

	            	    	case "resultsperpagestep" : 
	                        	program.setResultsPerPageStep( Integer.parseInt( settingValue ) );
	                            break;
	                        
	            	    	case "maxpenaltyfactor" : 
	                        	program.setMaxPenaltyFactor( Double.parseDouble( settingValue ));
	                            break;

	            	    	case "minpenaltyfactor" : 
	                        	program.setMinPenaltyFactor( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "penaltyfactorstep" : 
	                        	program.setPenaltyFactorStep( Double.parseDouble( settingValue ) );
	                            break;
	                       
	            	    	case "maxmnboostfactor" : 
	                        	program.setMaxMNBoostFactor( Double.parseDouble( settingValue ));
	                            break;

	            	    	case "minmnboostfactor" : 
	                        	program.setMinMNBoostFactor( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "mnboostfactorstep" : 
	                        	program.setMNBoostFactorStep( Double.parseDouble( settingValue ) );
	                            break;
	                            
	            	    	case "maxmdboostfactor" : 
	                        	program.setMaxMDBoostFactor( Double.parseDouble( settingValue ));
	                            break;

	            	    	case "minmdboostfactor" : 
	                        	program.setMinMDBoostFactor( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "mdboostfactorstep" : 
	                        	program.setMDBoostFactorStep( Double.parseDouble( settingValue ) );
	                            break;
	                            
	            	    	case "maxinitialmappingfraction" : 
	                        	program.setMaxInitialMappingFraction( Double.parseDouble( settingValue ));
	                            break;

	            	    	case "mininitialmappingfraction" : 
	                        	program.setMinInitialMappingFraction( Double.parseDouble( settingValue ) );
	                            break;

	            	    	case "initialmappingfractionstep" : 
	                        	program.setInitialMappingFractionStep( Double.parseDouble( settingValue ) );
	                            break;
	                            
	            	    	case "saveinterval" : 
	                        	saveInterval = Integer.parseInt( settingValue );
	                            break;
	 	                       
 	            	    	case "threadcount" : 
 	                        	//threadCount = Integer.parseInt( settingValue );
 	                            break;
            	    	}
    	    		}
	    		}
    	    }
    	    
    		in.close();
		}
    	catch ( Exception e )
    	{
    		System.out.println( "Failed to read settings check your configuration file" );
    		e.printStackTrace();
		}
}
	
	public static void main( String[] agrs ) 
	{
		/*loadConfigSettings( antConfigFile );
		//nonThreadedEvaluation();
		
		loadConfigSettings( argoumlConfigFile );
		nonThreadedEvaluation();
		
		loadConfigSettings( jabrefConfigFile );
		nonThreadedEvaluation();*/
		
		loadConfigSettings( jittacConfigFile );
		nonThreadedEvaluation();

		/*loadConfigSettings( promMConfigFile );
		nonThreadedEvaluation();
		
		loadConfigSettings( teammatesConfigFile );
		nonThreadedEvaluation();*/
	}
	
	public static void nonThreadedEvaluation()
	{
		int runCount = 1;
		long algoStartTime = System.currentTimeMillis();
		
		String programName = program.getProgramName();
		String [] weightingType = program.getWeightingType();
		String [] normalizationType = program.getNormalizationType();
		String [] thresholdType = program.getThresholdType();
		double maxTechniqueWeighting = program.getMaxTechniqueWeighting();		
		double minTechniqueWeighting = program.getMinTechniqueWeighting();
		double techniqueWeightingStep = program.getTechniqueWeightingStep();
		double maxThreshold = program.getMaxThreshold();
		double minThreshold = program.getMinThreshold();
		double thresholdStep = program.getThresholdStep();
		int maxResultsPerPage = program.getMaxResultsPerPage();
		int minResultsPerPage = program.getMinResultsPerPage();
		int resultsPerPageStep = program.getResultsPerPageStep();
		double maxPenaltyFactor = program.getMaxPenaltyFactor();
		double minPenaltyFactor = program.getMinPenaltyFactor();
		double penaltyFactorStep  = program.getPenaltyFactorStep();
		double maxMNBoostFactor = program.getMaxMNBoostFactor();
		double minMNBoostFactor = program.getMinMNBoostFactor();
		double mnBoostFactorStep  = program.getMNBoostFactorStep();
		double maxMDBoostFactor = program.getMaxMDBoostFactor();
		double minMDBoostFactor = program.getMinMDBoostFactor();
		double mdBoostFactorStep  = program.getMDBoostFactorStep();
		double maxInitialMappingFraction = program.getMaxInitialMappingFraction();
		double minInitialMappingFraction = program.getMinInitialMappingFraction();
		double initialMappingFractionStep  = program.getInitialMappingFractionStep();
		
		ArrayList<ArrayList<String>> programResults = new ArrayList<>();
		
		System.out.println( "\n*** Evaluating: " + programName + " ***\n" );
		//System.out.print( "Compiling configuration parameters " );
		
		for( int runWeightingType = 0; runWeightingType < weightingType.length; runWeightingType++ )
		{
			for( int runNormalizationType = 0; runNormalizationType < normalizationType.length; runNormalizationType++ )
			{
				for( int runThresholdType = 0; runThresholdType < thresholdType.length; runThresholdType++ )
				{
					for( int runResultsPerPage = maxResultsPerPage; runResultsPerPage >= minResultsPerPage; runResultsPerPage = runResultsPerPage - resultsPerPageStep )
					{	
						for( double runThreshold = maxThreshold; runThreshold >= minThreshold; runThreshold = runThreshold - thresholdStep )
						{	
							for( double runPenaltyFactor = maxPenaltyFactor; runPenaltyFactor >= minPenaltyFactor; 
									runPenaltyFactor = runPenaltyFactor - penaltyFactorStep )
							{	
								for( double runMNBoostFactor = maxMNBoostFactor; runMNBoostFactor >= minMNBoostFactor; 
										runMNBoostFactor = runMNBoostFactor - mnBoostFactorStep )
								{	
									for( double runMDBoostFactor = maxMDBoostFactor; runMDBoostFactor >= minMDBoostFactor; 
											runMDBoostFactor = runMDBoostFactor - mdBoostFactorStep )
									{
										for( double runInitialMappingFraction = maxInitialMappingFraction; runInitialMappingFraction >= minInitialMappingFraction; 
												runInitialMappingFraction = runInitialMappingFraction - initialMappingFractionStep )
										{	//System.out.println( "runInitialMappingFraction: " + runInitialMappingFraction );
											for( double runTechniqueWeighting = maxTechniqueWeighting; runTechniqueWeighting >= minTechniqueWeighting; 
													runTechniqueWeighting = runTechniqueWeighting - techniqueWeightingStep )
											{	
												ArrayList<Map<String, String>> autoMapRun = new ArrayList<>();
												autoMapRun.addAll( new AutoMap( program, runCount, weightingType[ runWeightingType ], 
														normalizationType[ runNormalizationType ], thresholdType[ runThresholdType ], 
														runTechniqueWeighting, ( 1 - runTechniqueWeighting ), runThreshold, runPenaltyFactor,
														runMNBoostFactor, runMDBoostFactor, runInitialMappingFraction, runResultsPerPage, 
														evalutionTimeStamp, createIndex ).call() );
												
												ArrayList<String> webResultsEntry = new ArrayList<String>();
												
												Map<String, String> lastPage = autoMapRun.get( autoMapRun.size() - 1 );
												//Map<String, String> firstPage = autoMapRun.get( 0 );
												
												int totalNumOfAcceptedClasses = Integer.parseInt( lastPage.get( "totalNumOfAcceptedClasses" ) );
												int numOfMappedClasses = Integer.parseInt( lastPage.get( "numOfMappedClasses" ) ); 
												int numOfRejectedMappings = Integer.parseInt( lastPage.get( "numOfRejectedMappings" ) );
												int numOfClasses = Integer.parseInt( lastPage.get( "numOfClasses" ) ); 
												int numOfUnMappedClasses = numOfClasses - numOfMappedClasses; 
												double recall = ( double ) numOfMappedClasses / ( double ) numOfClasses; 
												int numOfPages = autoMapRun.size(); 
												double precision; 
												  
												if( totalNumOfAcceptedClasses == 0 && numOfRejectedMappings == 0 )
													precision = 0.00f;
												else 
													precision = ( double ) totalNumOfAcceptedClasses / ( double ) ( totalNumOfAcceptedClasses + numOfRejectedMappings );
												
												double f1score = 2 * ( ( precision * recall ) / ( precision + recall ) );
												  
												BigDecimal roundedRunTechniqueWeighting = new BigDecimal( runTechniqueWeighting ); 
												BigDecimal roundedRecall = new BigDecimal( Double.toString( recall ) ); 
												BigDecimal roundedRunThreshold = new BigDecimal( runThreshold ); 
												BigDecimal roundedPrecision = new BigDecimal( Double.toString( precision ) ); 
												BigDecimal roundedF1Score = new BigDecimal( Double.toString( f1score ) ); 
												BigDecimal roundedRunPenaltyFactor = new BigDecimal( runPenaltyFactor );
												BigDecimal roundedRunMNBoostFactor = new BigDecimal( runMNBoostFactor );
												BigDecimal roundedRunMDBoostFactor = new BigDecimal( runMDBoostFactor );
												BigDecimal roundedRunInitialMappingFraction = new BigDecimal( runInitialMappingFraction );
												roundedRunTechniqueWeighting = roundedRunTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
												roundedRunThreshold = roundedRunThreshold.setScale( 2, RoundingMode.HALF_UP ); 
												roundedRecall = roundedRecall.setScale( 2, RoundingMode.HALF_UP );
												roundedPrecision = roundedPrecision.setScale( 2, RoundingMode.HALF_UP );
												roundedF1Score = roundedF1Score.setScale( 2, RoundingMode.HALF_UP );
												roundedRunPenaltyFactor = roundedRunPenaltyFactor.setScale( 2, RoundingMode.HALF_UP );
												roundedRunMNBoostFactor = roundedRunMNBoostFactor.setScale( 2, RoundingMode.HALF_UP );
												roundedRunMDBoostFactor = roundedRunMDBoostFactor.setScale( 2, RoundingMode.HALF_UP );
												roundedRunInitialMappingFraction = roundedRunInitialMappingFraction.setScale( 2, RoundingMode.HALF_UP );
												
												webResultsEntry.add( lastPage.get( "runCount" )  ); 
												webResultsEntry.add( lastPage.get( "weightingType" ) );
												webResultsEntry.add( lastPage.get( "normalizationType" ) );
												webResultsEntry.add( lastPage.get( "thresholdType" ) );
												webResultsEntry.add( roundedRunTechniqueWeighting.toString() ); 
												webResultsEntry.add( roundedRunThreshold.toString());
												webResultsEntry.add( roundedRunPenaltyFactor.toString() );
												webResultsEntry.add( roundedRunMNBoostFactor.toString() );
												webResultsEntry.add( roundedRunMDBoostFactor.toString() );
												webResultsEntry.add( roundedRunInitialMappingFraction.toString() );
												webResultsEntry.add( Integer.toString( runResultsPerPage ) ); 
												webResultsEntry.add( Integer.toString( numOfClasses ) ); 
												webResultsEntry.add( Integer.toString( numOfPages ) );
												webResultsEntry.add( roundedRecall.toString() ); 
												webResultsEntry.add( roundedPrecision.toString() ); 
												webResultsEntry.add( roundedF1Score.toString() ); 
												webResultsEntry.add( Integer.toString( numOfUnMappedClasses ) ); 
												webResultsEntry.add( Integer.toString( numOfRejectedMappings ) );
												
												programResults.add( webResultsEntry );
												
												if( resultsCache == saveInterval ) 
												{ 
													generatePartialJSONAlgorithmData();
													resultsCache++; 
												}
												
												runCount++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}	
		
		algorithmWebResults.put( programName, programResults );
		
		long algoEndTime = System.currentTimeMillis();
		int algoRunSeconds = ( int ) ( algoEndTime - algoStartTime ) / 1000;
		double algoRunMinutes = ( double ) algoRunSeconds / 60;
		//BigDecimal runRoundedMinutes = new BigDecimal( Double.toString( algoRunMinutes ) );
		BigDecimal runRoundedHours = new BigDecimal( Double.toString( algoRunMinutes / 60 ) );
		//runRoundedMinutes = runRoundedMinutes.setScale( 2, RoundingMode.HALF_UP );
		runRoundedHours = runRoundedHours.setScale( 2, RoundingMode.HALF_UP );
		System.out.println( "*** " + programName + " Evaluation Complete  - " + algoRunSeconds + " seconds ( " + runRoundedHours 
				+ " hours ) ***\n\n" );
		
		generatePartialJSONAlgorithmData();
		generateAlgorithmResultsWebPage( programName );
	 	
	 	System.out.println( "EVALUATION END - Open Web page to see results" );
	}

	/*public static void threadedEvaluation()
	{
		int runCount = 1;
		long algoStartTime = System.currentTimeMillis();
		
		String programName = program.getProgramName();
		String [] weightingType = program.getWeightingType();
		double maxTechniqueWeighting = program.getMaxTechniqueWeighting();		
		double minTechniqueWeighting = program.getMinTechniqueWeighting();
		double techniqueWeightingStep = program.getTechniqueWeightingStep();
		double maxThreshold = program.getMaxThreshold();
		double minThreshold = program.getMinThreshold();
		double thresholdStep = program.getThresholdStep();
		int maxResultsPerPage = program.getMaxResultsPerPage();
		int minResultsPerPage = program.getMinResultsPerPage();
		int resultsPerPageStep = program.getResultsPerPageStep();
		ExecutorService exec = null;
		ArrayList<ArrayList<String>> programResults = new ArrayList<>();
		
		System.out.println( "\n*** Evaluating: " + programName + " ***\n" );
		System.out.print( "Compiling configuration parameters " );
		
		for( int runWeightingType = 0; runWeightingType < weightingType.length; runWeightingType++ )
		{
			for( int runResultsPerPage = maxResultsPerPage; runResultsPerPage >= minResultsPerPage; runResultsPerPage = runResultsPerPage - resultsPerPageStep )
			{
				autoMapRuns.clear();
				
				for( double runThreshold = maxThreshold; runThreshold >= minThreshold; runThreshold = runThreshold - thresholdStep )
				{
					for( double runTechniqueWeighting = maxTechniqueWeighting; runTechniqueWeighting >= minTechniqueWeighting; 
							runTechniqueWeighting = runTechniqueWeighting - techniqueWeightingStep )
					{
						autoMapRuns.add( new AutoMap( program, runCount, weightingType[ runWeightingType ], runTechniqueWeighting, 
								( 1 - runTechniqueWeighting ), runThreshold, runResultsPerPage, evalutionTimeStamp ) );
						
						System.out.print( "C" + runCount + " " );
						
						runCount++;
					}
				}
				
				try 
				{
					exec = Executors.newFixedThreadPool( threadCount );
					results = exec.invokeAll( autoMapRuns );
					
					for( Future<ArrayList<Map<String, String>>> runResults : results )
					{
						Map<String, String> lastPage;
						Map<String, String> firstPage;
						
						ArrayList<String> webResultsEntry = new ArrayList<String>();
						
						if( !runResults.get().isEmpty() )
						{
							lastPage = runResults.get().get( runResults.get().size() - 1 );
							firstPage = runResults.get().get( 0 );
							
							int numOfMappedClasses = Integer.parseInt( lastPage.get( "numOfMappedClasses" ) ); 
							int numOfRejectedMappings = Integer.parseInt( lastPage.get( "numOfRejectedMappings" ) );
							int numOfClasses = Integer.parseInt( lastPage.get( "numOfClasses" ) ); 
							int numOfUnMappedClasses = numOfClasses - numOfMappedClasses; 
							double recall = ( double ) numOfMappedClasses / ( double ) numOfClasses; 
							int numOfPages = runResults.get().size(); 
							double precision;
							  
							if( numOfMappedClasses == 0 && numOfRejectedMappings == 0 )
								precision = 0.00f;
							else 
								precision = ( double ) numOfMappedClasses / ( double ) ( numOfMappedClasses + numOfRejectedMappings );
							  
							BigDecimal roundedRunTechniqueWeighting = new BigDecimal( firstPage.get( "irTechniqueWeighting" ) ); 
							BigDecimal roundedRecall = new BigDecimal( Double.toString( recall ) ); 
							BigDecimal roundedRunThreshold = new BigDecimal( lastPage.get( "threshold" ) ); 
							BigDecimal roundedPrecision = new BigDecimal( Double.toString( precision ) ); 
							roundedRunTechniqueWeighting = roundedRunTechniqueWeighting.setScale( 2, RoundingMode.HALF_UP );
							roundedRunThreshold = roundedRunThreshold.setScale( 2, RoundingMode.HALF_UP ); 
							roundedRecall = roundedRecall.setScale( 2, RoundingMode.HALF_UP );
							roundedPrecision = roundedPrecision.setScale( 2, RoundingMode.HALF_UP );
							  
							webResultsEntry.add( lastPage.get( "runCount" )  ); 
							webResultsEntry.add( lastPage.get( "weightingType" ) ); 
							webResultsEntry.add( roundedRunTechniqueWeighting.toString() ); 
							webResultsEntry.add( roundedRunThreshold.toString()); 
							webResultsEntry.add( firstPage.get( "resultsPerPage" ) ); 
							webResultsEntry.add( Integer.toString( numOfClasses ) ); 
							webResultsEntry.add( Integer.toString( numOfPages ) );
							webResultsEntry.add( roundedRecall.toString() ); 
							webResultsEntry.add( roundedPrecision.toString() ); 
							webResultsEntry.add( Integer.toString( numOfUnMappedClasses ) ); 
							webResultsEntry.add( Integer.toString( numOfRejectedMappings ) );
							
						}
						else
						{
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  );  
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  );  
							webResultsEntry.add( "0"  ); 
							webResultsEntry.add( "0"  ); 
						}
						
						programResults.add( webResultsEntry );
						
						if( resultsCache == saveInterval ) 
						{ 
							generatePartialJSONAlgorithmData();
							resultsCache++; 
						}
					}
				} 
				catch ( InterruptedException | ExecutionException e ) 
				{
					e.printStackTrace();
				}
			}	
		}
		
		algorithmWebResults.put( program.getProgramName(), programResults );
		exec.shutdown();
		
		long algoEndTime = System.currentTimeMillis();
		int algoRunSeconds = ( int ) ( algoEndTime - algoStartTime ) / 1000;
		double algoRunMinutes = ( double ) algoRunSeconds / 60;
		//BigDecimal runRoundedMinutes = new BigDecimal( Double.toString( algoRunMinutes ) );
		BigDecimal runRoundedHours = new BigDecimal( Double.toString( algoRunMinutes / 60 ) );
		//runRoundedMinutes = runRoundedMinutes.setScale( 2, RoundingMode.HALF_UP );
		runRoundedHours = runRoundedHours.setScale( 2, RoundingMode.HALF_UP );
		System.out.println( "*** " + program.getProgramName() + " Evaluation Complete  - " + algoRunSeconds + " seconds ( " + runRoundedHours 
				+ " hours ) ***\n\n" );
		
		generatePartialJSONAlgorithmData();
		generateAlgorithmResultsWebPage();
	 	
	 	System.out.println( "EVALUATION END - Open Web page to see results" );
	}*/
	
	private static void generatePartialJSONAlgorithmData()
	{   
	    try
	    {	
	    	for( String programName : algorithmWebResults.keySet() )
		    {
		    	ArrayList<ArrayList<String>> programResults = algorithmWebResults.get( programName );
		    	String data = "";
		    	File resultsData  = new File( "eval-results/results/" + evalutionTimeStamp + "/" + programName + "-algo-results-data.json" );
		    	
		    	if( resultsData.exists() )
		    	{
		    		BufferedReader in = new BufferedReader( new FileReader( resultsData ) );
					String inputLine;
					
				    while( ( inputLine = in.readLine() ) != null )
				    {
				    	data = data + inputLine + "\n";
				    }
				    
			    	in.close();
		    	}
		    	
		    	File resultsDataDir  = new File( "eval-results/results/" + evalutionTimeStamp );
		    	resultsDataDir.mkdir();
		    	BufferedWriter writer = new BufferedWriter( new FileWriter( resultsData ) );
			    
		    	for ( ArrayList<String> webResultsEntry : programResults )
		    	{
		    		if( data.isEmpty() )
				    {
			    		data = data + "{ \n\t\"data\": [ ";
						data = data + "\n\t\t[";
				    }
				    else
				    {
				    	data = data.substring( 0, data.lastIndexOf( "]" ) - 2 );
						data = data + ",\n\t\t[";
				    }
		    		
		    		for( String i : webResultsEntry )
					{
						data = data + "\n\t\t\t\"" + i + "\",";
					}
		    		
		    		data = data + "\n\t\t\t\"<a href='" + "run-results/" + programName + "/" + programName + "-run-" + webResultsEntry.get( 0 ) + ".html"  + "'>Run Details</a>\"";
		    		data = data + "\n\t\t]";
					data = data + "\n\t]\n}";
		    	}
			    
			    writer.write( data );
			    writer.close();
		    }
		    
		    algorithmWebResults.clear();
		    resultsCache = 1;
		} 
	    catch( IOException e )
	    {
			e.printStackTrace();
		}
	}

	public static void generateAlgorithmResultsWebPage( String programName )
	{	
		try
		{
			String pageHTML = "";
			
			/*File headerAFile  = new File( "eval-results/lib/templates/algo-headerA.html" );
			BufferedReader in = new BufferedReader( new FileReader( headerAFile ) );
			String inputLine;
			
		    while( ( inputLine = in.readLine() ) != null )
		    {
		    	pageHTML = pageHTML + inputLine + "\n";
		    }
		    
		    in.close();*/
		    
		    String algoHeaderA = algoHeaderA();
		    
		    pageHTML = pageHTML + algoHeaderA;
			
			String tabName = program.getProgramName();
			
			pageHTML = pageHTML + "\n\t\t\t<li class=\"tabs-title is-active\"><a href=\"#panel1\" aria-selected=\"true\">" + tabName + "</a></li>\n";
			pageHTML = pageHTML + "\t\t\t<li class=\"tabs-title\"><a href=\"#panel2\">Notes</a></li>\n";
			pageHTML = pageHTML + "\t\t</ul>\n";
			pageHTML = pageHTML + "\t\t<div class=\"tabs-content\" data-tabs-content=\"results-tabs\">\n";
			
			/*File tableFile  = new File( "eval-results/lib/templates/algo-table.html" );
	    	in = new BufferedReader( new FileReader( tableFile ) );
	    	inputLine = "";
	    	String tableHtml = "";
		    
		    while( ( inputLine = in.readLine() ) != null )
		    {
		    	tableHtml = tableHtml + inputLine + "\n";
		    }
		    
		    in.close();*/
		    
		    String algoTable = algoTable();
		    
		    /*File notesFile  = new File( "eval-results/lib/templates/algo-notes.html" );
	    	in = new BufferedReader( new FileReader( notesFile ) );
	    	inputLine = "";
	    	String notesHtml = "";
		    
		    while( ( inputLine = in.readLine() ) != null )
		    {
		    	notesHtml = notesHtml + inputLine + "\n";
		    }
		    
		    in.close();*/
		    
		    String algoNotes = algoNotes();

			String closingDiv = "\t\t\t\t</div>\n";
			closingDiv = closingDiv + "\t\t\t</div>\n";
			
			pageHTML = pageHTML + "\t\t\t<div class=\"tabs-panel is-active\" id=\"panel1\">\n";
			pageHTML = pageHTML + "\t\t\t\t<div>\n";
			pageHTML = pageHTML + "\t\t\t\t\t<table id=\"" + tabName + "Table\" class=\"display\" style=\"width:100%\">\n";
			pageHTML = pageHTML + algoTable;
			pageHTML = pageHTML + "\n\t\t\t\t\t</table>\n";
			pageHTML = pageHTML + closingDiv;

			pageHTML = pageHTML + "\t\t\t<div class=\"tabs-panel\" id=\"panel2\">\n";
			pageHTML = pageHTML + "\t\t\t\t<div>\n";
			pageHTML = pageHTML + algoNotes + "\n";
			pageHTML = pageHTML + closingDiv;
			
			/*File headerBFile = new File( "eval-results/lib/templates/algo-headerB.html" );
	    	in = new BufferedReader( new FileReader( headerBFile ) );
	    	inputLine = "";
		    
		    while( ( inputLine = in.readLine() ) != null )
		    {
		    	pageHTML = pageHTML + inputLine + "\n";
		    }
		    
		    in.close();*/
		    
		    String algoHeaderB = algoHeaderB();
		    
		    pageHTML = pageHTML + algoHeaderB;
		    
	    	pageHTML = pageHTML + "\n\t\t\t$( '#" + tabName + "Table').DataTable( {\n";
	    	pageHTML = pageHTML + "\t\t\t\t// see https://datatables.net for more options\n";
	    	pageHTML = pageHTML + "\t\t\t\tajax: '" + tabName + "-algo-results-data.json',\n";
		    
		    /*File footerFile = new File( "eval-results/lib/templates/algo-footer.html" );
	    	in = new BufferedReader( new FileReader( footerFile ) );
	    	inputLine = "";
		    
		    while( ( inputLine = in.readLine() ) != null )
		    {
		    	pageHTML = pageHTML + inputLine + "\n";
		    }
		    
		    in.close();*/
		    
		    String algoFooter = algoFooter();
		    
		    pageHTML = pageHTML + algoFooter;
		        
		    File resultsPageFile = new File( "eval-results/results/" + evalutionTimeStamp + "/" + programName + "-algo-eval-results.html"  );
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
	
	private static String algoHeaderA()
	{
		return "<!doctype html>\r\n" + 
				"<html class=\"no-js\" lang=\"en\">\r\n" + 
				"<head>\r\n" + 
				"	<meta charset=\"utf-8\" />\r\n" + 
				"	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\r\n" + 
				"	<title>Automated Mapping Results</title>\r\n" + 
				"	<link rel=\"stylesheet\" href=\"../../lib/foundation.min.css\">\r\n" + 
				"	<link rel=\"stylesheet\" type=\"text/css\" href=\"../../lib/jquery.dataTables.css\">\r\n" + 
				"	<style>\r\n" + 
				"		.top-left \r\n" + 
				"		{\r\n" + 
				"		  	float: left;\r\n" + 
				"		}\r\n" + 
				"		.top-right \r\n" + 
				"		{\r\n" + 
				"		  	float: right;\r\n" + 
				"		}\r\n" + 
				"	</style>\r\n" + 
				"</head>\r\n" + 
				"<body>\r\n" + 
				"	<div class=\"top-bar\">\r\n" + 
				"		<div class=\"row\">\r\n" + 
				"			<div class=\"top-bar-left\">\r\n" + 
				"				<br><strong>Algorithm Results</strong><br><br>\r\n" + 
				"			</div>\r\n" + 
				"		</div>\r\n" + 
				"	</div>\r\n" + 
				"	<br>\r\n" + 
				"	<div class=\"column\">\r\n" + 
				"		<ul class=\"tabs\" data-tabs id=\"results-tabs\">";
	}
	
	private static String algoHeaderB()
	{
		return "		</div>\r\n" + 
				"	</div>\r\n" + 
				"	<script type=\"text/javascript\" src=\"../../lib/jquery-2.1.4.min.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" src=\"../../lib/foundation.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" charset=\"utf8\" src=\"../../lib/jquery.dataTables.js\"></script>\r\n" + 
				"	<script type=\"text/javascript\" > \r\n" + 
				"		$( document ).foundation();\r\n" + 
				"		$( document ).ready( function () {";
	}
	
	private static String algoTable()
	{
		return "					    <thead>\r\n" + 
				"					    	<tr> \r\n" + 
				"								<th colspan=\"1\"></th>\r\n" + 
				"								<th colspan=\"10\">Algorithm Parameters</th>\r\n" + 
				"								<th colspan=\"8\">Algorithm Results</th> \r\n" + 
				"							</tr>\r\n" + 
				"					        <tr>\r\n" + 
				"					            <th>Run #</th>\r\n" + 
				"					            <th>Weighting Type*</th>\r\n" + 
				"								<th>Normalization Type*</th>\r\n" +
	            "								<th>Threshold Type*</th>\r\n" +
				"					            <th>Search Technique Starting Weight*</th>\r\n" + 
				"					            <th>Threshold*</th>\r\n" +
				"					            <th>Penalty Factor*</th>\r\n" +
				"					            <th>Module Name Boost Factor*</th>\r\n" +
				"					            <th>Module Description Boost Factor*</th>\r\n" +
				"					            <th>Initial Mapping Fraction*</th>\r\n" +
				"					            <th>Results per Page*</th>\r\n" + 
				"					            <th># of Classes</th>\r\n" + 
				"					            <th># of Result Pages</th>\r\n" + 
				"					            <th>Recall</th>\r\n" + 
				"					            <th>Precision</th>\r\n" + 
				"					            <th>F1Score</th>\r\n" + 
				"					            <th># of Unmapped Classes</th>\r\n" + 
				"					            <th># of Rejected Recommendations</th>\r\n" + 
				"					            <th>Run Results</th>\r\n" + 
				"					        </tr>\r\n" + 
				"					    </thead>";
	}
	
	private static String algoNotes()
	{
		return "					<p>\r\n" + 
				"						All column headers marked with a ' <strong>*</strong> ' refer to changeable parameters of the algorithm.\r\n" +
				"					</p>" +
				"					<p>\r\n" + 
										testingNote + "\r\n" +
				"					</p>";
	}
	
	private static String algoFooter() {
		
		return "				order: [[ 14, \"dsc\" ], [ 16, \"asc\" ], [ 13, \"dsc\" ], [ 15, \"asc\" ]],\r\n" + 
				"				dom: '<\"top-left\"fl><\"top-right\"i>rt<\"bottom\"p>',\r\n" + 
				"				//stateSave: true,\r\n" + 
				"				pagingType: \"full_numbers\",\r\n" + 
				"				scrollX: true,\r\n" + 
				"				language: {\r\n" + 
				"		            lengthMenu: \"Entries per page _MENU_\",\r\n" + 
				"		        }\r\n" + 
				"			} );\r\n" + 
				"		} );\r\n" + 
				"	</script>\r\n" + 
				"</body>\r\n" + 
				"</html>";
	}
	
}