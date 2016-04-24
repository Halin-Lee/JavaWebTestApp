package me.halin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SignUpDemo extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String prefix;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		// ��servletConfig�ж�ȡ��ʼ���������˲�����web��xml��ʼ��
		prefix = getServletConfig().getInitParameter("SIGN_UP_DEMO_PREFIX");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// û���ύUserName���ض����ύҳ��
		// resp.sendRedirect("SignUp.html"); //�ض���
		 req.getRequestDispatcher("SignUp.html").forward(req, resp) ; //װ����SignUp��html

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// servlet super�ķ������ܵ��ã�����ᵱ��servlet����������ͷ���
		// super.doGet(req, resp);

		req.setCharacterEncoding("UTF-8");
		String userName = req.getParameter("userName");
		resp.setContentType("text/html;charset=utf-8"); // ע������chasesetҪ��getWritter֮ǰ

		if (userName == null || userName.isEmpty()) {
			// û���ύUserName�����ش���
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "�û���Ϊ��");
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
