package org.coreasm.rmi.webclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.coreasm.rmi.server.remoteinterfaces.EngineControl;
import org.coreasm.rmi.server.remoteinterfaces.ErrorSubscription;

/**
 * Servlet implementation class ErrorProvider
 */
@WebServlet("/Errors")
public class ErrorProvider extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ErrorProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		EngineControl ctrl;
		HttpSession session = request.getSession(false);
		String engId = (String) request.getParameter("engineId");
		if (engId == null) {
			response.sendRedirect("index.jsp");
		} else if (session != null) {
			// Request wird nur bearbeitet, wenn zuvor Post an CoreASMControl
			// ging
			ctrl = getEngine(engId, session);
			if (ctrl != null) {
				ConcurrentHashMap<String, ErrorSubImp> subMap = (ConcurrentHashMap<String, ErrorSubImp>) session
						.getAttribute("Errors");
				ErrorSubImp sub = subMap.get(engId);
				if (sub != null) {

					List<String> errors = sub.getErrors(true);
					Iterator<String> itr = errors.iterator();

					PrintWriter out = response.getWriter();
					response.setContentType("application/json");
					out.println('[');
					while (itr.hasNext()) {
						String err = itr.next();
						out.print("\"" + err + "\"");
						if (itr.hasNext()) {
							out.println(',');
						} else {
							out.println();
						}
					}
					out.println(']');

				}

			}

		}
	}

	private EngineControl getEngine(String Id, HttpSession session) {
		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			return null;
		}
		EngineControl ctrl = (EngineControl) engineMap.get(Id);
		if (ctrl == null) {
			return null;
		}
		return ctrl;
	}
}
