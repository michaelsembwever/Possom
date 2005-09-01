/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal.filters.picsearch;

import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.command.PicSearchConnectorCommand;
import no.schibstedsok.front.searchportal.response.SearchResponseImpl;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.geodata.maputil.CoordHelper;

import java.io.Writer;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PicSearchConsumer extends SearchConsumer {

    Logger log = Logger.getLogger(this.getClass());

    /**
     * Create a new FastSearchConsumer.
     *
     * @param response
     * @param configuration
     */
    public PicSearchConsumer(Writer response, SearchConfiguration configuration) {
        super(response, configuration);
    }

    public void run() {
        PicSearchConnectorCommand cmd = new PicSearchConnectorCommand();
        cmd.setConfiguration(configuration);
        cmd.execute();

        CommandResponse response = cmd.getResponse();

        response.getTotalDocumentsAvailable();
        printVelocityToWriter(response, myWriterRef, configuration.getTemplate());
    }

    private void printVelocityToWriter(Object results, Writer myWriterRef, String templateName) {

        initVelocity();

        try {                                    
            Writer w = new StringWriter();

            Template template = Velocity.getTemplate(templateName);

            VelocityContext context = new VelocityContext();
            context.put("result", results);
            context.put("contextPath", getContextPath());
            template.merge(context, w);

            w.close();

            myWriterRef.write(w.toString());

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
    private void initVelocity() {

        /** Ignored if already initialized (makes this method testable.) */
        try {
            Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
            Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init();
        } catch (Exception e) {
            // FIXME
            log.error("Error", e);
        }


    }

}
