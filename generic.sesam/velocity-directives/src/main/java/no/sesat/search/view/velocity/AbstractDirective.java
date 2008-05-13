/* Copyright (2007-2008) Schibsted Søk AS
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
 *
 * AbstractDirective.java
 *
 * Created on 15/06/2007, 21:30:25
 *
 */

package no.sesat.search.view.velocity;

import no.sesat.search.datamodel.DataModel;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/** Useful wrapper around Velocity's Directive class.
 * Adds:
 *  * methods to simplify fetching arguments,
 *  * method to get the current datamodel out of the InternalContext Adapter,
 *
 *
 * @version $Id$
 */
public abstract class AbstractDirective extends Directive {

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractDirective.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public AbstractDirective() {
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected DataModel getDataModel(final InternalContextAdapter context){
        return (DataModel)context.get("datamodel");
    }

    protected String getArgument(final InternalContextAdapter context, final Node node, final int i){
        return node.jjtGetChild(i).value(context).toString();
    }

    protected Object getObjectArgument(final InternalContextAdapter context, final Node node, final int i){
        return node.jjtGetChild(i).value(context);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
