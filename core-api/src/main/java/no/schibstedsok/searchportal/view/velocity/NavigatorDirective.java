/*
 * NavigatorDirective.java
 * 
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import no.schibstedsok.searchportal.util.NavigatorHelper;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * @author andersjj
 */
public class NavigatorDirective extends Directive {

    private static final String NAME = "navigator";
    
    public NavigatorDirective() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        final NavigatorHelper navhelp = NavigatorHelper.getInstance();
        context.put("navtool", navhelp);
        
        return true;
    }

}
