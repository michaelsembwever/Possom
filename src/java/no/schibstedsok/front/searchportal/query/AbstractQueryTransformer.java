package no.schibstedsok.front.searchportal.query;

/**
 * AbstractQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public abstract class AbstractQueryTransformer implements QueryTransformer {


    public String getFilter(){
        return null;
    }
}
