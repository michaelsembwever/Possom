/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
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

/**
 *
 * @author mick
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
