// Copyright (2006-2007) Schibsted Søk AS
/*
 * HittaSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.SpellingSuggestion;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import no.schibstedsok.searchportal.mode.config.HittaCommandConfig;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.Application;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.WhoWhereSplit;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.QuerySuggestion;
import no.schibstedsok.searchportal.result.SpellingSuggestion;
import no.schibstedsok.searchportal.result.Suggestion;
import no.schibstedsok.searchportal.result.WeightedSuggestion;
import org.apache.axis.client.Stub;
import org.apache.log4j.Logger;
import se.hitta.www.HittaService.HittaServiceLocator;
import se.hitta.www.HittaService.HittaServiceSoap;

/** Search against the Swedish Hitta WebService. \u2706
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class HittaSearchCommand extends AbstractWebServiceSearchCommand{


    // Constants -----------------------------------------------------

    private static final Collection<TokenPredicate> WHO_PREDICATES = Collections.unmodifiableCollection(
            Arrays.asList(
                TokenPredicate.COMPANYENRICHMENT,
                TokenPredicate.FIRSTNAME,
                TokenPredicate.LASTNAME,
                TokenPredicate.PHONENUMBER
            ));

    private static final Logger LOG = Logger.getLogger(HittaSearchCommand.class);
    private static final String ERR_FAILED_HITTA_SEARCH = "Failed Hitta search command";
    private static final String DEBUG_CONF_NFO = "Conf details --> ";
    private static final String DEBUG_SEARCHING_1 = "Searching for who->";
    private static final String DEBUG_SEARCHING_2 = "Searching for where->";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of HittaSearchCommand.
     */
    public HittaSearchCommand(final Context cxt) {

        super(cxt);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    /** @inherit **/
    public SearchResult execute(){

        final HittaCommandConfig conf = (HittaCommandConfig) context.getSearchConfiguration();
        int hits = 0;

        LOG.debug(DEBUG_CONF_NFO + conf.getCatalog() + ' ' + conf.getKey());

        if(getTransformedQuery().equals(untransformedQuery)){

            try {

                final HittaServiceLocator locator = new HittaServiceLocator();
                final HittaServiceSoap service = locator.getHittaServiceSoap();
                ((Stub)service).setTimeout(1000);
                final WhoWhereSplitter splitter = new WhoWhereSplitter(
                        new WhoWhereSplitter.Context(){
                            private final List<Application> applications 
                                    = Arrays.asList(Application.WHITE, Application.YELLOW);
                            
                            public Map<Clause,String> getTransformedTerms(){
                                return HittaSearchCommand.this.getTransformedTerms();
                            }                
                            public Query getQuery() {
                                return context.getDataModel().getQuery().getQuery();
                            }
                            public List<Application> getApplications(){
                                return applications;
                            }
                    });

                final WhoWhereSplit splitQuery = splitter.getWhoWhereSplit();

                if( splitQuery.getWho().length() >0 ){

                    getParameters().put("hittaWho", splitQuery.getWho());
                    getParameters().put("hittaWhere", splitQuery.getWhere());

                    LOG.debug(DEBUG_SEARCHING_1 + splitQuery.getWho());
                    LOG.debug(DEBUG_SEARCHING_2 + splitQuery.getWhere());

                    if(conf.getCatalog().equalsIgnoreCase("white")){
                        hits = service.getWhiteAmount(splitQuery.getWho(), splitQuery.getWhere(), conf.getKey());

                    }else if(conf.getCatalog().equalsIgnoreCase("pink")){
                        hits = service.getPinkAmount(splitQuery.getWho(), splitQuery.getWhere(), conf.getKey());

                    }
                }
            } catch (ServiceException ex) {
                LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
            } catch (RemoteException ex) {
                LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
            }
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

        private static final String ERR_NOT_SUPPORTED = "Not part of this implementation";

        public WebServiceSearchResult(final SearchCommand command) {
            super(command);
        }

        public void addResult(final SearchResultItem item) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addSpellingSuggestion(final SpellingSuggestion suggestion) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public Map<String, List<WeightedSuggestion>> getSpellingSuggestionsMap() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<Suggestion> getQuerySuggestions() {
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