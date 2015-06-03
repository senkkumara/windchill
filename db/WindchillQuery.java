package ext.hydratight.db;

import ext.hydratight.GeneralUtils;
import ext.hydratight.db.Query;

/**
 *	Allows the querying of the local Windchill database.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class WindchillQuery
		extends Query
{

	private final String DB_PORT_PROPERTY = "wt.pom.jdbc.port";
	private final String DB_SERVICE_PROPERTY = "wt.pom.jdbc.service";
	private final String DB_HOST_PROPERTY = "wt.pom.jdbc.host";
	private final String DB_USERNAME_PROPERTY = "wt.pom.dbUser";
	private final String DB_PASSWORD_PROPERTY = "wt.pom.dbPassword";

	/**
	 *	Constuctor.
	 */
	public WindchillQuery()
	{
		super(null, null, null);
		setDefaultConnectionDetails();	
	}

	/**
	 *	Set the default Oracle connection to those specified in db.properties.
	 */
	private void setDefaultConnectionDetails()
	{
		String host = GeneralUtils.getWindchillProperty(DB_HOST_PROPERTY);
		String port = GeneralUtils.getWindchillProperty(DB_PORT_PROPERTY);
		String service = GeneralUtils.getWindchillProperty(DB_SERVICE_PROPERTY);
		String pwd = GeneralUtils.getWindchillProperty(DB_PASSWORD_PROPERTY);
		if (pwd.equals("ecrypted.wt.pom.dbPassword")) {
			pwd = "pdmadmin";
		}

		SERVER_HOSTNAME = GeneralUtils.getWindchillProperty(SERVER_HOST_PROPERTY);
		URL = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service.toUpperCase();
		USERNAME = GeneralUtils.getWindchillProperty(DB_USERNAME_PROPERTY);
		PASSWORD = pwd;
	}

}