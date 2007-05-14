// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.query.token.JepTokenEvaluator;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.query.token.VeryFastListQueryException;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import org.apache.log4j.Logger;
import java.text.NumberFormat;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.nfunk.jep.type.Complex;

/** Create a single result item that transforms the query into a mathematical expression with it's solution.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class MathExpressionSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(MathExpressionSearchCommand.class);
    private static final String ERR_INTERRUPTED = "Interrupted";
    private static final double ZERO_THREASHOLD = 0.00000001D;

    /**
     * @param cxt         The context to work within.
     */
    public MathExpressionSearchCommand(final Context cxt) {

        super(cxt);
    }


    /** {@inherit} **/
    public ResultList<? extends ResultItem> execute() {


        final NumberFormat f = NumberFormat.getInstance();

        final BasicSearchResult<ResultItem> searchResult = new BasicSearchResult<ResultItem>();

        try{
            final Complex result = ((JepTokenEvaluator)getEngine()
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

                ResultItem item = new BasicSearchResultItem();

                final String r = getQuery().getQueryString() + " = " + s;

                LOG.debug("Adding result " + r);

                item = item.addField("result", r);

                searchResult.setHitCount(1);
                searchResult.addResult(item);
            }

        }catch(VeryFastListQueryException ie){
            LOG.warn(ERR_INTERRUPTED);
        }
        return searchResult;
    }
}
