/*
 * AgeFilterTransformer.java
 *
 */

package no.schibstedsok.searchportal.query.transform;

import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;

/**
 * @author maek
 * @version $Id$
 */
@Controller("AgefilterQueryTransformer")
public final class AgefilterQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private String ageField; // In seconds
    private String ageSymbol;


    /**
     *
     * @param ageField
     */
    public void setAgeField(final String ageField) {
        this.ageField = ageField;
    }

    /**
     *
     * @return
     */
    public String getAgeField(){
        return ageField;
    }

    /**
     *
     * @param ageSymbol
     */
    public void setAgeSymbol(final String ageSymbol) {
        this.ageSymbol = ageSymbol;
    }

    /**
     *
     * @return
     */
    public String getAgeSymbol(){
        return ageSymbol;
    }

    @Override
    public AgefilterQueryTransformerConfig readQueryTransformer(final Element qt) {

        super.readQueryTransformer(qt);
        setAgeField(qt.getAttribute("field"));
        String optionalAttribute = qt.getAttribute("age-symbol");
        if (optionalAttribute != null && optionalAttribute.length() > 0) {
            setAgeSymbol(optionalAttribute);
        }
        return this;
    }
}
