/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.mode.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import no.sesat.search.query.token.JepTokenEvaluator;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import org.apache.log4j.Logger;
import java.text.NumberFormat;
import no.sesat.search.query.token.Categories;
import no.sesat.search.query.token.EvaluationException;
import no.sesat.search.query.token.TokenEvaluator;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.nfunk.jep.type.Complex;

/** Create a single result item that transforms the query into a mathematical expression with it's solution.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class MathExpressionSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(MathExpressionSearchCommand.class);
    private static final String ERR_INTERRUPTED = "Interrupted -- ";
    private static final double ZERO_THREASHOLD = 0.00000001D;

    /** Default Constructor.
     * @param cxt         The context to work within.
     */
    public MathExpressionSearchCommand(final Context cxt) {

        super(cxt);
    }

    @Override
    public ResultList<ResultItem> execute() {


        final NumberFormat f = NumberFormat.getInstance();

        final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

        try{
            final TokenEvaluator evaluator = getEngine().getEvaluator(Categories.MATH);
            final Method getComplex = evaluator.getClass().getMethod("getComplex", String.class);
            final Complex result = (Complex) getComplex.invoke(evaluator, getQuery().getQueryString());

            if (result != null) {
                final StringBuilder s = new StringBuilder(f.format(result.re()));

                if (Math.abs(result.im()) > ZERO_THREASHOLD) {
                    if (result.im() < 0) {

                        s.append(" - " + f.format(Math.abs(result.im())) + "i");
                    } else {
                        s.append(" + " + f.format(result.im()) + "i");
                    }
                }

                ResultItem item = new BasicResultItem();

                final String r = getQuery().getQueryString() + " = " + s;

                LOG.debug("Adding result " + r);

                item = item.addField("result", r);

                searchResult.setHitCount(1);
                searchResult.addResult(item);
            }

        }catch(EvaluationException ie){
            LOG.warn(ERR_INTERRUPTED + ie);
        }catch(NoSuchMethodException nsme){
            LOG.warn(ERR_INTERRUPTED + nsme);
        }catch(IllegalAccessException iae){
            LOG.warn(ERR_INTERRUPTED + iae);
        }catch(InvocationTargetException ite){
            LOG.warn(ERR_INTERRUPTED + ite);
        }
        return searchResult;
    }
}
