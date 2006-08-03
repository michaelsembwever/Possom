// Copyright (2006) Schibsted Søk AS
/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.schibstedsok.searchportal.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import no.schibstedsok.searchportal.command.SearchCommand;
import no.schibstedsok.searchportal.configuration.SearchConfiguration;


/** This factory creates the appropriate command for a given SearchConfiguration.
 * Uses a simple renaming lookup with reflection to create the SearchCommand.
 * That is, it tries to return a class with the same name except "Configuration" replaced with "Command".
 *
 * @author mick
 * @version $Id$
 */
public final class SearchCommandFactory {
    
    private static final String ERR_NO_COMMAND = "Cannot find suitable command for ";

    private SearchCommandFactory() {  }

    /** Create the appropriate command.
     **/
    public static SearchCommand createSearchCommand(
            final SearchCommand.Context cxt, 
            final Map<String,Object> parameters) {


        final SearchConfiguration config = cxt.getSearchConfiguration();
        try {

            final Class<SearchCommand> commandCls = (Class<SearchCommand>)
                    Class.forName(config.getClass().getName().replaceAll("onfiguration", "ommand"));
            final Constructor<SearchCommand> commandConstr 
                    = commandCls.getConstructor(SearchCommand.Context.class, Map.class);
            return commandConstr.newInstance(cxt, parameters);
            
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (SecurityException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException(ERR_NO_COMMAND + config.getName(), ex);
        }
        
      }
}
