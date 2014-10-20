package org.coreasm.rmi.webclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.coreasm.rmi.server.remoteinterfaces.*;

/**
 * Servlet implementation class CoreASMControl
 */
@WebServlet(urlPatterns = "/Control", loadOnStartup = 1)
@MultipartConfig
public class CoreASMControl extends HttpServlet {
	public enum Command {
		start, stop, pause
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CoreASMControl() {
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

		getServletContext().setAttribute("RMIServer", server);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher disp = getServletContext().getRequestDispatcher("/engine.jsp");
	    disp.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		EngineControl ctrl = (EngineControl) session.getAttribute("Control");
		if (ctrl == null) {
			ServletContext ctx = getServletContext();
			ServerControl server = (ServerControl) ctx.getAttribute("RMIServer");
			ctrl = server.getNewEngine();
			session.setAttribute("Control", ctrl);
			UpdateSubscription sub = new UpdateSubImp();
			((EngineControl) session.getAttribute("Control"))
					.subscribe(sub);
			session.setAttribute("Subscription", sub);
			session.setAttribute("UpdateCount", 0);
		}

		
		if (!ServletFileUpload.isMultipartContent(request)) {
			switch (Command.valueOf(request.getParameter("command"))) {
			case start:
				ctrl.start();
				break;
			case stop:
				ctrl.stop();
				break;
			case pause:
				ctrl.pause();
				break;
			}
		} else {
			Part file = request.getPart("file");
			InputStream in = file.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    byte[] data = new byte[4096];
		    int count = in.read(data);
		    while(count != -1)
		    {
		        baos.write(data, 0, count);
		        count = in.read(data);
		    }
		    byte[] spec = baos.toByteArray();
		    ctrl.load(spec);
		    RequestDispatcher disp = getServletContext().getRequestDispatcher("/engine.jsp");
		    disp.forward(request, response);
		}
	}

}
