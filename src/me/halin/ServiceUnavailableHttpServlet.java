package me.halin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServiceUnavailableHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		System.out.println("服务开启");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html;charset=utf-8");
		
		String delayStr = req.getParameter("delay");
		Integer delay = null;
		try {

			delay = Integer.parseInt(delayStr);
		} catch (NumberFormatException e) {
			// TODO: handle exception
		}
		if (delay == null || delay < 0) {
			throw new UnavailableException("服务永久不可用");
		} else if(delay == 0){
			PrintWriter wirter = resp.getWriter();
			wirter.print("服务正常");
			wirter.close();
		}else {
			throw new UnavailableException("服务暂时不可用", delay);
		}

	}

	@Override
	public void destroy() {
		super.destroy();
		System.out.println("服务停止");

	}

}
