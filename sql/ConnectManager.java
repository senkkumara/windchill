package ext.site.sql;

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
import wt.util.WTProperties;

import java.sql.SQLException;

/**
 *	All Public methods *MUST* have the following structure:
 *		1:	Run query
 *		2:	Process data
 *		3:	Close statement
 *		4:	Return data
 *
 *	TODO:
 *		- Allow optional paths for location of SQL
 *		- Allow passing of DB credentials
 */
public class ConnectManager {

	protected final String WT_HOME = System.getenv("WT_HOME");
	protected final String SERVER_HOST_PROPERTY = "wt.rmi.server.hostname";
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

	public ConnectManager(String url, String usr, String pwd)
	{
		URL = url;
		USERNAME = usr;
		PASSWORD = pwd;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// GETTERS / SETTERS ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getURL()
	{
		return URL;
	}

	public ConnectManager setURL(String url)
	{
		URL = url;
		return this;
	}

	public String getPassword()
	{
		return PASSWORD;
	}

	public ConnectManager setPassword(String pwd)
	{
		PASSWORD = pwd;
		return this;
	}

	public String getUsername()
	{
		return USERNAME;
	}

	public ConnectManager setUserName(String usr)
	{
		USERNAME = usr;
		return this;
	}

	public String getQuery()
	{
		return QUERY;
	}

	public ConnectManager setQuery(String qry)
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
	public Map<String, Map<String, String>> query(boolean raw, String sql, String idCol, Map<String, String> args,
			boolean groupByColumn)

		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, Map<String, String>> data = getDataTable(idCol, groupByColumn);
		close();
		return data;
	}

	public Map<String, Map<String, String>> query(String sql)
		throws Exception, SQLException
	{
		return query(false, sql, null, null, false);
	}

	public Map<String, Map<String, String>> query(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return query(false, sql, null, args, false);
	}

	public Map<String, Map<String, String>> query(String sql, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(false, sql, null, null, groupByColumn);
	}

	public Map<String, Map<String, String>> query(String sql, Map<String, String> args, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(false, sql, null, args, groupByColumn);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql)
		throws Exception, SQLException
	{
		return query(raw, sql, null, null, false);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return query(raw, sql, null, args, false);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(raw, sql, null, null, groupByColumn);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql, Map<String, String> args, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(raw, sql, null, args, groupByColumn);
	}

	public Map<String, Map<String, String>> query(String sql, String idCol)
		throws Exception, SQLException
	{
		return query(false, sql, idCol, null, false);
	}

	public Map<String, Map<String, String>> query(String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return query(false, sql, idCol, args, false);
	}

	public Map<String, Map<String, String>> query(String sql, String idCol, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(false, sql, idCol, null, groupByColumn);
	}

	public Map<String, Map<String, String>> query(String sql, String idCol, Map<String, String> args, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(false, sql, idCol, args, groupByColumn);
	}

	public Map<String, Map<String, String>> query(boolean raw, String idCol, String sql)
		throws Exception, SQLException
	{
		return query(raw, sql, idCol, null, false);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return query(raw, sql, idCol, args, false);
	}

	public Map<String, Map<String, String>> query(boolean raw, String sql, String idCol, boolean groupByColumn)
		throws Exception, SQLException
	{
		return query(raw, sql, idCol, null, groupByColumn);
	}

	/**
	 *	Returns a List containing the full table of results from the query.<br />
	 *	<br />
	 *	Each member of the list corresponds to a row in the results and consists of a Map. The map contains<br />
	 *	a key-value pair where the key is the column and the value is the value within the results:
	 *	<ul>
	 *		<li>List&lt;Map&lt;[COLUMN NAME], [VALUE]&gt;&gt;</li>
	 *	</ul>
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceprable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>args - null</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("PARTNUMBER", "001AA000001");<br />
	 *	<br />
	 *	ext.site.Connect.getTableByRow(false,"part-query.sql", "part_number", args);
	 *	</code>
	 *	
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@return a list containing the table returned from a SQL query
	 */
	public List<Map<String, String>> getTableByRow(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		run(raw, sql, args);									// Run Query
		List<Map<String, String>> data = getDataTableByRow();	// Process Results
		close();												// Close Statement
		return data;											// Return Results
	}

	public List<Map<String, String>> getTableByRow(String sql)
		throws Exception, SQLException
	{
		return getTableByRow(false, sql, null);
	}

	public List<Map<String, String>> getTableByRow(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getTableByRow(false, sql, args);
	}

	public List<Map<String, String>> getTableByRow(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getTableByRow(raw, sql, null);
	}

	/**
	 *	Returns a Map containing the full table of results from the query.<br />
	 *	<br />
	 *	The map contains a key-value pair where the key is the column name and the value is<br />
	 *	a list of the values in the column:
	 *	<ul>
	 *		<li>Map&lt;[COLUMN NAME], List&lt;[VALUE]&gt;&gt;</li>
	 *	</ul>
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceprable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>args - null</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("PARTNUMBER", "001AA000001");<br />
	 *	<br />
	 *	ext.site.Connect.getTableByColumn(false,"part-query.sql", args);
	 *	</code>
	 *	
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, List<String>> getTableByColumn(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		run(raw, sql, args);										// Run Query
		Map<String, List<String>> data = getDataTableByColumn();	// Process Results
		close();													// Close Statement
		return data;												// Return Results
	}

	public Map<String, List<String>> getTableByColumn(String sql)
		throws Exception, SQLException
	{
		return getTableByColumn(false, sql, null);
	}

	public Map<String, List<String>> getTableByColumn(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getTableByColumn(false, sql, args);
	}

	public Map<String, List<String>> getTableByColumn(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getTableByColumn(raw, sql, null);
	}

	/**
	 *	Returns a list of values in a given column.<br />
	 *	<br />
	 *	This column may be the only column returned or a nominated column. Please note that if more than one column<br />
	 *	is returned and none are nominated, an exception will be thrown.<br />
	 *	<br />
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *		<li>col (if more than one column is returned from the query)</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceprable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>args - null</li>
	 *		<li>col - null</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("ECNNUMBER", "ECN0000001");<br />
	 *	<br />
	 *	ext.site.Connect.getColumn(false,"ecn-query.sql", args, "status");
	 *	</code>
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@param col the name of the column to retrieve
	 *		@return a list containing the table returned from a SQL query
	 */
	public List<String> getColumn(boolean raw, String sql, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		run(raw, sql, args);						// Run Query
		List<String> data = getDataColumn(col);		// Process Results
		close();									// Close Statement
		return data;								// Return Results
	}

	public List<String> getColumn(String sql)
		throws Exception, SQLException
	{
		return getColumn(false, sql, null, null);
	}

	public List<String> getColumn(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumn(false, sql, args, null);
	}

	public List<String> getColumn(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getColumn(raw, sql, null, null);
	}

	public List<String> getColumn(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumn(raw, sql, args, null);
	}

	public List<String> getColumn(String sql, String col)
		throws Exception, SQLException
	{
		return getColumn(false, sql, null, col);
	}

	public List<String> getColumn(String sql, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		return getColumn(false, sql, args, col);
	}

	public List<String> getColumn(boolean raw, String sql, String col)
		throws Exception, SQLException
	{
		return getColumn(raw, sql, null, col);
	}

	/**
	 *	Returns a Map of values in a given column with corresponding value in a nominated ID column.<br />
	 *	<br />
	 *	This column may be the only column - other than the ID column - that is returned or a nominated column.<br />
	 *	Please note that if more than one column is returned and none are nominated, an exception will be thrown.<br />
	 *	<br />
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *		<li>idCol</li>
	 *		<li>col (if more than one column plus the ID column is returned from the query)</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceprable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>args - null</li>
	 *		<li>col - null</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("ECNNUMBER", "ECN0000001");<br />
	 *	<br />
	 *	ext.site.Connect.getColumnWithID(false,"ecn-query.sql", "number", args, "status");
	 *	</code>
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param idCol a String containing the name of the column to act as the unique identifier, e.g. "Part Number"
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@param col the name of the column to retrieve
	 *		@return a map containing the table returned from a SQL query	
	 */
	public Map<String, String> getColumnWithID(boolean raw, String sql, String idCol, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, String> data = getDataColumnWithID(idCol, col);
		close();
		return data;
	}

	public Map<String, String> getColumnWithID(String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnWithID(false, sql, idCol, null, null);
	}

	public Map<String, String> getColumnWithID(String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnWithID(false, sql, idCol, args, null);
	}

	public Map<String, String> getColumnWithID(boolean raw, String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnWithID(raw, sql, idCol, null, null);
	}

	public Map<String, String> getColumnWithID(boolean raw, String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnWithID(raw, sql, idCol, args, null);
	}

	public Map<String, String> getColumnWithID(String sql, String idCol, String col)
		throws Exception, SQLException
	{
		return getColumnWithID(false, sql, idCol, null, col);
	}

	public Map<String, String> getColumnWithID(String sql, String idCol, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		return getColumnWithID(false, sql, idCol, args, col);
	}

	public Map<String, String> getColumnWithID(boolean raw, String sql, String idCol, String col)
		throws Exception, SQLException
	{
		return getColumnWithID(raw, sql, idCol, null, col);
	}

	/**
	 *	
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@param col the name of the column to retrieve
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, Integer> getColumnByValue(boolean raw, String sql, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, Integer> data = getDataColumnByValue(col);
		close();
		return data;
	}

	public Map<String, Integer> getColumnByValue(String sql)
		throws Exception, SQLException
	{
		return getColumnByValue(false, sql, null, null);
	}

	public Map<String, Integer> getColumnByValue(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnByValue(false, sql, args, null);
	}

	public Map<String, Integer> getColumnByValue(String sql, Map<String, String> args, String col)
		throws Exception, SQLException
	{
		return getColumnByValue(false, sql, args, col);
	}

	public Map<String, Integer> getColumnByValue(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getColumnByValue(raw, sql, null, null);
	}

	public Map<String, Integer> getColumnByValue(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnByValue(raw, sql, args, null);
	}

	/**
	 *	
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param idCol a String containing the name of the column to act as the unique identifier, e.g. "Part Number"
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@param col the name of the column to retrieve
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, List<String>> getColumnByValueWithID(boolean raw, String sql, String idCol,
			Map<String, String> args, String col)

		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, List<String>> data = getDataColumnByValueWithID(idCol);
		close();
		return data;
	}

	public Map<String, List<String>> getColumnByValueWithID(String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnByValueWithID(false, sql, idCol, null, null);
	}

	public Map<String, List<String>> getColumnByValueWithID(String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnByValueWithID(false, sql, idCol, args, null);
	}

	public Map<String, List<String>> getColumnByValueWithID(String sql, String idCol,Map<String, String> args, String col)
		throws Exception, SQLException
	{
		return getColumnByValueWithID(false, sql, idCol, args, col);
	}

	public Map<String, List<String>> getColumnByValueWithID(boolean raw, String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnByValueWithID(raw, sql, idCol, null, null);
	}

	public Map<String, List<String>> getColumnByValueWithID(boolean raw, String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnByValueWithID(raw, sql, idCol, args, null);
	}

	/**
	 *	
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, Map<String, Integer>> getColumnsByValue(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, Map<String, Integer>> data = getDataColumnsByValue();
		close();
		return data;
	}

	public Map<String, Map<String, Integer>> getColumnsByValue(String sql)
		throws Exception, SQLException
	{
		return getColumnsByValue(false, sql, null);
	}

	public Map<String, Map<String, Integer>> getColumnsByValue(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnsByValue(false, sql, args);
	}

	public Map<String, Map<String, Integer>> getColumnsByValue(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getColumnsByValue(raw, sql, null);
	}

	/**
	 *	
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param idCol a String containing the name of the column to act as the unique identifier, e.g. "Part Number"
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, Map<String, List<String>>> getColumnsByValueWithID(boolean raw, String sql, String idCol,
			Map<String, String> args)

		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, Map<String, List<String>>> data = getDataColumnsByValueWithID(idCol);
		close();
		return data;
	}

	public Map<String, Map<String, List<String>>> getColumnsByValueWithID(String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnsByValueWithID(false, sql, idCol, null);
	}

	public Map<String, Map<String, List<String>>> getColumnsByValueWithID(String sql, String idCol, Map<String, String> args)
		throws Exception, SQLException
	{
		return getColumnsByValueWithID(false, sql, idCol, args);
	}

	public Map<String, Map<String, List<String>>> getColumnsByValueWithID(boolean raw, String sql, String idCol)
		throws Exception, SQLException
	{
		return getColumnsByValueWithID(raw, sql, idCol, null);
	}

	/**
	 *	Returns a Map containing a single row of data from query - if the query returns more than<br />
	 *	one row the first will be taken and the remainder ignored.<br />
	 *	<br />
	 *	The map contains a key-value pair where the key is the column name and the value is <br />
	 *	the value for the row:
	 *	<ul>
	 *		<li>Map&lt;[COLUMN NAME], [VALUE]&gt;</li>
	 *	</ul>
	 *	This function is overloaded.<br />
	 *	<br />
	 *	The following parameters are required:
	 *	<ul>
	 *		<li>sql</li>
	 *	</ul>
	 *	The remaining parameters are all <b>optional</b>; all permuatations of parameters are acceprable.<br />
	 *	They take the following default values:
	 *	<ul>
	 *		<li>raw - FALSE</li>
	 *		<li>args - null</li>
	 *	</ul>
	 *	USAGE:<br /><code>
	 *	java.util.Map&lt;String, String&gt; args = new java.util.HashMap&lt;String, String&gt;();<br />
	 *	data.add("PARTNUMBER", "001AA000001");<br />
	 *	<br />
	 *	ext.site.Connect.getTableByRow(false,"part-query.sql", args);
	 *	</code>
	 *
	 *		@param raw the boolean as to whether the sql provided is raw sql or the path to a SQL file
	 *		@param sql a String containing raw SQL or the path to a SQL file
	 *		@param args a Map where the key is the String placeholder in the SQL and the value is that which it is to be substituted for
	 *		@return a map containing the table returned from a SQL query
	 */
	public Map<String, String> getRow(boolean raw, String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		run(raw, sql, args);
		Map<String, String> data = getDataRow();
		close();
		return data;
	}

	public Map<String, String> getRow(String sql)
		throws Exception, SQLException
	{
		return getRow(false, sql, null);
	}

	public Map<String, String> getRow(String sql, Map<String, String> args)
		throws Exception, SQLException
	{
		return getRow(false, sql, args);
	}

	public Map<String, String> getRow(boolean raw, String sql)
		throws Exception, SQLException
	{
		return getRow(raw, sql, null);
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
		} else {
			if (start != null) {
				path = start + file;
			} else {
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

		} catch (Throwable t) {
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
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException cnf) {
			throw new Exception(cnf);
		}

		if (sql == null) {
			throw new Exception("No sql provided!");
		}

		if (! raw) {
			getFile(sql);
		} else {
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
		System.out.println ("Connection Opened.");
		conn.setAutoCommit(false);
		stmt = conn.createStatement();

		try {
			System.out.println ("Executing Query...");
			System.out.println ("\n\n" + QUERY);
			System.out.println ("\n\n");
			rs = stmt.executeQuery(QUERY);

		} catch (SQLException ex) {
			close();
			throw new SQLException();
		}
	}

	/**
	 *	
	 */
	private Map<String, Map<String, String>> getDataTable(String id, boolean groupByColumn)
		throws SQLException
	{
		Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();

		if (groupByColumn) {
			data = getDataTableGroupByColumn(id);
		} else {
			data = getDataTableGroupByRow(id);
		}

		rs.close();

		return data;

	}

	/**
	 *	
	 */
	private Map<String, Map<String, String>> getDataTableGroupByColumn(String idCol)
		throws SQLException
	{
		Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();

		Map<String, String> col = null;
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}

		GetColumns:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			col = new HashMap<String, String>();

			rs.first();
			GetCells:
			while (rs.next()) {
				int count = 0;
				String id = null;

				if (idCol != null) {
					id = rs.getString(idCol);
				} else {
					id = Integer.toString(++count);
				}

				col.put(id, rs.getString(i));
			}

			data.put(rsmd.getColumnName(i), col);
		}

		rs.close();

		return data;
	}

	/**
	 *	
	 */
	private Map<String, Map<String, String>> getDataTableGroupByRow(String idCol)
		throws SQLException
	{
		Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();

		Map<String, String> row = null;
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}
		
		int count = 0;
		String id = null;

		GetRows:
		while (rs.next()) {
			if (idCol != null) {
				id = rs.getString(idCol);
			} else {
				id = Integer.toString(++count);
			}

			row = new HashMap<String, String>();
			GetCells:
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				row.put(rsmd.getColumnName(i), rs.getString(i));
			}

			data.put(id, row);
		}

		rs.close();

		return data;
	}

	/**
	 *
	 */
	private List<Map<String, String>> getDataTableByRow()
		throws SQLException
	{
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();

		Map<String, String> row = null;
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}

		GetRows:
		while (rs.next()) {
			row = new HashMap<String, String>();
			GetCells:
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				row.put(rsmd.getColumnName(i), rs.getString(i));
			}

			data.add(row);
		}

		rs.close();

		return data;
	}

	/**
	 *
	 */
	private Map<String, List<String>> getDataTableByColumn()
		throws SQLException
	{
		Map<String, List<String>> data = new HashMap<String, List<String>>();

		List<String> col = null;
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}

		GetColumns:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			col = new ArrayList<String>();

			rs.first();
			GetCells:
			while (rs.next()) {
				col.add(rs.getString(i));
			}

			data.put(rsmd.getColumnName(i), col);
		}

		rs.close();

		return data;
	}

	/**
	 *	
	 */
	private List<String> getDataColumn(String col)
		throws Exception, SQLException
	{
		List<String> data = new ArrayList<String>();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount() < 1) {
			throw new Exception("No column returned!");
		}

		if (col==null && rsmd.getColumnCount() > 1) {
			throw new Exception("More than one column in result set and no column specified!");
		}

		GetCells:
		while (rs.next()) {
			if (col != null) {
				data.add(rs.getString(col));
				continue GetCells;

			} else {
				data.add(rs.getString(1));
			}
		}

		rs.close();

		return data;
	}

	/**
	 *	
	 */
	private Map<String, String> getDataColumnWithID(String idCol, String col)
		throws Exception, SQLException
	{
		Map<String, String> data = new HashMap<String, String>();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount() == 0) {
			throw new Exception("No column returned!");
		}

		if (idCol == null) {
			throw new Exception("No ID Column provided!");
		}

		if (idCol.equals(col)) {
			throw new Exception("The ID column equals the required column...");
		}

		if (col == null && rsmd.getColumnCount() > 2) {
			throw new Exception("Required column was not specifed and there are more than two columns!");
		}

		int idColID = 0;
		int colID = 0;
		if (col == null) {
			idColID = getColumnID(idCol);

			if (idColID < 1) {
				throw new Exception("ID Column not found!");
			}

			--idColID;
			colID = 2 - idColID;
		}

		GetCells:
		while(rs.next()) {
			if (col == null) {
				data.put(rs.getString(idCol), rs.getString(colID));
			} else {
				data.put(rs.getString(idCol), rs.getString(col));
			}
		}

		rs.close();

		return data;
	}

	/**
	 *
	 */
	private Map<String, Integer> getDataColumnByValue(String col)
		throws Exception, SQLException
	{
		Map<String, Integer> data = new HashMap<String, Integer>();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}

		if (col==null && rsmd.getColumnCount() > 1) {
			throw new Exception("More than one column in result set and no column specified!");
		}

		GetCells:
		while (rs.next()) {
			String val = null;
			if (col != null) {
				val = rs.getString(col);
			} else {
				val = rs.getString(1);
			}

			if (! data.containsKey(val)) {
				data.put(val, 1);
				continue GetCells;
			}

			Integer count = data.get(val);
			count = new Integer(count.intValue() + 1);
			data.put(val, count);

		}

		rs.close();

		return data;
	}

	/**
	 *	
	 */
	private Map<String, List<String>> getDataColumnByValueWithID(String idCol)
		throws Exception, SQLException
	{
		return null;
	}

	/**
	 *	
	 */
	private Map<String, Map<String, Integer>> getDataColumnsByValue()
		throws Exception, SQLException
	{
		Map<String, Map<String, Integer>> data = new HashMap<String, Map<String, Integer>>();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;
		}

		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			data.put(rsmd.getColumnName(i), getDataColumnByValue(rsmd.getColumnName(i)));
		}

		rs.close();

		return data;
	}

	/**
	 *	
	 */
	private Map<String, Map<String, List<String>>> getDataColumnsByValueWithID(String idCol)
		throws Exception, SQLException
	{
		return null;
	}

	/**
	 *	Retrieves a single row of data as a Map - key-value pair.<br />
	 *	<br />
	 *	The key is the column name.<br />
	 *	The value is the value in the given column.
	 *
	 *		@return a map containing the data from a single row of the ResultSet
	 */
	private Map<String, String> getDataRow()
		throws SQLException
	{
		Map<String, String> data = new HashMap<String, String>();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return null;		// No columns...
		}

		rs.first();		// Select the first row only - others will be ignored!
		GetCells:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			data.put(rsmd.getColumnName(i), rs.getString(i));
		}

		rs.close();

		return data;
	}

	/**
	 *	Get the ID of a column argument.
	 *
	 *		@param col the name of the column to get ID for
	 *		@return the int ID of the column
	 */
	private int getColumnID(String col)
		throws SQLException
	{
		ResultSetMetaData rsmd = rs.getMetaData();

		FindColumn:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if (! rsmd.getColumnName(i).equals(col)) {
				return i;
			}
		}

		return 0;		// ResultSet columns are 1-based, so 0 is not possible.
	}

	/**
	 *	Return a boolean of whether the column argument is in the ResultSet.
	 *	
	 *		@param col the name of the column to check if it exists
	 *		@return boolean whether the column is in ResultSet
	 */
	private boolean isColumn(String col)
		throws SQLException
	{
		ResultSetMetaData rsmd = rs.getMetaData();

		FindColumn:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if (! rsmd.getColumnName(i).equals(col)) {
				return true;
			}
		}

		return false;
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

	protected String getWindchillProperty(String prop)
	{
		String val = null;
		try {
			val = WTProperties.getLocalProperties().getProperty(prop);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return val;
	}

}