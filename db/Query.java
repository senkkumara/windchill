package ext.hydratight.db;

import ext.hydratight.GeneralUtils;
import ext.hydratight.db.DataTable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.sql.SQLException;

/**
 *	Allows querying any Oracle database.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class Query
{

	protected final String WT_HOME = System.getenv("WT_HOME");
	protected final String SERVER_HOST_PROPERTY = "wt.rmi.server.hostname";
	protected String DRIVER = "oracle.jdbc.driver.OracleDriver";
	protected String SERVER_HOSTNAME = "";
	protected String DB_HOSTNAME = "";
	protected String URL = null;
	protected String USERNAME = null;
	protected String PASSWORD = null;
	protected String SQL_PATH = WT_HOME + "/src/ext/site/";
	protected String QUERY = "";
	protected Connection conn = null;
	protected ResultSet rs = null;
	protected Statement stmt = null;

	/**
	 *	Constructor
	 */
	public Query(String url, String usr, String pwd)
	{
		URL = url;
		USERNAME = usr;
		PASSWORD = pwd;
	}

	/**
	 *	Constructor
	 */
	public Query(String host, String port, String service, String usr, String pwd)
	{
		this("jdbc:oracle:thin:@//" + host + ":" + port + "/" + service.toUpperCase(), usr, pwd);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// GETTERS / SETTERS ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 *	Driver getter.
	 *
	 *		@return value of driver
	 */
	public String getDriver()
	{
		return DRIVER;
	}

	/**
	 *	Driver setter.
	 *
	 *		@param dvr the value of driver to set
	 */
	public Query setDriver(String dvr)
	{
		DRIVER = dvr;
		return this;
	}

	/**
	 *	URL getter.
	 *
	 *		@return value of URL
	 */
	public String getURL()
	{
		return URL;
	}

	/**
	 *	URL setter.
	 *
	 *		@param url the value of URL to set
	 */
	public Query setURL(String url)
	{
		URL = url;
		return this;
	}

	/**
	 *	Password getter.
	 *
	 *		@return value of password
	 */
	public String getPassword()
	{
		return PASSWORD;
	}

	/**
	 *	Password setter.
	 *
	 *		@param pwd the value of password to set
	 */
	public Query setPassword(String pwd)
	{
		PASSWORD = pwd;
		return this;
	}

	/**
	 *	Username getter.
	 *
	 *		@return value of username
	 */
	public String getUsername()
	{
		return USERNAME;
	}

	/**
	 *	Username setter.
	 *
	 *		@param usr the value of username to set
	 */
	public Query setUserName(String usr)
	{
		USERNAME = usr;
		return this;
	}

	/**
	 *	Query getter.
	 *
	 *		@return value of query
	 */
	public String getQuery()
	{
		return QUERY;
	}

	/**
	 *	Query setter.
	 *
	 *		@param qry the value of query to set
	 */
	public Query setQuery(String qry)
	{
		QUERY = qry;
		return this;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC FUNCTIONS /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 *	Returns a Map containing the full table of the results from the query - this is the most<br />
	 *	generic of all of the database querying functions, making no assumptions of the output<br />
	 *	from the script.<br />
	 *	<br />
	 *	The return from this function is a map that may take one of two forms depending on the value of the<br />
	 *	groupByColumn argument:
	 *	<ol>
	 *		<li>groupByColumn = FALSE:<br />
	 *			Map&lt;[ID / COUNTER], Map&lt;[COLUMN NAME], [VALUE]&gt;&gt;</li>
	 *		<li>groupByColumn= TRUE:<br />
	 *			Map&lt;[COLUMN NAME], Map&lt;[ID / COUNTER], [VALUE]&gt;&gt;</li>
	 *	</ol>
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceptable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>idCol - a numberic counter (1-based)</li>
	 *		<li>args - null</li>
	 *		<li>groupByColumn - FALSE</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("PARTNUMBER", "001AA000001");<br />
	 *	<br />
	 *	ext.site.Connect.query(false,"part-query.sql", "part_number", args, false);
	 *	</code>
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param idCol a String containing the name of the column to act as the unique identifier, e.g. "Part Number"
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@param groupByColumn a boolean as to whether the returned data should be grouped by row (false) or column (true)
	 *		@return a map containing the table returned from a SQL query
	 */
	public DataTable query(String sql, boolean raw, String idCol, Map<String, String> args)
			throws Exception, SQLException
	{
		this.run(raw, sql, args);
		DataTable data = new DataTable(rs, idCol);
		this.close();
		return data;
	}

	public DataTable query(String sql, boolean raw, String idCol)
			throws Exception, SQLException
	{
		return this.query(sql, raw, idCol, null);
	}

	public DataTable query(String sql, boolean raw, Map<String, String> args)
			throws Exception, SQLException
	{
		return this.query(sql, raw, null, args);
	}

	public DataTable query(String sql, String idCol, Map<String, String> args)
			throws Exception, SQLException
	{
		return this.query(sql, false, idCol, args);
	}

	public DataTable query(String sql, boolean raw)
			throws Exception, SQLException
	{
		return this.query(sql, raw, null, null);
	}

	public DataTable query(String sql, String idCol)
			throws Exception, SQLException
	{
		return this.query(sql, false, idCol, null);
	}

	public DataTable query(String sql, Map<String, String> args)
			throws Exception, SQLException
	{
		return this.query(sql, false, null, args);
	}

	public DataTable query(String sql)
			throws Exception, SQLException
	{
		return this.query(sql, false, null, null);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE FUNCTIONS ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 *	Retrieves the contents of the file found at the specified path - this path<br />
	 *	may be relative or absolute.
	 */
	private void getFile(String file, boolean absolute, String start)
	{
		String path = null;
		if (absolute) {
			path = file;
		}
		else {
			if (start != null) {
				path = start + file;
			}
			else {
				path = SQL_PATH + file;
			}
		}

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			QUERY = sb.toString();

		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void getFile(String file, boolean absolute)
	{
		getFile(file, absolute, null);
	}

	/**
	 *	Retrieves the contents of the file found at the specified path.
	 *	
	 *		@param file the path to the file to retrieve.
	 */
	private void getFile(String file)
	{
		getFile(file, false, null);
	}

	/**
	 *	
	 */
	private void processInputs(String placeholder, String arg)
	{
		QUERY = QUERY.replace(placeholder, arg);
	}

	/**
	 *	
	 */
	private void processInputs(Map<String, String> args)
	{
		if (args != null) {
			for (Entry<String, String> arg : args.entrySet()) {
				processInputs(arg.getKey(), arg.getValue());
			}
		}

	}

	/**
	 *	
	 */
	private void run(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		try {
			Class.forName(DRIVER);
		}
		catch (ClassNotFoundException cnf) {
			throw new Exception(cnf);
		}

		if (sql == null) {
			throw new Exception("No sql provided!");
		}

		if (! raw) {
			getFile(sql);
		}
		else {
			QUERY = sql;
		}

		if (QUERY.equals("")) {
			throw new Exception("No query retrieved!");
		}

		processInputs(args);
		execute();
	}

	/**
	 *	
	 */
	private void execute()
		throws SQLException
	{
		conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		System.out.println("Connection Opened.");
		conn.setAutoCommit(false);
		stmt = conn.createStatement();

		try {
			System.out.println("Executing Query...");
			System.out.println("\n\n" + QUERY);
			System.out.println("\n\n");
			rs = stmt.executeQuery(QUERY);
			System.out.println(rs);

		}
		catch (SQLException ex) {
			ex.printStackTrace();
			close();
			throw new SQLException();
		}
	}

	/**
	 *	Closes the statement - this MUST be applied after all queries.
	 */
	private void close()
		throws SQLException
	{
		System.out.println("Closing");
		if (! stmt.isClosed()) {
			System.out.println("Statement was not closed...");
			stmt.close();
		}

		if(! conn.isClosed())
		{
			System.out.println("Connection was not closed...");
			conn.close();
		}

		System.out.println ("Connection Closed.");
	}

}