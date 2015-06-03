package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.Manager;
import wt.maturity.PromotionNotice;

/**
 *	Manages all custom code in Promotion Request workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class PromotionManager
		extends Manager
{
	private PromotionNotice pn;

	/**
	 *	Constructor
	 */
	@Override
	public PromotionManager(WTObject o)
	{
		this.pn = (PromotionNotice)o;
	}

	/**
	 *	Constructor
	 */
	public PromotionManager(PromotionNotice p)
	{
		this.pn = p;
	}

	/**
	 *	Promotion Notice getter.
	 *
	 *		@return PromotionNotice
	 */
	public PromotionNotice getPromotionNotice()
	{
		return this.pn;
	}

	/**
	 *	Promotion Notice setter.
	 *
	 *		@param p the PromotionNotice object to set.
	 *		@return PromotionManager
	 */
	public PromotionManager setPromotionNotice(PromotionNotice p)
	{
		this.pn = p;
		return this;
	}


	public String getTargetState()
	{
		return null;
	} 

	public List<ObjectReference> getInvalidNumbers()
	{
		return null;
	}

	public List<ObjectReference> syncNames()
	{
		return null;
	}

	public boolean isAdministrator()
	{
		return false;
	}

	public String getApproverRoute()
	{
		return null;
	}

	public boolean isTeamValid()
	{
		return false;
	}

	public void resetTeam()
	{

	}

	public String getImplementationRoute()
	{
		return null;
	}

	public List<ObjectReference> incrementRevisions()
	{
		return null;
	}

	public void release()
	{

	}

	public boolean inactivate()
	{
		return false;
	}
}