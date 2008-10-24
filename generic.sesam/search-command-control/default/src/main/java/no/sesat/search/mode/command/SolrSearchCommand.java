/*
 * Copyright (2008) Schibsted Søk AS
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

import java.net.MalformedURLException;
import java.util.Map;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.config.SolrCommandConfig;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.config.SiteConfiguration;
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

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public SolrSearchCommand(final Context cxt) {

        super(cxt);
        try {

            final String serverUrl = ((SolrCommandConfig)cxt.getSearchConfiguration()).getServerUrl();
            final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();
            server = new CommonsHttpSolrServer(siteConf.getProperty(serverUrl));

        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    // Public --------------------------------------------------------

    @Override
    public ResultList<ResultItem> execute() {

        final ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

        try {
            // set up query
            final SolrQuery query = new SolrQuery()
                    .setQuery(getTransformedQuery())
                    .setStart(getOffset())
                    .setRows(getSearchConfiguration().getResultsToReturn())
                    .setFields(getSearchConfiguration().getResultFieldMap().keySet().toArray(new String[]{}));

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
    public String getTransformedQuery() {
        return super.getTransformedQuery() + getSearchConfiguration().getQuerySuffix();
    }

    @Override
    public SolrCommandConfig getSearchConfiguration() {
        return (SolrCommandConfig)super.getSearchConfiguration();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------


    protected BasicResultItem createItem(final SolrDocument doc) {

        BasicResultItem item = new BasicResultItem();

        for (final Map.Entry<String,String> entry : getSearchConfiguration().getResultFieldMap().entrySet()){

            item = item.addField(entry.getValue(), (String)doc.getFieldValue(entry.getKey()));

        }

        return item;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
