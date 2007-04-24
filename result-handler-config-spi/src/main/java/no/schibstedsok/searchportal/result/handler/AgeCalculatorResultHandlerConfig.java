// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/**
 * Calculate Age.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("AgeCalculatorResultHandler")
public final class AgeCalculatorResultHandlerConfig extends AbstractResultHandlerConfig {

    private String targetField;
    private String sourceField;
    private String recursiveField;
    private String ageFormatKey = "age";
    private Boolean asDate = Boolean.FALSE;

    private static final Logger LOG = Logger.getLogger(AgeCalculatorResultHandlerConfig.class);


    /**
     * @return
     */
    public String getRecursiveField() {
        return recursiveField;
    }

    /**
     * @param recursiveField
     */
    public void setRecursiveField(String recursiveField) {
        this.recursiveField = recursiveField;
    }


    /**
     * @return
     */
    public String getTargetField() {
        return targetField;
    }


    /**
     * @param targetField
     */
    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }


    /**
     * @param string
     */
    public void setSourceField(final String string) {
        sourceField = string;
    }

    /**
     * @return
     */
    public String getSourceField() {
        return sourceField;
    }

    /**
     * @param asDate
     */
    public void setAsDate(final Boolean asDate) {
        this.asDate = asDate;
    }

    /**
     * @return
     */
    public Boolean getAsDate() {
        return asDate;
    }

    public String getAgeFormatKey() {
        return ageFormatKey;
    }

    public void setAgeFormatKey(String ageFormatKey) {
        this.ageFormatKey = ageFormatKey;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        setTargetField(element.getAttribute("target"));
        setSourceField(element.getAttribute("source"));
        AbstractDocumentFactory.fillBeanProperty(this, null, "asDate", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "recursiveField", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "ageFormatKey", ParseType.String, element, "age");
        return this;
    }


}
