
package org.tartarus.snowball;

import org.tartarus.snowball.ext.englishStemmer;

public class TestApp 
{
    public static void main( String [] args ) throws Throwable
    {
	    SnowballStemmer stemmer = ( SnowballStemmer ) new englishStemmer();	
	    
		final String stopWords [] = { "diagrams", "parts", "Tom's", "Ruths", "as", "menus", "news" };
		
		for( int i = 0; i < stopWords.length; i++ )
		{
			stemmer.setCurrent( stopWords[ i ]  );
			stemmer.stem();
			System.out.println( stemmer.getCurrent() );
		}
    }
}
