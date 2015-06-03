package ext.hydratight.org;

import wt.org.StandardOrganizationServicesManager;
import wt.org.WTGroup;
import wt.org.WTOrganization;

import wt.util.WTException;

/**
 *	This class provides help with Principals (Users and Groups) in Windchill.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class PrincipalUtils
{

	/**
	 *	Returns WTGroup based on organization and name parameters.
	 *
	 *		@param org the WTOrganization containing the group
	 *		@param name the name of the group
	 *		@return the required group if found, null if not
	 */
	public static WTGroup getGroup(WTOrganization org, String name)
		throws WTException
	{
		StandardOrganizationServicesManager sosm = new StandardOrganizationServicesManager();
		return sosm.getGroup(name, org);
	}

	public static WTUser getUser(WTOrganization org, String name)
		throws WTException
	{
		StandardOrganizationServicesManager sosm = new StandardOrganizationServicesManager();
		return sosm.getUser(name, org);
	}
	
}