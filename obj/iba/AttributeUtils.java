package ext.hydratight.obj.iba;

import ext.hydratight.obj.iba.value.StringValueUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.maturity.PromotionNotice;
import wt.org.WTUser;
import wt.part.WTPart;

import wt.util.WTException;

/**
 *	This class provides help with retrieval and modification of IBAs in Windchill.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 9.1 M070.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class AttributeUtils
{

	static final private String REQUESTER_IBA = "Requester";
	static final private String APPROVER_IBA = "Approver";
	
	/**
	 *	Sets the Requester IBA of a Change Notice argument.
	 *
	 *		@param cn the Change Notice to have it's requestor set.
	 *		@throws WTException
	 */
	public static void setRequester(WTChangeOrder2 cn)
			throws WTException
	{
		StringValueUtils.setStringValue((WTObject)cn, REQUESTER_IBA, cn.getCreatorEMail());
	}
	
	/**
	 *	Sets the Requester IBA of a Change Notice argument from a Change Request argument.
	 *
	 *		@param cn the Change Notice to have it's requestor set
	 *		@param cr the Change Request the requester is derived from
	 *		@throws WTException
	 */
	public static void setRequester(WTChangeRequest2 cr, WTChangeOrder2 cn)
			throws WTException
	{
		StringValueUtils.setStringValue((WTObject)cn, REQUESTER_IBA, cr.getCreatorEMail());
	}
	
	/**
	 *	Sets the Approver IBA of a Change Notice argument.
	 *
	 *		@param cn the Change Request to have it's approver set
	 *		@param approver the WTUser whom approved the notice
	 *		@throws WTException
	 */
	public static void setApprover(WTChangeOrder2 cn, WTUser approver)
			throws WTException
	{
		StringValueUtils.setStringValue((WTObject)cn, APPROVER_IBA, approver.getEMail());
	}
	
	/**
	 *	Sets the Approver IBA of a Change Request argument.
	 *
	 *		@param cn the Change Request to have it's approver set
	 *		@param approver the WTUser whom approved the request
	 *		@throws WTException
	 */
	public static void setApprover(WTChangeRequest2 cr, WTUser approver)
			throws WTException
	{
		StringValueUtils.setStringValue((WTObject)cr, APPROVER_IBA, approver.getEMail());
	}

}