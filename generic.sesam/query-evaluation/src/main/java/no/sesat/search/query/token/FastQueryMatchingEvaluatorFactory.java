/* Copyright (2005-2008) Schibsted Søk AS
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
package no.sesat.search.query.token;

import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/** Responsible for loading and serving the VeryFast (Fast Query Matching) Token Evaluator.
 *
 * One VeryFastTokenEvaluator is constructed per query and used for all tokens of type FAST.
 *
 * @version <tt>$Id$</tt>
 */
public final class FastQueryMatchingEvaluatorFactory extends AbstractEvaluatorFactory{

    // Constants -----------------------------------------------------

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(FastQueryMatchingEvaluatorFactory.class);

    private static final String ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR = "Failed to construct the fast evaluator";
    private static final String ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED =
            "Interrupted waiting for FastEvaluatorCreator. Evaluation on this query will fail.";


    // Attributes -----------------------------------------------------

    private final Future fastEvaluatorCreator;
    private VeryFastTokenEvaluator fastEvaluator;

    // Constructors -----------------------------------------------------

    public FastQueryMatchingEvaluatorFactory(final Context cxt) throws SiteKeyedFactoryInstantiationException {

        super(cxt);

        try {
            VeryFastTokenEvaluator.initImpl(cxt);

        }catch (ParserConfigurationException ex) {
            throw new SiteKeyedFactoryInstantiationException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, ex);
        }

        fastEvaluatorCreator = EXECUTOR.submit(new FastEvaluatorCreator(cxt));
    }

    public TokenEvaluator getEvaluator(final TokenPredicate token) throws EvaluationException{

        final Context cxt = getContext();

        TokenEvaluator result = isResponsibleFor(token) ? getFastEvaluator() : null;
        if(result == null && null != cxt.getSite().getParent()){

            result = instanceOf(ContextWrapper.wrap(
                    Context.class,
                    cxt.getSite().getParent().getSiteContext(),
                    cxt
                )).getEvaluator(token);
        }
        if(null == result || TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR == result){
            // if we cannot find an evaulator, then always fail evaluation.
            //  Rather than encourage a NullPointerException
            result = TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR;
        }
        return result;
    }

    @Override
    public boolean isResponsibleFor(final TokenPredicate token) {

        try {

            return getFastEvaluator().isResponsibleFor(token);

        }catch (EvaluationException ex) {
            LOG.error("failed using VeryFastTokenEvaluator", ex);
            return false;
        }
    }


    // private -----------------------------------------------------

    private VeryFastTokenEvaluator getFastEvaluator() throws EvaluationException {

        try {
            fastEvaluatorCreator.get();

        } catch (InterruptedException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
            throw new EvaluationException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, ex);
        } catch (ExecutionException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
            throw new EvaluationException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, ex);
        }
        if( null == fastEvaluator ){
            throw new EvaluationException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, new NullPointerException());
        }

        return fastEvaluator;
    }

    // inner classes -----------------------------------------------------

    private final class FastEvaluatorCreator implements Runnable{

        private final Context context;

        private FastEvaluatorCreator(final Context cxt) {

            this.context = cxt;
        }

        public void run() {

            MDC.put("UNIQUE_ID", context.getUniqueId());
            try {

                fastEvaluator = new VeryFastTokenEvaluator(context);

            } catch (EvaluationException ex) {
                LOG.error(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR);
            }
            MDC.remove("UNIQUE_ID");
        }

    }

}
