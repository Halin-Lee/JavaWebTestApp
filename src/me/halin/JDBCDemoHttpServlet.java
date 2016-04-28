package me.halin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class JDBCDemoHttpServlet
 */
@WebServlet(description = "JDBC测试", urlPatterns = { "/JDBCDemoHttpServlet" })
public class JDBCDemoHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String dbUrl;
	private String userName;
	private String password;
	private String tableName;

	private String databaseName;

	@Override
	public void init() throws ServletException {

		super.init();
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

		try {
			Connection conn = DriverManager.getConnection(dbUrl, userName,
					password);
			java.sql.Statement statement = conn.createStatement();
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
		}
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JDBCDemoHttpServlet() {
		super();
		// TODO Auto-generated constructor stub
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
		try {
			Connection conn = DriverManager.getConnection(dbUrl, userName,
					password);
			java.sql.Statement statement = conn.createStatement();
			statement.executeUpdate("use tempDatabase");

			switch (method) {
			case "set": {
				String insertStr = String.format(
						"replace INTO %s VALUES ('%d', '%s')", tableName, id,
						title);
				writer.println("执行语句 " + insertStr + "\r\n");
				statement.addBatch(insertStr);
				statement.executeBatch();
				String output = String.format("插入数据成功 id：%d title:%s", id,
						title);
				writer.println(output);
			}
				break;

			case "delete": {

				String deleteString = String.format(
						"delete from %s where id=%d", tableName, id);

				statement.addBatch(deleteString);
				statement.executeBatch();
				String output = String.format("删除成功 id：%d title:%s", id,
						title);
				writer.println(output);
			}
				break;

			case "get":
			default: {
				String queryString = "select * from " + tableName
						+ " where id=" + id;
				writer.write("执行语句 " + queryString + "\r\n");
				ResultSet resultSet = statement.executeQuery(queryString);

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
		}
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
