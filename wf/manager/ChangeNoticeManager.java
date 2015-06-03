package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.ChangeManager;
import java.util.List;
import java.util.Map;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;

/**
 *	Manages all custom code in Change Notice workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class ChangeNoticeManager
		extends ChangeManager
{
	private WTChangeOrder2 cn;

	/**
	 *	Constructor
	 */
	@Override
	public ChangeNoticeManager(WTObject o)
	{
		this.cn = (WTChangeOrder2)o;
	}

	/**
	 *	Constructor
	 */
	public ChangeNoticeManager(WTChangeOrder2 c)
	{
		this.cn = c;
	}

	/**
	 *	Change Notice getter.
	 *
	 *		@return WTChangeOrder2
	 */
	public WTChangeOrder2 getChangeNotice()
	{
		return this.cn;
	}

	/**
	 *	Change Notice setter.
	 *
	 *		@param c the Change Notice object to set.
	 *		@return ChangeNoticeManager
	 */
	public ChangeNoticeManager setChangeNotice(WTChangeOrder2 c)
	{
		this.cn = c;
		return this;
	}

	/**
	 *	Validate the Change Notice.
	 *
	 *		@return Map
	 */
	public Map<String, List<ObjectReference>> validate()
	{
		return null;
	}

	/**
	 *	
	 */
	public boolean isValid()
	{
		boolean valid = false;
		Map<String, List<ObjectReference>> results = this.validate();
		if (results.size() == 0) {
			valid = true;
		}

		return valid;
	}

	public List<Objectreference> validateStates()
	{
		return null;
	}

	private boolean isRevised(Versioned v)
	{
		return false;
	}

	private boolean isStateValid()
	{
		return false;
	}

	public boolean reactivate()
	{
		return false;
	}

	public void setRequester()
	{

	}

	public List<ObjectReference> setDrawingAttributes()
	{
		return null;
	}

	public String getAuditorRoute()
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

	public boolean release()
	{
		return false;
	}
}