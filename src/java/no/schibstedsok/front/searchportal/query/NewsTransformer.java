package no.schibstedsok.front.searchportal.query;



/**
 * NewsTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class NewsTransformer  implements QueryTransformer {



    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery(String originalQuery) {
       return originalQuery;
    }

    /**
     * Set filter for thiw query.
     * Example to add docdatetime argument
     * <p/>
     * +docdatetime:>2005-10-28
     *
     * @return filterstring
     */
    public String getFilter(String origQuery) {
        if(origQuery == null) {
            throw new IllegalArgumentException("setQuery not called with minimum empty query");
        }

        if("".equals(origQuery.trim())){
            System.out.println("Transformer: set filter..");
            return " +size:>0 ";
        }

        // FOR FUTURE USE
        /*
        if("Siste Norske Nyheter".equals(origQuery)){
            return "+contentsource:Norske Nyheter";
        }else if("Siste Nordiske Nyheter".equals(origQuery)){
            return "+contentsource:Nordiske Nyheter";
        }else if("Siste Internasjonale Nyheter".equals(origQuery)){
            return "+contentsource:Internasjonale Nyheter";
        }else if("Siste Mediearkivet Nyheter".equals(origQuery)){
            return "+contentsource:Mediearkivet";
        }
        */
        return null;
    }


}
