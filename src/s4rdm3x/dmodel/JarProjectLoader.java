package s4rdm3x.dmodel;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by tohto on 2017-08-16.
 */
public class JarProjectLoader
{
	public dmProject buildProjectFromJAR( String a_jarFileName, String a_rootPackage ) throws IOException
    {
        String [] roots = { a_rootPackage };
        return buildProjectFromJAR( a_jarFileName, roots );
    }
	
	public dmProject buildProjectFromJAR( String a_jarFileName, String[] a_rootPackages ) throws IOException
    {
        JarFile jarFile = new JarFile( a_jarFileName );
        ASMdmProjectBuilder builder = new ASMdmProjectBuilder();        
        Enumeration<JarEntry> entries = jarFile.entries();
        
        dmProject project = builder.getProject();
        project.addPackageToBlackList( "java.lang" );
        project.addPackageToBlackList( "java.util" );
        project.addClassToBlackList( "int" );
        project.addClassToBlackList( "float" );
        project.addClassToBlackList( "boolean" );
        project.addClassToBlackList( "void" );
        project.addClassToBlackList( "double" );
        project.doTrackConstantDeps( false );
        
        builder.dontPrint();

        while( entries.hasMoreElements() )
        {
            JarEntry entry = entries.nextElement();

            if( entry.getName().endsWith( ".class" ) )
            {	
                if( startsWithAny( entry.getName(), a_rootPackages ) )
                {
                    InputStream in = jarFile.getInputStream( entry );
                    ClassReader classReader = new ClassReader( in );
                    classReader.accept( builder, 0 );
                }
            }
        }
        
        jarFile.close();
        
        return builder.getProject();
    }

    private boolean startsWithAny( String a_fullString, String[] a_starts )
    {
        if ( a_starts.length == 0 )
        {
            return true;
        }
        
        a_fullString = a_fullString.replace( "\\", "." ).replace( "/", "." );
        
        for( String start : a_starts )
        {
        	start = start.replace( "\\", "." ).replace( "/", "." );
        	
        	if( a_fullString.startsWith( start ) )
            {	
                return true;
            }
        }
        
        return false;
    }
    
}