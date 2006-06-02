// Copyright (2006) Schibsted Søk AS
/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.schibstedsok.front.searchportal.command.impl;

import java.util.Map;
import no.schibstedsok.front.searchportal.command.AdvancedFastSearchCommand;
import no.schibstedsok.front.searchportal.command.BlendingNewsSearchCommand;
import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.command.HittaWebServiceSearchCommand;
import no.schibstedsok.front.searchportal.command.MathExpressionCommand;
import no.schibstedsok.front.searchportal.command.MobileSearchCommand;
import no.schibstedsok.front.searchportal.command.NewsSearchCommand;
import no.schibstedsok.front.searchportal.command.OverturePPCCommand;
import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.StaticSearchCommand;
import no.schibstedsok.front.searchportal.command.StockSearchCommand;
import no.schibstedsok.front.searchportal.command.WebSearchCommand;
import no.schibstedsok.front.searchportal.command.WhiteSearchCommand;
import no.schibstedsok.front.searchportal.command.YellowGeoSearch;
import no.schibstedsok.front.searchportal.configuration.AdvancedFastConfiguration;
import no.schibstedsok.front.searchportal.configuration.BlendingNewsSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.HittaServiceSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.MathExpressionConfiguration;
import no.schibstedsok.front.searchportal.configuration.MobileSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.NewsSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.StaticSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.StockSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.WebSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.WhiteSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.YellowSearchConfiguration;


/** This factory creates the appropriate command for a given
 *
 * @author mick
 * @version $Id$
 */
public final class SearchCommandFactory {

    private SearchCommandFactory() {  }

    /** Create the appropriate command.
     **/
    public static SearchCommand createSearchCommand(final SearchCommand.Context cxt, final Map parameters) {


        final SearchConfiguration config = cxt.getSearchConfiguration();

        // [FIXME] remove hardcoded knowledge of subclasses from here.
        // this is the drawback of removing the command dependency from configuration classes.
        // It is also not as performance savvy as the original implementation.
        // Possibilities are 1) move association to xml (tabs.xml?) or 2) use class naming scheme.
        // An example of (2) would be XXXSearchConfiguration --> XXXSearchCommand
        
        if (config instanceof BlendingNewsSearchConfiguration) {
            return new BlendingNewsSearchCommand(cxt, parameters);
            
        } else if (config instanceof YellowSearchConfiguration) {
            return new YellowGeoSearch(cxt, parameters);
            
        } else if (config instanceof StockSearchConfiguration) {
            return new StockSearchCommand(cxt, parameters);
            
        } else if (config instanceof WhiteSearchConfiguration) {
            return new WhiteSearchCommand(cxt, parameters);
            
        } else if (config instanceof WebSearchConfiguration) {
            return new WebSearchCommand(cxt, parameters);
            
        } else if (config instanceof NewsSearchConfiguration) {
            return new NewsSearchCommand(cxt, parameters);
        } else if (config instanceof AdvancedFastConfiguration) {
            return new AdvancedFastSearchCommand(cxt, parameters);
        } else if (config instanceof FastConfiguration) {
            return new FastSearchCommand(cxt, parameters);
        } else if (config instanceof MathExpressionConfiguration) {
            return new MathExpressionCommand(cxt, parameters);
        } else if (config instanceof OverturePPCConfiguration) {
            return new OverturePPCCommand(cxt, parameters);
            
        } else if (config instanceof PicSearchConfiguration) {
            return new PicSearchCommand(cxt, parameters);
            
        } else if (config instanceof SensisSearchConfiguration) {
            return new FastSearchCommand(cxt, parameters);
            
        } else if (config instanceof MobileSearchConfiguration)  {
            return new MobileSearchCommand(cxt, parameters);
            
        } else if (config instanceof StaticSearchConfiguration) {
            return new StaticSearchCommand(cxt, parameters);
            
        } else if (config instanceof FastConfiguration) {
            return new FastSearchCommand(cxt, parameters);
            
        } else if (config instanceof HittaServiceSearchConfiguration) {
            return new HittaWebServiceSearchCommand(cxt, parameters);
            
        }
        
        throw new UnsupportedOperationException("Cannot find suitable command for " + config.getName());
    }
}
