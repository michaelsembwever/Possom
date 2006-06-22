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

/**
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
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    public static String chop(final String s, final int length) {
        
        final StringBuilder choppedString = new StringBuilder();
        if (s.length() <= length){
            choppedString.append(s);
            
        } else {
            
            final String sub = s.substring(0, length);
            final String lastChar = Character.toString(sub.charAt(sub.length() - 1));
            if (lastChar.equals(".")){
                choppedString.append(sub.substring(0, length)).append("..");
                
            }else if (lastChar.equals(" ")){
                choppedString.append(sub.substring(0, length)).append(" ...");
                
            }else {
		        final int lastSpace = sub.lastIndexOf(" ");

                if (lastSpace >= 0) {
                    choppedString.append(sub.substring(0, sub.lastIndexOf(" "))).append(" ...");
                    
                } else {
                    choppedString.append(sub.substring(0, length)).append("...");
                }
	        }
        }
        

        
        final LinkedList<String> tags = new LinkedList<String>();
        
        final Matcher matcher = openTag.matcher(choppedString);
        while( matcher.find() ){
            LOG.trace("matched " + matcher.group());
            if( closeTag.matcher(matcher.group()).find() ){
                LOG.trace("removing");
                tags.removeLast();
            }else if( !singleTag.matcher(matcher.group()).find() ){
                LOG.trace("adding");
                tags.addLast(matcher.group());
            }
        }
        
        for(String tag : tags){
            LOG.trace("restoring " + tag);
            choppedString.append(tag.replaceFirst("<","</"));
        }
        
        LOG.debug(DEBUG_CHOPSUEY + choppedString);
        
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
