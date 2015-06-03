package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.ChangeManager;
import wt.change2.WTChangeRequest2;

/**
 *	Manages all custom code in Change Request workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class ChangeRequestManager
		extends ChangeManager
{
	private WTChangeRequest2 cr;

	/**
	 *	Constructor
	 */
	@Override
	public ChangeRequestManager(WTObject o)
	{
		this.cr = (WTChangeRequest2)o;
	}

	/**
	 *	Constructor
	 */
	public ChangeRequestManager(WTChangeRequest2 c)
	{
		this.cr = c;
	}

	/**
	 *	Change Request getter.
	 *
	 *		@return WTChangeRequest2
	 */
	public WTChangeRequest2 getChangeRequest()
	{
		return this.cr;
	}

	/**
	 *	Change Request setter.
	 *
	 *		@param c the Change Request object to set.
	 *		@return ChangeRequestManager
	 */
	public ChangeRequestManager setChangeRequest(WTChangeRequest2 c)
	{
		this.cr = c;
		return this;
	}
}