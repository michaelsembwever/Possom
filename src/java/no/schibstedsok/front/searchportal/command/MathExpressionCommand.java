// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.front.searchportal.query.token.JepTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;

import java.util.Map;
import java.text.NumberFormat;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MathExpressionCommand extends AbstractSearchCommand {
    
    private static final Logger log = Logger.getLogger(MathExpressionCommand.class);
    private static final double ZERO_THREASHOLD = 0.00000001D;

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public MathExpressionCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }


    public SearchResult execute() {

        final Complex result = ((JepTokenEvaluator)context.getRunningQuery()
                .getTokenEvaluatorFactory()
                .getEvaluator(TokenPredicate.MATHPREDICATE))
                .getComplex();
        final NumberFormat f = NumberFormat.getInstance();

        final BasicSearchResult searchResult = new BasicSearchResult(this);

        
        if (result != null) {
            String s = null;

            s = f.format(result.re());

            if (Math.abs(result.im()) > ZERO_THREASHOLD) {
                if (result.im() < 0) {

                    s = s + " - " + f.format(Math.abs(result.im())) + "i";
                } else {
                    s = s + " + " + f.format(result.im()) + "i";
                }
            }

            final SearchResultItem item = new BasicSearchResultItem();

            final String r = context.getQuery().getQueryString() + " = " + s;

            if (log.isDebugEnabled()) {
                log.debug("Adding result " + r);
            }

            item.addField("result", r);

            searchResult.setHitCount(1);
            searchResult.addResult(item);
        }

        return searchResult;
    }
}
