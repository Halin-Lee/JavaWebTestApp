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
//		resp.setCharacterEncoding("UTF-8");

		// RequestDispatcher dispatcher =
		// getServletContext().getRequestDispatcher("/RequestDispatcherSubFolder/SubFolderFile.html");
		// //使用/相当于根路径
		// RequestDispatcher dispatcher =
		// req.getRequestDispatcher("RequestDispatcherSubFolder/SubFolderFile.html");
		// //可以不使用/，相当于当前路径


		RequestDispatcher dispatcher = getServletContext().getContext("/JavaWebTestApp")
				.getRequestDispatcher("/index.html");
		String method = req.getParameter("method");
		

//		getWriter会导致include乱码（与utf-8有关），暂时未能解决
		if (method != null && method.equalsIgnoreCase("forward")) {
			dispatcher.forward(req, resp);
			PrintWriter writer = resp.getWriter();
			writer.println("我什么都不能写了");
		} else {
			resp.setCharacterEncoding("UTF-8");
			PrintWriter writer = resp.getWriter();
			dispatcher.include(req, resp);		
			writer.println("我还可以再写点什么");
			writer.close();
		}

	}

}
