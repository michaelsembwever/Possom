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
import no.schibstedsok.front.searchportal.command.OverturePPCCommand;
import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.YellowSearchCommand;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.MathExpressionConfiguration;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.YellowSearchConfiguration;
import no.schibstedsok.front.searchportal.query.RunningQuery;


/** This factory creates the appropriate command for a given 
 *
 * @author mick
 * @version $Id$
 */
public final class SearchCommandFactory {
    
    /** Being a factory for all the commands - it propagates all the contextual needs of the underlying commands it 
     * creates.
     */
    public interface Context{
        SearchConfiguration getSearchConfiguration();
    }
    
    /**
     * Creates a new instance of SearchCommandFactory
     */
    public SearchCommandFactory(final Context cxt) {
        this.context = cxt;
    }
    
    /** Create the appropriate command.
     * [TODO] Note that the provided Context argument can be passed along as is to all command objects since the Context
     * implements all their Contexts as well.
     * [TODO] Move RunningQuery into Context.
     **/
    public SearchCommand createSearchCommand(final RunningQuery query, final Map parameters){
        final SearchConfiguration config = context.getSearchConfiguration();
        
        // [FIXME] remove hardcoded knowledge of subclasses from here.
        // this is the drawback of removing the command dependency from configuration classes.
        // It is also not as performance savvy as the original implementation.
        // Possibilities are 1) move association to xml (tabs.xml?) or 2) use class naming scheme.
        // An example of possibility (2) would be XXXSearchConfiguration --> XXXSearchCommand
        if( config instanceof FastConfiguration ){
            return new FastSearchCommand(query, (FastConfiguration)config, parameters);
        }else if( config instanceof MathExpressionConfiguration ){
            return new MathExpressionCommand(query, config, parameters);
        }else if( config instanceof OverturePPCConfiguration ){
            return new OverturePPCCommand(query, (OverturePPCConfiguration)config, parameters);
        }else if( config instanceof PicSearchConfiguration ){
            return new PicSearchCommand(query, (PicSearchConfiguration)config, parameters);
        }else if( config instanceof SensisSearchConfiguration ){
            return new FastSearchCommand(query, (FastConfiguration)config, parameters);
        }else if( config instanceof YellowSearchConfiguration ){
            return new YellowSearchCommand(query, (YellowSearchConfiguration)config, parameters);
        }
        throw new UnsupportedOperationException("Cannot find suitable command for "+config.getName());
    }
    
    private final Context context;
}
