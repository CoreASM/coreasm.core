package org.coreasm.rmi.webclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.coreasm.engine.absstorage.Update;
import org.coreasm.rmi.server.remoteinterfaces.EngineControl;

/**
 * Servlet implementation class UpdateProvider
 */
@WebServlet(urlPatterns = "/Updates", loadOnStartup = 2)
public class UpdateProvider extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UpdateProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		UpdateSubImp sub;
		HttpSession session = request.getSession(false);

		// Request wird nur bearbeitet, wenn zuvor Post an CoreASMControl ging
		if (session != null) {
			sub = (UpdateSubImp) session.getAttribute("Subscription");
			if (sub == null) {
				sub = new UpdateSubImp();
				((EngineControl) session.getAttribute("Control"))
						.subscribe(sub);
				session.setAttribute("Subscription", sub);
				session.setAttribute("UpdateCount", 0);
			}

			int count = (int) session.getAttribute("UpdateCount");
			List<Set<Update>> updates = sub.getUpdates(true);
			Iterator<Set<Update>> itr = updates.iterator();

			PrintWriter out = response.getWriter();
			response.setContentType("text/html");

			while (itr.hasNext()) {
				Set<Update> updt = itr.next();
				out.println("<li>");
				out.println("Update " + count + ":");
				out.println("<ul>");
				Iterator<Update> itr2 = updt.iterator();
				while (itr2.hasNext()) {
					out.println("<li>" + itr2.next().toString() + "</li>");
				}
				out.println("</ul>");
				out.println("</li>");
			}
		}
	}

}
