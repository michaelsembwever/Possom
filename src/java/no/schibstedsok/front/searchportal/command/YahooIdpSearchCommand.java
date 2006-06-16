// Copyright (2006) Schibsted Søk AS
/*
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.YahooIdpConfiguration;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
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
    
    private static final String ERR_FAILED_CREATING_URL = "Failed to create command url";

    private static final String COMMAND_URL_PATTERN =
            "/search?Client={0}&Database={1}&DateRange={2}&FirstResult={3}&Numresults={4}&RegionMix={5}&SpellState={6}&"
            + "QueryEncoding={7}&QueryLanguage=unknown&Fields={8}&Unique={9}&"
            + "Query={10}";

    private static final String RESULT_ELEMENT_NAME = "result";

    public YahooIdpSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {
        try {
            final Document doc = getXmlResult();
            final SearchResult searchResult = new BasicSearchResult(this);


            if (doc != null) {
                final Element searchResponseE = doc.getDocumentElement();
                final Element headerE = (Element) searchResponseE.getElementsByTagName("HEADER").item(0);
                final Element totalHitsE = (Element) headerE.getElementsByTagName("TOTALHITS").item(0);
                searchResult.setHitCount(Integer.parseInt(totalHitsE.getTextContent()));

                // build results
                final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT_NAME);
                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicSearchResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
                // build navigators

                final NodeList wordCountList = searchResponseE.getElementsByTagName("WORDCOUNTS");
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

    protected String createRequestURL() {

        final YahooIdpConfiguration conf = (YahooIdpConfiguration) context.getSearchConfiguration();

        final String dateRange = "-" + new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        final String wrappedTransformedQuery = "ALLWORDS(" + getTransformedQuery() + ')';

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
                    URLEncoder.encode( conf.getDateRange().length() >0 ? conf.getDateRange() : dateRange , "UTF-8"),
                    getParameter("offset"),
                    conf.getResultsToReturn(),
                    conf.getRegionMix(),
                    conf.getSpellState(),
                    conf.getEncoding(),
                    fields.toString(),
                    conf.getUnique(),
                    wrappedTransformedQuery
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
            item.addField(entry.getValue(), result.getElementsByTagName(entry.getKey()).item(0).getTextContent());
        }

        return item;
    }

    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            appendToQueryRepresentation(getTransformedTerm(clause));
        }
    }
    protected void visitImpl(final PhraseClause clause) {
        if (clause.getField() == null) {
            appendToQueryRepresentation("PHRASEWORDS(" + getTransformedTerm(clause) + ')');
        }
    }
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    protected void visitImpl(final AndClause clause) {
        appendToQueryRepresentation("ALLWORDS(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation("ANYWORDS(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }
    protected void visitImpl(final AndNotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }
    protected void visitImpl(final XorClause clause) {
        // [TODO] we need to determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        clause.getFirstClause().accept(this);
        // clause.getSecondClause().accept(this);
    }


}
