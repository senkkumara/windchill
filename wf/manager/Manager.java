package ext.hydratight.wf.manager;

/**
 *	Manages all custom code in all workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class manager
{
	private WTObject obj;
	static final protected String DESIGN_STATE = "DESIGN";
	static final protected String PRE_PRODUCTION_STATE = "PREPRODUCTION";
	static final protected String PRE_PRODUCTION_CHANGE_STATE = "PREPRODUCTIONCHANGE";
	static final protected String PRODUCTION_STATE = "PRODUCTION";
	static final protected String PRODUCTION_CHANGE_STATE = "PRODUCTIONCHANGE";
	static final protected String UNDER_REVIEW_STATE = "UNDERREVIEW";
	static final protected String REWORK_STATE = "REWORK";
	static final protected String OBSOLETE_STATE = "OBSOLETE";

	/**
	 *	Constructor
	 */
	public Manager(WTObject o)
	{
		this.obj = o;
	}
}