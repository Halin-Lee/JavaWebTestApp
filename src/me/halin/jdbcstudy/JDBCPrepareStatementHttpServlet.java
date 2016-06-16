package me.halin.jdbcstudy;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction;

/**
 * Servlet implementation class JDBCPrepareStatmentHttpServlet
 */
@WebServlet(urlPatterns = { "/JDBC/JDBCPrepareStatmentHttpServlet" }, initParams = {
		@WebInitParam(name = "url", value = "jdbc:mysql://localhost:3306"),
		@WebInitParam(name = "user", value = "root"),
		@WebInitParam(name = "password", value = "123456"),
		@WebInitParam(name = "tableName", value = "tempTable"),
		@WebInitParam(name = "databaseName", value = "tempDatabase"),
		@WebInitParam(name = "driverClass", value = "com.mysql.jdbc.Driver") })
public class JDBCPrepareStatementHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String dbUrl;
	private String userName;
	private String password;
	private String tableName;
	private String databaseName;

	private PreparedStatement getPreparedStatement;
	private PreparedStatement setPreparedStatement;
	private PreparedStatement deletePreparedStatement;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JDBCPrepareStatementHttpServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// 驱动配置主要将mysql-connector-java放入tomcat的lib文件夹
		String driverClass = getInitParameter("driverClass");
		dbUrl = getInitParameter("url");
		userName = getInitParameter("user");
		password = getInitParameter("password");
		databaseName = getInitParameter("databaseName");
		tableName = getInitParameter("tableName");

		try {
			// 加载Driver类
			Class.forName(driverClass);
			System.out.println("驱动加载完成");
		} catch (ClassNotFoundException e) {
			System.out.println("驱动加载失败  error:" + e);
		}

		Connection conn = null;
		Statement statement = null;
		try {

			// 建表
			conn = DriverManager.getConnection(dbUrl, userName, password);
			statement = conn.createStatement();
			statement.executeUpdate("create database if not exists "
					+ databaseName);
			statement.executeUpdate("use tempDatabase");
			statement
					.executeUpdate("create table if not exists "
							+ tableName
							+ "(id INT not null primary key,title VARCHAR(50) not null)");

			System.out.println("数据库加载成功");

		} catch (SQLException e) {
			System.out.println("数据库加载失败" + e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8"); // 注，设置chaseset要在getWritter之前

		String method = request.getParameter("method");
		method = method == null ? "get" : method;

		String idStr = request.getParameter("id");

		String title = request.getParameter("title");

		PrintWriter writer = response.getWriter();
		int id;
		try {
			id = Integer.parseInt(idStr);
		} catch (NumberFormatException e) {
			writer.write("id错误,Error:" + e);
			writer.close();
			return;
		}

		Connection conn = null;
		Statement statement = null;
		try {
			conn = DriverManager.getConnection(dbUrl, userName, password);
			statement = conn.createStatement();
			statement.executeUpdate("use tempDatabase");

			// 初始化操作，在这里prepareStatement并没有发挥他的作用，只是做测试，真正高效是在同一个conn下多次执行时，如for循环插入多个数据
			String getQueryString = "select * from " + tableName
					+ " where id=?";
			getPreparedStatement = conn.prepareStatement(getQueryString);
			String setQueryString = String.format(
					"replace INTO %s VALUES (?, ?)", tableName);
			setPreparedStatement = conn.prepareStatement(setQueryString);
			String deleteString = String.format("delete from %s where id=?",
					tableName);
			deletePreparedStatement = conn.prepareStatement(deleteString);

			switch (method) {
			case "set": {
				setPreparedStatement.setInt(1, id);
				setPreparedStatement.setString(2, title);
				setPreparedStatement.executeBatch();
				String output = String.format("插入数据成功 id：%d title:%s", id,
						title);
				writer.println(output);
			}
				break;

			case "delete": {

				deletePreparedStatement.setInt(1, id);
				deletePreparedStatement.executeBatch();
				String output = String.format("删除成功 id：%d title:%s", id, title);
				writer.println(output);
			}
				break;

			case "get":
			default: {

				getPreparedStatement.setInt(1, id);
				ResultSet resultSet = getPreparedStatement.executeQuery();

				while (resultSet.next()) {
					title = resultSet.getString("title");
					writer.write("查到数据：id" + id + ",title:" + title + "\r\n");
				}

				if (!resultSet.first()) {
					writer.write("找不到数据" + "\r\n");
				}
			}
				break;
			}

		} catch (SQLException e) {
			writer.write("数据库操作失败" + e);
			System.out.print("数据库操作失败" + e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
