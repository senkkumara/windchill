package ext.hydratight.obj.iba.value;

import ext.hydratight.obj.iba.value.ValueUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.service.IBAValueHelper;
import wt.part.WTPart;

import java.rmi.RemoteException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class StringValueUtils
{
//*******************************************************************************************
/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// NO CHECK OUT ////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
//*******************************************************************************************

//////////////////////////////////////////// GET ////////////////////////////////////////////
	
	/**
	 *	Returns the String value of an attribute from a IBAHolder parameter
	 *
	 *		@param hdr the IBAHolder containing the attribute
	 *		@param attr the name of the IBA
	 *		@throws WTException
	 *		@return String
	 */
	public static String getStringValue(IBAHolder hdr, String attr)
			throws WTException
	{
		String value = null;
		DefaultAttributeContainer cont;
		AttributeDefDefaultView defView;
		AbstractValueView[] absView;

		try {
			cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
			if (PersistenceHelper.isPersistent(hdr)) {
				hdr = IBAValueHelper.service.refreshAttributeContainer(hdr, null, null, null);
				cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
			}
			
			if (cont == null) {
				return null;
			}
			
			defView = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attr);
			absView = cont.getAttributeValues(defView);
			
			if (absView.length != 0) {
				if (!( absView[0] instanceof StringValueDefaultView)) {
					throw new WTException(new StringBuilder("Attribute ")
						.append(attr)
						.append(" is not of type String")
						.toString());
				}

				return ((StringValueDefaultView) absView[0]).getValue();
			}
		
		}
		catch (Exception e) {
			throw new WTException(e);
		}
		
		return value;
	}
	
	/**
	 *	Returns the String value of an attribute from a WTObject parameter
	 *
	 *		@param hdr the WTObject containing the attribute
	 *		@param attr the name of the IBA
	 *		@throws WTException
	 *		@return String
	 */
	public static String getStringValue(WTObject obj, String attr)
			throws WTException
	{
		if (! (obj instanceof IBAHolder)) {
			throw new WTException(new StringBuilder(obj.toString())
				.append(" cannot hold attributes ...")
				.toString());
		}

		return getStringValue((IBAHolder)obj, attr);
	}
	
	/**
	 *		Returns a Map containing the attribute name and values for a parameter WTObject.
	 *
	 *			@param obj the WTObject to retrieve the attribute values from
	 *			@param attrs a List contianing the attribute names to retrieve
	 *			@throws WTException
	 *			@return Map<String, String>
	 */
	public static Map<String, String> getStringValues(WTObject obj, List<String> attrs)
			throws WTException
	{
		Map<String, String> values = new HashMap<String, String>();
		String value;
		
		Get:
		for (String attr : attrs) {
			value = getStringValue(obj, attr);
			values.put(attr, value);
		}
		
		return values;
	}
	
	public static Map<WTObject, String> getStringValues(List<WTObject> objs, String attr)
			throws WTException
	{
		Map<WTObject, String> values = new HashMap<WTObject, String>();
		String value;
		
		Get:
		for (WTObject obj : objs) {
			value = getStringValue(obj, attr);
			values.put(obj, value);
		}
		
		return values;
	}
	
	public static Map<WTObject, Map<String, String>> getStringValues(Map<WTObject,
				List<String>> objs)
				
			throws WTException
	{
		Map<WTObject, Map<String, String>> values = new HashMap<WTObject,
				Map<String, String>>();

		List<String> attrs;
		Map<String, String> table;
		
		Get:
		for (WTObject obj : objs.keySet()) {
			attrs = objs.get(obj);
			table = getStringValues(obj, attrs);
			values.put(obj, table);

		}
		
		return values;
	}
	
	public static String getStringValue(WTPart part, String attr)
			throws WTException
	{
		return getStringValue((WTObject)part, attr);
	}
	
	public static Map<String, String> getStringValues(WTPart part, List<String> attrs)
			throws WTException
	{
		Map<String, String> values = new HashMap<String, String>();
		String value;
		
		Get:
		for (String attr : attrs) {
			value = getStringValue((WTObject)part, attr);
			values.put(attr, value);
		}
		
		return values;
	}
	
	public static Map<String, List<WTObject>> getStringValues(String attr, List<WTObject> objs)
			throws WTException
	{
		Map<String, List<WTObject>> values = new HashMap<String, List<WTObject>>();
		String value;
		List<WTObject> list;
		
		Get:
		for (WTObject obj : objs) {
			value = getStringValue(obj, attr);
			
			if (! values.containsKey(value)) {
				values.put(value, new ArrayList<WTObject>());
			}
			list = values.get(value);
			list.add(obj);
		}
		
		return values;
	}
	
	public static Map<String, List<WTObject>> getObjectsByStringValues(String attr, List<WTObject> objs)
			throws WTException
	{
		return getStringValues(attr, objs);
	}
	
	
//////////////////////////////////////////// SET ////////////////////////////////////////////
	
	private static DefaultAttributeContainer updateContainerStringValue(IBAHolder hdr,
				String attr, String value, DefaultAttributeContainer cont)
				
			throws WTException
	{
		AttributeDefDefaultView defView;
		StringDefView strView;
		AbstractValueView[] absView;
		StringValueDefaultView newStrView;

		try {
			defView = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attr);
			
			if (!(defView instanceof StringDefView)) {
				throw new WTException(new StringBuilder("IBA ")
					.append(attr)
					.append(" is not of type String!")
					.toString());
			}
			
			strView = (StringDefView)defView;
			absView = cont.getAttributeValues(strView);
		
			if (absView.length == 0) {
				newStrView = new StringValueDefaultView(strView, value);
				cont.addAttributeValue(newStrView);
			}
			else {
				newStrView = (StringValueDefaultView)absView[0];
				newStrView.setValue(value);
				cont.updateAttributeValue(newStrView); 
			}
			
		}
		catch (RemoteException | WTPropertyVetoException ex) {
			throw new WTException(ex);
		}
		
		return cont;
	}
	
	public static IBAHolder setStringValue(IBAHolder hdr, String attr, String value)
			throws WTException
	{
		DefaultAttributeContainer cont = ValueUtils.getContainer(hdr);
		updateContainerStringValue(hdr, attr, value, cont);			
		hdr.setAttributeContainer(cont);
		ValueUtils.updateDB(hdr);
		
		return hdr;
	}
	
	public static void setStringValue(WTObject obj, String attr, String value)
			throws WTException
	{
		setStringValue((IBAHolder)obj, attr, value);
	}
	
	public static void setStringValues(WTObject obj, Map<String, String> attrs)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();
		IBAHolder hdr = (IBAHolder)obj;
		DefaultAttributeContainer cont = ValueUtils.getContainer(hdr);
		String value;
		
		Set:
		for (String attr : attrs.keySet()) {
			value = attrs.get(attr);
			try {
				cont = updateContainerStringValue(hdr, attr, value, cont);
			}
			catch (WTException ex) {
				e.add(ex);
				continue Set;
			}
		}
		hdr.setAttributeContainer(cont);
		ValueUtils.updateDB(hdr);
		
		if (e.size() > 0) {
			throw new WTException(new StringBuilder(e.size())
				.append(" attributes failed to be set on ")
				.append(obj)
				.toString());
		}
	}
	
	public static void setStringValues(String attr, String value, List<WTObject> objs)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();
		IBAHolder hdr;
		
		Set:
		for (WTObject obj : objs) {
			hdr = (IBAHolder)obj;
			try {
				setStringValue(hdr, attr, value);
				
			}
			catch (WTException ex) {
				e.add(ex);
				continue Set;
			}

		}
		
		if (e.size() > 0) {
			throw new WTException(new StringBuilder(e.size())
				.append(" attributes failed to be set")
				.toString());
		}
	}
	
	public static void setStringValues(Map<String, String> attrs, List<WTObject> objs)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();
		
		Set:
		for (WTObject obj : objs) {
			try {
				setStringValues(obj, attrs);
				
			}
			catch (WTException ex) {
				e.add(ex);
				continue Set;
			}
		}
		
		if (e.size()>0) {
			throw new WTException(new StringBuilder(e.size())
				.append(" attributes failed to be set")
				.toString());
		}
	}
	
	public static void setStringValues(Map<WTObject, Map<String, String>> map)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();
		WTObject obj;
		Map<String, String> attrs;
		
		Set:
		for (Map.Entry<WTObject, Map<String, String>> entry : map.entrySet()) {
			obj = entry.getKey();
			attrs = entry.getValue();
			
			try {
				setStringValues(obj, attrs);
				
			}
			catch (WTException ex) {
				e.add(ex);
				continue Set;
			}			

		}
		
		if (e.size() > 0) {
			throw new WTException(new StringBuilder("Failed to set values on ")
				.append(e.size())
				.append(" objects.")
				.toString());
		}
	}
	
	public static void setStringValue(WTPart part, String attr, String value)
			throws WTException
	{
		setStringValue((IBAHolder)part, attr, value);
	}
	
	public static void setStringValues(WTPart part, Map<String, String> attrs)
			throws WTException
	{
		setStringValues((WTObject)part, attrs);
	}
	
//*******************************************************************************************
/////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// CHECK OUT //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
//*******************************************************************************************

	/**
	 *	This method sets the value of an IBA parameter on an parameter Object.
	 *
	 *		@param obj the Object containing the IBA.
	 *		@param IBA a String containing the name of the IBA.
	 *		@param value a String containing the value of the IBA to be set.
	 *		@return boolean
	 */
/*	public static Boolean setValue(WTObject obj, String name, String value)
	{

		Boolean complete = false;
		System.out.println(SIGNATURE + " >> Setting " + name + " on " + obj + " to " + value);
		
		IBAHolder holder = (IBAHolder)obj;
		
		// Check the new value differs from the old value.
		String currentValue = getValue(obj, name);
		if (currentValue.equals(value)) {
			// No need to change value!
			System.out.println(SIGNATURE + " >> Value of " + name + " on " + obj +
					" is already set as " + value + "!");
					
			return true;
		}
		
		// Does Object require checking out / in to make changes?
		boolean workable = WorkableUtils.isWorkable(obj);
		
		////////////////////////////////// WORKABLE OBJECTS ONLY //////////////////////////////////
		// Workable Objects must be checked out to make IBA value changes.						 //
		if (workable) {
			Workable w = null;
			try {
				w = WorkableUtils.checkOut(obj);
			} catch (Exception e) {
				return false;
			}
			holder = (IBAHolder)w;
			boolean isCheckedOut = WorkableUtils.isWorkingCopy(w);		// Verify is working copy
			if (! isCheckedOut && holder == null) {
				// Object not checked out!
				System.out.println("SET VALUES >> Failed to check out " + obj + "!");
				return complete;		// Failed to check object out.
			}
		}
		//																						 //
		///////////////////////////////////////////////////////////////////////////////////////////
		
		// Retrieve attribute
		AttributeDefDefaultView iba = getAttribute(holder, name, true);
		if (iba == null) {		// Failed to retrieve attribute
			if (workable) {
				// Undo check out on failed object
				Workable w = WorkableUtils.undoCheckOut(obj);
			}
			return complete;
		}
		DefaultAttributeContainer container =(DefaultAttributeContainer)holder.getAttributeContainer();
		
		//Set up new attribute value
		AbstractValueView newValue = createNewValue(iba, value);
		if (newValue == null) {
			// No new value, "Undo Check Out" object - only applies to workable object.
			if (workable) {
				Workable w = WorkableUtils.undoCheckOut(obj);		// Undo check out of object.
			}
			System.out.println(SIGNATURE + " >> No new value available to set!");
			return complete;
		}
		
		// Apply new attribute value
		try {
			container.deleteAttributeValues(iba);		// Remove old value(s) of attribute.
			container.addAttributeValue(newValue);		// Add new value of attribute.
			
		} catch (IBAConstraintException e) {
			if (workable) {
				// Undo check out on failed object
				Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
			}
			//
					
			return complete;
		}
		
		// Persist changes
		try {
			holder = (IBAHolder)PersistenceHelper.manager.save((Persistable)holder);
			
		} catch (WTException e) {
			if (workable) {
				// Undo check out on failed object
				Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
			}
			//
					
			return complete;
		}

		////////////////////////////////// WORKABLE OBJECTS ONLY //////////////////////////////////
		// Workable Objects once changed must be checked in.									 //
		if (workable) {
			// "Check In" modified object - only applies to workable object.
			Workable w = null;
			try {
				w = WorkableUtils.checkIn(obj, "Changed IBA " + name + " to " + value, true);
				complete = true;
				
			} catch (Exception e) {
				WorkableUtils.undoCheckOut(obj);
				return false;
			}
			boolean checkedOut = WorkableUtils.isCheckedOut(w);
			if (checkedOut) {
				System.out.println(SIGNATURE + " >> Blast! Failed to check in or undo check out " + obj + "!");
				return false;		// Failed to check in & undo check out object.
			}
		
		} else {
			complete = true;
			
		} 
		//																						 //
		///////////////////////////////////////////////////////////////////////////////////////////
	
		return complete;
	}*/
	
	/**
	 *	This method sets the values of multiple IBA on an argument Object.
	 *
	 *		@param obj the Object containing the IBAs.
	 *		@param values a Hashtable of IBA names and values to be set.
	 *		@return boolean
	 */
/*	public static Boolean setValues(WTObject obj, Hashtable<String, String> values)
	{

		Boolean complete = true;
		
		System.out.println(SIGNATURE + " >> Setting " + values.size() + " IBA values on " + obj);
		
		IBAHolder holder = (IBAHolder)obj;
		boolean workable = WorkableUtils.isWorkable(obj);		// Does Object require checking out / in
																// to make changes?
		
		////////////////////////////////// WORKABLE OBJECTS ONLY //////////////////////////////////
		// Workable Objects must be checked out to make IBA value changes.						 //
		if (workable) {
			Workable w = null;
			try {
				w = WorkableUtils.checkOut(obj);		// "Check Out" object
				
			} catch (Exception e) {
				boolean isCheckedOut = WorkableUtils.isCheckedOut((Workable)obj);
				if (isCheckedOut) {
					w = WorkableUtils.undoCheckOut(obj);
				}
				return complete;
			}
			holder = (IBAHolder)w;
			boolean isCheckedOut = WorkableUtils.isWorkingCopy(w);
			if (! isCheckedOut && holder == null) {
				System.out.println("SET VALUES >> Failed to check out " + obj + "!");
				return complete;		// Failed to check object out.
			}
		}
		//																						 //
		///////////////////////////////////////////////////////////////////////////////////////////
		
		// Refresh the object's attribute container.
		holder = refreshAttributeContainer(holder);
		if (holder == null) {
			if (workable) {
				Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
			}
			return complete;
		}
		
		// Loop through each IBA / value pair and set it.
		SetValues:
		for (String IBA : values.keySet()) {
			String value = values.get(IBA);
			DefaultAttributeContainer container = (DefaultAttributeContainer)holder.	
					getAttributeContainer();
			
			// Get the Object's attribute
			AttributeDefDefaultView iba = getAttribute(holder, IBA);
			if (iba == null) {
				if (workable) {
					Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
				}
				return complete;
			}
			
			//Set up new attribute value
			AbstractValueView newValue = createNewValue(iba, value);
			if (newValue == null) {
				// No new value, "Undo Check Out" object - only applies to workable object.
				if (workable) {
					Workable w = WorkableUtils.undoCheckOut(obj);		// Undo check out of object.
				}
				System.out.println(SIGNATURE + " >> No new value available to set!");
				return complete;
			}
			
			// Apply new attribute value
			try {
				container.deleteAttributeValues(iba);		// Remove existing value(s).
				System.out.println("SET DRAWING ATTRIBUTE >> Old " + IBA + " Value Removed!");
				container.addAttributeValue(newValue);		// Add new value.
				System.out.println("SET DRAWING ATTRIBUTE >> New " + IBA + " Value Added!");
				
			} catch (IBAConstraintException e) {
				if (workable) {
					Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
				}
				//
				
				return complete;
			}
			
			try {
				// Save changes - but not Check In.
				holder = (IBAHolder)PersistenceHelper.manager.save((Persistable)holder);
				
			} catch (WTException e) {
				if (workable) {
					Workable w = WorkableUtils.undoCheckOut(obj);		// Undo checkout to cancel changes.
				}
				//
					
				return complete;
			}
		}
		
		////////////////////////////////// WORKABLE OBJECTS ONLY //////////////////////////////////
		// Workable Objects once changed must be checked in.									 //
		if (workable) {
			// "Check In" modified object - only applies to workable object.
			Workable w = null;
			try {
				w = WorkableUtils.checkIn(obj, "Changed multiple IBA values.", true);
				complete = true;
				
			} catch (Exception e) {
				WorkableUtils.undoCheckOut(obj);
				return complete;
			}
			boolean checkedOut = WorkableUtils.isCheckedOut(w);
			if (workable) {
				System.out.println(SIGNATURE + " >> Blast! Failed to check in or undo check out " + obj + "!");
				return complete;		// Failed to check in & undo check out object.
			}
			
		} else {
			complete = true;
		}
		//																						 //
		///////////////////////////////////////////////////////////////////////////////////////////
		
		return complete;
	}*/
	
	/**
	 *	This method sets the values of multiple IBA on multiple Objects.<br />
	 *	<br />
	 *	This method will only return a non-null value in the event of a failure during the setting of an attribute.<br />
	 *	Handling of the failed IBA setting is down to the calling method.
	 *
	 *		@param valueMap a String Vector containing the names of the IBA.
	 *		@return List<WTObject>
	 */
/*	public static List<WTObject> setValues(Map<WTObject, Hashtable<String, String>> valueMap)
	{

		List<WTObject> failed = new ArrayList<WTObject>();
		System.out.println(SIGNATURE + " >> Setting IBA on " + valueMap.size() + " object.");
		
		// Loop through each Map entry - each containing a WTObject and a Hashtable of IBA and values.
		SetValues:
		for (Map.Entry<WTObject, Hashtable<String, String>> entry : valueMap.entrySet()) {
			WTObject obj = entry.getKey();		// Retrieve the Object.
			Hashtable<String, String> values = entry.getValue();		// Retrieve the Hashtable of IBA
																		// and values.
			boolean set = setValues(obj, values);
			if (! set) {
				failed.add(obj);		// Add failed entries to output.
			}
		}
		
		return failed;
	
	}*/

}