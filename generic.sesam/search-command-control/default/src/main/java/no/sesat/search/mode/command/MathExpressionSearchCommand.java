/* Copyright (2006-2008) Schibsted Søk AS
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

import no.sesat.search.query.token.JepTokenEvaluator;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.query.token.VeryFastListQueryException;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import org.apache.log4j.Logger;
import java.text.NumberFormat;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
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

        final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

        try{
            final Complex result = ((JepTokenEvaluator)getEngine()
                    .getEvaluator(TokenPredicate.Categories.MATHPREDICATE))
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

                ResultItem item = new BasicResultItem();

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
