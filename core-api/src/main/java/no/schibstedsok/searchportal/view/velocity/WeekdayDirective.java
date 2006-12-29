package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
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
        if (node.jjtGetNumChildren() >  1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        Calendar cal = Calendar.getInstance();
        if (node.jjtGetNumChildren() == 1) {
            int offset = Integer.parseInt(node.jjtGetChild(0).value(context).toString());
            cal.add(Calendar.DAY_OF_YEAR, offset);
        }

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
        return true;
    }
}
