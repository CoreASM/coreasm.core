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
import org.coreasm.rmi.server.remoteinterfaces.EngineControl;
import org.coreasm.rmi.server.remoteinterfaces.ServerControl;

/**
 * Servlet implementation class CoreASMControl
 */
@WebServlet(urlPatterns = "/Control", loadOnStartup = 2)
@MultipartConfig
public class CoreASMControlProvider extends HttpServlet {
	public enum Command {
		start, stop, pause, join, update, step, getAgents, changeValue, reset, getEngine, loadSpec
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CoreASMControlProvider() {
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
		boolean keepSpec = false;
		HttpSession session = request.getSession();
		EngineControl ctrl = getEngine(engId, session);
		PrintWriter out;
		String agent, val, loc;

		String com = request.getParameter("command");
		if (com == null && isMultipart) {
			com = "loadSpec";
		}
		if (ctrl != null) {
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
					out = response.getWriter();
					out.print(ctrl.getIdNr());
					out.flush();
					break;
				case update:
					agent = request.getParameter("agent");
					val = request.getParameter("value");
					ctrl.addUpdate(val, agent);
					break;
				case step:
					ctrl.singleStep();
					break;
				case getAgents:
					String agents = ctrl.getAgentlist();
					out = response.getWriter();
					response.setContentType("application/json");
					out.println(agents);
					break;
				case changeValue:
					loc = request.getParameter("location");
					val = request.getParameter("value");
					ctrl.changeValue(loc, val);
					break;
				case reset:
					// defaults keepSpec to false
					keepSpec = Boolean
							.valueOf(request.getParameter("keepSpec"));
					ctrl.reset(keepSpec);
					break;
				case getEngine:
					out = response.getWriter();
					engId = ctrl.getIdNr();
					out.print(engId);
					out.flush();
					break;
				default:
					break;
				}
			}
		} else {
			if (com != null) {
				switch (Command.valueOf(com)) {
				case loadSpec:
				case getEngine:
					out = response.getWriter();
					if (isMultipart) {
						ctrl = getNewEngine(session);
						Part file = request.getPart("spec");
						if (ctrl != null) {
							if (file != null) {
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
							}
							engId = ctrl.getIdNr();
						}
						out.print(engId);
					}
					out.flush();
					break;
				case join:
					ctrl = joinEngine(engId, session);
					out = response.getWriter();
					out.print(ctrl.getIdNr());
					out.flush();
					break;
				default:
					break;
				}
			}
		}
	}

	private EngineControl getEngine(String id, HttpSession session) {
		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			engineMap = new ConcurrentHashMap<String, EngineControl>();
			session.setAttribute("EngineMap", engineMap);
		}

		EngineControl ctrl = null;
		if (id != null) {
			ctrl = (EngineControl) engineMap.get(id);
		}

		return ctrl;
	}

	private EngineControl joinEngine(String id, HttpSession session) {
		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			engineMap = new ConcurrentHashMap<String, EngineControl>();
			session.setAttribute("EngineMap", engineMap);
		}

		EngineControl ctrl = null;

		if (id != "") {
			ctrl = (EngineControl) engineMap.get(id);
		}

		if (ctrl == null) {
			ServletContext ctx = getServletContext();
			ServerControl server = (ServerControl) ctx
					.getAttribute("RMIServer");
			try {
				if (id == "") {
					ctrl = null;
				} else {
					ctrl = server.connectExistingEngine(id);
				}
				if (ctrl != null) {
					addEngine(id, ctrl, session);
				}
			} catch (RemoteException e) {
				ctrl = null;
			}
		}
		return ctrl;

	}

	private EngineControl getNewEngine(HttpSession session) {
		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			engineMap = new ConcurrentHashMap<String, EngineControl>();
			session.setAttribute("EngineMap", engineMap);
		}

		EngineControl ctrl = null;
		String id;

		ServletContext ctx = getServletContext();
		ServerControl server = (ServerControl) ctx.getAttribute("RMIServer");
		try {
			ctrl = server.getNewEngine();
			id = ctrl.getIdNr();
			if (ctrl != null) {
				addEngine(id, ctrl, session);
			}
		} catch (RemoteException e) {			
			ctrl = null;
		}
		return ctrl;
	}

	private void addEngine(String id, EngineControl ctrl, HttpSession session)
			throws RemoteException {
		ConcurrentHashMap<String, EngineControl> engineMap = (ConcurrentHashMap<String, EngineControl>) session
				.getAttribute("EngineMap");
		if (engineMap == null) {
			engineMap = new ConcurrentHashMap<String, EngineControl>();
			session.setAttribute("EngineMap", engineMap);
		}
		engineMap.put(id, ctrl);
		UpdateSubImp sub = new UpdateSubImp();
		ErrorSubImp errSub = new ErrorSubImp();
		ctrl.subscribeUpdates(sub);
		ConcurrentHashMap<String, UpdateSubImp> subMap = (ConcurrentHashMap<String, UpdateSubImp>) session
				.getAttribute("Subscriptions");
		if (subMap == null) {
			subMap = new ConcurrentHashMap<String, UpdateSubImp>();
			session.setAttribute("Subscriptions", subMap);
		}
		subMap.putIfAbsent(id, sub);

		ctrl.subscribeErrors(errSub);
		ConcurrentHashMap<String, ErrorSubImp> errMap = (ConcurrentHashMap<String, ErrorSubImp>) session
				.getAttribute("Errors");
		if (errMap == null) {
			errMap = new ConcurrentHashMap<String, ErrorSubImp>();
			session.setAttribute("Errors", errMap);
		}
		errMap.putIfAbsent(id, errSub);
	}

}
