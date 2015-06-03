package ext.site.sql;

import ext.site.sql.ConnectManager;

public class WindchillConnectManager extends ConnectManager {

	private final String DB_PORT_PROPERTY = "wt.pom.jdbc.port";
	private final String DB_SERVICE_PROPERTY = "wt.pom.jdbc.service";
	private final String DB_HOST_PROPERTY = "wt.pom.jdbc.host";
	private final String DB_USERNAME_PROPERTY = "wt.pom.dbUser";
	private final String DB_PASSWORD_PROPERTY = "wt.pom.dbPassword";

	public WindchillConnectManager()
	{
		super(null, null, null);
		setDefaultConnectionDetails();
	}

	private void setDefaultConnectionDetails()
	{
		String host = getWindchillProperty(DB_HOST_PROPERTY);
		String port = getWindchillProperty(DB_PORT_PROPERTY);
		String service = getWindchillProperty(DB_SERVICE_PROPERTY);
		String pwd = getWindchillProperty(DB_PASSWORD_PROPERTY);
		if (pwd.equals("ecrypted.wt.pom.dbPassword")) {
			pwd = "pdmadmin";
		}

		SERVER_HOSTNAME = getWindchillProperty(SERVER_HOST_PROPERTY);
		URL = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service.toUpperCase();
		USERNAME = getWindchillProperty(DB_USERNAME_PROPERTY);
		PASSWORD = pwd;
	}

}