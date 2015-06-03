package ext.hydratight.db;

import ext.hydratight.db.DataColumn;
import ext.hydratight.db.DataRow;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import java.sql.SQLException;

public class DataTable
{

	private ResultSet rset; 
	private List<DataRow> data;
	private List<String> cols;
	private String id;
	private int rowCount;
	private int colCount;

	////////////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS ////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public DataTable(ResultSet rs)
			throws SQLException
	{
		this.rset = rs;
		this.id = null;
		extract();
	}

	public DataTable(ResultSet rs, String i)
			throws SQLException
	{
		this.rset = rs;
		this.id = i;
		extract();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// GETTERS / SETTERS ///////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public DataTable setID(String i)
	{
		this.id = i;
		return this;
	}

	public String getID()
	{
		return this.id;
	}

	public List<DataRow> getData()
	{
		return this.data;
	}

	public List<String> getColumnNames()
	{
		return this.cols;
	}

	public int getColumnCount()
	{
		return this.colCount;
	}

	public int getRowCount()
	{
		return this.rowCount;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC FUNCTIONS ////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 *	Get row where value of ID column is the argument value "i".
	 */
	public DataRow getRow(String i)
	{
		//if (! cols.contains())
		return null;
	}

	/**
	 *	
	 */
	public DataRow getRow(String col, String i)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataTable getRows(String i)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataTable getRows(String col, String i)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataColumn getColumn(String col)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataTable getColumns(String[] cls)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataTable getColumnWithID(String dtCol, String idCol)
	{
		return null;
	}

	/**
	 *	
	 */
	public DataTable getColumnsWithID(String[] dtCol, String idCol)
	{
		return null;
	}

	/**
	 *	
	 */
	public boolean containsColumn(String col)
	{
		return cols.contains(col);
	}

	/**
	 *	
	 */
	public boolean containsValue(String val)
	{
		return false;
	}

	/**
	 *	
	 */
	public int countValue(String col, String val)
	{
		return 0;
	}

	/**
	 *	
	 */
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getByValue()
	{
		return null;
	}

	/**
	 *	
	 */
	public LinkedHashMap<String, Integer> getByValue(String col)
	{
		return null;
	}

	/**
	 *	
	 */
	public String toString()
	{
		return data.toString();
	}

	/**
	 *	
	 */
	public String toHTML()
	{
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE FUNCTIONS ///////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 *	Extracts data from ResultSet
	 */
	private void extract()
			throws SQLException
	{
		this.extractColumns();
		this.extractData();
		this.colCount = cols.size();
		this.rowCount = data.size();
		this.rset.close();
	}

	private void extractData()
			throws SQLException
	{
		DataRow row;
		ResultSetMetaData rsmd = this.rset.getMetaData();
		if (rsmd.getColumnCount()==0) {
			return;
		}

		this.data = new ArrayList<DataRow>();

		Row:
		while (this.rset.next()) {
			row = new DataRow();

			Cell:
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				row.put(rsmd.getColumnName(i), this.rset.getString(i));
			}

			this.data.add(row);
		}
	}

	private void extractColumns()
			throws SQLException
	{
		ResultSetMetaData rsmd = this.rset.getMetaData();
		this.cols = new ArrayList<String>();

		Get:
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			this.cols.add(rsmd.getColumnName(i));
		}
	}

}