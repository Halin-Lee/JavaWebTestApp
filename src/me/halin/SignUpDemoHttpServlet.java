package me.halin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SignUpDemoHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String prefix;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		// 从servletConfig中读取初始化参数，此参数在web。xml初始化
		prefix = getServletConfig().getInitParameter("SIGN_UP_DEMO_PREFIX");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// 没有提交UserName，重定向到提交页面
		// resp.sendRedirect("SignUp.html"); //重定向
		 req.getRequestDispatcher("SignUp.html").forward(req, resp) ; //装发到SignUp。html

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// servlet super的方法不能调用，否则会当该servlet不处理该类型方法
		// super.doGet(req, resp);

		req.setCharacterEncoding("UTF-8");
		String userName = req.getParameter("userName");
		resp.setContentType("text/html;charset=utf-8"); // 注，设置chaseset要在getWritter之前

		if (userName == null || userName.isEmpty()) {
			// 没有提交UserName，返回错误
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "用户名为空");
			return;
		}

		String info = prefix + "," + userName;
		resp.setContentType("text/html");

		PrintWriter out = resp.getWriter();
		out.println("<html><head><title>");
		out.println("Sign Up Succeed");
		out.println("</title></head>");
		out.println("<body>");
		out.println(info);
		out.println("<br>");
		out.println("Your User-Agent: " + req.getHeader("User-Agent"));
		out.println("<br>");
		out.println("Your IP: " + req.getRemoteAddr());
		out.println("<br>");
		out.println("Your Port: " + req.getRemotePort());
		out.println("<br>");
		out.println("<br>");
		out.println("<br>");
		out.println("<br>");
		out.println("<br>");
		out.println("</body></html>");
		out.close();

	}

}
