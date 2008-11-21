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
package no.sesat.search.query.transform;

import java.util.Map;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.BinaryOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import org.apache.log4j.Logger;

/** Transforms the query into <br/>
 * titles:^"query"$
 * <br/>
 *   Ensures that only an exact match within the titles field is returned.
 *
 *
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class ExactMatchQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(ExactMatchQueryTransformer.class);

    private transient boolean writtenStart = false;
    private transient Boolean visitingLast = null;
    private final StringBuilder exact;
    private LeafClause first;
    private ExactMatchQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public ExactMatchQueryTransformer(final QueryTransformerConfig config){

        this.config = (ExactMatchQueryTransformerConfig) config;

        final String filter = null != this.config.getField()
                    ? this.config.getField() + ':'
                    : "";

        exact = new StringBuilder(filter + "^\"");

    }


    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {

        if(!writtenStart){

            first = clause;
            exact.append(getTransformedTerms().get(clause).replaceAll("^\"", ""));

            writtenStart = true;
            // also, if we got here without giving visitingLast a value then this is the only LeafClause in the query
            visitingLast = null == visitingLast;

        }else{
            exact.append(' ' + getTransformedTerms().get(clause));
            // everything gets blanked by default
            getTransformedTerms().put(clause, "");
        }

        if(visitingLast){
            visitLast();
        }
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final BinaryOperatorClause clause) {

        // remember what visitingLast was
        final Boolean original = visitingLast;
        // turn it off. left child can never be the last term in the query.
        visitingLast = false;
        clause.getFirstClause().accept(this);
        // restore visitingLast.
        visitingLast = original;
        if( null == visitingLast ){
            //  if it is yet to be assigned an value (ie this is the topmost DoubleOperatorClause) then assign true.
            visitingLast = true;
        }

        clause.getSecondClause().accept(this);
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final UnaryClause clause) {

        visitLast();

        // not visiting will mean they remain unblanked. shouldn't be an issue as they'll appear after the $
    }

    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

    private void visitLast(){

        if(exact.charAt(exact.length()-1) == '\"'){
            exact.setLength(exact.length()-1);
        }
        exact.append("\"$");
        // first clause gets the whole phrased string
        getTransformedTerms().put(first, exact.toString());
    }
}
