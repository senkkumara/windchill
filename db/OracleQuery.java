package ext.hydratight.db;

import ext.hydratight.GeneralUtils;
import ext.hydratight.db.Query;

/**
 *	Allows the querying of the Oracle database attached to a given Windchill test instance.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class OracleQuery
		extends Query
{

	protected final String PRODUCTION_SERVER_HOST_NAME = "amdtcappsp08.actuant.pri";

	/**
	 *	Constuctor.
	 */
	public OracleQuery()
	{
		super(null, null, null);
		setDefaultConnectionDetails();
		SERVER_HOSTNAME = GeneralUtils.getWindchillProperty(SERVER_HOST_PROPERTY);
		getConnectionDetails();		// Retrieve DB credentials - based on host name.
	}

	/**
	 *	Set the default Oracle connection to ATUTEST.
	 */
	private void setDefaultConnectionDetails()
	{
		SERVER_HOSTNAME = "";
		URL = "jdbc:oracle:thin:@//amdtcerpdbt01.actuant.pri:1533/ATUTEST";
		USERNAME = "apps";
		PASSWORD = "we1c0me";
	}

	/**
	 *	Sets the Oracle database credentials based on the retrieved value of "SERVER_HOSTNAME".
	 */
	private void getConnectionDetails()
	{
		if (SERVER_HOSTNAME.equals(PRODUCTION_SERVER_HOST_NAME)) {
			URL = "jdbc:oracle:thin:@//amdtcerpdbp01.actuant.pri:1527/ATUPROD";
			USERNAME = "appsro";
			PASSWORD = "we1c0me09";
		}

		System.out.println("\n\nORACLE: " + URL + "\n\n");
	}

}