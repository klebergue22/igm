/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.web;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PdfServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token"); // ?token=CERT_123
        if (token == null || token.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token requerido");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_GONE, "Sesión expirada");
            return;
        }

        Object obj = session.getAttribute(token);
        if (!(obj instanceof byte[])) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no disponible");
            return;
        }

        byte[] bytes = (byte[]) obj;

        // Cabeceras para inline preview; para descarga forzada, usa attachment
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"Certificado.pdf\"");
        resp.setContentLength(bytes.length);

        OutputStream out = resp.getOutputStream();
        out.write(bytes);
        out.flush();

        // Si quieres que el token sea “de un solo uso”, descomenta:
        // session.removeAttribute(token);
    }
}
