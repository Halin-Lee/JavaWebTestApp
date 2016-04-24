package me.halin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html;charset=utf-8");
		PrintWriter writer = resp.getWriter();

		// RequestDispatcher dispatcher =
		// getServletContext().getRequestDispatcher("/RequestDispatcherSubFolder/SubFolderFile.html");
		// //使用/相当于根路径
		// RequestDispatcher dispatcher =
		// req.getRequestDispatcher("RequestDispatcherSubFolder/SubFolderFile.html");
		// //可以不使用/，相当于当前路径


		RequestDispatcher dispatcher = getServletContext().getContext("/Test")
				.getRequestDispatcher("/index.html");
		String method = req.getParameter("method");
		if (method != null && method.equalsIgnoreCase("forward")) {
			dispatcher.forward(req, resp);
			writer.println("我什么都不能写了");
		} else {

			dispatcher.include(req, resp);
			writer.println("我还可以再写点什么");

		}

		writer.close();
	}

}
