package ext.site.sql;

import ext.site.sql.ConnectManager;
import wt.util.WTProperties;

public class OracleConnectManager extends ConnectManager {

	protected final String PRODUCTION_SERVER_HOST_NAME = "amdtcappsp08.actuant.pri";

	public OracleConnectManager()
	{
		super(null, null, null);
		setDefaultConnectionDetails();
		SERVER_HOSTNAME = getWindchillProperty(SERVER_HOST_PROPERTY);
		getConnectionDetails();		// Retrieve DB credentials - based on host name.
	}

	private void setDefaultConnectionDetails()
	{
		SERVER_HOSTNAME = "";
		URL = "jdbc:oracle:thin:@//amdtcerpdbt01.actuant.pri:1533/ATUTEST";
		USERNAME = "apps";
		PASSWORD = "we1c0me";
	}

	/**
	 *	Sets the Oracle database credentials based on the retrieved value of "SERVER_HOSTNAME".<br />
	 *	<br />
	 *	The default credentials are for ATUTEST.
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