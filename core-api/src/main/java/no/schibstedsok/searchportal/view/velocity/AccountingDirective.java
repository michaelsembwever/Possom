// Copyright (2006-2007) Schibsted Søk AS
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

import no.schibstedsok.searchportal.result.Boomerang;

/**
 * Created by IntelliJ IDEA.
 * @author SSTHKJER
 * @version $Id$
 * Date: 03.apr.2006
 * Time: 09:21:04
 */
public final class AccountingDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(AccountingDirective.class);


    private static final String NAME = "accounting";

    /**
     * returns the name of the directive.
     *
     * @return the name of the directive.
     */
    public String getName() {
        return NAME;
    }

    /**
     * returns the type of the directive. The type is LINE.
     * @return The type == LINE
     */
    public int getType() {
        return LINE;
    }

    /**
     * Renders the html for the accountings.
     * This isn't an optimal solution, but the way data is stored
     * it was the fastest way to do it. For me.
     *
     * IF THIS MARKUP ARE BEING CHANGED, MOBILE PEOPLE MUST BE INFORMED
     *
     * @param context
     * @param writer
     * @param node
     *
     * @throws java.io.IOException
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     * @throws org.apache.velocity.exception.ParseErrorException
     * @throws org.apache.velocity.exception.MethodInvocationException
     * @return the encoded string.
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) 
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        // The text string from datafield which all the accountingsnumber is stored
        final String s = getArgument(context, node, 0);

        final StringBuilder html = new StringBuilder("<table cellspacing=\"1\">");

        // New line seperator
        final String[] row = s.split("#sepnl#");
        String[] col;
        boolean bgcolor = false;
        String text = "";
        boolean quitLoop = false;

        final String lpUrl = Boomerang.getUrl(getDataModel(context).getSite().getSite(),
                "http://www.lindorff.no/", 
                "category:static;subcategory:provider");

        //print rows
        for (int i = 0; i < row.length; i++) {

            // column seperator
            col = row[i].split("#sep#");

            //print columns
            for (int k = 0; k < col.length; k++) {

                // do some tests before each new row
                if (k==0) {
                    // #balance0# means print a new table
                    if (col[0].indexOf("#balanse0#") > -1) {
                        html.append("</table>");
                        html.append("<table class=\"balance\" cellspacing=\"1\">");
                        bgcolor = false;
                    } else if (col[0].indexOf("#lederlonn0#") > -1) {                        
                        html.append("</table>* Alle tall i hele 1000 kroner.");
                        html.append("<table class=\"balance\" cellspacing=\"1\">");
                        bgcolor = false;
                    // #balance1# means don't show rest of the numbers
                    } else if (col[0].indexOf("#balanse1#") > -1) {
                        quitLoop = true;
                        break;
                    }
                    // checks if the row should be bold
                    if (col[0].indexOf("#bold#") == -1) {
                        text = col[0];
                        if (!bgcolor) {
                            html.append("<tr class=\"bg1\">");
                        } else {
                            html.append("<tr class=\"bg2\">");                            
                        }
                    } else {
                        if (!bgcolor) {
                            html.append("<tr class=\"bold_line bg1\">");
                        } else {
                            html.append("<tr class=\"bold_line bg2\">");
                        }
                        text = col[0].substring(col[0].indexOf("#bold#")+6);
                    }
                } else
                    text = col[k];

                html.append("<td class=\"col"+ (k+1) + "\">" + text.trim() + "</td>");
            }
            if (!quitLoop) {
                bgcolor = !bgcolor;
                html.append("</tr>");
            } else
                break;
        }
        html.append("</table>");
        html.append("<div id=\"lindorff\">");
        html.append("* Regnskapet viser kun hovedtall, og er levert av Lindorff Decision.</div>");        
        html.append("<a href=\"" + lpUrl + "\" target=\"_blank\">");
        html.append("<img src=\"/images/lindorff_logo.gif\" width=\"81\" height=\"31\" alt=\"Linforff logo\" /></a>");

        writer.write(html.toString());

        if (node.getLastToken().image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}