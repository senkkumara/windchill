package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.List;
import wt.epm.EPMDocument;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeIssue;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.maturity.PromotionNotice;
import wt.part.WTPart;
import wt.util.WTProperties;
import wt.vc.wip.Workable;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.work.WfAssignedActivity;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides help with Workable (those that can be checked out / in) objects in Windchill:
 *	<ol>
 *	<li>Check Out</li>
 *	<li>Check In</li>
 *	<li>Undo Check Out </li>
 *	</ol>
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class WorkableUtils
{
	
	private static final String CHECK_IN_COMMENTS = "Checked in programmatically after changes.";
	
	/**
	*	Returns a Boolean depending on whether the WTObject parameter is workable and can
	*	therefore be<br />
	*	checked out / in.
	*	
	*		@param obj the WTObject to be verified.
	*		@return boolean whether object can be checked out.
	*		@throw wt.util.WTException
	*/
	public static Boolean isWorkable(WTObject obj)
			throws WTException
	{
		return (obj instanceof Workable);
	}
	
	public static boolean isCheckedOut(WTObject obj)
			throws WTException
	{
		boolean checkedOut = false;
		Workable w;

		if (! isWorkable(obj)) {
			throw new WTException(new StringBuilder("Object ")
				.append(obj.toString())
				.append(" is not workable!")
				.toString());
		}
			
		w = (Workable)obj;
		if (WorkInProgressHelper.isCheckedOut(w) || WorkInProgressHelper.isWorkingCopy(w)) {
			checkedOut = true;
			return checkedOut;
		}
		
		return checkedOut;
	}
	
	public static List<ObjectReference> isCheckedOut(QueryResult qr)
			throws WTException
	{
		List<ObjectReference> out = new ArrayList<ObjectReference>();
		WTObject obj;
		Workable w;
		
		Check:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			if (! isWorkable(obj)) {
				continue Check;
			}
			
			w = (Workable)obj;
			if (WorkInProgressHelper.isCheckedOut(w) || WorkInProgressHelper.isWorkingCopy(w)) {
				out.add(ObjectReference.newObjectReference((Persistable)obj));
			}

		}
		
		return out;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////// CHECK OUT /////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
	
	public static Workable checkOut(Workable w)
			throws WTException
	{
		Folder folder;
	
		if (WorkInProgressHelper.isCheckedOut(w)) {		// Verify is checked in.
			throw new WTException("Object is already checked out!");
		}
		
		try {
			// Get the check out folder
			folder = WorkInProgressHelper.service.getCheckoutFolder();
			
			// Check out object
			w = WorkInProgressHelper.service.checkout(w, folder, "").getWorkingCopy();	
						
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		return w;
	}
	
	/**
	 *	Returns Workable working copy of checked out WTObject parameter.
	 *
	 *		@param obj the WTObject to be checked out - must be workable!
	 *		@return a working copy of WTObject.
	 *		@exception Exception
	 */
	public static Workable checkOut(WTObject obj)
			throws WTException
	{
		if (! isWorkable(obj)) {
			throw new WTException("Object cannot be checked out!");
		}
		
		return checkOut((Workable)obj);
	}
	
	public static Workable checkOut(WTPart part)
			throws WTException
	{
		return checkOut((Workable)part);
	}
	
	public static Workable checkOut(EPMDocument epm)
			throws WTException
	{
		return checkOut((Workable)epm);
	}
	
	public static Workable checkOut(WTDocument doc)
			throws WTException
	{
		return checkOut((Workable)doc);
	}

/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////// CHECK IN //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////// Workable //////////////////////////////////////////
	
	public static Workable checkIn(Workable w, String comments, boolean undo)
			throws WTException
	{
		if (! WorkInProgressHelper.isCheckedOut(w)) {		// Verify is checked out.
			throw new WTException("Object is not checked out!");
		}
		
		try {
			w = WorkInProgressHelper.service.checkin(w, comments);		// Check in object.
			
		}
		catch (WTPropertyVetoException pve) {
			if (! undo) {
				throw new WTException(pve);
			}
			
			if (undo) {		// Undo check out if error occurred and specified.
				w = undoCheckOut(w);
			}
		}
		
		return w;
	}
	
	public static Workable checkIn(Workable w)
			throws WTException
	{
		return checkIn(w, CHECK_IN_COMMENTS, false);
	}
	
	public static Workable checkIn(Workable w, String comments)
			throws WTException
	{		
		return checkIn(w, comments, false);
	}
	
	public static Workable checkIn(Workable w, boolean undo)
			throws WTException
	{
		return checkIn(w, CHECK_IN_COMMENTS, undo);
	}
	
///////////////////////////////////////// WTObject //////////////////////////////////////////
	
	public static Workable checkIn(WTObject obj, String comments, boolean undo)
			throws WTException
	{
		if (! isWorkable(obj)) {
			throw new WTException("Object cannot be checked out!");
		}
		
		return checkIn((Workable)obj, comments, undo);
	}
	
	/**
	 *	Returns Workable checked in parameter WTObject.
	 *
	 *		@param obj the WTObject to be checked in - must be workable!
	 *		@return a checked in copy of the WTObject.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTObject obj)
			throws WTException
	{	
		return checkIn(obj, CHECK_IN_COMMENTS, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTObject.
	 *
	 *		@param obj the WTObject to be checked in - must be workable!
	 *		@param comments the Comments to be added to the Object's iteration history.
	 *		@return a checked in copy of the WTObject.
	 *		@exception Exception
	 */
	public static Workable checkIn(WTObject obj, String comments) 
			throws WTException 
	{
		return checkIn(obj, comments, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTObject.
	 *
	 *		@param obj the WTObject to be checked in - must be workable!
	 *		@return a checked in copy of the WTObject.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTObject obj, boolean undo)
			throws WTException
	{
		Workable w = null;
		
		try {
			w = checkIn(obj, CHECK_IN_COMMENTS);
			
		} catch (WTException wte) {
			if (undo) {		// Undo check out if error occurred and specified.
				w = undoCheckOut(obj);
			}
		}
		
		return w;
	}
	
////////////////////////////////////////// WTPart ///////////////////////////////////////////
	
	public static Workable checkIn(WTPart part, String comments, boolean undo)
			throws WTException
	{
		return checkIn((Workable)part, comments, undo);
	}
	
	/**
	 *	Returns Workable checked in parameter WTPart.
	 *
	 *		@param part the WTPart to be checked in.
	 *		@return a checked in copy of the WTPart.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTPart part)
			throws WTException
	{	
		return checkIn(part, CHECK_IN_COMMENTS, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTPart.
	 *
	 *		@param part the WTPart to be checked in.
	 *		@param comments the Comments to be added to the Part's iteration history.
	 *		@return a checked in copy of the WTPart.
	 *		@exception Exception
	 */
	public static Workable checkIn(WTPart part, String comments) 
			throws WTException 
	{
		return checkIn(part, comments, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTPart.
	 *
	 *		@param part the WTPart to be checked in.
	 *		@return a checked in copy of the WTPart.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTPart part, boolean undo)
			throws WTException
	{
		return checkIn(part, CHECK_IN_COMMENTS, undo);
	}
	
	public static Workable checkIn(EPMDocument epm, String comments, boolean undo)
			throws WTException
	{
		return checkIn((Workable)epm, comments, undo);
	}
	
/////////////////////////////////////// EPM Document ////////////////////////////////////////
	
	/**
	 *	Returns Workable checked in parameter EPMDocument.
	 *
	 *		@param epm the EPMDocument to be checked in.
	 *		@return a checked in copy of the EPMDocument.
	 *		@exception WTException
	 */
	public static Workable checkIn(EPMDocument epm)
			throws WTException
	{	
		return checkIn(epm, CHECK_IN_COMMENTS, false);
	}
	
	/**
	 *	Returns Workable checked in parameter EPMDocument.
	 *
	 *		@param epm the EPMDocument to be checked in.
	 *		@param comments the Comments to be added to the EPMDocument's iteration history.
	 *		@return a checked in copy of the EPMDocument.
	 *		@exception Exception
	 */
	public static Workable checkIn(EPMDocument epm, String comments) 
			throws WTException 
	{
		return checkIn(epm, comments, false);
	}
	
	/**
	 *	Returns Workable checked in parameter EPMDocument.
	 *
	 *		@param epm the EPMDocument to be checked in.
	 *		@return a checked in copy of the EPMDocument.
	 *		@exception WTException
	 */
	public static Workable checkIn(EPMDocument epm, boolean undo)
			throws WTException
	{
		return checkIn(epm, CHECK_IN_COMMENTS, undo);
	}
	
	public static Workable checkIn(WTDocument doc, String comments, boolean undo)
			throws WTException
	{
		return checkIn((Workable)doc, comments, undo);
	}
	
//////////////////////////////////////// WTDocument /////////////////////////////////////////
	
	/**
	 *	Returns Workable checked in parameter WTDocument.
	 *
	 *		@param doc the WTDocument to be checked in.
	 *		@return a checked in copy of the WTDocument.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTDocument doc)
			throws WTException
	{	
		return checkIn(doc, CHECK_IN_COMMENTS, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTDocument.
	 *
	 *		@param doc the WTDocument to be checked in.
	 *		@param comments the Comments to be added to the WTDocument's iteration history.
	 *		@return a checked in copy of the WTDocument.
	 *		@exception Exception
	 */
	public static Workable checkIn(WTDocument doc, String comments) 
			throws WTException 
	{
		return checkIn(doc, comments, false);
	}
	
	/**
	 *	Returns Workable checked in parameter WTDocument.
	 *
	 *		@param doc the WTDocument to be checked in.
	 *		@return a checked in copy of the WTDocument.
	 *		@exception WTException
	 */
	public static Workable checkIn(WTDocument doc, boolean undo)
			throws WTException
	{
		return checkIn(doc, CHECK_IN_COMMENTS, undo);
	}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// UNDO CHECKOUT ///////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 *	Returns Workable parameter WTObject once its check out has been undone.
	 *
	 *		@param obj the WTObject that needs to have it's check out undone.
	 *		@return a Workable checked in copy of the WTObject.
	 *		@exception Exception
	 */
	public static Workable undoCheckOut(Workable w)
			throws WTException
	{	
		if (! WorkInProgressHelper.isCheckedOut(w)) {		// Verify is checked out.
			throw new WTException("Object is not checked out!");
		}
		
		try {			
			w = WorkInProgressHelper.service.undoCheckout(w);
					
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		return w;
	}
	
	/**
	 *	Returns Workable parameter WTObject once its check out has been undone.
	 *
	 *		@param obj the WTObject that needs to have it's check out undone.
	 *		@return a Workable checked in copy of the WTObject.
	 *		@exception Exception
	 */
	public static Workable undoCheckOut(WTObject obj)
			throws WTException
	{
		Workable w;

		if (! isWorkable(obj)) {
			throw new WTException("Object cannot be checked out!");
		}
		
		w = (Workable)obj;
		if (WorkInProgressHelper.isCheckedOut(w)) {		// Verify is checked out.
			throw new WTException("Object is not checked out!");
		}
		
		try {
			w = WorkInProgressHelper.service.undoCheckout(w);
						
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		return w;
		
	}
	
	public static Workable undoCheckOut(WTPart part)
			throws WTException
	{
		return undoCheckOut((Workable)part);
	}
	
	public static Workable undoCheckOut(EPMDocument epm)
			throws WTException
	{
		return undoCheckOut((Workable)epm);
	}
	
	public static Workable undoCheckOut(WTDocument doc)
			throws WTException
	{
		return undoCheckOut((Workable)doc);
	}
}