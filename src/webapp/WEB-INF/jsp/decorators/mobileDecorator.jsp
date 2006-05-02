<%@ page import="java.io.StringWriter"%><%@ page import="java.net.URLEncoder"%><%@ page import="java.util.List"%><%@ page import="java.util.Iterator"%><%@ page import="com.opensymphony.module.sitemesh.Page"%><%@ page import="com.opensymphony.module.sitemesh.RequestConstants"%><%@ page import="com.opensymphony.module.sitemesh.util.OutputConverter"%><%@ page import="no.schibstedsok.front.searchportal.i18n.TextMessages"%><%@ page import="no.schibstedsok.front.searchportal.output.VelocityResultHandler"%><%@ page import="no.schibstedsok.front.searchportal.query.run.RunningQuery" %><%@ page import="no.schibstedsok.front.searchportal.result.Enrichment"%><%@ page import="no.schibstedsok.front.searchportal.result.Modifier"%><%@ page import="no.schibstedsok.front.searchportal.site.Site"%><%@ page import="org.apache.commons.lang.StringEscapeUtils" %><%@ page import="org.apache.velocity.Template"%><%@ page import="org.apache.velocity.VelocityContext"%><%@ page import="org.apache.velocity.app.VelocityEngine"%><% 
    final Site site = (Site)request.getAttribute(Site.NAME_KEY);
    final Page siteMeshPage = (Page) request.getAttribute(RequestConstants.PAGE);
    final TextMessages text = (TextMessages) request.getAttribute("text");
    final VelocityEngine engine = VelocityResultHandler.getEngine(site);
    final Template template = VelocityResultHandler.getTemplate(engine, site, "/pages/main");
    final RunningQuery query = (RunningQuery) request.getAttribute("query");
    final List sources = query.getSources();

    if (template != null){
        final VelocityContext context = VelocityResultHandler.newContextInstance(engine);

        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            Modifier mod = (Modifier) iter.next();
            if ( mod.getName().equals("sesam_hits")) {
                context.put("sesam_hits", text.getMessage("numberFormat", new Integer(mod.getCount())));
            }
        }
        context.put("request", request);
        context.put("response", response);
        context.put("page", siteMeshPage);
        context.put("base", request.getContextPath());
        context.put("title", OutputConverter.convert(siteMeshPage.getTitle()));
        context.put("text", text);
        {
            final StringWriter buffer = new StringWriter();
            siteMeshPage.writeBody(OutputConverter.getWriter(buffer));
            context.put("body", buffer.toString());
        }
        template.merge(context, out);}%>