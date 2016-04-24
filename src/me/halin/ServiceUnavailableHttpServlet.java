package me.halin;

import java.io.IOException;

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

		String delayStr = req.getParameter("delay");
		Integer delay = null;
		try {

			delay = Integer.parseInt(delayStr);
		} catch (NumberFormatException e) {
			// TODO: handle exception
		}
		System.out.println(delayStr);
		if (delay == null || delay <= 0) {
			throw new UnavailableException("服务永久不可用");
		} else {
			throw new UnavailableException("服务暂时不可用", delay);
		}

	}

	@Override
	public void destroy() {
		super.destroy();
		System.out.println("服务停止");

	}

}
