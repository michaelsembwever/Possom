// Copyright (2006) Schibsted Søk AS
/*
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.YahooIdpSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * Search against Yahoo! Index Data Protocol 2.0.
 *
 * @author mick
 * @version $Id$
 */
public final class YahooIdpSearchCommand extends AbstractYahooSearchCommand {

    private static final Logger LOG = Logger.getLogger(YahooIdpSearchCommand.class);
    private static final String ERR_FAILED_CREATING_URL = "Failed to create command url";

    private static final String COMMAND_URL_PATTERN =
            "/search?Client={0}&Database={1}&DateRange={2}&"
            + "FirstResult={3}&Numresults={4}&"
            + "Region={5}&RegionMix={6}&SpellState={7}&Language={8}&LanguageMix={9}&"
            + "QueryEncoding={10}&Fields={11}&Unique={12}&Filter={13}&"
            + "Query={14}";
    private static final String DATE_PATTERN = "yyyy/MM/dd";

    private static final String HEADER_ELEMENT = "HEADER";
    private static final String TOTALHITS_ELEMENT ="TOTALHITS";
    private static final String RESULT_ELEMENT = "RESULT";
    private static final String WORDCOUNTS_ELEMENT = "WORDCOUNTS";

    private static final String ALLWORDS = "ALLWORDS(";
    private static final String ANYWORDS = "ANYWORDS(";
    private static final String PHRASEWORDS = "PHRASEWORDS(";

    /** TODO comment me. **/
    public YahooIdpSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    /** TODO comment me. **/
    public SearchResult execute() {
        try {
            final Document doc = getXmlResult();
            final SearchResult searchResult = new BasicSearchResult(this);


            if (doc != null) {
                final Element searchResponseE = doc.getDocumentElement();
                final Element headerE = (Element) searchResponseE.getElementsByTagName(HEADER_ELEMENT).item(0);
                final Element totalHitsE = (Element) headerE.getElementsByTagName(TOTALHITS_ELEMENT).item(0);
                searchResult.setHitCount(Integer.parseInt(totalHitsE.getFirstChild().getNodeValue()));
                if(!context.getSearchConfiguration().isPaging()){
                    context.getRunningQuery().addSource(new Modifier(
                            context.getSearchConfiguration().getName(),
                            searchResult.getHitCount(), null));
                }
                LOG.info("hitcount " + searchResult.getHitCount());

                // build results
                final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT);
                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicSearchResultItem item = createItem(listing);
                    // HACK to certain hide domains
                    final String hideDomain = getSearchConfiguration().getHideDomain();
                    final String host = new URL(item.getField("clickurl")).getHost().replaceAll("/$","");
                    if(hideDomain.length() == 0 || !host.endsWith(hideDomain)){
                        searchResult.addResult(item);
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
            return searchResult;
        } catch (IOException e) {
            throw new InfrastructureException(e);
        } catch (SAXException e) {
            throw new InfrastructureException(e);
        }
    }

    /** TODO comment me. **/
    protected String createRequestURL() {

        final YahooIdpSearchConfiguration conf = (YahooIdpSearchConfiguration) context.getSearchConfiguration();

        final String dateRange = "-" + new SimpleDateFormat(DATE_PATTERN).format(new Date());

        final String wrappedTransformedQuery = ALLWORDS + getTransformedQuery() + ")";

        final StringBuilder fields = new StringBuilder();
        for(final String field : context.getSearchConfiguration().getResultFields().keySet()){
            fields.append(field);
            fields.append(',');
        }
        fields.setLength(fields.length()-1);
        try {


            return MessageFormat.format(
                    COMMAND_URL_PATTERN,
                    conf.getPartnerId(),
                    conf.getDatabase(),
                    URLEncoder.encode(conf.getDateRange().length() >0 ? conf.getDateRange() : dateRange , "UTF-8"),
                    getParameter("offset"),
                    conf.getResultsToReturn(),
                    conf.getRegion(),
                    conf.getRegionMix(),
                    conf.getSpellState(),
                    conf.getLanguage(),
                    conf.getLanguageMix(),
                    conf.getEncoding(),
                    fields.toString(),
                    conf.getUnique(),
                    conf.getFilter(),
                    URLEncoder.encode(wrappedTransformedQuery, "UTF-8")
                    );
        } catch (UnsupportedEncodingException ex) {
            throw new InfrastructureException(ERR_FAILED_CREATING_URL, ex);
        }
    }

    /**
     **/
    protected BasicSearchResultItem createItem(final Element result) {

        final BasicSearchResultItem item = new BasicSearchResultItem();

        for(final Map.Entry<String,String> entry : context.getSearchConfiguration().getResultFields().entrySet()){

            final Element fieldE = (Element) result.getElementsByTagName(entry.getKey().toUpperCase()).item(0);
            if(fieldE.getChildNodes().getLength() >0){
                item.addField(entry.getValue(), fieldE.getFirstChild().getNodeValue());
            }
        }

        return item;
    }

    /** TODO comment me. **/
    protected void visitImpl(final PhraseClause clause) {
        if (clause.getField() == null) {
            appendToQueryRepresentation(PHRASEWORDS + getTransformedTerm(clause) + ')');
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final AndClause clause) {
        appendToQueryRepresentation(ALLWORDS);
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }
    /** TODO comment me. **/
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(ANYWORDS);
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }
    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final AndNotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }


    /** Assured that associated SearchConfiguration is always of this type. **/
    public YahooIdpSearchConfiguration getSearchConfiguration() {

        return (YahooIdpSearchConfiguration)super.getSearchConfiguration();
    }

}
