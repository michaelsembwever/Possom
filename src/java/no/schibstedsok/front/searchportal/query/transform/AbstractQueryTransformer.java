// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;


import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;

/**
 * AbstractQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>

 * @vesrion $Revision$, $Author$, $Date$
 */
public abstract class AbstractQueryTransformer extends AbstractReflectionVisitor implements QueryTransformer {



    public String getFilter(final Context cxt) {
        return null;
    }

    public String getFilter(final Context cxt, final java.util.Map parameters) {
        return null;
    }

    /** @deprecated modify the context's transformedTerms map instead **/
    public String getTransformedQuery(final Context cxt) {
        return cxt.getTransformedQuery();
    }

}
