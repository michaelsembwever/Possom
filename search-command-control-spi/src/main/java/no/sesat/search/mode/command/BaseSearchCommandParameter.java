/*
 * Copyright (2008) Schibsted Søk AS
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
 *
 */
package no.sesat.search.mode.command;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import org.apache.log4j.Logger;

/** A base implementation that provides looking up the value in order from a number of sources.
 * Current sources implemented are REQUEST, USER, and CONFIGURATION. <br/><br/>
 *
 * isActive() always returns true. subclass to implement dynamic usecases. <br/><br/>
 *
 * {@link http://sesat.no/new-design-proposal-for-searchcommand-and-abstractsearchcommand.html}
 * <br/>
 * @see Origin
 *
 * @version $Id$
 */
class BaseSearchCommandParameter implements SearchCommandParameter {

    // Static --------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BaseSearchCommandParameter.class);

    protected enum Origin {
        /**
         * Obtain the value from the datamodel's parameters.
         * The plain string representation is returned.
         * The user must encode or escape if necessary.
         */
        REQUEST,
        /**
         * Obtain the value from the datamodel's user.
         */
        USER,
        /**
         * Obtain the value from the SearchConfiguration through introspection.
         */
        CONFIGURATION,
        /**
         * Obtain the value in a custom manner. getValue() must be overridden to provide this 'custom manner'.
         */
        CUSTOM
    };

    // Attributes ----------------------------------------------------

    private final String name;

    private final Origin[] lookupOrder;

    private transient Origin origin = null;

    private final SearchCommand.Context context;

    private final SearchConfiguration searchConfiguration;

    private final DataModel datamodel;

    // Constructors --------------------------------------------------

    BaseSearchCommandParameter(
            final SearchCommand.Context context,
            final String name,
            final Origin... lookupOrder) {

        this.context = context;
        this.name = name;
        this.lookupOrder = Arrays.copyOf(lookupOrder, lookupOrder.length);
        this.searchConfiguration = context.getSearchConfiguration();
        this.datamodel = context.getDataModel();
    }

    // Public --------------------------------------------------------

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return true;
    }

    public String getValue() {

        String result = null;
        if (isActive()) {

            for (Origin origin : lookupOrder) {

                if (null != this.origin) {
                    // shortcut to the origin found last time.
                    origin = this.origin;
                }
                switch (origin) {

                    case REQUEST:
                        final StringDataObject sdo = datamodel.getParameters().getValue(name);

                        if (null != sdo) {
                            result = sdo.getString();
                        }
                        break;

                    case USER:
                        result = datamodel.getUser().getUser().getUserPropertiesMap().get(name);
                        break;

                    case CONFIGURATION:

                        try{
                            final PropertyDescriptor[] properties = Introspector.getBeanInfo(
                                    searchConfiguration.getClass())
                                    .getPropertyDescriptors();

                            for (PropertyDescriptor property : properties) {
                                if (name.equals(property.getName())) {

                                    if (null != property.getReadMethod()) {

                                        result = (String) property.getReadMethod().invoke(
                                                searchConfiguration,
                                                new Object[0]);

                                        break;
                                    }
                                }
                            }
                        }catch(IntrospectionException ie){
                            LOG.error("Failed to find parameter", ie);
                        }catch(IllegalAccessException ie){
                            LOG.error("Failed to find parameter", ie);
                        }catch(InvocationTargetException ie){
                            LOG.error("Failed to find parameter", ie);
                        }
                        break;

                    case CUSTOM:
                        throw new UnsupportedOperationException(
                                "SearchParameter's with Origin.CUSTOM must override getValue");
                }
                if (null != result) {
                    this.origin = origin;
                    break;
                }
            }
        }
        return result;

    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** The origin the current value was found from.
     *
     * @return the origin the current value was found from.
     */
    protected Origin getOrigin() {

        if (null == origin) {
            getValue();
        }
        return origin;
    }

    /** The context the command is running within.
     *
     * @return the context the command is running within.
     */
    protected SearchCommand.Context getContext(){
        return context;
    }

    // Private -------------------------------------------------------
}
