package ext.hydratight.obj;

import ext.hydratight.obj.AttributeUtils;
import ext.hydratight.obj.ClassificationUtils;
import ext.hydratight.obj.StructureUtils;
import ext.hydratight.wf.ChangeUtils;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import wt.change2.WTChangeOrder2;
import wt.fc.collections.WTCollection;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleManaged;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.part.WTPart;

import java.lang.Exception;
import java.rmi.RemoteException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides help with object validation during Workflows.<br />
 *	<ol>
 *	<li>Numbers
 *	<ol>
 *	<li>Parts</li>
 *	<li>EPM Documents</li>
 *	</ol><br /></li>
 *	<li>Part Classification</li>
 *	<li>Weight</li>
 *	<li>BOM Releases</li>
 *	</ol>br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class ValidationUtils
{
	
	private static final String[] PART_PRE = {"R", "SWE", "OSP_"};
	private static final String[] EPM_PRE = {"AM_", "APP_", "CP_", "CMP_", "GEN_", "MAS_"};
	private static final String[] EPM_SUF = {".ASM", ".DRW", ".PRT", ".FRM"};
	
	public static Map<String, boolean> isValid(WTPart part)
	{
		Map<String, boolean> result = new HashMap<String, boolean>();
		
		boolean number = isNumberValid(part);
		boolean bom = isBOMValid(part);
		boolean classification = isClassificationValid(part);
		boolean weight = isWeightvalid(part);
		//boolean association = isAssociationValid(part);
		
		result.put("Number", number);
		result.put("BOM", bom);
		result.put("Classification", classification);
		result.put("Weight", weight);
		//result.put("Association", association));
		
		return result;
	}
	
	public static Map<String, boolean> isValid(EPMDocument epm)
	{
		Map<String, boolean> result = new HashMap<String, boolean>();
		
		boolean number = isNumberValid(epm);
		
		result.put("Number", number);
		
		return result;
	}
	
	/**
	 *	Returns boolean based on whether an argument Part's number is valid.
	 *
	 *		@param part the WTPart to have it's number validated.
	 *		@return boolean
	 */
	public static boolean isNumberValid(WTPart part)
	{
	
		Boolean valid = false;	
		String num = part.getNumber();
		int length = num.length();
		if (VERBOSE) System.out.println("IS PART NUMBER VALID >> Validating " + num);
		
		String[] PART_PRE = {"R", "S", "OSP_", "X"};
		
		switch (length) {
			case 12:
				// Majority of numbers
				valid = true;
				break;
				
			case 15:
				// Sales Kits
				if (num.indexOf("-SK") > 0) {
					valid = true;
				}
				break;
				
			default:
				// Raw Materials, Sweeney Torque Multipliers & Outside Processes
				for (int i = 0; i < PART_PRE.length; i++) {
					String prefix = PART_PRE[i];
					int preLength = prefix.length();
					if (num.substring(0, preLength).equals(prefix)) {
						valid = true;
					}
				}				
				break;
				
		}
		
		// Print result to system logs.
		if (VERBOSE) System.out.println("IS Part VALID >> Is " + num + " valid? " +
				valid.toString().toUpperCase());
		
		return valid;
	}
	
	/**
	 *	Returns boolean based on whether an argument EPM Document's number is valid.
	 *
	 *		@param epm the EPM Document to have it's number validated.
	 *		@return boolean
	 */
	public static boolean isNumberValid(EPMDocument epm)
	{
	
		Boolean valid = false;
		
		String num = epm.getNumber();
		int length = num.length();
		System.out.println("IS EPMDOCUMENT NUMBER VALID >> Validating " + num);
		
		String[] EPM_PRE = {"AM_", "APP_", "CP_", "CMP_", "GEN_", "MAS_"};
		String[] EPM_SUF = {".ASM", ".DRW", ".PRT", ".FRM"};
		
		// Validate the number's prefix (e.g. "AM_")
		boolean validPre = false;	// Default is prefix is invalid.
		if (length != 16 || length != 19) {		// A "standard" part number plus extension or
												// A sales kit part number plus extension.
												
			for (int i = 0; i < EPM_PRE.length; i++) {	// Cycle through predefined valid prefixes.
				if (num.indexOf(EPM_PRE[i]) > -1) {
					System.out.println("IS EPMDOCUMENT NUMBER VALID >> " + num +
						"has an prefix of " + EPM_SUF[i]);
					
					validPre = true;
				}
			}
		} else {
			System.out.println("IS EPMDOCUMENT NUMBER VALID >> " + num + " does not have a prefix.");
			validPre = true;
		}
		
		// Validate the number's extension (e.g. ".PRT")
		boolean validSuf = false;	// Default is extension is invalid.
		for (int i = 0; i < EPM_SUF.length; i++) {	// Cycle through predefined valid extensions.
			if (num.indexOf(EPM_SUF[i]) > -1) {
				System.out.println("IS EPMDOCUMENT NUMBER VALID >> " + num +
					"has an extension of " + EPM_SUF[i]);
					
				validSuf = true;
			}
		}
		
		// Both the prefix & extension must be valid.
		if (validPre && validSuf) {
			valid = true;
		}
		
		
		// Print result to system logs.
		System.out.println("IS EPMDOCUMENT VALID >> Is " + num + " valid? " +
				valid.toString().toUpperCase());
				
		return valid;
	}
	
	/**
	 *	Returns boolean based on whether an argument Part has either been classified or has had <br />
	 *	it's classification attributes populated by other means - generally speaking, migrated.
	 *
	 *		@param part the Part to have it's classification attributes validated.
	 *		@return boolean
	 */
	public static boolean isClassificationValid(WTPart part)
			throws Exception, WTException
	{
		
		System.out.println("IS CLASSIFICATION VALID >> Validating the classification of "+
				part.getNumber());
				
		// Verify the part has been verified.
		Boolean classified = ClassificationUtils.isClassified(part);
		
		if (! classified) {		// Check the classification attributes have been populated by other means.
			classified = ClassificationUtils.hasClassificationAttributes(part);
		}
		
		// Print result to system logs.
		System.out.println("IS CLASSIFICATION VALID >> Is " + part.getNumber() + " valid? " +
				classified.toString().toUpperCase());
				
		return classified;
		
	}
	
	/**
	 *	Returns boolean based on whether an argument Part has a valid weight attribute.
	 *
	 *		@param part the Part to have it's Weight attribute validated.
	 *		@return boolean
	 */
	public static boolean isWeightValid(WTPart part)
	{
	
		if (VERBOSE) System.out.println("IS WEIGHT VALID >> Validating the Weight of "+
				part.getNumber());
				
		Boolean valid = true;
		try {
			System.out.println("IS WEIGHT VALID >> Validating Weight of " + part.getNumber());
			String weight = AttributeUtils.getValue((WTObject)part, "Weight");		// Retrieve "Weight" value.
			if (weight == null ||			// Has a value.
				weight.equals("") ||		// String is not empty.
				weight.equals("0")) {		// Not equal to "0" - Oracle does not accept a zero value.
																				
				valid = false;				// Part weight does not satisfy the above three conditions.
				
			}
			
		} catch (Exception ex) {
			System.out.println("IS WEIGHT VALID >> Part " + part.getNumber() +
					" does not contain a \"Weight\" attribute!");
					
			valid = false;		// No "Weight" attribute on Part.
			
		}
		
		if (VERBOSE) System.out.println("IS WEIGHT VALID >> Is " + part.getNumber() + " valid? " +
				valid.toString().toUpperCase());
				
		return valid;
		
	}
	
	public static boolean isBOMValid(WTPart part)
	{
		return false;
	}
	
	/**
	 *	This method validates the life cycle state of the child item of a BOM.
	 *
	 *		@param child the WTPart who's state is to be validated.
	 *		@param qr the QueryResult containing the other part(s) attached to the container being verified.
	 *		@param validStates the String array containing the valid states for the WTPart.
	 *		@param incQr the boolean that specifies whether if the WTPart is among the QueryResult it may be
	 *				considered valid.
	 *
	 *		@return boolean
	 */
	public static boolean isChildsStateValid(WTPart child, QueryResult qr, String[] validStates,
			boolean incQr) {
	
		System.out.println("IS CHILD STATE VALID >> Validating " + child.getNumber());
		LifeCycleManaged lmChild = (LifeCycleManaged)child;
		String childState = lmChild.getState().toString();
		System.out.println("IS CHILD STATE VALID >> " + child.getNumber() + " is in the " +
				childState + " state.");
				
		Boolean valid = false;
		// Check Part's state against valid states.
		for (int i = 0; i < validStates.length; i++) {
			String validState = validStates[i];
			if (childState.equals(validState)) {
				System.out.println("IS CHILD STATE VALID >> " + child.getNumber() +
						" is in a valid state (" + validStates[i] + ").");
						
				valid = true;
			}
		}
		
		// Check Part is a member of the supplied QueryResult.
		if (! valid && incQr) {
			qr.reset();		// Reset the QueryResult - since it may have been used once before.
			String childNumber = child.getNumber();
			
			while (qr.hasMoreElements()) {
				WTPart part = (WTPart)qr.nextElement();
				String partNumber = part.getNumber();
				if (partNumber.equals(childNumber)) {
					System.out.println("IS CHILD STATE VALID >> " + childNumber + " is in QueryResult.");
				
					valid = true;
				}
			}
		}
		
		// Print result to system logs.
		System.out.println("IS CHILD STATE VALID >> Is " + child.getNumber() + " valid? " +
				valid.toString().toUpperCase());
				
		return valid;
	
	}
	
	/**
	 *	This method validates the BOM of each parts in a given Promotion Request.<br />
	 *	<br />
	 *	It's primary goal is to ensure that no Parts are inadvertently released to Oracle in the wrong state.
	 *
	 *		@param pn the Promotion Request containing the part(s) to be validated.
	 *		@return List
	 */
	public static List<WTPart> findInvalidBOM(PromotionNotice pn)
			throws WTException, WTPropertyVetoException {
	
		QueryResult qr = PromotionHelper.getPromoteables(pn);
		
		// The valid states are determined by the "Promotion State" of the Promotion Request.
		String state = PromotionHelper.getTargetState(pn);
		String[] validStates = PromotionHelper.validPromotionStates(state);
		
		List<WTPart> missing = new ArrayList<WTPart>();
		if (! state.equals("CANCELLED") || state.equals("OBSOLETE")) {
			return missing;
		}
		
		// Retrieve the BOM among the parts on the Promotion Request.
		Map<WTPart, List<WTPart>> boms = StructureUtils.getChildMap(qr);
		for (Entry<WTPart, List<WTPart>> entry : boms.entrySet()) {
			WTPart parent = entry.getKey();
			List<WTPart> children = entry.getValue();
			
			if (validStates.length > 0) {
				if (children.size() > 0) {
					for (Iterator it = children.listIterator(); it.hasNext();) {
						WTPart child = (WTPart) it.next();
						boolean valid = isChildsStateValid(child, qr, validStates, true);
						
						if (! valid) {
							missing.add(child);		// Add to container if is invalid.
						}
					}
				}
			} else {
				// If there are NO valid states then ALL the child parts must be invalid.
				System.out.println("VALDIATE BOM >> There are NO VALID STATES for children of " +
						parent.getNumber());
						
				for (Iterator it = children.listIterator(); it.hasNext();) {
					WTPart child = (WTPart) it.next();
					missing.add(child);		// Add to container.
				}
			}
		}
		
		
		// Print results to system logs.
		System.out.println("VALIDATE BOM >> Found " + missing.size() +
				" missing child Parts.");
				
		return missing;
	}
	
	/**
	 *	This method validates the BOM of each parts in a given Change Notice.<br />
	 *	<br />
	 *	It's primary goal is to ensure that no Parts are inadvertently released to Oracle in the wrong state.
	 *
	 *		@param cn the Change Notice containing the part(s) to be validated.
	 *		@return List
	 */
	public static List<WTPart> findInvalidBOM(WTChangeOrder2 cn)
			throws WTException, WTPropertyVetoException {
	
		QueryResult qr = ChangeUtils.getResultingObjects(cn);		// Only need to validate the Resulting
																	// objects.
	
		List<WTPart> missing = new ArrayList<WTPart>();
		
		// Retrieve the BOM among the parts on the Change Notice.
		Map<WTPart, List<WTPart>> boms = StructureUtils.getChildMap(qr);
		// Review each BOM individually.
		for (Entry<WTPart, List<WTPart>> entry : boms.entrySet()) {
			WTPart parent = entry.getKey();
			LifeCycleManaged lmParent = (LifeCycleManaged)parent;
			
			// The valid states are determined by the state of the parent part.
			String state = lmParent.getState().toString();
			String[] validStates = ChangeUtils.getValidChangeStates(state);
			List<WTPart> children = entry.getValue();
			
			if (validStates.length > 0) {
				if (children.size() > 0) {
					// Review the state of each child in the BOM.
					for (Iterator it = children.listIterator(); it.hasNext();) {
						WTPart child = (WTPart) it.next();
						boolean valid = isChildsStateValid(child, qr, validStates, false);
						
						if (! valid) {
							missing.add(child);		// Add to container if is invalid.
						}
					}
				}
			} else {
				// If there are NO valid states then ALL the child parts must be invalid.
				System.out.println("VALDIATE BOM >> There are NO VALID STATES for children of " +
						parent.getNumber());
					
				for (Iterator it = children.listIterator(); it.hasNext();) {
					WTPart child = (WTPart) it.next();
					missing.add(child);		// Add to container.
				}
			}
		}
		
		// Print results to system logs.
		System.out.println("VALIDATE BOM >> Found " + missing.size() +
				" missing child Parts.");
		
		return missing;
	}
	
		/**
	 *	Returns a List of Object(s) that have failed the number validation criteria.
	 *
	 *		@param qr the QueryResult containing the Object(s) to be validated.
	 *		@return List
	 */
	public static List<WTObject> findInvalidNumbers(QueryResult qr)
	{
	
		List<WTObject> invalidObjects = new ArrayList<WTObject>();

		while (qr.hasMoreElements()) {
			Persistable obj = (Persistable) qr.nextElement();
			if(obj instanceof WTPart){		// Validate Part numbers.
				boolean valid = isNumberValid((WTPart)obj);
				if (! valid) {
					invalidObjects.add((WTObject)obj);
				}

			} else if(obj instanceof EPMDocument){		// Validate EPM Document numbers.
				boolean valid = isNumberValid((EPMDocument)obj);
				if (! valid) {
					invalidObjects.add((WTObject)obj);
				}
				
			}
		}
		
		System.out.println("VALIDATE NUMBERS >> Found " + invalidObjects.size() +
				" invalid objects.");
				
		return invalidObjects;
	}

	
		/**
	 *	This method validates the classification of the parts in a given QueryResult.<br />
	 *	<br />
	 *	It first checks to see if the part is classified. If not, it determines whether the part has <br />
	 *	the classification attributes populated - a migrated part.
	 *
	 *		@param qr the QueryResult containing the part(s) to be validated.
	 *		@return List
	 */
	public static List<WTPart> findUnclassifiedParts(QueryResult qr)
			throws Exception, WTException
	{
	
		List<WTPart> invalidParts = new ArrayList<WTPart>();		// Create a container for invalid parts.
		while (qr.hasMoreElements()) {
			Persistable obj = (Persistable) qr.nextElement();
			if(obj instanceof WTPart){		// Only Parts can be classified.
				WTPart part = (WTPart)obj;
				boolean valid = isClassificationValid(part);
				if (! valid) {
					invalidParts.add(part);		// Add invalid parts to the container.
				}
			}
		}
		
		// Print results to system logs.
		System.out.println("VALIDATE CLASSIFICATION >> Found " + invalidParts.size() +
				" invalid Parts.");
				
		return invalidParts;
	}
	
	/**
	 *	This method validates the Weight of the parts in a given QueryResult.
	 *
	 *		@param qr the QueryResult containing the part(s) to be validated.
	 *		@return List
	 */
	public static List<WTPart> findInvalidWeights(QueryResult qr) throws WTException,
			WTPropertyVetoException, RemoteException {
		
		List<WTPart> invalidParts = new ArrayList<WTPart>();		// Create container for invalid parts.
		while (qr.hasMoreElements()) {
			Persistable obj = (Persistable) qr.nextElement();
			if(obj instanceof WTPart){		// Only need to validate Part Weights.
				WTPart part = (WTPart)obj;
				boolean valid = isWeightValid(part);
				
				if (! valid) {
					invalidParts.add(part);		// Add invalid Parts to the container.
				}
			}
		}
		
		// Print results to system logs.
		System.out.println("VALIDATE WEIGHTS >> Found " + invalidParts.size() +
				" invalid Parts.");
				
		return invalidParts;		// Return the container.
		
	}
}