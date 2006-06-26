/* Copyright (2005-2006) Schibsted Søk AS
 *
 * StringChopper.java
 *
 * Created on June 22, 2006, 5:10 PM
 *
 */

package no.schibstedsok.front.searchportal.result;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/** My favourite dish of ChopSuey.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class StringChopper {
    
    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(StringChopper.class);
    
    private static final String DEBUG_CHOPSUEY = "Chopped it up to ";
    
    private static final Pattern openTag = Pattern.compile("<[^<]+>");
    private static final Pattern closeTag = Pattern.compile("</[^<]+>");
    private static final Pattern singleTag = Pattern.compile("<[^<]+/>");
    private static final Pattern la = Pattern.compile("<");
    private static final Pattern ra = Pattern.compile(">");
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    public static String chop(final String s, final int length) {
        
        final StringBuilder choppedString = new StringBuilder();
        
        
        if (s.length() <= length){
            choppedString.append(s);
            
        } else {
            
            // chop the string first
            choppedString.append(s.substring(0, length));
                    
            // if we chopped a tag in half remove the half left over.
            int laCount = 0;
            for( Matcher m = la.matcher(choppedString); m.find(); ++laCount);
            int raCount = 0;
            for( Matcher m = ra.matcher(choppedString); m.find(); ++raCount);
            if( laCount != raCount ){
                choppedString.setLength(choppedString.lastIndexOf("<"));
            }
            
            // append the dot-dot-dot
            switch( choppedString.charAt( choppedString.length() - 1 ) ){
                case '.':
                    choppedString.append("..");
                    break;
                case ' ':
                    choppedString.append(" ...");
                    break;
                default:
                    final int lastSpace = choppedString.lastIndexOf(" ");

                    if (lastSpace >= 0) {
                        choppedString.setLength(lastSpace);
                        choppedString.append(" ...");
                    } else {
                        choppedString.append("...");
                    }
                    break;
	        }
            
            // balance opening tags if the chop happened inbetween open and close tags.
            final LinkedList<String> tags = new LinkedList<String>();

            final Matcher matcher = openTag.matcher(choppedString);
            while( matcher.find() ){
                if( closeTag.matcher(matcher.group()).find() ){
                    tags.removeFirst();
                }else if( !singleTag.matcher(matcher.group()).find() ){
                    tags.addFirst(matcher.group());
                }
            }

            for(String tag : tags){
                choppedString.append(tag.replaceFirst("<","</"));
            }

            LOG.trace(DEBUG_CHOPSUEY + choppedString);
        
        }
        
        return choppedString.toString();
    }
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of StringChopper */
    private StringChopper(){
    }
    
    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
    
    
}
