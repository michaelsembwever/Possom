// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.query.token.JepTokenEvaluator;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.query.token.VeryFastListQueryException;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import org.apache.log4j.Logger;

import java.util.Map;
import java.text.NumberFormat;
import org.nfunk.jep.type.Complex;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class MathExpressionSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(MathExpressionSearchCommand.class);
    private static final String ERR_INTERRUPTED = "Interrupted";
    private static final double ZERO_THREASHOLD = 0.00000001D;

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public MathExpressionSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }


    /** TODO comment me. **/
    public SearchResult execute() {


        final NumberFormat f = NumberFormat.getInstance();

        final BasicSearchResult searchResult = new BasicSearchResult(this);

        try{
        final Complex result = ((JepTokenEvaluator)context.getTokenEvaluationEngine()
                .getEvaluator(TokenPredicate.MATHPREDICATE))
                .getComplex();

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

            LOG.debug("Adding result " + r);

            item.addField("result", r);

            searchResult.setHitCount(1);
            searchResult.addResult(item);
        }

        }catch(VeryFastListQueryException ie){
            LOG.warn(ERR_INTERRUPTED);
        }
        return searchResult;
    }
}
