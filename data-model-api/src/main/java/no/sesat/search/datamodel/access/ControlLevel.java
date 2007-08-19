/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * ControlLevel.java
 *
 * Created on 23 January 2007, 14:20
 *
 */

package no.sesat.search.datamodel.access;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public enum ControlLevel {
    /**
     * Default state the DataModel is created in.
     * Also used when the DataModel is being cleaned at the end of each request.
     */
    DATA_MODEL_CONSTRUCTION,
    /**
     * The state while in http filters or the search servlet.
     */
    REQUEST_CONSTRUCTION,
    /**
     * The state while the RunningQuery is being constructed.
     */
    RUNNING_QUERY_CONSTRUCTION,
    /**
     * The state while the SearchCommand is being constructed.
     */
    SEARCH_COMMAND_CONSTRUCTION,
    /**
     * The state while SearchCommands are being executed.
     */
    SEARCH_COMMAND_EXECUTION,
    /**
     * The state while RunningQuery result handlers are being processed.
     */
    RUNNING_QUERY_RESULT_HANDLING, // TODO rename to RUNNING_QUERY_HANDLING
    /**
     * The state while jsps and velocity templates are being executed.
     */
    VIEW_CONSTRUCTION;


//    /**
//     *
//     * @return
//     */
//    public ControlLevel next(){
//
//        switch(this){
//            case DATA_MODEL_CONSTRUCTION:
//                return REQUEST_CONSTRUCTION;
//            case REQUEST_CONSTRUCTION:
//                return RUNNING_QUERY_CONSTRUCTION;
//            case RUNNING_QUERY_CONSTRUCTION:
//                return SEARCH_COMMAND_CONSTRUCTION;
//            case SEARCH_COMMAND_CONSTRUCTION:
//                return SEARCH_COMMAND_QUERY_TRANSFORMATION;
//            case SEARCH_COMMAND_QUERY_TRANSFORMATION:
//                return SEARCH_COMMAND_EXECUTION;
//            case SEARCH_COMMAND_EXECUTION:
//                return SEARCH_COMMAND_RESULT_HANDLING;
//            case SEARCH_COMMAND_RESULT_HANDLING:
//                return RUNNING_QUERY_RESULT_HANDLING;
//            case RUNNING_QUERY_RESULT_HANDLING:
//                return VIEW_CONSTRUCTION;
//            case VIEW_CONSTRUCTION:
//                // a new request
//                return REQUEST_CONSTRUCTION;
//
//            default:
//                throw new IllegalStateException("WTF?!");
//        }
//    }
}
