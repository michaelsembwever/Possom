/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * DataModelContext.java
 *
 * Created on 19 March 2007, 10:49
 *
 */

package no.sesat.search.datamodel;

/** Defines the context for consumers of a DataModel.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public interface DataModelContext {
    /**
     * 
     * @return 
     */
    DataModel getDataModel();
}
