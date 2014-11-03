package org.coreasm.rmi.webclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.coreasm.rmi.server.remoteinterfaces.EngineDriverInfo;
import org.coreasm.rmi.server.remoteinterfaces.ServerAdminControl;
import org.coreasm.rmi.server.remoteinterfaces.ServerControl;

/**
 * Servlet implementation class CoreASMServerControl
 */
@WebServlet(urlPatterns = "/AdminControl", loadOnStartup = 1)
public class CoreASMServerControl extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CoreASMServerControl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String name = "RMIServer";
		Registry registry;
		ServerControl server = null;
		String host = "localhost";

		try {
			registry = LocateRegistry.getRegistry(host);
			try {
				server = (ServerControl) registry.lookup(name);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		ServletContext ctx = getServletContext();
		ctx.setAttribute("RMIServer", server);
		ctx.setAttribute("AdminControl", (ServerAdminControl) server);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		ServletContext ctx = getServletContext();
		ServerAdminControl ctrl = (ServerAdminControl) ctx
				.getAttribute("AdminControl");
		ArrayList<EngineDriverInfo> infoLst = ctrl.getEngineList();
		Iterator<EngineDriverInfo> itr = infoLst.iterator();
		PrintWriter out = response.getWriter();
		while (itr.hasNext()) {
			out.println(getListItem(itr.next()));
		}
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private String getListItem(EngineDriverInfo engineInfo) {
		StringBuilder item = new StringBuilder();
		item.append("<li>");
		item.append("<p>Id: " + engineInfo.getId() + "</p>");
		item.append("<p>Status: " + engineInfo.getStatus() + "</p>");
		item.append("<button class=\"command\" value=\"" + engineInfo.getId()
				+ "\" type=\"submit\">Stop</button>");
		item.append("</li>");
		return item.toString();
	}

}
