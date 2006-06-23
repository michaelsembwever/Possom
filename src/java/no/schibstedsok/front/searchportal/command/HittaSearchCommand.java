// Copyright (2006) Schibsted Søk AS
/*
 * HittaSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import no.schibstedsok.front.searchportal.configuration.HittaSearchConfiguration;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.spell.QuerySuggestion;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;
import org.apache.log4j.Logger;
import se.hitta.www.HittaService.HittaServiceLocator;
import se.hitta.www.HittaService.HittaServiceSoap;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class HittaSearchCommand extends AbstractWebServiceSearchCommand{


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(HittaSearchCommand.class);
    private static final String DEBUG_CONF_NFO = "Conf details --> ";
    private static final String DEBUG_SEARCHING = "Searching for ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of HittaSearchCommand
     */
    public HittaSearchCommand(final Context cxt, final Map parameters) {
        super (cxt, parameters);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    public SearchResult execute(){

        final HittaSearchConfiguration conf = (HittaSearchConfiguration) context.getSearchConfiguration();
        int hits = 0;

        LOG.debug(DEBUG_CONF_NFO + conf.getCatalog() + ' ' + conf.getKey());

        try {
            final HittaServiceLocator locator = new HittaServiceLocator();
            final HittaServiceSoap service = locator.getHittaServiceSoap();
            
            final String transformedQuery = getTransformedQuery();
            LOG.debug(DEBUG_SEARCHING + transformedQuery);

            if(conf.getCatalog().equalsIgnoreCase("white")){
                hits = service.getWhiteAmount(transformedQuery, "", conf.getKey())
                        ;//+ service.getWhiteAmount("", getTransformedQuery(), conf.getKey());
            }else if(conf.getCatalog().equalsIgnoreCase("pink")){
                hits = service.getPinkAmount(transformedQuery, "", conf.getKey())
                        ;//+ service.getPinkAmount("", getTransformedQuery(), conf.getKey());
            }

        } catch (ServiceException ex) {
            LOG.error("", ex);
        } catch (RemoteException ex) {
            LOG.error("", ex);
        }

        final SearchResult result = new WebServiceSearchResult(this);
        result.setHitCount(hits);


        return result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    private static final class WebServiceSearchResult extends BasicSearchResult{

        private static final String ERR_NOT_SUPPORTED ="Not part of this implementation";

        public WebServiceSearchResult(final SearchCommand command) {
            super(command);
        }

        public void addResult(final SearchResultItem item) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addSpellingSuggestion(final SpellingSuggestion suggestion) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public Map<String, List<SpellingSuggestion>> getSpellingSuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<QuerySuggestion> getQuerySuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addQuerySuggestion(final QuerySuggestion query) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<SearchResultItem> getResults() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

    }
}
