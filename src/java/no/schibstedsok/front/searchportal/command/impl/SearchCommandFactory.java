/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.schibstedsok.front.searchportal.command.impl;

import java.lang.UnsupportedOperationException;
import java.util.Map;

import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.command.MathExpressionCommand;
import no.schibstedsok.front.searchportal.command.OlympicSearchCommand;
import no.schibstedsok.front.searchportal.command.OverturePPCCommand;
import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.YellowSearchCommand;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.MathExpressionConfiguration;
import no.schibstedsok.front.searchportal.configuration.OlympicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.YellowSearchConfiguration;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;


/** This factory creates the appropriate command for a given 
 *
 * @author mick
 * @version $Id$
 */
public final class SearchCommandFactory {
    
    /** Create the appropriate command.
     **/
    public static SearchCommand createSearchCommand(final SearchCommand.Context cxt, final Map parameters){
        
        
        final SearchConfiguration config = cxt.getSearchConfiguration();
        
        // [FIXME] remove hardcoded knowledge of subclasses from here.
        // this is the drawback of removing the command dependency from configuration classes.
        // It is also not as performance savvy as the original implementation.
        // Possibilities are 1) move association to xml (tabs.xml?) or 2) use class naming scheme.
        // An example of possibility (2) would be XXXSearchConfiguration --> XXXSearchCommand
        if( config instanceof FastConfiguration ){
            return new FastSearchCommand(cxt, parameters);
        }else if( config instanceof MathExpressionConfiguration ){
            return new MathExpressionCommand(cxt, parameters);
        }else if( config instanceof OverturePPCConfiguration ){
            return new OverturePPCCommand(cxt, parameters);
        }else if( config instanceof PicSearchConfiguration ){
            return new PicSearchCommand(cxt, parameters);
        }else if( config instanceof SensisSearchConfiguration ){
            return new FastSearchCommand(cxt, parameters);
        }else if( config instanceof YellowSearchConfiguration ){
            return new YellowSearchCommand(cxt, parameters);
        }else if( config instanceof OlympicSearchConfiguration ){
            return new OlympicSearchCommand(cxt, parameters);
        }
        throw new UnsupportedOperationException("Cannot find suitable command for "+config.getName());
    }
    
}
