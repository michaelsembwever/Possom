/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.http.servlet;

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
import no.sesat.search.datamodel.DataModel;
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
