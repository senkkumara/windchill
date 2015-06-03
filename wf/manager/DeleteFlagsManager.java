package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.Manager;
import wt.vc.baseline.ManagedBaseline;

/**
 *	Manages all custom code in the "Hydratight Misc. - Delete Flags" workflow.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class DeleteFlagManager
		extends Manager
{
	private ManagedBaseline bl;

	/**
	 *	Constructor
	 */
	@Override
	public DeleteFlagManager(WTObject o)
	{
		this.bl = (ManagedBaseline)o;
	}

	/**
	 *	Constructor
	 */
	public DeleteFlagManager(ManagedBaseline b)
	{
		this.bl = b;
	}

	/**
	 *	Managed Baseline getter.
	 *
	 *		@return ManagedBaseline
	 */
	public ManagedBaseline getBaseline()
	{
		return this.bl;
	}

	/**
	 *	Managed Baseline setter.
	 *
	 *		@param b the Managed Baseline object to set.
	 *		@return DeleteFlagManager
	 */
	public DeleteFlagManager setBaseline(ManagedBaseline b)
	{
		this.bl = b;
		return this;
	}

	/**
	 *	Delete the flags on the objects on the Baseline
	 */
	public void delete()
	{
		
	}
}