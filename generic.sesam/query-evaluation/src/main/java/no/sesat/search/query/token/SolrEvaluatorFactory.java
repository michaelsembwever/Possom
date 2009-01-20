/* Copyright (2005-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.token;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import no.sesat.commons.ioc.ContextWrapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.SiteConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @version <tt>$Id$</tt>
 */
public final class SolrEvaluatorFactory extends AbstractEvaluatorFactory{

    // Constants -----------------------------------------------------

    private static final String SOLR_EVALUATOR_XMLFILE = "SolrEvaluators.xml";

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(SolrEvaluatorFactory.class);

    private static final String ERR_FAILED_CONSTRUCTING_EVALUATOR = "Failed to construct the evaluator";
    private static final String ERR_FAILED_INITIALISATION = "Failed reading configuration files";

    private static final String TOKEN_HOST_PROPERTY = "tokenevaluator.solr.serverUrl";


    // Attributes -----------------------------------------------------

    private final Future solrEvaluatorCreator;
    private SolrTokenEvaluator solrEvaluator;
    private SolrServer server;
    private final Site site;

    // TODO this will leak when sites are redeploy without Sesat being restarted.
    private static final Map<Site,Map<TokenPredicate,String[]>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String[]>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();

    // Constructors -----------------------------------------------------

    public SolrEvaluatorFactory(final Context cxt) throws SiteKeyedFactoryInstantiationException {

        super(cxt);

        this.site = cxt.getSite();

        try{
            final Properties props = SiteConfiguration.instanceOf(
                            ContextWrapper.wrap(SiteConfiguration.Context.class, cxt)).getProperties();

            final String serverUrl = props.getProperty(TOKEN_HOST_PROPERTY);
            server = new CommonsHttpSolrServer(serverUrl);

        } catch (MalformedURLException ex) {

            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);
        }

        solrEvaluatorCreator = EXECUTOR.submit(new SolrEvaluatorCreator(cxt));

        try {
            init(cxt);

        } catch (ParserConfigurationException ex) {

            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);
        }
    }

    // public -----------------------------------------------------

    public TokenEvaluator getEvaluator(final TokenPredicate token) throws EvaluationException{

        final Context cxt = getContext();

        TokenEvaluator result = isResponsibleFor(token) ? getSolrEvaluator() : null;
        if(result == null && null != site.getParent()){

            result = instanceOf(ContextWrapper.wrap(
                    Context.class,
                    site.getParent().getSiteContext(),
                    cxt
                )).getEvaluator(token);

        }
        if(null == result || TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR == result){
            // if we cannot find an evaulator, then always fail evaluation.
            //  Rather than encourage a NullPointerException
            result = TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR;
        }
        return result;
    }

    @Override
    public boolean isResponsibleFor(final TokenPredicate token) {

        return null != getListNames(token);
    }


    // Package protected ---------------------------------------------

    SolrServer getSolrServer(){
        return server;
    }

    boolean usesListName(final String listname, final String exactname){

        boolean uses = false;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = this.site;

            while(!uses && null != site){

                // find listnames used for this token predicate
                for(String[] listnames : LIST_NAMES.get(site).values()){
                    uses |= 0 <= Arrays.binarySearch(listnames, listname, null);
                    uses |= null != exactname && 0 <= Arrays.binarySearch(listnames, exactname, null);
                    if(uses){  break; }
                }

                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return uses;
    }

    String[] getListNames(final TokenPredicate token){



        String[] listNames = null;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = this.site;

            while(null == listNames && null != site){

                // find listnames used for this token predicate
                listNames = LIST_NAMES.get(site).get(token);

                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return listNames;
    }

    boolean responsible(){

        boolean responsible = false;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site loopSite = this.site;

            while(null != loopSite){

                // find listnames used for this token predicate
                responsible =  !LIST_NAMES.get(loopSite).isEmpty();
                if(responsible){ break; }

                // prepare to go to parent
                loopSite = loopSite.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return responsible;
    }

    // private -----------------------------------------------------

    private static void init(final Context cxt) throws ParserConfigurationException{

        final Site site = cxt.getSite();
        final Site parent = site.getParent();
        final boolean parentUninitialised;

        try{
            LIST_NAMES_LOCK.readLock().lock();

            // initialise the parent site's configuration
            parentUninitialised = (null != parent && null == LIST_NAMES.get(parent));

        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }

        if(parentUninitialised){
            init(ContextWrapper.wrap(
                    AbstractEvaluatorFactory.Context.class,
                    parent.getSiteContext(),
                    cxt
                ));
        }

        if(null == LIST_NAMES.get(site)){

            try{
                LIST_NAMES_LOCK.writeLock().lock();

                    // create map entry for this site
                    LIST_NAMES.put(site, new HashMap<TokenPredicate,String[]>());

                    // initialise this site's configuration
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder builder = factory.newDocumentBuilder();

                    final DocumentLoader loader = cxt.newDocumentLoader(cxt, SOLR_EVALUATOR_XMLFILE, builder);
                    loader.abut();

                    LOG.info("Parsing " + SOLR_EVALUATOR_XMLFILE + " started");
                    final Map<TokenPredicate,String[]> listNames = LIST_NAMES.get(site);
                    final Document doc = loader.getDocument();

                    if(null != doc && null != doc.getDocumentElement()){

                        final Element root = doc.getDocumentElement();
                        final NodeList lists = root.getElementsByTagName("list");
                        for (int i = 0; i < lists.getLength(); ++i) {

                            final Element list = (Element) lists.item(i);

                            final String tokenName = list.getAttribute("token");
                            LOG.info(" ->list@token: " + tokenName);

                            TokenPredicate token;
                            try{
                                token = TokenPredicateUtility.getTokenPredicate(tokenName);

                            }catch(IllegalArgumentException iae){
                                LOG.debug(tokenName + " does not exist. Will create it. Underlying exception was " + iae);
                                token = TokenPredicateUtility.createAnonymousTokenPredicate(tokenName);
                            }

                            final String[] listNameArr = list.getAttribute("list-name").split(",");
                            LOG.info(" ->lists: " + list.getAttribute("list-name"));

                            // update each listname to the format the fast query matching servers use
                            if(null != listNameArr){
                                for(int j = 0; j < listNameArr.length; ++j){
                                    listNameArr[j] = listNameArr[j];
                                }

                                // put the listnames in
                                Arrays.sort(listNameArr, null);
                                listNames.put(token, listNameArr);
                            }


                        }
                    }
                    LOG.info("Parsing " + SOLR_EVALUATOR_XMLFILE + " finished");
            }finally{
                LIST_NAMES_LOCK.writeLock().unlock();
            }
        }
    }

    private SolrTokenEvaluator getSolrEvaluator() throws EvaluationException {

        try {

            // when the root logger is set to DEBUG do not limit connection times
            if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
                // default timeout is one second. TODO make configuration.
                solrEvaluatorCreator.get(1000, TimeUnit.MILLISECONDS);
            }else{
                solrEvaluatorCreator.get();
            }

        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new EvaluationException(ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new EvaluationException(ex.getMessage(), ex);
        } catch (TimeoutException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new EvaluationException(ex.getMessage(), ex);
        }
        if( null == solrEvaluator ){
            throw new EvaluationException("NPE", new NullPointerException());
        }

        return solrEvaluator;
    }


    // inner classes -----------------------------------------------------

    private final class SolrEvaluatorCreator implements Runnable{

        private final Context context;

        private SolrEvaluatorCreator(final Context cxt) {

            this.context = cxt;
        }

        public void run() {

            MDC.put("UNIQUE_ID", context.getUniqueId());
            try {
                solrEvaluator = new SolrTokenEvaluator(context, SolrEvaluatorFactory.this);

            } catch (EvaluationException ex) {
                LOG.error(ERR_FAILED_CONSTRUCTING_EVALUATOR);
            }

            MDC.remove("UNIQUE_ID");
        }

    }

}
