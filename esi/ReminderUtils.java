package ext.hydratight.esi;

import ext.hydratight.GeneralUtils;
import ext.hydratight.obj.IdentityUtils;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import wt.change2.ChangeRecord2;
import wt.change2.ChangeNoticeComplexity;
import wt.change2.WTChangeActivity2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.org.WTOrganization;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.vc.VersionControlHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides methods that aid ESI reminders, including:
 *	<ol>
 *		<li>Lot Controlled Part</li>
 *		<li>Serial Controlled Parts</li>
 *		<li>Inactivate Parts</li>
 *		<li>Reactivate Parts</li>
 *	</ol>
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class ReminderUtils
{

	static final private String OBSOLETE_STATE = "OBSOLETE";
	static final private String LOT = "lot";
	static final private String SERIAL = "serial";
	
	/**
	 *	Returns a list of ObjectReferences for item(s) that will require some form of <br />
	 *	control when sent to Oracle.
	 *
	 *		@param qr the QueryResult containing candidate parts that may require control
	 *		@param p a String array containing the part number prefixes that require control
	 *		@throws WTException
	 *		@return List<ObjectReference>
	 */	
	private static List<ObjectReference> getControlledParts(QueryResult qr, String reg)
			throws WTException
	{
		List<ObjectReference> contr = new ArrayList<ObjectReference>();	
		Persistable per;
		
		Check:
		while (qr.hasMoreElements()) {
			per = (Persistable)qr.nextElement();
			
			if (per instanceof WTPart) {
				if (isControlled((WTPart)per, reg)) {
					contr.add(ObjectReference.newObjectReference(per));
				}
			}
		}
		
		return contr;
	}
	
	/**
	 *	Returns a boolean value of whether the Part requires control in Oracle.
	 *
	 *		@param part the WTPart
	 *		@param pxs a String List containing the part number prefixes that require control
	 *		@param sxs a String List containing the part number suffixes that require control
	 *		@return boolean
	 */
	private static boolean isControlled(WTPart part, String reg)
	{
		
		String number = part.getNumber();
		
		if (reg != null) {
			return GeneralUtils.regex(number, reg);
		}
		
		return false;
	}
	
	/**
	 *	Returns a list of ObjectReferences for item(s) that will require "Lot Control" <br />
	 *	when sent to Oracle.
	 *
	 *		@param qr the QueryResult containing candidate parts that may require Lot Control
	 *		@throws WTException
	 *		@return List<ObjectReference>
	 */	
	public static List<ObjectReference> getLotControlledParts(QueryResult qr)
			throws WTException
	{	
		return getControlledParts(qr, "^R"); 
	}
	
	/**
	 *	Returns a list of ObjectReferences for item(s) that will require "Serial Control" <br />
	 *	when sent to Oracle.
	 *
	 *		@param qr the QueryResult containing candidate parts that may require Serial Control
	 *		@throws WTException
	 *		@return List<ObjectReference>
	 */	
	public static List<ObjectReference> getSerialControlledParts(QueryResult qr)
			throws WTException
	{
		return getControlledParts(qr, "^H");
	}
	
	/**
	 *	Returns a list of ObjectReferences for item(s) that require inactivating in Oracle.
	 *
	 *		@param qr the QueryResult containing candidate parts that may require inactivating
	 *		@throws WTException
	 *		@return List<ObjectReference>
	 */
	public static List<ObjectReference> getInactivateParts(QueryResult qr)
			throws WTException
	{	
		List<ObjectReference> inactivate = new ArrayList<ObjectReference>();
		Persistable per;
		WTPart part;
		
		Find:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof WTPart) {	// Only Parts require inactivating
				inactivate.add(ObjectReference.newObjectReference(per));
			}
		}
		
		return inactivate;
	}
	
	/**
	 *	Returns a list of ObjectReferences for item(s) that require reactivating in Oracle.
	 *
	 *		@param qr the QueryResult containing candidate parts that may require reactivating
	 *		@throws WTException
	 *		@return List<ObjectReference>
	 */
	public static List<ObjectReference> getReactivateParts(QueryResult qr)
			throws WTException
	{	
		List<ObjectReference> reactivate = new ArrayList<ObjectReference>();
		Persistable per;
		String state;
		
		Find:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof WTPart) {
				state = ((LifeCycleManaged)per).getLifeCycleState().toString();
				if (state.equals(OBSOLETE_STATE)){	// Objects in the "Obsolete" state require reactivating
					reactivate.add(ObjectReference.newObjectReference(per));
				}
			}
		}
		
		return reactivate;
	}
	
}