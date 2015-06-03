package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentHelper;
import wt.epm.ReviseOptions;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.collections.WTKeyedHashMap;
import wt.fc.collections.WTKeyedMap;
import wt.part.WTPart;
import wt.vc.StandardVersionControlService;
import wt.vc.Versioned;
import wt.vc.VersionIdentifier;
import wt.vc.VersionControlHelper;
import wt.vc.wip.Workable;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides help with version modification during Workflows.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class VersionUtils
{	
	/**
	 *	Returns String containing version of WTObject.
	 *
	 *		@param obj the WTObject to retrieve revision from.
	 *		@return String
	 */
	public static String getRevisionLabel(WTObject obj)
	{		
		String rev = null;
		Versioned v;
		
		if (obj instanceof Versioned) {
			v = (Versioned)obj;
			rev = getRevisionLabel(v);
		}

		return rev;
	}
	
	public static String getRevisionLabel(Versioned v)
	{
		return v.getVersionIdentifier().getValue();
	}
	
	/**
	 *	Returns Map with WTObjects as keys and Strings containing the key's revision as values.<br />
	 *	<br />
	 *	Note: If a revision cannot be retrieved, the size of the output Map will differ from the input List.
	 *
	 *		@param objs the List containing the WTObjects to retrieve revisions from.
	 *		@return a Map containing the WTObjects and associated revisions.
	 */
	public static Map<WTObject, String> getRevisionLabels(List<WTObject> objs)
	{
		Map<WTObject, String> revs = new HashMap<WTObject, String>();
		String rev;
		
		Get:
		for (WTObject obj : objs) {
			rev = getRevisionLabel(obj);		// Retrieve the revision.
			if (rev != null) {		// Only add non-null values to Map.
				revs.put(obj, rev);
			}
		}
		
		return revs;
	}
	
	/**
	 *	This method sets the version label of the WTObject parameter to that of the String parameter.
	 *
	 *		@param obj the WTObject to have it's revision label set.
	 *		@return boolean
	 */
	public static void setRevisionLabel(WTObject obj, String rev)
			throws WTException
	{
		if (obj instanceof Versioned) {
			setRevisionLabel((Versioned)obj, rev);
		}
	}
	
	public static void setRevisionLabel(Versioned v, String rev)
			throws WTException
	{
		StandardVersionControlService svcs =
				StandardVersionControlService.newStandardVersionControlService();
				
		svcs.changeRevision(v, rev);
	}
	
	/**
	 *	This method takes a Map parameter and sets the key WTObject's version label to that specified<br />
	 *	by the corresponding String value.<br />
	 *	<br />
	 *	Returns a List of WTObjects (if any) who's revision was not incremented. If none fail, null<br />
	 *	is returned.
	 *	
	 *		@param revs the Map containing the object(s) (keys) to have their version to be set (values).
	 *		@return List<WTObject> the Object(s) which failed to have their revision set.
	 */
	public static Map<ObjectReference, String> setRevisionLabels(Map<ObjectReference, String> revs)
	{
		Map<ObjectReference, String> f = new HashMap<ObjectReference, String>();
		ObjectReference ref;
		WTObject obj;
		String rev;
		
		Set:
		for (Map.Entry<ObjectReference, String> entry : revs.entrySet()) {
			ref = entry.getKey();
			obj = (WTObject)ref.getObject();		// Get Object
			rev = entry.getValue();					// Get revision
		
			try {
				setRevisionLabel(obj, rev);
				
			}
			catch (WTException wte) {
				f.put(ref, rev);
				wte.printStackTrace();
				continue Set;
			}
		} 

		return f;		
	}
	
	/**
	 *	This method increments the version label of the Versioned argument.
	 *
	 *		@param obj the WTObject to have it's revision label incremented.
	 */
	public static void incrementRevisionLabel(WTObject obj)
			throws WTException
	{
		if (! (obj instanceof Versioned)) {
			throw new WTException(new StringBuilder(obj.toString())
				.append(" is not a versioned object...")
				.toString());
		}
		
		Versioned vc = (Versioned)obj;
		VersionIdentifier vi;
		String rev;

		try {
			vi = VersionControlHelper.nextVersionId(vc);		// Retrieve the next version identifier.
			
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}

		rev = vi.getValue();		// Retrieve next version as a String.
		setRevisionLabel(obj, rev);		// Set new version identifier.
	}
	
	/**
	 *	This method increments the version label of the objects in a QueryResult parameter.<br />
	 *	<br />
	 *	Returns a List of WTObjects (if any) who's revision was not incremented. If none fail, null<br />
	 *	is returned.
	 *	
	 *		@param qr the QueryResult containing the object(s) to have their version incremented.
	 */
	public static List<ObjectReference> incrementRevisionLabels(QueryResult qr)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		WTObject obj;
		
		Increment:
		while (qr.hasMoreElements()) {
			obj = (WTObject) qr.nextElement();
			if (! (obj instanceof Versioned)) {
				continue Increment;
			}

			try {
				incrementRevisionLabel(obj);
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference((Persistable)obj));
				wte.printStackTrace();
				continue Increment;
			}
		}
		
		return f;
	}
	
	/**
	 *	This method increments the version label of the objects in a List parameter.<br />
	 *	<br />
	 *	Returns a List of WTObjects (if any) who's revision was not incremented. If none fail, null<br />
	 *	is returned.
	 *	
	 *		@param objs the List containing the object(s) to have their version incremented.
	 */
	public static List<ObjectReference> incrementRevisionLabels(List<ObjectReference> refs)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		WTObject obj;
		
		Increment:
		for (ObjectReference ref : refs) {
			obj = (WTObject)ref.getObject();
			if (! (obj instanceof Versioned)) {
				continue Increment;
			}

			try {
				incrementRevisionLabel(obj);
				
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference((Persistable)obj));
				wte.printStackTrace();
				continue Increment;
			}
		}
		
		return f;
	}
	
	/**
	 *	Returns WTObject of new revision of parameter WTObject.<br />
	 *	<br />
	 *	Returns null if the WTObject is not version controlled or an exception is thrown.
	 *
	 *		@param obj the WTObject to be revised.
	 *		@return WTObject of new revision.
	 */
	public static WTObject revise(WTObject obj)
			throws WTException
	{
		WTObject newRev = null;
		Versioned vc = (Versioned)obj;

		try {
			newRev = (WTObject)VersionControlHelper.service.newVersion(vc);		// Create new revision.
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		PersistenceHelper.manager.store(newRev);		// Save new revision.

		return newRev;
	}
	
	/**
	 *	Returns a Map containing two lists. The first list with a String key "successful" contains <br />
	 *	WTObject(s) that were revised, the second with a String key "failed" contain those that were not.
	 *
	 *		@param qr a QueryResult to be revised.
	 *		@return Map with String keys ("successful" and "failed") and List values.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<WTObject> revise(QueryResult qr)
			throws WTException
	{
		WTKeyedHashMap map = new WTKeyedHashMap();
		WTObject obj;
		WTKeyedMap revs;
		Versioned v;
		ReviseOptions opts;
		
		Revise:
		while (qr.hasMoreElements()) {
			obj = (WTObject) qr.nextElement();
			if (! (obj instanceof Versioned)) {
				continue Revise;
			}
			v = (Versioned)obj;
            opts = new ReviseOptions((Workable)obj);
          	map.put(obj, opts); 
      	}
		
		revs = EPMDocumentHelper.service.reviseAll(map);
		
		return (Collection<WTObject>)revs.values();
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<WTObject> revise(List<WTObject> objs)
			throws WTException
	{
		WTKeyedHashMap map = new WTKeyedHashMap();
		Versioned v;
		WTKeyedMap revs;
		ReviseOptions opts;
		
		Revise:
		for (WTObject obj : objs) {
			if (! (obj instanceof Versioned)) {
				continue Revise;
			}
			v = (Versioned)obj;
			opts = new ReviseOptions((Workable)obj);
			map.put(obj, opts);
		}
		
		revs = EPMDocumentHelper.service.reviseAll(map);
		
		return (Collection<WTObject>)revs.values();
	}
	
	/**
	 *	Returns Boolean regarding whether the WTObject's revision is numeric.
	 *
	 *		@param obj the WTObject containing the revision.
	 *		@return boolean.
	 */
	public static boolean isVersionNumeric(WTObject obj)
			throws WTException
	{
		boolean isNumeric = false;
		String rev;
		double d;
		
		if (obj instanceof Versioned) {
			rev = getRevisionLabel(obj);
			try {
				d = Double.parseDouble(rev);		// Attempt to parse to Double.
				isNumeric = true;
			}
			catch (NumberFormatException ex) {
				// Do nothing .. Version is not numeric!
			}
		}
		
		return isNumeric;
	}

}