package no.schibstedsok.front.searchportal.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;

import no.schibstedsok.front.searchportal.security.MD5Generator;

/**
 * Created by IntelliJ IDEA.
 * User: SSTHKJER
 * Date: 05.apr.2006
 * Time: 15:34:43
 * To change this template use File | Settings | File Templates.
 */
public class RolesDirective extends Directive {
    private static transient Log log = LogFactory.getLog(RolesDirective.class);


    private static final String NAME = "roles";

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
     * Renders the html for rolesinfo on infopages.
     * This isn't an optimal solution, but the way data is stored
     * it was the fastest way to do it. For me.
     *
     * Since a person/company could have 100(uptil over 1000) of roles,
     * we want to just show the 30 first and then have a link to the rest.
     * Do this with javascript.
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
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 3) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        // The text string from datafield which all the roledata is stored
        String s = node.jjtGetChild(0).value(context).toString();

        // Yellow or Person page (used for linking)
        String page = node.jjtGetChild(1).value(context).toString();

        // Needs the query to build up the link
        String query = node.jjtGetChild(2).value(context).toString();

        // New line seperator
        String[] row = s.split("#sepnl#");
        String[] col;
        boolean bgcolor = false;
        String text = "";
        String name = "";
        String recordid = "";

        // Needs this for link, find a way to import password..
        MD5Generator md5 = new MD5Generator("S3SAM rockz");
        String html = "";

        html = "<div><table id=\"roletable\" bgcolor=\"#CCCCCC\" cellspacing=\"1\">";

        // print rows
        for (int i = 0; i < row.length; i++) {

            // show 30 first rows
            if (i==30) {
                html += "</table></div><div id=\"more_roles\" style=\"display: none;\"><table id=\"roletable\" bgcolor=\"#CCCCCC\" cellspacing=\"1\">";
            }
            // column seperator
            col = row[i].split("#sep#");

            // print columns
            for (int k = 0; k < col.length; k++) {

                if (k==1) {
                    // recordid seperator
                    name = StringUtils.substringBefore(col[1], "#id#");
                    recordid = StringUtils.substringAfter(col[1], "#id#").trim();
                    if (recordid.equals(""))
                        text = name;
                    else {
                        // create link to infopage
                        if (page.equals("y"))
                            text = "<a href=\"?c=wip&amp;q=" + query + "&amp;personId=" + recordid + "&amp;personId_x=" + md5.generateMD5(recordid) + "\">" + name + "</a>";
                        else
                            text = "<a href=\"?c=yip&amp;q=" + query + "&amp;companyId=" + recordid + "&amp;companyId_x=" + md5.generateMD5(recordid) + "\">" + name + "</a>";
                    }
                } else
                    text = col[k].trim();

                if (!bgcolor) {
                    html += "<td class=\"col"+ (k+1) + "\" bgcolor=\"#FFFFFF\">" + text.trim() + "</td>";
                } else {
                    html += "<td class=\"col"+ (k+1) + "\" style=\"background-color: #EBEBEB;\">" + text.trim() + "</td>";
                }
            }
            bgcolor = !bgcolor;
            html += "</tr>";
        }

        // create expand link with javascript (pretty bad, huh!!)
        html += "</table></div>";
        html += "<div id=\"expand_roles\" style=\"display:";
        if (row.length > 30)
            html += "block";
        else
            html += "none";
        html += "\"><a href=\"#\" onclick=\"javascript:document.getElementById('more_roles').style.display='block'; document.getElementById('expand_roles').style.display='none'; document.getElementById('hide_roles').style.display='block'\">Vis alle</a></div>";

        html += "<div id=\"hide_roles\" style=\"display: none\"><a href=\"#\" onclick=\"javascript:document.getElementById('more_roles').style.display='none'; document.getElementById('expand_roles').style.display='block'; document.getElementById('hide_roles').style.display='none'\">Skjul</a></div>";


        writer.write(html);
        Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }

}
