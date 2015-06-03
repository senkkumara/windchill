package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.ChangeManager;
import wt.change2.WTChangeActivity2;

/**
 *	Manages all custom code in Change Activity workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class ChangeActivityManager
		extends ChangeManager
{
	private WTChangeActivity2 ca;

	/**
	 *	Constructor
	 */
	@Override
	public ChangeActivityManager(WTObject o)
	{
		this ca = (WTChangeActivity2)o;
	}

	/**
	 *	Constructor
	 */
	public ChangeActivityManager(WTChangeActivity2 c)
	{
		this.ca = c;
	}

	/**
	 *	Change Activity getter.
	 *
	 *		@return WTChangeActivity2
	 */
	public WTChangeActivity2 getChangeActivity()
	{
		return this.ca;
	}

	/**
	 *	Change Activity setter.
	 *
	 *		@param c the Change Activity object to set.
	 *		@return ChangeActivityManager
	 */
	public ChangeActivityManager setChangeActivity(WTChangeActivity2 c)
	{
		this.ca = c;
		return this;
	}

	/**
	 *	Retrieve the description from the Change Activity.
	 *
	 *		@return String
	 */
	public String getDescription()
	{
		return ca.getDescription();
	}
}