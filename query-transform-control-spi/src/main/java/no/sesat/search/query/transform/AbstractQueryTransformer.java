/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.query.transform;


import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * AbstractQueryTransformer is part of no.sesat.search.query
 *
 * @author <a href="ola@schibstedsok.no">ola at schibstedsok</a>

 * @vesrion $Id: AbstractQueryTransformer.java 3359 2006-08-03 08:13:22Z mickw $
 */
public abstract class AbstractQueryTransformer extends AbstractReflectionVisitor implements QueryTransformer {

    private static final Logger LOG = Logger.getLogger(AbstractQueryTransformer.class);

    private static final String INFO_OLD_IMPLEMENTATION_STILL = " has not been adapted to Visitor pattern";

    private Context context;

    /** Only to be used by XStream and tests **/
    protected AbstractQueryTransformer(){
    }

    /** {@inherit} **/
    //@Override // TODO uncomment for java 6
    public void setContext(final Context cxt) {
        context = cxt;
    }

    /**
     *
     * @return
     */
    protected Context getContext() {
        return context;
    }

    //@Override // TODO uncomment for java 6
    public String getFilter() {
        return "";
    }

    /** {@inherit} **/
    //@Override // TODO uncomment for java 6
    public String getFilter(final java.util.Map parameters) {
        return "";
    }

    /** @deprecated modify the context's transformedTerms map instead **/
    //@Override // TODO uncomment for java 6
    public String getTransformedQuery() {
        return getContext().getTransformedQuery();
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final Object clause) {
        LOG.info( getClass().getSimpleName() + INFO_OLD_IMPLEMENTATION_STILL);
    }

    /**
     *
     * @param clause
     */
    protected final void visitImpl(final XorClause clause) {

        getContext().visitXorClause(this, clause);
    }

}
