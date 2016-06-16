package me.halin.jdbcstudy;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.crypto.provider.RSACipher;

/**
 * Servlet implementation class JDBCMetadataHttpServlet
 */
@WebServlet(description = "metadata测试,获得数据库的元数据", urlPatterns = { "/JDBC/JDBCMetadataHttpServlet" }, initParams = {
		@WebInitParam(name = "url", value = "jdbc:mysql://localhost:3306"),
		@WebInitParam(name = "user", value = "root"),
		@WebInitParam(name = "password", value = "123456"),
		@WebInitParam(name = "tableName", value = "tempTable"),
		@WebInitParam(name = "databaseName", value = "tempDatabase"),
		@WebInitParam(name = "driverClass", value = "com.mysql.jdbc.Driver") })
public class JDBCMetadataHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String dbUrl;
	private String userName;
	private String password;
	private String tableName;
	private String databaseName;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JDBCMetadataHttpServlet() {
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
		// TODO Auto-generated method stub

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8"); // 注，设置chaseset要在getWritter之前

		PrintWriter writer = response.getWriter();

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbUrl, userName, password);
			Statement statement = conn.createStatement();
			statement.executeUpdate("use tempDatabase");

			// http://blog.sina.com.cn/s/blog_707a9f0601014y1a.html
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			ResultSet resultSet = databaseMetaData.getTables(databaseName,
					null, null, new String[] { "TABLE" });
			while (resultSet.next()) {
				writer.write("找到数据库项:" + resultSet.getString("TABLE_NAME")
						+ "\r\n");
			}

			resultSet = statement.executeQuery("select * from " + tableName);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			writer.write("找到" + columnCount + "列,具体如下： \r\n");

			for (int i = 1; i <= columnCount; i++) {
				String columnTypeString = resultSetMetaData.getColumnTypeName(i);
				String columnNameString = resultSetMetaData.getColumnName(i);
				int displaySize = resultSetMetaData.getColumnDisplaySize(i);
				writer.write(columnTypeString + " " + columnNameString + " "
						+ displaySize + "|\r\n");
			}

		} catch (SQLException e) {
			writer.write("数据库操作失败" + e);
			e.printStackTrace();
		} finally {
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
