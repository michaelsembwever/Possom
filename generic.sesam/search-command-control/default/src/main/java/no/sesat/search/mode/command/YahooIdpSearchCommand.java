/*
 * Copyright (2006-2008) Schibsted Søk AS
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import no.sesat.search.mode.config.YahooIdpCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Search against Yahoo! Index Data Protocol 2.0.
 *
 *
 * @version $Id$
 */
public class YahooIdpSearchCommand extends AbstractYahooSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(YahooIdpSearchCommand.class);
    private static final String ERR_FAILED_CREATING_URL = "Failed to create command url";

    private static final String COMMAND_URL_PATTERN =
            "/search?Client={0}&Database={1}&DateRange={2}&"
            + "FirstResult={3}&Numresults={4}&"
            + "{5}RegionMix={6}{7}&{8}LanguageMix={9}&"
            + "QueryEncoding={10}&Fields={11}&Unique={12}&Filter={13}&"
            + "Query={14}&"
            + "{15}";

    private static final String DATE_PATTERN = "yyyy/MM/dd";

    private static final String HEADER_ELEMENT = "HEADER";
    private static final String TOTALHITS_ELEMENT ="TOTALHITS";
    private static final String DEEPHITS_ELEMENT = "DEEPHITS";
    private static final String RESULT_ELEMENT = "RESULT";
    private static final String NUMRESULT_ELEMENT = "NUMRESULTS";
    private static final String WORDCOUNTS_ELEMENT = "WORDCOUNTS";

    // private static final String ALLWORDS = "ALLWORDS("; // not used for now. could be as ANYWORDS optimisation
    private static final String ANYWORDS = "ANYWORDS(";
    private static final String PHRASEWORDS = "PHRASEWORDS(";
    private static final String OMNISEARCH_HACK = "a b c d e f g h i j k l m n o p q r s t u v w x y z";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Create new overture command.
     *
     * @param cxt The context to execute in.
     */
    public YahooIdpSearchCommand(final Context cxt) {

        super(cxt);

        setXmlRestful(
                new AbstractXmlRestful(cxt) {
                    public String createRequestURL() {

                        final YahooIdpCommandConfig conf = (YahooIdpCommandConfig)cxt.getSearchConfiguration();

                        final String dateRange = '-' + new SimpleDateFormat(DATE_PATTERN).format(new Date());

                        final String wrappedTransformedQuery = ANYWORDS

                                // support "*" searches that return everything in the index.
                                + ("*".equals(YahooIdpSearchCommand.this.getQuery().getQueryString())
                                ? OMNISEARCH_HACK
                                : YahooIdpSearchCommand.this.getTransformedQuery()) + ' '

                                + YahooIdpSearchCommand.this.getFilter() + ')';

                        final StringBuilder fields = new StringBuilder();

                        for (final String field : cxt.getSearchConfiguration().getResultFieldMap().keySet()) {
                            fields.append(field);
                            fields.append(',');
                        }

                        fields.setLength(fields.length() - 1);

                        try {
                            return MessageFormat.format(
                                    COMMAND_URL_PATTERN,
                                    YahooIdpSearchCommand.this.getPartnerId(),
                                    conf.getDatabase(),
                                    URLEncoder.encode(conf.getDateRange().length() >0 ? conf.getDateRange() : dateRange , "UTF-8"),
                                    YahooIdpSearchCommand.this.getOffset(),
                                    conf.getResultsToReturn(),
                                    (0 < conf.getRegion().length() ? "Region=" + conf.getRegion() + '&' : ""),
                                    conf.getRegionMix(),
                                    "enabled".equals(conf.getSpellState()) ? "&SpellState=enabled" : "",
                                    (0 < conf.getLanguage().length() ? "Language=" + conf.getLanguage() + '&' : ""),
                                    conf.getLanguageMix(),
                                    conf.getEncoding(),
                                    fields.toString(),
                                    YahooIdpSearchCommand.this.getParameter("unique") != null ? "" : conf.getUnique(),
                                    conf.getFilter(),
                                    URLEncoder.encode(wrappedTransformedQuery, "UTF-8"),
                                    YahooIdpSearchCommand.this.getAffilDataParameter()
                                    );

                        } catch (UnsupportedEncodingException ex) {
                            throw new SearchCommandException(ERR_FAILED_CREATING_URL, ex);
                        }
                    }
            });
    }

    // Public --------------------------------------------------------

    public ResultList<ResultItem> execute() {

        try {

            final ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

            if(getTransformedQuery().trim().length() > 0
                    || getFilter().trim().length() > 0
                    || "*".equals(getQuery().getQueryString())){

                final Document doc = getXmlRestful().getXmlResult();

                if (doc != null) {
                    final Element searchResponseE = doc.getDocumentElement();
                    final Element headerE = (Element) searchResponseE.getElementsByTagName(HEADER_ELEMENT).item(0);
                    final Element totalHitsE = (Element) headerE.getElementsByTagName(TOTALHITS_ELEMENT).item(0);
                    final Element deepHitsE = (Element) headerE.getElementsByTagName(DEEPHITS_ELEMENT).item(0);
                    final Element numResultsE = (Element) headerE.getElementsByTagName(NUMRESULT_ELEMENT).item(0);

                    int totalHits;
                    try {
                        totalHits = Integer.parseInt(totalHitsE.getFirstChild().getNodeValue());
                    }catch(NumberFormatException e) {
                        totalHits = Integer.MAX_VALUE;
                    }
                    searchResult.addField("totalhits", String.valueOf(totalHits));

                    int deepHits;
                    try {
                        deepHits = Integer.parseInt(deepHitsE.getFirstChild().getNodeValue());
                    }catch(NumberFormatException e) {
                        deepHits = Integer.MAX_VALUE;
                    }
                    searchResult.addField("deephits", String.valueOf(deepHits));
                    searchResult.setHitCount(deepHits);

                    if(searchResult.getHitCount() > totalHits) {
                        searchResult.addField("hasMoreHits", "true");
                    }

                    int numResults;
                    try {
                        numResults = Integer.parseInt(numResultsE.getFirstChild().getNodeValue());
                    }catch(NumberFormatException e) {
                        numResults = Integer.MAX_VALUE;
                    }
                    searchResult.addField("numResults", String.valueOf(numResults));


                    // build results
                    final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT);
                    for (int i = 0; i < list.getLength(); ++i) {
                        final Element listing = (Element) list.item(i);
                        final BasicResultItem item = createItem(listing);
                        // HACK to certain hide domains
                        final String hideDomain = getSearchConfiguration().getHideDomain();
                        final String host = new URL(item.getField("clickurl")).getHost().replaceAll("/$","");
                        if(hideDomain.length() == 0 || !host.endsWith(hideDomain)){
                            searchResult.addResult(item);
                        } else {
                            // Improvent of HACK. Keeps the hitcount more accurate. SEARCH-2032
                            searchResult.setHitCount(searchResult.getHitCount() - 1);
                        }
                    }
                    // build navigators
                    final NodeList wordCountList = searchResponseE.getElementsByTagName(WORDCOUNTS_ELEMENT);
                    for (int i = 0; i < wordCountList.getLength(); ++i) {
                        final Element listing = (Element) wordCountList.item(i);
                        // TODO make modifiers fast independant!
    //                  final Modifier modifier = new Modifier()
    //                  getRunningQuery().addSource(modifier);
                    }
                }
            }
            return searchResult;

        } catch (SocketTimeoutException ste) {

            LOG.error(getSearchConfiguration().getId() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();

        } catch (IOException e) {
            throw new SearchCommandException(e);

        } catch (SAXException e) {
            throw new SearchCommandException(e);
        }
    }

    /** Assured that associated SearchConfiguration is always of this type. **/
    @Override
    public YahooIdpCommandConfig getSearchConfiguration() {
        return (YahooIdpCommandConfig)super.getSearchConfiguration();
    }

    @Override
    public String getTransformedQuery() {
        final String tq = super.getTransformedQuery();
        if(tq == null) {
            LOG.debug("transformedQuery is null, using \"\"");
            return "";
        }
        return tq;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected String getFilter() {
        return super.getFilter();
    }

    @Override
    protected int getOffset() {
        return super.getOffset();
    }

    @Override
    protected String getParameter(String paramName) {
        return super.getParameter(paramName);
    }

    @Override
    protected BasicResultItem createItem(final Element result) {

        final BasicResultItem item = new BasicResultItem();

        for (final Map.Entry<String,String> entry : context.getSearchConfiguration().getResultFieldMap().entrySet()){

            final Element fieldE = (Element) result.getElementsByTagName(entry.getKey().toUpperCase()).item(0);
            if(fieldE.getChildNodes().getLength() >0){
                item.addField(entry.getValue(), fieldE.getFirstChild().getNodeValue());
            }
        }

        return item;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
