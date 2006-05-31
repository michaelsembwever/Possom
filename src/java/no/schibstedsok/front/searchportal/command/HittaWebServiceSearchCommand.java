// Copyright (2006) Schibsted Søk AS
/*
 * HittaWebServiceSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import no.schibstedsok.front.searchportal.configuration.HittaServiceSearchConfiguration;
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
public final class HittaWebServiceSearchCommand extends AbstractWebServiceSearchCommand{


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(HittaWebServiceSearchCommand.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of HittaWebServiceSearchCommand */
    public HittaWebServiceSearchCommand(final Context cxt, final Map parameters) {
        super (cxt, parameters);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    public SearchResult execute(){

        final HittaServiceSearchConfiguration conf = (HittaServiceSearchConfiguration) context.getSearchConfiguration();
        int hits = 0;


        try {
            final HittaServiceLocator locator = new HittaServiceLocator();

            final HittaServiceSoap service = locator.getHittaServiceSoap();

            if(conf.getCatalog().equalsIgnoreCase("white")){
                hits = service.getWhiteAmount(getTransformedQuery(), "", "")
                        + service.getWhiteAmount("", getTransformedQuery(), "");
            }else if(conf.getCatalog().equalsIgnoreCase("pink")){
                hits = service.getPinkAmount(getTransformedQuery(), "", "")
                        + service.getPinkAmount("", getTransformedQuery(), "");
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
