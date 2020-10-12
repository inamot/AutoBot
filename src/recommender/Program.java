/**
 * @author ZTS
 *
 */

package recommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Program 
{	
	String programName;
	String programUrl;
	String programSourceUrl;
	String programJarRootPackage;
	String moduleNames [];
	String moduleDescriptions [];
	int moduleDependencies [][];
	String packageExclusion [];
	String [] weightingType;
	String [] normalizationType;
	String [] thresholdType;
	double maxTechniqueWeighting;		
	double minTechniqueWeighting;
	double techniqueWeightingStep;
	double maxThreshold;					
	double minThreshold;
	double thresholdStep;
	int maxResultsPerPage;					
	int minResultsPerPage;
	int resultsPerPageStep;
	double maxPenaltyFactor;					
	double minPenaltyFactor;
	double penaltyFactorStep;
	double maxMNBoostFactor;					
	double minMNBoostFactor;
	double mnBoostFactorStep;
	double maxMDBoostFactor;					
	double minMDBoostFactor;
	double mdBoostFactorStep;
	double maxInitialMappingFraction;					
	double minInitialMappingFraction;
	double initialMappingFractionStep;
	
	Map<String, ArrayList<String>> programMap = new HashMap<>();
	
	public Program()
	{
		
	}
	
	public void setProgramName( String programName ) 
	{	
		this.programName = programName;	
	}
	
	public void setProgramUrl( String programUrl ) 
	{	
		this.programUrl = programUrl;	
	}
	
	public void setProgramSourceUrl( String programSourceUrl ) 
	{	
		this.programSourceUrl = programSourceUrl;	
	}

	public void setModuleNames( String moduleNames [] ) 
	{	
		this.moduleNames = moduleNames;	
	}
	
	public void setModuleDescriptions( String moduleDescriptions [] ) 
	{	
		this.moduleDescriptions = moduleDescriptions;	
	}
	
	public void setModuleDependencies( int moduleDependencies [][] ) 
	{	
		this.moduleDependencies = moduleDependencies;	
	}
	
	
	public String getProgramName()
	{	
		return programName;	
	}
	
	public String getProgramUrl()
	{	
		return programUrl;	
	}
	
	public String getProgramSourceUrl()
	{	
		return programSourceUrl;	
	}
	
	public String getProgramJarRootPackage() {
		return programJarRootPackage;
	}

	public void setProgramJarRootPackage(String programJarRootPackage) {
		this.programJarRootPackage = programJarRootPackage;
	}

	public String [] getModuleNames()
	{	
		return moduleNames;	
	}
	
	public String getModuleDescriptions() []
	{	
		return moduleDescriptions;	
	}
	
	public int getModuleDependencies() [][]
	{	
		return moduleDependencies;	
	}
	
	
	public double getMaxTechniqueWeighting() {
		return maxTechniqueWeighting;
	}

	public void setMaxTechniqueWeighting(double maxTechniqueWeighting) {
		this.maxTechniqueWeighting = maxTechniqueWeighting;
	}

	public double getMinTechniqueWeighting() {
		return minTechniqueWeighting;
	}

	public void setMinTechniqueWeighting(double minTechniqueWeighting) {
		this.minTechniqueWeighting = minTechniqueWeighting;
	}

	public double getTechniqueWeightingStep() {
		return techniqueWeightingStep;
	}

	public void setTechniqueWeightingStep(double techniqueWeightingStep) {
		this.techniqueWeightingStep = techniqueWeightingStep;
	}

	public double getMaxThreshold() {
		return maxThreshold;
	}

	public void setMaxThreshold(double maxThreshold) {
		this.maxThreshold = maxThreshold;
	}

	public double getMinThreshold() {
		return minThreshold;
	}

	public void setMinThreshold(double minThreshold) {
		this.minThreshold = minThreshold;
	}

	public double getThresholdStep() {
		return thresholdStep;
	}

	public void setThresholdStep(double thresholdStep) {
		this.thresholdStep = thresholdStep;
	}

	public int getMaxResultsPerPage() {
		return maxResultsPerPage;
	}

	public void setMaxResultsPerPage(int maxResultsPerPage) {
		this.maxResultsPerPage = maxResultsPerPage;
	}

	public int getMinResultsPerPage() {
		return minResultsPerPage;
	}

	public void setMinResultsPerPage(int minResultsPerPage) {
		this.minResultsPerPage = minResultsPerPage;
	}

	public int getResultsPerPageStep() {
		return resultsPerPageStep;
	}

	public void setResultsPerPageStep(int resultsPerPageStep) {
		this.resultsPerPageStep = resultsPerPageStep;
	}
	
	public String[] getWeightingType() {
		return weightingType;
	}

	public void setWeightingType(String[] weightingType) {
		this.weightingType = weightingType;
	}
	
	public String[] getNormalizationType() {
		return normalizationType;
	}

	public void setNormalizationType(String[] normalizationType) {
		this.normalizationType = normalizationType;
	}

	public String[] getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(String[] thresholdType) {
		this.thresholdType = thresholdType;
	}

	public double getMaxPenaltyFactor() {
		return maxPenaltyFactor;
	}

	public void setMaxPenaltyFactor(double maxPenaltyFactor) {
		this.maxPenaltyFactor = maxPenaltyFactor;
	}

	public double getMinPenaltyFactor() {
		return minPenaltyFactor;
	}

	public void setMinPenaltyFactor(double minPenaltyFactor) {
		this.minPenaltyFactor = minPenaltyFactor;
	}

	public double getPenaltyFactorStep() {
		return penaltyFactorStep;
	}

	public void setPenaltyFactorStep(double penaltyFactorStep) {
		this.penaltyFactorStep = penaltyFactorStep;
	}

	public double getMaxMNBoostFactor() {
		return maxMNBoostFactor;
	}

	public void setMaxMNBoostFactor(double maxMNBoostFactor) {
		this.maxMNBoostFactor = maxMNBoostFactor;
	}

	public double getMinMNBoostFactor() {
		return minMNBoostFactor;
	}

	public void setMinMNBoostFactor(double minMNBoostFactor) {
		this.minMNBoostFactor = minMNBoostFactor;
	}

	public double getMNBoostFactorStep() {
		return mnBoostFactorStep;
	}

	public void setMNBoostFactorStep(double mnBoostFactorStep) {
		this.mnBoostFactorStep = mnBoostFactorStep;
	}

	public double getMaxMDBoostFactor() {
		return maxMDBoostFactor;
	}

	public void setMaxMDBoostFactor(double maxMDBoostFactor) {
		this.maxMDBoostFactor = maxMDBoostFactor;
	}

	public double getMinMDBoostFactor() {
		return minMDBoostFactor;
	}

	public void setMinMDBoostFactor(double minMDBoostFactor) {
		this.minMDBoostFactor = minMDBoostFactor;
	}

	public double getMDBoostFactorStep() {
		return mdBoostFactorStep;
	}

	public void setMDBoostFactorStep(double mdBoostFactorStep) {
		this.mdBoostFactorStep = mdBoostFactorStep;
	}

	public double getMaxInitialMappingFraction() {
		return maxInitialMappingFraction;
	}

	public void setMaxInitialMappingFraction(double maxInitialMappingFraction) {
		this.maxInitialMappingFraction = maxInitialMappingFraction;
	}

	public double getMinInitialMappingFraction() {
		return minInitialMappingFraction;
	}

	public void setMinInitialMappingFraction(double minInitialMappingFraction) {
		this.minInitialMappingFraction = minInitialMappingFraction;
	}

	public double getInitialMappingFractionStep() {
		return initialMappingFractionStep;
	}

	public void setInitialMappingFractionStep(double initialMappingFractionStep) {
		this.initialMappingFractionStep = initialMappingFractionStep;
	}

	public String[] getPackageExclusion() {
		return packageExclusion;
	}

	public void setPackageExclusion(String[] packageExclusion) {
		this.packageExclusion = packageExclusion;
	}

	
	public Map<String, ArrayList<String>> getProgramMap() {
		return programMap;
	}
	

	public void setProgramMap(Map<String, ArrayList<String>> programMap) {
		this.programMap = programMap;
	}

}
