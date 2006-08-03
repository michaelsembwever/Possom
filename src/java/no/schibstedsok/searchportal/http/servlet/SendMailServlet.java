package no.schibstedsok.searchportal.http.servlet;

import no.schibstedsok.searchportal.mode.SiteConfiguration;

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
import javax.mail.internet.AddressException;
import java.io.IOException;
import java.util.Properties;
import no.schibstedsok.searchportal.site.Site;

/**
 * Sends a mail from the YIP-page contactform to the companyaddressmail
 *
 *
 * User: ssthkjer
 * Date: 16.des.2005
 * Time: 12:39:42
 */
public class SendMailServlet extends HttpServlet {

    public Properties props;
    public void init() {
        props = SiteConfiguration.valueOf(Site.DEFAULT).getProperties(); // FIXME !!! Must work per Site
    }
    public void destroy() {}

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String emailFrom = req.getParameter("emailFrom");
        String emailTo = req.getParameter("emailTo");
        String name = req.getParameter("mailName");
        if (name.trim().equals(""))
            name = "ukjent";
        String phone = req.getParameter("phone");
        if (phone.trim().equals(""))
            phone = "";
        else
            phone = " - tlf: " + phone;

        try {
            InternetAddress from = new InternetAddress(emailFrom);
            InternetAddress to = new InternetAddress(emailTo);

            Session session = Session.getDefaultInstance(props);

            Message msg = new MimeMessage(session);
            String txt = req.getParameter("text");
            txt = txt + "\n\n\n" + "Denne forespørselen er sendt via Sesam bedriftssøk (http://www.sesam.no)";
            msg.setSubject("Kontaktskjema Sesam fra " + name + phone);
            msg.setText(txt);
            msg.setFrom(from);
            msg.addRecipient(MimeMessage.RecipientType.TO, to);

            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();  //To change body of catch  statement use File | Settings | File Templates.
        }
        String redir = req.getContextPath() + req.getParameter("rdir");
        res.sendRedirect(redir);
    }

   
}
