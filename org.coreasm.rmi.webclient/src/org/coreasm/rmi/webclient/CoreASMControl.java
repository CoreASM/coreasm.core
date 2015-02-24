package org.coreasm.rmi.webclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
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
@WebServlet(urlPatterns = "/Control", loadOnStartup = 2)
@MultipartConfig
public class CoreASMControl extends HttpServlet {
	public enum Command {
		start, stop, pause, join, update, step, getAgents
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher disp = getServletContext().getRequestDispatcher(
				"/engine.jsp");
		disp.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String engId = request.getParameter("engineId");
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		HttpSession session = request.getSession();
		EngineControl ctrl = getEngine(engId, session);
		if (ctrl != null) {
			engId = ctrl.getIdNr();
			if (!isMultipart) {
				String com = request.getParameter("command");
				if (com != null) {
					switch (Command.valueOf(com)) {
					case start:
						ctrl.start();
						break;
					case stop:
						ctrl.stop();
						break;
					case pause:
						ctrl.pause();
						break;
					case join:
						request.setAttribute("EngineId", engId);
						RequestDispatcher disp = getServletContext()
								.getRequestDispatcher("/engine.jsp");
						disp.forward(request, response);
						break;
					case update:
						String agent = (String) request.getParameter("agent");
						String val = (String) request.getParameter("value");
						ctrl.addUpdate(val, agent);
						break;
					case step:
						ctrl.singleStep();
						break;
					case getAgents:
						String agents = ctrl.getAgentlist();
						PrintWriter out = response.getWriter();
						response.setContentType("application/json");
						out.println(agents);
						break;
					}
				}
			} else {
				Part file = request.getPart("file");
				InputStream in = file.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] data = new byte[4096];
				int count = in.read(data);
				while (count != -1) {
					baos.write(data, 0, count);
					count = in.read(data);
				}
				byte[] spec = baos.toByteArray();
				ctrl.load(spec);
				request.setAttribute("EngineId", engId);
				RequestDispatcher disp = getServletContext()
						.getRequestDispatcher("/engine.jsp");
				disp.forward(request, response);
			}
		}
	}

	private EngineControl getEngine(String Id, HttpSession session) {

		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			engineMap = new ConcurrentHashMap<String, EngineControl>();
			session.setAttribute("EngineMap", engineMap);
		}
		EngineControl ctrl = null;
		if (Id != null) {
			ctrl = (EngineControl) engineMap.get(Id);
		}

		if (ctrl == null) {
			ServletContext ctx = getServletContext();
			ServerControl server = (ServerControl) ctx
					.getAttribute("RMIServer");
			try {
				if (Id == null) {
					ctrl = server.getNewEngine();
					Id = ctrl.getIdNr();
				} else {
					ctrl = server.connectExistingEngine(Id);
				}
				if(ctrl != null) {
					engineMap.put(Id, ctrl);
					UpdateSubImp sub = new UpdateSubImp();
					ErrorSubImp errSub = new ErrorSubImp();
					ctrl.subscribeUpdates(sub);
					ConcurrentHashMap<String, UpdateSubImp> subMap = (ConcurrentHashMap<String, UpdateSubImp>) session
							.getAttribute("Subscriptions");
					if (subMap == null) {
						subMap = new ConcurrentHashMap<String, UpdateSubImp>();
						session.setAttribute("Subscriptions", subMap);
					}
					subMap.putIfAbsent(Id, sub);
					
					ctrl.subscribeErrors(errSub);
					ConcurrentHashMap<String, ErrorSubImp> errMap = (ConcurrentHashMap<String, ErrorSubImp>) session
							.getAttribute("Errors");
					if (errMap == null) {
						errMap = new ConcurrentHashMap<String, ErrorSubImp>();
						session.setAttribute("Errors", errMap);
					}
					errMap.putIfAbsent(Id, errSub);					
				}
			} catch (RemoteException e) {
			}
		}
		return ctrl;
	}

}
