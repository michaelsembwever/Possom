// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.http.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import no.schibstedsok.searchportal.datamodel.DataModel;
import org.apache.log4j.Logger;
/**
 * Sends a mail from the YIP-page contactform to the companyaddressmail
 *
 *
 * @Author: ssthkjer
 * @Version $Id: SendMailServlet.java 3829 2006-10-23 10:00:55Z mickw $
 */
public final class SendMailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SendMailServlet.class);

    protected void doGet(
            final HttpServletRequest req, 
            final HttpServletResponse res) throws ServletException, IOException {

        final DataModel datamodel = (DataModel) req.getSession().getAttribute(DataModel.KEY);
        final Properties props = datamodel.getSite().getSiteConfiguration().getProperties();
        
        final String emailFrom = req.getParameter("emailFrom");
        final String emailTo = req.getParameter("emailTo");
        
        String name = req.getParameter("mailName");
        if (name.trim().equals("")){
            name = "ukjent";
        }
        
        String phone = req.getParameter("phone");
        if (phone.trim().equals("")){
            phone = "";
        }else{
            phone = " - tlf: " + phone;
        }
        
        try {
            final InternetAddress from = new InternetAddress(emailFrom);
            final InternetAddress to = new InternetAddress(emailTo);

            final Session session = Session.getDefaultInstance(props);

            final Message msg = new MimeMessage(session);
            final String txt = req.getParameter("text") 
                    + "\n\n\n" + "Denne forespørselen er sendt via Sesam bedriftssøk (http://www.sesam.no)";
            
            msg.setSubject("Kontaktskjema Sesam fra " + name + phone);
            msg.setText(txt);
            msg.setFrom(from);
            msg.addRecipient(MimeMessage.RecipientType.TO, to);

            Transport.send(msg);
            
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
        }
        
        final String redir = req.getContextPath() + req.getParameter("rdir");
        res.sendRedirect(redir);
    }

   
}
