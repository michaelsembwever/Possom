/*
 * Copyright (2005-2007) Schibsted Søk AS
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
package no.sesat.search.query.transform;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import org.apache.log4j.Logger;

/**
 * A transformer to apply a regular expression to each term.
 * TODO Does not handle multi-term applications yet.
 *
 * If the regular expression has a capturing group,
 * it is only that group that is replacement,
 * not the match to the whole regular expression.
 * <b>It is therefore critical to use non-capturing groups for |?+* operations in the expressions.</b>
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 *
 */
public final class RegexpQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(RegexpQueryTransformer.class);

    private static final String DEBUG_APPLIED_REGEXP = "Applied regexp to term ";

    private Pattern regExPattern;
    private final ClassLoader cl = RegexpQueryTransformerConfig.class.getClassLoader();
    private final RegexpQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public RegexpQueryTransformer(final QueryTransformerConfig config){
        final ClassLoader cl = config.getClass().getClassLoader();
        this.config = (RegexpQueryTransformerConfig) config;
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {

        final String term = (String) getTransformedTerms().get(clause);
        if(null != term && term.length()>0){
            if(regExPattern == null){
                regExPattern = Pattern.compile(config.getRegexp());
            }
            final Matcher m = regExPattern.matcher(term);
            if(m.find()){
                LOG.debug(DEBUG_APPLIED_REGEXP + term);
                getTransformedTerms().put(clause, m.replaceAll(config.getReplacement()));
            }
        }
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final DoubleOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

}
