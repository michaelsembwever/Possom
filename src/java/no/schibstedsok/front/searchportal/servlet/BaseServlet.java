/*
 * Created on Nov 9, 2004
 *
 */

package no.schibstedsok.front.searchportal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lars Johansson
 *
 */
public class BaseServlet extends HttpServlet {

    /** The serialVersionUID */
	private static final long serialVersionUID = 3258689914219279152L;

	public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    }

}