/* Copyright (2006-2008) Schibsted ASA
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


import no.sesat.search.query.XorClause;
import no.sesat.commons.visitor.AbstractReflectionVisitor;
import org.apache.log4j.Logger;

/**
 * AbstractQueryTransformer is part of no.sesat.search.query
 *
 *

 * @vesrion $Id: AbstractQueryTransformer.java 3359 2006-08-03 08:13:22Z mickw $
 */
public abstract class AbstractQueryTransformer extends AbstractReflectionVisitor implements QueryTransformer {

    private static final Logger LOG = Logger.getLogger(AbstractQueryTransformer.class);

    private static final String INFO_OLD_IMPLEMENTATION_STILL = " has not been adapted to Visitor pattern";

    private Context context;

    /** Only to be used by XStream and tests **/
    protected AbstractQueryTransformer(){
    }

    public void setContext(final Context cxt) {
        context = cxt;
    }

    /** Get the context that was set with setContext()
     *
     * @return
     */
    protected Context getContext() {
        return context;
    }

    public String getFilter() {
        return "";
    }

    public String getFilter(final java.util.Map parameters) {
        return "";
    }

    /** @deprecated modify the context's transformedTerms map instead **/
    public String getTransformedQuery() {
        return getContext().getTransformedQuery();
    }

    @Override
    protected void visitImpl(final Object clause) {
        LOG.info( getClass().getSimpleName() + INFO_OLD_IMPLEMENTATION_STILL);
    }


    protected final void visitImpl(final XorClause clause) {

        getContext().visitXorClause(this, clause);
    }

}
