/* Copyright (2008) Schibsted Søk AS
 *   This file is part of SESAT.
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

package no.sesat.search.mode.config.querybuilder;

import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * @version $Id$
 */
public abstract class AbstractQueryBuilderConfig
        implements QueryBuilderConfig, QueryBuilderConfig.ModesW3cDomDeserialiser {

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractQueryBuilderConfig.class);

    // Attributes ----------------------------------------------------

    private boolean orGrouped = false;
    private boolean andGrouped = false;
    private boolean notGrouped = false;
    private boolean defaultGrouped = false;
    private boolean supportsNot = true;
    private String notPrefix = "NOT ";

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public AbstractQueryBuilderConfig() {
    }

    protected AbstractQueryBuilderConfig(
            final String notPrefix,
            final boolean orGrouped,
            final boolean andGrouped,
            final boolean defaultGrouped){

        this();
        this.notPrefix = notPrefix;
        this.orGrouped = orGrouped;
        this.andGrouped = andGrouped;
        this.defaultGrouped = defaultGrouped;
    }

    // Public --------------------------------------------------------

    public boolean getAndGrouped() {
        return andGrouped;
    }

    /**
     * Not configurable yet.
     * @return (
     */
    public String getAndGroupOpen(){
        return "(";
    }

    /**
     * Not configurable yet.
     * @return )
     */
    public String getAndGroupClose(){
        return ")";
    }

    public boolean getOrGrouped() {
        return orGrouped;
    }
    /**
     * Not configurable yet.
     * @return (
     */
    public String getOrGroupOpen(){
        return "(";
    }

    /**
     * Not configurable yet.
     * @return )
     */
    public String getOrGroupClose(){
        return ")";
    }

    public boolean getNotGrouped() {
        return notGrouped;
    }

    /**
     * Not configurable yet.
     * @return (
     */
    public String getNotGroupOpen(){
        return "(";
    }

    /**
     * Not configurable yet.
     * @return )
     */
    public String getNotGroupClose(){
        return ")";
    }

    public boolean getDefaultGrouped(){
        return defaultGrouped;
    }
    /**
     * Not configurable yet.
     * @return (
     */
    public String getDefaultGroupOpen(){
        return "(";
    }

    /**
     * Not configurable yet.
     * @return )
     */
    public String getDefaultGroupClose(){
        return ")";
    }

    public boolean getSupportsNot() {
        return supportsNot;
    }

    public void setAndGrouped(final boolean andGrouped) {
        this.andGrouped = andGrouped;
    }

    public void setOrGrouped(final boolean orGrouped) {
        this.orGrouped = orGrouped;
    }

    public void setDefaultGrouped(final boolean defaultGrouped){
        this.defaultGrouped = defaultGrouped;
    }

    public void setNotGrouped(final boolean notGrouped) {
        this.notGrouped = notGrouped;
    }

    public void setSupportsNot(final boolean supported) {
        supportsNot = supported;
    }

    @Override
    public AbstractQueryBuilderConfig readQueryBuilder(final Element element) {

        AbstractDocumentFactory.fillBeanProperty(this, null, "notPrefix", ParseType.String, element, "NOT ");
        AbstractDocumentFactory.fillBeanProperty(this, null, "orGrouped", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "andGrouped", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "notGrouped", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "defaultGrouped", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "supportsNot", ParseType.Boolean, element, "true");
        return this;
    }

    /**
     * The NOT keyword prefix.
     * This is not padded when placed infront of any NotClause
     * so it is crucial to add a space suffix if padding is required.
     *
     * @return
     */
    public String getNotPrefix() {
        return notPrefix;
    }

    public void setNotPrefix(final String not) {
        notPrefix = not;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
