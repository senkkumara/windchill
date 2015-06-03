package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.Manager;
import wt.change2.VersionableChangeItem;

/**
 *	Manages all custom code in Change Object workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class ChangeManager
		extends Manager
{
	private VersionableChangeItem vci;

	/**
	 *	Constructor
	 */
	@Override
	public ChangeManager(WTObject o)
	{
		this.vci = (VersionableChangeItem)o;
	}

	/**
	 *	Constructor
	 */
	public ChangeManager(VersionableChangeItem v)
	{
		this.vci = v;
	}

	/**
	 *	Versionable Change Item getter.
	 *
	 *		@return VerionableChangeItem
	 */
	public VersionableChangeItem getChangeItem()
	{
		return this.vci;
	}

	/**
	 *	Versionable Change Item setter.
	 *
	 *		@param v the Versionable Change Item to set.
	 *		@return ChangeManager
	 */
	public ChangeManager setChangeItem(VersionableChangeItem v)
	{
		this.vci = v;
		return this;
	}
}