/* Copyright (2008) Schibsted Søk AS
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
package no.sesat.search.mode.command.querybuilder;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.Clause;
import no.sesat.search.query.BinaryOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;

/** Abstract QueryBuilder providing basic support for mantaining context and stringBuilder fields (and related methods).
 *
 * @version $Id$
 */
public abstract class AbstractQueryBuilder extends AbstractReflectionVisitor implements QueryBuilder {

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractQueryBuilder.class);

    // Attributes ----------------------------------------------------

    private final Context context;
    private final QueryBuilderConfig config;
    private final StringBuilder sb = new StringBuilder(128);

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public AbstractQueryBuilder(final Context cxt, QueryBuilderConfig config) {

        context = cxt;
        this.config = config;
    }

    // Public --------------------------------------------------------

    public String getQueryString() {

        final Clause root = context.getQuery().getRootClause();
        sb.setLength(0);
        visit(root);
        return sb.toString().trim();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected final Context getContext(){
        return context;
    }

    protected QueryBuilderConfig getConfig(){
        return config;
    }

    /** Gets the transformed term, escaping any reserved words.
     *
     * @param clause
     * @return
     */
    protected String getEscapedTransformedTerm(final Clause clause){

        return escape(context.getTransformedTerm(clause));

    }

    /** Escapes any reserved words (including those fielded).
     * Case-insensitive.
     *
     * How to actually escape any matching words is left to the context to define via context.escape(word)
     *
     * @param string
     * @return possibilly escaped string
     */
    protected String escape(final String string){

        for (String word : getWordsToEscape()) {

            // Case-insensitive check against word.
            // Term might already be prefixed by the TermPrefixTransformer.
            if (string.toLowerCase().endsWith(':' + word.toLowerCase()) || string.equalsIgnoreCase(word)) {

                final Pattern p = Pattern.compile(
                        Matcher.quoteReplacement(word),
                        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

                return p.matcher(word).replaceAll(context.escape(string));
            }
        }

        return string;
    }

    protected Collection<String> getWordsToEscape(){
        return context.getReservedWords();
    }

    protected final void appendToQueryRepresentation(final CharSequence addition) {
        sb.append(addition);
    }

    protected final void appendToQueryRepresentation(final char addition) {
        sb.append(addition);
    }

    protected final int getQueryRepresentationLength() {
        return sb.length();
    }

    protected final void insertToQueryRepresentation(final int offset, final CharSequence addition) {
        sb.insert(offset, addition);
    }

    protected boolean isEmptyLeaf(final Clause clause) {

        boolean result = false;

        if(clause instanceof BinaryOperatorClause){
            final BinaryOperatorClause c = (BinaryOperatorClause)clause;
            result = isEmptyLeaf(c.getFirstClause()) && isEmptyLeaf(c.getSecondClause());

        }else if(clause instanceof UnaryClause){
            final UnaryClause c = (UnaryClause)clause;
            result = isEmptyLeaf(c.getFirstClause());

        }else if(clause instanceof LeafClause){
            final LeafClause c = (LeafClause)clause;
            final String tt = 0 == context.getTransformedTerm(c).length()
                    ? null
                    : context.getTransformedTerm(c);
            result =
                // no field and a valid term
                null == c.getField() && null == tt
                // or, a field that is an accepted filter
                || null != c.getField() && null != context.getFieldFilter(c);
        }
        return result;
    }

    protected boolean isNextLeafInsideNotClause(final Clause clause){

        boolean result = false;

         if(clause instanceof NotClause){
            result = !isEmptyLeaf(clause);
        }else if(clause instanceof UnaryClause){
            result = isNextLeafInsideNotClause(((UnaryClause)clause).getFirstClause());
        }

        return result;
    }

    protected void visitImpl(final XorClause clause) {
        getContext().visitXorClause(this, clause);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
