/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * 
 * This class parse out and export roles information from the field 'yproller' in Fast Search Engine.
 * It will exclude any other information wich is added to yproller and escape 
 * XML characters.
 * 
 * TODO: This class should export the data as XML , not the ackward "#sep#" format.
 * 
 * @author olas <ola@sesam.no>
 * @see RolesDirective RolesDirective
 */
public class RolesMobilePeopleExportDirective extends Directive {

    static Logger log = Logger.getLogger(RolesMobilePeopleExportDirective.class);
    /**
     * Name of the component
     */
    @Override
    public String getName() {
        return "rolesMobilePeopleExport";
    }

    /**
     * Component type
     */
    @Override
    public int getType() {
      return LINE;
    }

    /**
     * Render the request 
     */
    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) 
        throws IOException, 
               ResourceNotFoundException, 
               ParseErrorException, 
               MethodInvocationException {
  
        if(log.isDebugEnabled()) {
            log.debug("render() ...");
        }
        
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        // The text string from datafield which all the roledata is stored
        final String raw = node.jjtGetChild(0).value(context).toString();
        // Convert the input to old format since the parsing will break otherwise
        String s = convert2OldFormat(raw);
        
        if(s != null) {
            writer.write(StringEscapeUtils.escapeXml(s));
        }
        
        return true;
    }
    
    /**
     * Process input so the result contains "roles" only.
     * 
     * @param newOrOldFormat the input either as new or old format
     * @return oldFormat as String
     * @see RolesDirective#convert2OldFormat(String)
     */
    protected String convert2OldFormat(String newOrOldFormat) {

        if(newOrOldFormat == null ) {
            return null;
        }
        String oldFormat = newOrOldFormat.split("#aksjonaer0#")[0];
        oldFormat = oldFormat.replace("#roller0#", "");  
        return oldFormat;
    } 
    

}
