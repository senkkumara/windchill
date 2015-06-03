package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.vc.VersionControlHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides help with navigating Part structures in Windchill.
 */
public class StructureUtils
{

	/**
	 *	Returns a Map of Parent Parts to Lists of Child Parts - without quantities, see<br />
	 *	"getBOMWithQuanities" to retrieve BOM line quantities.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param qr the QueryResult containing the Parts.
	 *		@return Map<WTPart, List<WTPart>>
	 */
	public static Map<WTPart, List<WTPart>> getBOM(QueryResult qr)
			throws WTException
	{
		Map<WTPart,List<WTPart>> bom = new HashMap<WTPart, List<WTPart>>();
		WTSet parents = getParents(qr);		// Retrieve only the Parts with BOM
		
		if (parents == null || parents.size() == 0) {
			return null;
		}

		return getChildren(parents);
	}
	
	/**
	 *	Returns a Map of Parent Parts to HashMaps of Child Parts to Quantities.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param qr the QueryResult containing the Parts.
	 *		@return Map<WTPart,Map<WTPartMaster, Quantity>>
	 */
	public static Map<WTPart,Map<WTPartMaster, Quantity>> getBOMWithQuantities(QueryResult qr)
			throws WTException
	{
		Map<WTPart,Map<WTPartMaster, Quantity>> bom =
				new HashMap<WTPart,Map<WTPartMaster, Quantity>>();
		
		WTSet parents = getParents(qr);		// Retrieve only the Parts with BOM.
		if (parents == null || parents.size() == 0) {
			return null;
		}
		
		return getChildrenWithQuantities(parents);
	}
	
	/**
	 *	Returns a WTSet of Parts which have BOM.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param parts the WTCollection containing the candidate Parent Parts.
	 *		@return WTSet
	 */
	public static WTSet getParents(WTCollection parts)
		 throws WTException
	{
		WTSet parents = 													// Filter out Parts that 
				WTPartHelper.service.filterPartsWithChildren(parts);		// do not contain BOM
																			
		WTCollection coll = new WTArrayList();
		WTPart part;
		QueryResult qr;
		
		GetAllVersions:		// Retrieve all versions of each Part
		for (Object obj : parents) {
			part = (WTPart)obj;
			qr = VersionControlHelper.service.allVersionsOf(part);
			coll.add((WTPart)qr.nextElement());		// Retrieve the latest version of the part
		}
		
		return new WTHashSet(coll);
	}
	
	/**
	 *	Returns a WTSet of Parts which have BOM.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param qr the QueryResult containing the candidate Parent Parts.
	 *		@return WTSet
	 */
	public static WTSet getParents(QueryResult qr)
			throws WTException
	{
		WTCollection parts = new WTArrayList();
		WTObject obj;
		
		GetParts:		// Retrieve Parts ONLY from input
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			if (obj instanceof WTPart) {		// Check object is a Part
				parts.add((WTPart)obj);
			}
		}
		
		return getParents(parts);		// May return 'null'
	}
	
	/**
	 *	Returns a Map of Parent Parts to Lists of Child Parts - without quantities, see<br />
	 *	"getBOMWithQuanities" to retrieve BOM line quantities.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param parents a WTSet containing the parent Parts.
	 *		@return Map<WTPart, Map<WTPartMaster, Quantity>>
	 */
	public static Map<WTPart,List<WTPart>> getChildren(WTSet parents)
			throws WTException
	{
		Map<WTPart,List<WTPart>> childmap = new HashMap<WTPart,List<WTPart>>();
		WTPart parent;
		List<WTPart> childParts;
		ConfigSpec cs;
		QueryResult uses;
		Persistable[] per;
		
		GetChildren:		// Retrieve children of each BOM
		for (Object obj : parents) {
			parent = (WTPart)obj;		// Retrieve Parent Part
			childParts = new ArrayList<WTPart>();
			cs = new LatestConfigSpec();		// Only interested in latest version of BOM
			uses = WTPartHelper.service.getUsesWTParts(parent , cs);	// Retrieve Child Parts
			
			GetChild:		// Retrieve each child part
			while (uses.hasMoreElements()) {
				per = (Persistable[])uses.nextElement();
				childParts.add((WTPart)per[1]);
			}
		
			if (childParts.size() > 0) {		// Add List to Map if BOM was found
				childmap.put(parent, childParts);
			}
		}
		
		return childmap;
	}
	
	/**
	 *	Returns a Map of Parent Parts to HashMaps of Child Parts to Quantities.<br />
	 *	<br />
	 *	In the event of an exception, a null value will be returned. Any calling methods should account<br />
	 *	for this.
	 *
	 *		@param parents a WTSet containing the parent Parts.
	 *		@return Map<WTPart, Map<WTPartMaster, Quantity>>
	 */
	public static Map<WTPart, Map<WTPartMaster, Quantity>> getChildrenWithQuantities(WTSet parents)
			throws WTException
	{
		Map<WTPart,Map<WTPartMaster, Quantity>> bom =
				new HashMap<WTPart,Map<WTPartMaster, Quantity>> ();
		
		WTPart parent;
		Map<WTPartMaster, Quantity> children;
		QueryResult uses;
		WTPartUsageLink use;
		WTPartMaster child;
		Quantity quantity;

		GetChildren:		// Retrieve children for each BOM
		for (Object obj : parents) {
			parent = (WTPart) obj;
			children = new HashMap<WTPartMaster, Quantity>();
			uses = WTPartHelper.service.getUsesWTPartMasters(parent);		// Get parent-child links.
			
			GetChild:		// Retrieve each child part
			while (uses.hasMoreElements()) {								// Cycle through each BOM line.
				use = (WTPartUsageLink)uses.nextElement();
				child = (WTPartMaster)use.getUses();						// Retrieve Child part.
				quantity = use.getQuantity();								// Retrieve quantity of Child part.	
				children.put(child, quantity);
			}
			
			if (children.size() > 0) {			// One or BOM lines was found for parent.
				bom.put(parent, children);
			}
		}
		
		return bom;
	}
	
	/**
	 *	Returns String containing the Quantity Amount from a Quantity argument.
	 *
	 *		@param quantity the Quantity containing the amount.
	 *		@return String
	 */
	public static String getQuantity(Quantity quantity)
	{
		return String.valueOf(quantity.getAmount());		
	}
	
	/**
	 *	Returns String containing the Quantity UOM from a Quantity argument.
	 *
	 *		@param quantity the Quantity containing the UOM.
	 *		@return String
	 */
	public static String getUOM(Quantity quantity)
	{
		return quantity.getUnit().toString();
	}
	
}