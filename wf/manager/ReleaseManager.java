package ext.hydratight.wf.manager;

import ext.hydratight.wf.manager.Manager;
import wt.change2.WTChangeOrder2;

/**
 *	Manages all custom code in the "Hydratight Misc. - ESI Release" workflow.
 *
 *		@author Toby Pettit
 *		@version 1.0.0
 */
public class ReleaseManager
		extends Manager
{
	private WTChangeOrder2 cn;
	private int code;
	private String releaseMsg;
	private String preReleaseMsg[];

	/**
	 *	Constructor
	 */
	@Override
	public ReleaseManager(WTObject o)
	{
		this.cn = (WTChangeOrder2)o;
	}

	/**
	 *	Constructor
	 */
	public ReleaseManager(WTChangeOrder2 c)
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

	public ReleaseManager validate()
	{
		return this;
	}

	public ReleaseManager release()
	{
		return this;
	}

	public ReleaseManager postRelease()
	{
		return this;
	}

	public int getCode()
	{
		return this.code;
	}

	public String getReleaseMessage()
	{
		return this.releaseMsg;
	}

	public String[] getPreReleaseMessages()
	{
		return this.preReleaseMsg;
	}
}