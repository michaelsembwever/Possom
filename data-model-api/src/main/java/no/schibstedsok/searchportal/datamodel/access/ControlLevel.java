// Copyright (2007) Schibsted Søk AS
/*
 * ControlLevel.java
 *
 * Created on 23 January 2007, 14:20
 *
 */

package no.schibstedsok.searchportal.datamodel.access;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public enum ControlLevel {
    DATA_MODEL_CONSTRUCTION,
    REQUEST_CONSTRUCTION,
    RUNNING_QUERY_CONSTRUCTION,
    SEARCH_COMMAND_CONSTRUCTION,
    SEARCH_COMMAND_QUERY_TRANSFORMATION,
    SEARCH_COMMAND_EXECUTION,
    SEARCH_COMMAND_RESULT_HANDLING,
    RUNNING_QUERY_RESULT_HANDLING,
    VIEW_CONSTRUCTION;
    

    public ControlLevel next(){
        
        switch(this){
            case DATA_MODEL_CONSTRUCTION: 
                return REQUEST_CONSTRUCTION;
            case REQUEST_CONSTRUCTION: 
                return RUNNING_QUERY_CONSTRUCTION;
            case RUNNING_QUERY_CONSTRUCTION: 
                return SEARCH_COMMAND_CONSTRUCTION;
            case SEARCH_COMMAND_CONSTRUCTION:
                return SEARCH_COMMAND_QUERY_TRANSFORMATION;
            case SEARCH_COMMAND_QUERY_TRANSFORMATION:
                return SEARCH_COMMAND_EXECUTION;
            case SEARCH_COMMAND_EXECUTION:
                return SEARCH_COMMAND_RESULT_HANDLING;
            case SEARCH_COMMAND_RESULT_HANDLING:
                return RUNNING_QUERY_RESULT_HANDLING;
            case RUNNING_QUERY_RESULT_HANDLING:
                return VIEW_CONSTRUCTION;
            case VIEW_CONSTRUCTION:
                // a new request
                return REQUEST_CONSTRUCTION;
                
            default:
                throw new IllegalStateException("WTF?!");
        }
    }
}
