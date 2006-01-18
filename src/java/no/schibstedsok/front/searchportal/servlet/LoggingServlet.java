// Copyright (2005-2006) Schibsted Søk AS

package no.schibstedsok.front.searchportal.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;


/** View the Loggers loaded in the Context.
 * Also allows editing of the logger's level at runtime.
 *
 * XXX This servlet must be restricted to only schibsted internal network.
 *
 * @version $Id$
 */
public final class LoggingServlet extends HttpServlet{
 
    private static final Logger LOG = Logger.getLogger(LoggingServlet.class);
    private static final MessageFormat OPTIONS = new MessageFormat("<option value=\"ALL\" {0}>ALL</option><option value=\"DEBUG\" {1}>DEBUG</option><option value=\"INFO\" {2}>INFO</option><option value=\"WARN\"{3}>WARN</option><option value=\"ERROR\" {4}>ERROR</option><option value=\"FATAL\" {5}>FATAL</option><option value=\"OFF\"{6}>OFF</option>");
    public static final String SELECTED = "selected";
    private static final String URL = "../side.do?brukerSisteDokId=ja";

    public void doGet(
            final HttpServletRequest request, 
            final HttpServletResponse response) throws ServletException, IOException {    
        
        LOG.debug("start doGet");
        request.setCharacterEncoding("UTF-8"); // correct encoding
        final Enumeration en = LogManager.getCurrentLoggers();

        Logger log;
        // Sort first 
        final HashMap/*<String,Level>*/ unsorted = new HashMap/*<String,Level>*/();
        while( en.hasMoreElements() ){
            log = (Logger)en.nextElement();
            unsorted.put(log.getName(), log.getEffectiveLevel());
        }
        final List/*<String>*/ sortedList = new ArrayList/*<String>*/(unsorted.keySet());
        final /*StringBuilder*/StringBuffer buffer = new /*StringBuilder*/StringBuffer();
        Collections.sort(sortedList);
        try{

            //for( String key : sortedList ){
            for( Iterator it = sortedList.iterator(); it.hasNext(); ){
                final String key = (String)it.next();
                Level level = (Level)unsorted.get(key);
                String value = level.toString();
                
                // update if in request parameters
                final String param = request.getParameter(key);
                if( param != null && !param.equals(value) ){
                    final Level newLevel = Level.toLevel(param);
                    Logger.getLogger(key).setLevel(newLevel);
                    LOG.warn("Logger "+key+" has been changed to level: "+param);
                    level = newLevel;
                    value = param;
                }
                // output html
                final int option = getOption( level.toInt() );
                final String[] VALUES = new String[]{"","","","","","",""};
                VALUES[option] = SELECTED;
                // The MessageFormat constant does not support synchronous usage.
                synchronized( OPTIONS ){
                    buffer.append("<tr><td><b>"+key+"</b></td><td><select size=\"1\" name=\""+key+"\">"+OPTIONS.format(VALUES)+"</select></td></tr>");
                }
            }

            
            final ServletOutputStream ss = response.getOutputStream();
            response.setContentType("text/html;charset=UTF-8");
            ss.print("<form action=\"Log\"><div style=\"float: left;\"><table>");
            ss.print(buffer.toString());
            ss.print("</table></div><div style=\"float: right;\"><input class=\"submit\" type=\"submit\" value=\"Update\"/></div></form>");
            ss.close();
            
        }catch(IOException io){
            LOG.error("LoggingServlet.doGet "+io);
        }
    }
    
    private static int getOption(final int priority){
        
        int option;
        switch( priority ){
            case Level.ALL_INT:
                option = 0;
                break;
            case Level.DEBUG_INT:
                option = 1;
                break;
            case Level.INFO_INT:
                option = 2;
                break;
            case Level.WARN_INT:
                option = 3;
                break;
            case Level.ERROR_INT:
                option = 4;
                break;
            case Level.FATAL_INT:
                option = 5;
                break;
            case Level.OFF_INT:
            default:
                option = 6;
                break;
        }
        return option;
    }

    
}