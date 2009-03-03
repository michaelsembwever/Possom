/*
 * Copyright (2008-2009) Schibsted Søk AS
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
package no.sesat.search.mode.command;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.mode.config.SolrCommandConfig;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.Spi;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/** Searching against a Solr index using the Solrj client.
 * see http://wiki.apache.org/solr/Solrj
 *
 * The query syntax could be improved
 *  see http://lucene.apache.org/java/docs/queryparsersyntax.html
 *
 * @version $Id$
 */
public class SolrSearchCommand extends AbstractSearchCommand{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SolrSearchCommand.class);

    // Attributes ----------------------------------------------------

    private SolrServer server;
    private final FacetToolkit facetToolkit;

    // Static --------------------------------------------------------

    private static final ReferenceMap<String,SolrServer> SERVERS = new ReferenceMap<String,SolrServer>(
            ReferenceMap.Type.SOFT,
            new ConcurrentHashMap<String, Reference<SolrServer>>());

    // Constructors --------------------------------------------------

    public SolrSearchCommand(final Context cxt) {

        super(cxt);
        try {

            final String serverUrlKey = ((SolrCommandConfig)cxt.getSearchConfiguration()).getServerUrl();
            final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();
            final String serverUrl = siteConf.getProperty(serverUrlKey);

            server = SERVERS.get(serverUrl);

            if(null == server){
                server = new CommonsHttpSolrServer(serverUrl);
                SERVERS.put(serverUrl, server);
            }

        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        facetToolkit = createFacetToolkit();
    }

    // Public --------------------------------------------------------

    @Override
    public ResultList<ResultItem> execute() {

        final ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

        // @todo make the join between query and filter configurable
        final String q = getTransformedQuery()
                + (0 < getFilter().length() ? " (" + getFilter() + ')' : "");

        try {
            // set up query
            final SolrQuery query = new SolrQuery()
                    .setQuery(q)
                    .setFilterQueries(getSearchConfiguration().getFilteringQuery())
                    .setStart(getOffset())
                    .setRows(getSearchConfiguration().getResultsToReturn());

            // custom query type
            if(null != getSearchConfiguration().getQueryType() && 0 < getSearchConfiguration().getQueryType().length()){
                query.setQueryType(getSearchConfiguration().getQueryType());
            }

            // The request handler may be configured in the index which fields to return in the results
            if(0 < getSearchConfiguration().getResultFieldMap().size()){
                query.setFields(getSearchConfiguration().getResultFieldMap().keySet().toArray(new String[]{}));
            }

            createFacets(query);

            // when the root logger is set to DEBUG do not limit connection times
            if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
                query.setTimeAllowed(getSearchConfiguration().getTimeout());
            }

            final Map<String,String> sortMap = getSearchConfiguration().getSortMap();
            for(Map.Entry<String,String> entry : sortMap.entrySet()){
                final SolrQuery.ORDER order = SolrQuery.ORDER.valueOf(entry.getValue());
                query.addSortField(entry.getKey(), order);
            }

            DUMP.info(query.toString());

            // query
            final QueryResponse response = server.query(query);
            final SolrDocumentList docs = response.getResults();

            searchResult.setHitCount((int)docs.getNumFound());

            // iterate through docs
            for(SolrDocument doc : docs){

                searchResult.addResult(createItem(doc));
            }

        } catch (SolrServerException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return searchResult;
    }

    @Override
    public SolrCommandConfig getSearchConfiguration() {
        return (SolrCommandConfig)super.getSearchConfiguration();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected FacetToolkit createFacetToolkit(){

        FacetToolkit toolkit = null;
        final String toolkitName = getSearchConfiguration().getFacetToolkit();
        if(null != toolkitName && 0 < toolkitName.length()){
            toolkit = FacetToolkitFactory.getInstance(context, toolkitName);
        }
        return toolkit;
    }

    protected final void createFacets(final SolrQuery query){
        if(null != facetToolkit){
            facetToolkit.createFacets(context, query);
        }
    }

    protected BasicResultItem createItem(final SolrDocument doc) {

        Map<String,String> fieldNames;
        if(0 < getSearchConfiguration().getResultFieldMap().size()){
            fieldNames = getSearchConfiguration().getResultFieldMap();
        }else{
            // The request handler must be configured in the index as to which fields to return in the results
            fieldNames = new HashMap<String,String>();
            for(String fieldName : doc.getFieldNames()){
                fieldNames.put(fieldName, fieldName);
            }
        }

        BasicResultItem item = new BasicResultItem();

        for (final Map.Entry<String,String> entry : fieldNames.entrySet()){

            final Object value = doc.getFieldValue(entry.getKey());
            if(value instanceof String){
                item = item.addField(entry.getValue(), (String)doc.getFieldValue(entry.getKey()));
            }else if(value instanceof Serializable){
                item = item.addObjectField(entry.getValue(), (Serializable)doc.getFieldValue(entry.getKey()));
            }else{
                LOG.warn("Unable to add to ResultItem this non Serializable object: " + value);
            }

        }

        return item;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    /**
     * Provider to add facets from request to SolrQuery.
     */
    public interface FacetToolkit{
        void createFacets(SearchCommand.Context context, SolrQuery query);
    }

    protected static final class FacetToolkitFactory {

        // Constructors --------------------------------------------------

        /** Not possible to create a new instance of FacetToolkitFactory */
        private FacetToolkitFactory() {
        }

        // Public --------------------------------------------------------

        /** Factory call to instiantate a FacetToolkit.
         *
         * @param context context providing Resource
         * @param name the name of the class implementing FacetToolkit
         * @return
         */
        public static FacetToolkit getInstance(
                final Context context,
                final String name){

            try{
                final Site site = context.getDataModel().getSite().getSite();

                final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                        SiteClassLoaderFactory.Context.class,
                        new BaseContext() {
                            public Spi getSpi() {
                                return Spi.SEARCH_COMMAND_CONTROL;
                            }
                            public Site getSite(){
                                return site;
                            }
                        },
                        context
                    );

                final ClassLoader ctlLoader = SiteClassLoaderFactory.instanceOf(ctlContext).getClassLoader();

                @SuppressWarnings("unchecked")
                final Class<? extends FacetToolkit> cls = (Class<? extends FacetToolkit>)ctlLoader.loadClass(name);

                return cls.newInstance();

            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

    }
}
