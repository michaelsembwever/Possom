// Copyright (2006-2007) Schibsted Søk AS
/*
 * HittaSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


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
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
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
    public ResultList<? extends ResultItem> execute(){

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



        final WebServiceSearchResult result = new WebServiceSearchResult();
        result.setHitCount(hits);


        return (ResultList<? extends ResultItem>) result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    private static final class WebServiceSearchResult extends BasicSearchResult<ResultItem>{

        private static final String ERR_NOT_SUPPORTED = "Not part of this implementation";

        public WebServiceSearchResult() {
            super();
        }

        @Override
        public void addResult(final ResultItem item) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        @Override
        public void addSpellingSuggestion(final WeightedSuggestion suggestion) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        @Override
        public Map<String, List<WeightedSuggestion>> getSpellingSuggestionsMap() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        @Override
        public List<Suggestion> getQuerySuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        @Override
        public void addQuerySuggestion(final Suggestion query) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        @Override
        public List<ResultItem> getResults() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

    }

}