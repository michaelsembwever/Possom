/* Copyright (2006-2007) Schibsted Søk AS
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
package no.sesat.search.result.handler;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/** Perform a JEP operation.
 *
 *
 * @version <tt>$Id$</tt>
 */
@Controller("NumberOperationHandler")
public final class NumberOperationResultHandlerConfig extends AbstractResultHandlerConfig {

    private static final Logger LOG = Logger.getLogger(NumberOperationResultHandlerConfig.class);

    private final Collection<String> fields = new ArrayList<String>();

    /**
     *
     * @param fieldArray Array of fields that should be added.
     */
    public void addFields(final String[] fieldArray) {
        fields.addAll(Arrays.asList(fieldArray));
    }

    /**
     *
     * @return
     */
    public Collection<String> getFields(){

        return Collections.unmodifiableCollection(fields);
    }
    /**
     * Holds value of property operation.
     */
    private String operation;

    /**
     * Setter for property operation.
     * @param operation New value of property operation.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     *
     * @return
     */
    public String getOperation(){
        return operation;
    }

    /**
     * Holds value of property target.
     */
    private String target;

    /**
     * Setter for property target.
     * @param target New value of property target.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     *
     * @return
     */
    public String getTarget(){
        return target;
    }

    /**
     * Holds value of property minFractionDigits.
     */
    private int minFractionDigits;

    /**
     * Setter for property minFractionDigits.
     * @param minFractionDigits New value of property minFractionDigits.
     */
    public void setMinFractionDigits(int minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    /**
     *
     * @return
     */
    public int getMinFractionDigits(){
        return minFractionDigits;
    }

    /**
     * Holds value of property maxFractionDigits.
     */
    private int maxFractionDigits;

    /**
     * Setter for property maxFractionDigitis.
     * @param maxFractionDigits New value of property maxFractionDigitis.
     */
    public void setMaxFractionDigits(int maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    /**
     *
     * @return
     */
    public int getMaxFractionDigits(){
        return maxFractionDigits;
    }

    /**
     * Holds value of property minDigits.
     */
    private int minDigits;

    /**
     * Setter for property minDigits.
     * @param minDigits New value of property minDigits.
     */
    public void setMinDigits(int minDigits) {
        this.minDigits = minDigits;
    }

    /**
     *
     * @return
     */
    public int getMinDigits(){
        return minDigits;
    }
    /**
     * Holds value of property maxDigits.
     */
    private int maxDigits;

    /**
     * Setter for property maxDigits.
     * @param maxDigits New value of property maxDigits.
     */
    public void setMaxDigits(int maxDigits) {
        this.maxDigits = maxDigits;
    }

    /**
     *
     * @return
     */
    public int getMaxDigits(){
        return maxDigits;
    }

    /** {@inherit} **/
    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        addFields(element.getAttribute("fields").split(","));
        setTarget(AbstractDocumentFactory.parseString(element.getAttribute("target"), ""));
        setOperation(AbstractDocumentFactory.parseString(element.getAttribute("operation"), ""));
        setMinDigits(AbstractDocumentFactory.parseInt(element.getAttribute("min-digits"), 1));
        setMaxDigits(AbstractDocumentFactory.parseInt(element.getAttribute("max-digits"), 99));
        setMinFractionDigits(AbstractDocumentFactory.parseInt(element.getAttribute("min-fraction-digits"), 0));
        setMaxFractionDigits(AbstractDocumentFactory.parseInt(element.getAttribute("max-fraction-digits"), 99));

        return this;
    }


}
