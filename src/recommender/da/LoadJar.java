package recommender.da;

import s4rdm3x.dmodel.JarProjectLoader;
import s4rdm3x.dmodel.dmClass;
import s4rdm3x.dmodel.dmDependency;
import s4rdm3x.dmodel.dmProject;
import s4rdm3x.model.CGraph;
import s4rdm3x.model.CNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by tohto on 2017-08-23.
 */
public class LoadJar
{
    private String m_file;
    private String [] m_rootPackages;
    private dmProject m_project;

    /*public LoadJar( String a_file, String[] a_rootPackages )
    {
        m_file = a_file;
        m_rootPackages = new String[ a_rootPackages.length ];
        
        for( int i = 0; i < m_rootPackages.length; i++ )
        {
            m_rootPackages[ i ] = a_rootPackages[ i ].replace( '/', '.' ).replace( '\\', '.' );
        }
        
        m_project = null;
    }*/
    
    public LoadJar( String a_file, String a_rootPackage )
    {
        m_file = a_file;
        m_rootPackages = a_rootPackage.split( "," );
        m_project = null;
    }

    public dmProject getProject()
    {
        return m_project;
    }
    
    public Map<Map<String, String>, Integer> getClasses( String a_rootPackage ) throws IOException
    {
    	JarProjectLoader jarProjectLoader = new JarProjectLoader();
        m_project = jarProjectLoader.buildProjectFromJAR( m_file, m_rootPackages );
        //m_project.doTrackConstantDeps( true );
        Map<Map<String, String>, Integer> classDependency_Count = new HashMap<>();
        
    	for( dmClass dmclass : m_project.getClasses() )
        {
            String className = dmclass.getFileName();
            //int numOfDependencies = dmclass.getDependencyCount();
            //int numOfConcreteDependencies = dmclass.getConcreteMethodCount();
            //String dependents = "";
            
            if( className.contains( a_rootPackage ) ) // && !dmclass.isInner() && !dmclass.isAnonymous() )
            {       
                for( dmDependency dependency : dmclass.getDependencies() )
                {
                	//dependents = dependents + "(" + dependency.getCount() + ") " + dependency.getType().name() + ": " + dependency.getSource().getName()
                		//	+ " -> " + dependency.getTarget().getName() + ", ";
                	Map<String, String> classDependency = new HashMap<>();
                	classDependency.put( dependency.getSource().getName(), dependency.getTarget().getName() );
                	
                	int numOfCalls = dependency.getCount();
                   
        			if ( classDependency_Count.get( classDependency ) != null )
        				numOfCalls = numOfCalls + classDependency_Count.get( classDependency );
        			
                    classDependency_Count.put( classDependency, numOfCalls );
                }
            }
        }
    	
    	return classDependency_Count;
    }

    public Map<String, HashSet<String>> getClassMethods( String a_rootPackage ) throws IOException
    {
    	JarProjectLoader jarProjectLoader = new JarProjectLoader();
        m_project = jarProjectLoader.buildProjectFromJAR( m_file, m_rootPackages );
        //m_project.doTrackConstantDeps( true );

        Map<String, HashSet<String>> filteredClassMethods = new HashMap<>();
        
    	for( dmClass dmclass : m_project.getClasses() )
        {
            String className = dmclass.getFileName();
            HashSet<String> methods = new HashSet<String>();
                        
            for( dmClass.Method method : dmclass.getMethods())
            {
            	methods.add( method.toString() );
            }
            
            filteredClassMethods.put( className, methods );
        }
    	
    	return filteredClassMethods;
    }
}
