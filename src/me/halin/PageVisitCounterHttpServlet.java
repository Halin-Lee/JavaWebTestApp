package me.halin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageVisitCounterHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletContext context = getServletContext();

		Integer counter;
		synchronized (context) {
			counter = (Integer) context.getAttribute("PVCounter");
			if (counter == null) {
				counter = Integer.valueOf(0);
			}
			counter++;
			context.setAttribute("PVCounter", counter);
		}

		resp.setContentType("text/html;charset=utf-8");
		PrintWriter writer = resp.getWriter();
		writer.print("当前页面总访问量:" + counter);
		writer.close();
	}
}
