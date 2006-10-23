package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

public class WeekdayDirective extends Directive {

    private static final String NAME = "weekday";

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 0) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }
        
//        if (node.jjtGetChild(0).value(context) != null) {
//            final int daysToAdd = Integer.parseInt(node.jjtGetChild(0).value(context).toString());
            
            Calendar cal = Calendar.getInstance();
            List<HashMap> list = new ArrayList();
            
            for (int i = 0; i < 7; i++) {
                HashMap tmp = new HashMap();
                tmp.put("weekday", Integer.toString((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7));
                tmp.put("day", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                if (cal.get(Calendar.MONTH) > 9) {
                    tmp.put("month", Integer.toString(cal.get(Calendar.MONTH)));
                } else {
                    tmp.put("month", "0" + Integer.toString(cal.get(Calendar.MONTH)));
                }
                list.add(tmp);
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            context.put("weekdaylist", list);
//            final String input = node.jjtGetChild(0).value(context).toString();
//
//            if (input != null) {
//                writer.write(StringEscapeUtils.escapeXml(input));
//            }
//
//            final Token lastToken = node.getLastToken();
//
//            if (lastToken.image.endsWith("\n")) {
//                writer.write("\n");
//            }

//        }
        return true;
    }
}
