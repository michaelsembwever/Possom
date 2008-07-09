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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Responsible for loading and serving the JEP Expression Token Evaluators.
 * Which TokenPredicates it is applicable to is defined in JEP_EVALUATOR_XMLFILE configuration file.
 *
 *  <b>Immutable</b> (although hidden lazy static-initialisation exists).
 *
 * @version <tt>$Id$</tt>
 */
public final class JepEvaluatorFactory extends AbstractEvaluatorFactory{

    private static final Logger LOG = Logger.getLogger(JepEvaluatorFactory.class);


    /** The name of the file where regular expressions for each TokenPredicate will be configured. **/
    public static final String JEP_EVALUATOR_XMLFILE = "JepEvaluators.xml";

    private static final String ERR_DOC_BUILDER_CREATION
            = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";

    private volatile boolean init = false;

    /** JepTokenEvaluator's to use against the "*" query. Notes which tokens we're applicable to. **/
    private final Map<TokenPredicate,JepTokenEvaluator> jepEvaluators
            = new HashMap<TokenPredicate,JepTokenEvaluator>();

    public JepEvaluatorFactory(final Context cxt)
            throws SiteKeyedFactoryInstantiationException {

        super(cxt);

        try{
            init();

        }catch(ParserConfigurationException pce){
            throw new SiteKeyedFactoryInstantiationException(ERR_DOC_BUILDER_CREATION, pce);
        }

    }

    private void init() throws ParserConfigurationException {

        if (!init) {
            synchronized(jepEvaluators){
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                final DocumentBuilder builder = factory.newDocumentBuilder();

                final DocumentLoader loader = getContext().newDocumentLoader(getContext(), JEP_EVALUATOR_XMLFILE, builder);

                loader.abut();
                LOG.info("Parsing " + JEP_EVALUATOR_XMLFILE + " started");
                final Document doc = loader.getDocument();

                assert null != doc : "No document loaded for " + getContext().getSite().getName();

                final Element root = doc.getDocumentElement();
                if(null != root){
                    final NodeList evaluators = root.getElementsByTagName("evaluator");
                    for (int i = 0; i < evaluators.getLength(); ++i) {

                        final Element evaluator = (Element) evaluators.item(i);

                        final String tokenName = evaluator.getAttribute("token");
                        LOG.info(" ->evaluator@token: " + tokenName);

                        TokenPredicate token;
                        try{
                            token = TokenPredicateUtility.getTokenPredicate(tokenName);

                        }catch(IllegalArgumentException iae){
                            LOG.debug(tokenName + " does not exist. Will create it. Underlying exception was " + iae);
                            token = TokenPredicateUtility.createAnonymousTokenPredicate(
                                    tokenName);
                        }

                        final boolean queryDep = Boolean.parseBoolean(evaluator.getAttribute("query-dependant"));
                        LOG.info(" ->evaluator@query-dependant: " + queryDep);

                        final JepTokenEvaluator jepTokenEvaluator = new JepTokenEvaluator("*", queryDep);
                        jepEvaluators.put(token, jepTokenEvaluator);

                    }
                }
                LOG.info("Parsing " + JEP_EVALUATOR_XMLFILE + " finished");
                init = true;
            }
        }
    }

    public TokenEvaluator getEvaluator(final TokenPredicate token) throws EvaluationException {

        final Context cxt = getContext();

        TokenEvaluator result = jepEvaluators.get(token);
        if(result == null && null != cxt.getSite().getParent()){

            result = instanceOf(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext(){
                        public Site getSite(){
                            return cxt.getSite().getParent();
                        }
                    },
                    cxt
                )).getEvaluator(token);
        }
        if(null == result || TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR == result){
            // if we cannot find an evaulator, then always fail evaluation.
            //  Rather than encourage a NullPointerException
            return TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR;
        }
        return "*".equals(getContext().getQueryString())
                ? result
                : new JepTokenEvaluator(getContext().getQueryString(), result.isQueryDependant(token));
    }

}
