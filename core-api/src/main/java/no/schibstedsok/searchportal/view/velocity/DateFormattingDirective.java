/*
 * DateFormattingDirective.java
 *
 * Created on 24. november 2006, 11:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.view.velocity;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;


/**
 *
 * A velocity directive to format newsnavigator date
 * 
 * Newsdate comes from the fastnavigator in two forms:
 * 1. 10-2006     -> oktober 2006
 * 2. 10-24-2006  -> 10. oktober 2006
 *
 * if 'newsdateOnly' navigator, we shuold check if the date is today or yesterday
 *
 */
public final class DateFormattingDirective extends Directive {
    
    private static final Logger LOG = Logger.getLogger(DateFormattingDirective.class);

    private static final String NAME = "dateFormatting";

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        final String input = node.jjtGetChild(0).value(context).toString();
        final String navName = node.jjtGetChild(1).value(context).toString();        
        
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Map<String, String> map = new HashMap<String, String>();
        map.put("01", "Januar");
        map.put("02", "Februar");
        map.put("03", "Mars");
        map.put("04", "April");
        map.put("05", "Mai");
        map.put("06", "Juni");
        map.put("07", "Juli");
        map.put("08", "August");
        map.put("09", "September");
        map.put("10", "Oktober");
        map.put("11", "November");
        map.put("12", "Desember");
        
        String fDate = "";
        final Date today = new Date();
        long time = today.getTime();
        time -= 24*60*60*1000;
        final Date yesterday = new Date(time);
        String sToday = formatter.format(today);
        String sYesterday = formatter.format(yesterday);                        
        
        if (sToday.equals(input) && "newsdateOnly".equals(navName))
            fDate = "I dag";
        else if (sYesterday.equals(input) && "newsdateOnly".equals(navName))
            fDate = "I g&#229;r"; 
        else if (input.length() == 7)
            fDate = map.get(input.substring(0,2)) + " " + input.substring(3,7);
        else if (input.length() == 10)
            fDate = input.substring(0, 2) + ". " + map.get(input.substring(3,5)).toLowerCase() + " " + input.substring(6,10);
        else
            fDate = input;    

        writer.write(fDate);

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
    
}
