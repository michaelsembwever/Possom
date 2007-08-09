/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * StringChopper.java
 *
 * Created on June 22, 2006, 5:10 PM
 *
 */

package no.schibstedsok.searchportal.result;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/** My favourite dish of ChopSuey.
 * 
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
    
    /**
     * null safe.
     * @param s 
     * @param length 
     * @return 
     */
    public static String chop(final String s, final int length) {
        return chop(s, length, false);
    }  
    
    /**
     * null safe.
     * @param s 
     * @param length 
     * @param chopWord allowed to chop a word in half
     * @return 
     */    
    public static String chop(final String s, final int length, final boolean chopWord) {
        
        if(null != s){

            final StringBuilder choppedString = new StringBuilder(s);

            int laOriginalCount = 0, raOriginalCount = 0, markupLength = 0;
            boolean insideMarkup = false;
            for(int i = 0; i < choppedString.length(); ++i){ 
                if( '<' == choppedString.charAt(i) ){ ++laOriginalCount; insideMarkup = true;}

                if (insideMarkup) {
                    ++markupLength;
                }

                if( '>' == choppedString.charAt(i) ){ ++raOriginalCount; insideMarkup = false;}

            }

            // if we have more left than right arrows 
            while(laOriginalCount > raOriginalCount){
                choppedString.append('>');
                ++raOriginalCount;
                markupLength = 0; // Be safe, use original length if markup unbalanced.
            }

            // We're interested in limiting the length of the rendered string excluding the length of the markup.
            final int maxLength = length + markupLength;

            if(length > 0 && choppedString.length() > maxLength){

                // chop the string first
                choppedString.setLength(maxLength);

                // if we chopped a tag in half remove the half left over.
                int laCount = 0, raCount = 0;
                for(int i = 0; i < choppedString.length(); ++i){ 
                    if( '<' == choppedString.charAt(i) ){ ++laCount; }
                    else if( '>' == choppedString.charAt(i) ){ ++raCount; }
                }

                // if we have more left than right arrows
                if( laCount > raCount ){
                    choppedString.setLength(choppedString.lastIndexOf("<"));
                }

                // append the dot-dot-dot
                switch( choppedString.length() >0 ? choppedString.charAt( choppedString.length() - 1 ) : ' '){
                    case '.':
                        final String toString = choppedString.toString();
                        if( !toString.endsWith("...")){
                            if( toString.endsWith("..")){
                                choppedString.append('.');
                            }else {
                                choppedString.append("..");
                            }
                        }
                        break;
                    default:
                        if(!chopWord){
                            final int lastSpace = choppedString.lastIndexOf(" ");

                            if (lastSpace >= 0) {
                                choppedString.setLength(lastSpace + 1);
                            }
                        }
                        choppedString.append("...");
                        break;
                }

            }

            if(0 < laOriginalCount){
                // balance opening tags if the chop happened inbetween open and close tags.
                //LOG.debug("");LOG.debug("Balancing " + choppedString);

                final LinkedList<String> tags = new LinkedList<String>();
                final LinkedList<int[]> tagsToRemove = new LinkedList<int[]>();

                final Matcher matcher = openTag.matcher(choppedString);

                while( matcher.find() ){
                    if( closeTag.matcher(matcher.group()).find()) {

                        if(tags.size() > 0 && matcher.group().equalsIgnoreCase(tags.getFirst().replaceFirst("<", "</"))){

                            //LOG.debug("Found closing tag   " + matcher.group());
                            tags.removeFirst();

                        }else{

                            // we've found a premature closing tag. remove it.
                            //LOG.debug("Found unmatched closing tag " + matcher.group());
                            tagsToRemove.addFirst(new int[]{matcher.start(), matcher.end()});
                        }

                    }else if( singleTag.matcher(matcher.group()).find() ){

                        //LOG.debug("Ignoring single tag " + matcher.group());
                    }else{

                        //LOG.debug("Found opening tag  " + matcher.group());
                        tags.addFirst(matcher.group());
                    }
                }

                // remove tags that had no opening
                for(int[] startEnd : tagsToRemove){

                    //LOG.debug("Removing " + matcher.group());
                    choppedString.delete(startEnd[0], startEnd[1]);
                }

                // add tags to balance
                for(String tag : tags){

                    //LOG.debug("Adding " + tag.replaceFirst("<", "</"));
                    choppedString.append(tag.replaceFirst("<", "</"));
                }        
            }
            LOG.trace(DEBUG_CHOPSUEY + choppedString);

            return choppedString.toString();
        }
        return null;
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
