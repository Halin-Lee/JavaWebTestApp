package me.halin.jdbcstudy;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet implementation class JDBCTransactionHttpServlet
 *
 * @see <a
 *      href="http://localhost:8000/JavaWebTestApp/JDBC/JDBCTransactionHttpServlet?transactionType=readuncommitted">测试链接</a>
 *
 */
@WebServlet(description = "Transaction应用举例", urlPatterns = { "/JDBC/JDBCTransactionHttpServlet" }, initParams = {
		@WebInitParam(name = "url", value = "jdbc:mysql://localhost:3306"),
		@WebInitParam(name = "user", value = "root"),
		@WebInitParam(name = "password", value = "123456"),
		@WebInitParam(name = "tableName", value = "tempTable"),
		@WebInitParam(name = "databaseName", value = "tempDatabase"),
		@WebInitParam(name = "driverClass", value = "com.mysql.jdbc.Driver"),
		@WebInitParam(name = "transactionLevel", value = "1") })
public class JDBCTransactionHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String KEY_OLD_TITLE = "OldTitle";
	public static final String KEY_SET_TITLE = "SetTitle";
	public static final String KEY_LAST_TITLE = "LastTitle";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JDBCTransactionHttpServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String dbUrl;
	private String userName;
	private String password;
	private String tableName;
	private String databaseName;
	private Random random = new Random();
	private ExecutorService threadPool = Executors.newCachedThreadPool();

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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8"); // 注，设置chaseset要在getWritter之前
		PrintWriter writer = response.getWriter();
		// 取出参数
		String transactionTypeString = request.getParameter("transactionType");
		int transactionType = parseTransactionType(transactionTypeString);
		if (transactionType == -1) {
			System.out.print("未指定TransactionType");
			throw new ServletException("未指定TransactionType");
		}

		boolean checkDeadLock;
		String checkDeadLockString = request.getParameter("checkDeadLock");
		checkDeadLock = "true".equals(checkDeadLockString);

		try {
			System.out.println("开始检查");

			if (checkDeadLock) {
				deadLock(transactionType);
				System.out.println("死锁检测通过");
			}

			dirtyRead(transactionType);
			System.out.println("脏读检测通过");

			unrepeatableRead(transactionType);
			System.out.println("不可重复读检测通过");

			phantomRead(transactionType);
			System.out.println("幻读检测通过");

			writer.write("所有检查通过");

		} catch (SQLException | InterruptedException e) {
			System.out.print("数据库操作失败");
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	private void dirtyRead(int transactionType) throws SQLException,
			InterruptedException {
		Connection conn1 = null;
		Connection conn2 = null;

		try {
			conn1 = DriverManager.getConnection(dbUrl, userName, password);
			Statement statement1 = conn1.createStatement();
			statement1.executeUpdate("use tempDatabase");

			conn2 = DriverManager.getConnection(dbUrl, userName, password);
			final Statement statement2 = conn2.createStatement();
			statement2.executeUpdate("use tempDatabase");

			conn1.setAutoCommit(false);
			conn1.setTransactionIsolation(transactionType);
			conn2.setAutoCommit(false);
			conn2.setTransactionIsolation(transactionType);

			// 获得先前数值，不提交
			String title1 = get(statement1, 0);
			// 通过另一个链接写入，但不提交(因为serializable会导致死锁，必须使用异步)
			Future<?> future = setAsync(conn2, statement2, 0,
					Integer.toString(random.nextInt()), false);

			// 等待足够的时间让conn2写入
			Thread.sleep(1000);
			// 再从第一个链接读取，发现数值已被修改
			if (!title1.equals(get(statement1, 0)) && future.isDone()) {
				conn1.rollback();
				conn2.rollback();
				throw new RuntimeException("脏读");
			}
			conn1.commit();
			conn2.commit();

		} finally {
			if (conn1 != null) {
				conn1.close();
			}
			if (conn2 != null) {
				conn2.close();
			}
		}
	}

	private void unrepeatableRead(int transactionType) throws SQLException,
			InterruptedException {
		Connection conn1 = null;
		Statement statement1 = null;
		Connection conn2 = null;
		Statement statement2 = null;
		try {
			conn1 = DriverManager.getConnection(dbUrl, userName, password);
			statement1 = conn1.createStatement();
			statement1.executeUpdate("use tempDatabase");

			conn2 = DriverManager.getConnection(dbUrl, userName, password);
			statement2 = conn2.createStatement();
			statement2.executeUpdate("use tempDatabase");

			conn1.setAutoCommit(false);
			conn1.setTransactionIsolation(transactionType);
			conn2.setAutoCommit(false);
			conn2.setTransactionIsolation(transactionType);

			// 获得先前数值，不提交
			String title1 = get(statement1, 0);

			// 通过另一个链接写入，提交(因为serializable会导致死锁，必须使用异步)
			Future<?> future = setAsync(conn2, statement2, 0,
					Integer.toString(random.nextInt()), true);
			// 等待足够的时间让conn2写入
			Thread.sleep(1000);

			// 再从第一个链接读取，发现数值已被修改
			if (!title1.equals(get(statement1, 0)) && future.isDone()) {
				conn1.rollback();
				throw new RuntimeException("不可重复读取");
			}
			conn1.commit();

		} finally {
			if (conn1 != null) {
				conn1.close();
			}
			if (conn2 != null) {
				conn2.close();
			}
		}
	}

	private void phantomRead(int transactionType) throws SQLException,
			InterruptedException {
		Connection conn1 = null;
		Statement statement1 = null;
		Connection conn2 = null;
		Statement statement2 = null;
		int id = 10010;

		try {
			conn1 = DriverManager.getConnection(dbUrl, userName, password);
			statement1 = conn1.createStatement();
			statement1.executeUpdate("use tempDatabase");

			conn2 = DriverManager.getConnection(dbUrl, userName, password);
			statement2 = conn2.createStatement();
			statement2.executeUpdate("use tempDatabase");

			// 先清除之前的影响
			deleteRow(statement2, id);

			conn1.setAutoCommit(false);
			conn1.setTransactionIsolation(transactionType);
			conn2.setAutoCommit(false);
			conn2.setTransactionIsolation(transactionType);

			// 先获得总共有多少行
			int rowCount = getTotalCount(statement1);

			// 通过另一个链接写入，提交(因为serializable会导致死锁，必须使用异步)
			Future<?> future = setAsync(conn2, statement2, 0,
					Integer.toString(random.nextInt()), true);
			// 等待足够的时间让conn2写入
			Thread.sleep(1000);

			// 再从第一个链接获取总共有多少行，发现被修改
			if (rowCount == getTotalCount(statement1) && future.isDone()) {
				// 幻读指的是一个事务中无法获得实际的行数，书中说，幻读指的是读取同一个数据项时发现多一个，其实应该是说，第一个事务无法感知后面的更改
				conn1.rollback();
				throw new RuntimeException("幻读");
			}
			conn1.commit();

		} finally {
			if (conn1 != null) {
				conn1.close();
			}
			if (conn2 != null) {
				conn2.close();
			}
		}
	}

	private void deadLock(int transactionType) throws SQLException {
		Connection conn1 = null;
		Statement statement1 = null;
		Connection conn2 = null;
		Statement statement2 = null;
		try {
			conn1 = DriverManager.getConnection(dbUrl, userName, password);
			statement1 = conn1.createStatement();
			statement1.executeUpdate("use tempDatabase");

			conn2 = DriverManager.getConnection(dbUrl, userName, password);
			statement2 = conn2.createStatement();
			statement2.executeUpdate("use tempDatabase");

			conn1.setAutoCommit(false);
			conn1.setTransactionIsolation(transactionType);
			conn2.setAutoCommit(false);
			conn2.setTransactionIsolation(transactionType);

			// 获得先前数值，不提交
			String title1 = get(statement1, 0);

			// 通过另一个链接写入，提交(因为serializable会导致死锁，必须使用异步)
			set(statement2, 0, Integer.toString(random.nextInt()));
			conn2.commit();
			conn1.commit();

		} finally {
			if (conn1 != null) {
				conn1.close();
			}
			if (conn2 != null) {
				conn2.close();
			}
		}

	}

	/** 辅助方法，读取一行 */
	private String get(Statement statement, int id) throws SQLException {
		String queryString = "select * from " + tableName + " where id=" + id;
		ResultSet resultSet = statement.executeQuery(queryString);
		resultSet.next();
		return resultSet.getString("title");
	}

	/**
	 * 异步set
	 * 
	 * @return
	 */
	private Future<?> setAsync(Connection connection, Statement statement,
			int id, String title, boolean commited) {
		// 通过另一个链接写入，但不提交(因为serializable会导致死锁，必须使用异步)
		return threadPool.submit(() -> {
			try {
				set(statement, 0, Integer.toString(random.nextInt()));
				if (commited) {
					connection.commit();
				}

			} catch (Exception e) {
			}
		});
	}

	/** 辅助方法，设置一行 */
	private void set(Statement statement, int id, String title)
			throws SQLException {

		String insertStr = String.format("replace INTO %s VALUES ('%d', '%s')",
				tableName, id, title);
		statement.addBatch(insertStr);
		statement.executeBatch();
	}

	/** 辅助方法，获得数据库总共有多少行 */
	private int getTotalCount(Statement statement) throws SQLException {

		ResultSet resultSet = statement.executeQuery("select * from "
				+ tableName);
		resultSet.last();
		return resultSet.getRow();

	}

	private void insertRow(Statement statement, int id, String title)
			throws SQLException {
		String insertStr = String.format("INSERT INTO %s VALUES ('%d', '%s')",
				tableName, id, title);
		statement.addBatch(insertStr);
		statement.executeBatch();
	}

	private void deleteRow(Statement statement, int id) throws SQLException {
		String deleteString = String.format("delete from %s where id=%d",
				tableName, id);
		statement.addBatch(deleteString);
		statement.executeBatch();
	}

	/** 将transactionString转换为int */
	private int parseTransactionType(String typeString) {
		if (typeString == null) {
			return -1;
		}

		switch (typeString) {
		case "readuncommitted":
			return Connection.TRANSACTION_READ_UNCOMMITTED;

		case "readcommitted":
			return Connection.TRANSACTION_READ_COMMITTED;

		case "repeatable":
			return Connection.TRANSACTION_REPEATABLE_READ;

		case "serializable":
			return Connection.TRANSACTION_SERIALIZABLE;

		case "none":
			return Connection.TRANSACTION_NONE;

		default:
			return -1;
		}

	}

}
