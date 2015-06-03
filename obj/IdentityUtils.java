package ext.hydratight.obj;

import ext.hydratight.GeneralUtils;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTProperties;
import wt.vc.IterationIdentifier;
import wt.vc.Versioned;
import wt.vc.VersionIdentifier;

import java.io.UnsupportedEncodingException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides help with retrieval and modification of the identity of a number of objects in<br />
 *	Windchill.<br />
 *	<br />
 *	This includes the:
 *	<ol>
 *	<li>Searching</li>
 *	<li>Renaming</li>
 *	<li>Renumbering</li>
 *	</ol>
 *	Of:
 *	<ol>
 *	<li>WTParts</li>
 *	<li>EPMDocuments</li>
 *	<li>WTDocuments</li>
 *	</ol>
 *	<b>Note: This class was developed for <b>Windchill 9.1</b>.
 *
 *		@see wt.doc.WTDocument
 *		@see wt.epm.EPMDocument
 *		@see wt.part.WTPart
 */
public class IdentityUtils
{
	static final private String IDENTITY_STRING = "{0} ({1}.{2}): {3}";
	static final private String IDENTITY_STRING_ROW = "<tr><td>{0} ({1}.{2})</td><td>: {3}</td></tr>";
	static final private String COMMENTS_STRING_ROW = "{0}</td><td> - {1}";
	static private final String PORT = "8080";
	static final private String URL_STUB = "http://{0}:{1}/Windchill/app/#ptc1/tcomp/infoPage?oid={2}&u8=1";
	static final private String URL_ENCODING = "UTF-8";
	static final private String ANCHOR_TAG = "<a href=\"{0}\" style=\"color: {1}\">{2}</a>";
	static final private String ANCHOR_DEFAULT_COLOUR = "darkgreen";
	static final private String HOST_PROP = "java.rmi.server.hostname";	
	static private String HOST_NAME = "pdm.actuant.com";
	static {
		try {
			WTProperties wtprops = WTProperties.getLocalProperties();
			HOST_NAME = wtprops.getProperty(HOST_PROP);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static String getURLLink(WTObject obj, String text)
			throws WTException
	{
		return MessageFormat.format(ANCHOR_TAG, new Object[] {
				getURL(obj), ANCHOR_DEFAULT_COLOUR,  text
			});
	}
	
	public static String getURLLink(WTPart part)
			throws WTException
	{
		return getURLLink((WTObject)part, part.getNumber());
	}
	
	public static String getURLLink(EPMDocument epm)
			throws WTException
	{
		return getURLLink((WTObject)epm, epm.getNumber());
	}
	
	public static String getURLLink(WTDocument doc)
			throws WTException
	{
		return getURLLink((WTObject)doc, doc.getNumber());
	}
	
	private static String getURL(WTObject obj)
			throws WTException
	{
		String di = (new ReferenceFactory()).getReferenceString(obj);
		
		try {
			di = URLEncoder.encode(di, URL_ENCODING);
		
		} catch (UnsupportedEncodingException uee) {
			throw new WTException(uee);
		}
		
		return MessageFormat.format(URL_STUB, new Object[] {
				HOST_NAME, PORT, di
			});
	
	}
	
	/**
	 *	
	 */
	private static StringBuilder append(StringBuilder sb, String str)
	{
		if (sb.toString().equals("")) {
			sb.append(str);
		} else {
			sb.append("\n").append(str);
		}
		
		return sb;
	}
	
	private static String createIdentityString(WTObject obj, String number, String version,
				String iteration, String name, boolean markup)
				
			throws WTException
	{
		String template = IDENTITY_STRING;
		if (markup) {
			template = IDENTITY_STRING_ROW;
		}
		
		return MessageFormat.format(template, new Object[] {
				getURLLink(obj, number), version, iteration, name
			});
	}
	
	private static String createIdentityString(WTObject obj, String number, String version, 
				String iteration, String name)
				
			throws WTException
	{
		return createIdentityString(obj, number, version, iteration, name, false);
	}
	
	/**
	 *	Returns a String containing the details of the parts in a QueryResult argument.<br>
	 *	<br>
	 *	The String is returned in the following format<sup>1</sup>:<br>
	 *	<em>&lt;PART NO 1&gt;: &lt;PART NAME 1&gt;, &lt;PART VERSION 1&gt;<br>
	 *	&lt;PART NO 2&gt;: &lt;PART NAME 2&gt;, &lt;PART VERSION 2&gt;<br>
	 *	...<br>
	 *	...<br>
	 *	...<br>
	 *	&lt;PART NO (n - 1)&gt;: &lt;PART NAME (n - 1)&gt;, &lt;PART VERSION (n - 1)&gt;<br>
	 *	&lt;PART NO (n)&gt;: &lt;PART NAME (n)&gt;, &lt;PART VERSION (n)&gt;<br>
	 *	<br>
	 *	PART COUNT: n</em><br>
	 *
	 *		@param qr the QueryResult containing the Parts to be listed.
	 *		@return String
	 *		@see wt.fc.QueryResult
	 */
	public static String getPartIdentities(QueryResult qr, boolean markup)
			throws WTException
	{
		StringBuilder sb = new StringBuilder();
		String id;
		Persistable per;
	
		Get:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof WTPart) {
				id = getIdentity((WTPart)per, markup);
				sb = append(sb, id);
			}
		}
		
		if (sb.toString().equals("")) {
			return null;
		}
		
		return sb.toString();
	}
	
	public static String getPartIdentities(QueryResult qr)
			throws WTException
	{
		return getPartIdentities(qr, false);
	}

	/**
	 *	Returns a String containing the identity of a Part argument.<br>
	 *	<br>
	 *	The String is returned in the following format:<br>
	 *	<em>&lt;PART NO&gt;: &lt;PART NAME&gt;, &lt;PART VERSION&gt;</em>
	 *
	 *		@param part the WTPart for which an identity String is to be created.
	 *		@return String
	 *		@see wt.part.WTPart
	 */
	public static String getIdentity(WTPart part, boolean markup)
			throws WTException
	{
		Versioned v = (Versioned)part;
		VersionIdentifier vi = v.getVersionIdentifier();
		IterationIdentifier ii = v.getIterationIdentifier();
		
		// Collect Part identity details.
		String number = part.getNumber();
		String name = part.getName();
		String version = vi.getValue();
		String iteration = ii.getValue();
		
		return createIdentityString((WTObject)part, number, version, iteration, name, markup);
	}
	
	public static String getIdentity(WTPart part)
			throws WTException
	{
		return getIdentity(part, false);
	}

	/**
	 *	Returns a String containing the details of the EPMDocs in a QueryResult argument.<br>
	 *	<br>
	 *	The String is returned in the following format<sup>1</sup>:<br>
	 *	<em>&lt;EPM NO 1&gt;: &lt;EPM NAME 1&gt;, &lt;EPM VERSION 1&gt;<br>
	 *	&lt;EPM NO 2&gt;: &lt;EPM NAME 2&gt;, &lt;EPM VERSION 2&gt;<br>
	 *	...<br>
	 *	...<br>
	 *	...<br>
	 *	&lt;EPM NO (n - 1)&gt;: &lt;EPM NAME (n - 1)&gt;, &lt;EPM VERSION (n - 1)&gt;<br>
	 *	&lt;EPM NO (n)&gt;: &lt;EPM NAME (n)&gt;, &lt;EPM VERSION (n)&gt;<br>
	 *
	 *		@param qr the QueryResult containing the EPMDocuments to be listed.
	 *		@return String
	 *		@see wt.fc.QueryResult
	 */
	public static String getEPMIdentities(QueryResult qr, boolean markup)
			throws WTException
	{
		StringBuilder sb = new StringBuilder();
		Persistable per;
		String id;
	
		Get:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof EPMDocument) {
				id = getIdentity((EPMDocument)per, markup);
				sb = append(sb, id);
			}
		}
		
		if (sb.toString().equals("")) {
			return null;
		}
		
		return sb.toString();
	}
	
	public static String getEPMIdentities(QueryResult qr)
			throws WTException
	{
		return getEPMIdentities(qr, false);
	}
	
	/**
	 *	Returns a String containing the details of an EPM Document argument.<br>
	 *	<br>
	 *	The String is returned in the following format:<br>
	 *	<em>&lt;EPM NO&gt;: &lt;EPM NAME&gt;, &lt;EPM VERSION&gt;</em>
	 *
	 *		@param epm the EPMDocument for which an identity String is to be created.
	 *		@return String
	 *		@see wt.epm.EPMDocument
	 */
	public static String getIdentity(EPMDocument epm, boolean markup)
			throws WTException
	{
		Versioned v = (Versioned)epm;
		VersionIdentifier vi = v.getVersionIdentifier();
		IterationIdentifier ii = v.getIterationIdentifier();
	
		// Collect EPM identity details.
		String number = epm.getNumber();
		String name = epm.getName();
		String version = vi.getValue();
		String iteration = ii.getValue();
		
		return createIdentityString((WTObject)epm, number, version, iteration, name, markup);
	}
	
	public static String getIdentity(EPMDocument epm)
			throws WTException
	{
		return getIdentity(epm, false);
	}
	
	/**
	 *	Returns a String containing the details of the WTDocuments in a QueryResult argument.<br>
	 *	<br>
	 *	The String is returned in the following format<sup>1</sup>:<br>
	 *	<em>&lt;DOC NO 1&gt;: &lt;DOC NAME 1&gt;, &lt;DOC VERSION 1&gt;<br>
	 *	&lt;DOC NO 2&gt;: &lt;DOC NAME 2&gt;, &lt;DOC VERSION 2&gt;<br>
	 *	...<br>
	 *	...<br>
	 *	...<br>
	 *	&lt;DOC NO (n - 1)&gt;: &lt;DOC NAME (n - 1)&gt;, &lt;DOC VERSION (n - 1)&gt;<br>
	 *	&lt;DOC NO (n)&gt;: &lt;DOC NAME (n)&gt;, &lt;DOC VERSION (n)&gt;<br>
	 *
	 *		@param qr the QueryResult containing the WTDocuments to be listed.
	 *		@return String
	 *		@see wt.fc.QueryResult
	 */
	public static String getDocumentIdentities(QueryResult qr, boolean markup)
			throws WTException
	{
		StringBuilder sb = new StringBuilder();
		Persistable per;
		String id;
	
		Get:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof WTDocument) {
				id = getIdentity((WTDocument)per, markup);
				sb = append(sb, id);
			}
		}
		
		if (sb.toString().equals("")) {
			return null;
		}
		
		return sb.toString();
	}
	
	public static String getDocumentIdentities(QueryResult qr)
			throws WTException
	{
		return getDocumentIdentities(qr, false);
	}
	
	/**
	 *	Returns a String containing the details of an WTDocument argument.<br>
	 *	<br>
	 *	The String is returned in the following format:<br>
	 *	<em>&lt;DOC NO&gt;: &lt;DOC NAME&gt;, &lt;DOC VERSION&gt;</em>
	 *
	 *		@param doc the WTDocument for which an identity String is to be created.
	 *		@return String
	 *		@see wt.doc.WTDocument
	 */
	public static String getIdentity(WTDocument doc, boolean markup)
			throws WTException
	{
		Versioned v = (Versioned)doc;
		VersionIdentifier vi = v.getVersionIdentifier();
		IterationIdentifier ii = v.getIterationIdentifier();
	
		String number = doc.getNumber();
		String name = doc.getName();
		String version = vi.getValue();
		String iteration = ii.getValue();
		
		return createIdentityString((WTObject)doc, number, version, iteration, name, markup);
	}
	
	public static String getIdentity(WTDocument doc)
			throws WTException
	{
		return getIdentity(doc, false);
	}
	
	public static String getIdentities(List<ObjectReference> refs, boolean markup)
			throws WTException
	{
	
		StringBuilder sb = new StringBuilder();
		String id;
		
		if (markup) {
			sb.append("<table>");
		}
			
		Get:
		for (ObjectReference ref : refs) {
			id = getIdentity(ref, markup);
			
			if ((! markup) && sb.length() > 0) {
				sb.append("\n");
			}
			
			if (id != null) {
				sb.append(id);
			}
			
		}
		
		if (sb.toString().equals("<table>") || sb.toString() == "") {
			return null;
		}
		
		if (markup) {
			sb.append("</table>");
		}
	
		return sb.toString();
	
	}
	
	public static String getIdentities(List<ObjectReference> refs)
			throws WTException
	{
		return getIdentities(refs, false);
	}
	
	public static String getIdentity(ObjectReference ref, boolean markup)
			throws WTException
	{
		WTObject obj = (WTObject)ref.getObject();
		return getIdentity(obj, markup);
	}
	
	public static String getIdentity(ObjectReference ref)
			throws WTException
	{
		return getIdentity(ref, false);
	}
	
	public static String getIdentities(QueryResult qr, boolean markup)
			throws WTException
	{
		StringBuilder sb = new StringBuilder();
		Persistable per;
		String id;
	
		Get:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if (per instanceof WTObject) {
				id = getIdentity((WTObject)per, markup);
				sb = append(sb, id);
			}
		}
		
		if (sb.toString().equals("")) {
			return null;
		}
		
		return sb.toString();
	}
	
	public static String getIdentities(QueryResult qr)
			throws WTException
	{
		return getIdentities(qr, false);
	}
	
	public static String getIdentity(WTObject obj, boolean markup)
			throws WTException
	{
		String id = null;
		if (obj instanceof WTPart) {
			id = getIdentity((WTPart)obj, markup);
		}
		else if (obj instanceof EPMDocument) {
			id = getIdentity((EPMDocument)obj, markup);
		}
		else if (obj instanceof WTDocument) {
			id = getIdentity((WTDocument)obj, markup);
		}
		
		return id;
	}
	
	public static String getIdentity(WTObject obj)
			throws WTException
	{
		return getIdentity(obj, false);
	}
	
	/**
	*	Returns an EPMDocumentMaster for a given part number String parameter.
	*
	*		@param number the String containing the <b>exact</b> number to be searched for.
	*		@return EPMDocumentMaster
	*		@see wt.epm.EPMDocumentMaster
	*/
	public static EPMDocumentMaster findEPMDocumentMaster(String number)
			throws WTException
	{
		EPMDocumentMaster em = null;
		QuerySpec qs;
		QueryResult qr;
		
		// Create search
		qs = new QuerySpec(EPMDocumentMaster.class);
		qs.appendWhere(new SearchCondition(EPMDocumentMaster.class, EPMDocumentMaster.NUMBER,
				SearchCondition.EQUAL, number), null);
				
		qr = PersistenceHelper.manager.find((StatementSpec)qs);
		
		// Check search returned EPMDocumentMaster(s)
		if (qr.size() > 0) {
			em = (EPMDocumentMaster)qr.nextElement();
		}
		
		return em;
	}
	
	/**
	*	Returns a WTDocumentMaster for a given part number String parameter.
	*
	*		@param number the String containing the <b>exact</b> number to be searched for.
	*		@return WTDocumentMaster
	*		@see wt.doc.WTDocumentMaster
	*/
	public static WTDocumentMaster findWTDocumentMaster(String number)
			throws WTException
	{
		WTDocumentMaster dm = null;
		QuerySpec qs;
		QueryResult qr;
		
		// Create search.
		qs = new QuerySpec(WTDocumentMaster.class);
		qs.appendWhere(new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER,
				SearchCondition.EQUAL, number), null);
				
		qr = PersistenceHelper.manager.find((StatementSpec)qs);
		
		// Check search returned WTDocumentMaster(s).
		if (qr.size() > 0) {
			dm = (WTDocumentMaster)qr.nextElement();
		}
		
		return dm;
	}
	
	/**
	*	Returns a WTPartMaster for a given part number String parameter.
	*
	*		@param number the String containing the <b>exact</b> number to be searched for.
	*		@return WTPartMaster
	*		@see wt.part.WTPartMaster
	*/
	public static WTPartMaster findWTPartMaster(String number)
			throws WTException
	{
		WTPartMaster pm = null;
		QuerySpec qs;
		QueryResult qr;
		
		// Create search.
		qs = new QuerySpec(WTPartMaster.class);
		qs.appendWhere(new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, number), null);
		qr = PersistenceHelper.manager.find((StatementSpec)qs);
		
		// Check search returned WTPartMaster(s).
		if (qr.size() > 0) {
			pm = (WTPartMaster)qr.nextElement();
		}
		
		return pm;
	}
	
	/**
	*	Returns void, finds an EPMDocumentMaster based on an item number parameter and renames it to a 
	*	name parameter.
	*
	*		@param number the item number of the EPMDocument to be renamed.
	*		@param newName the new name of the EPMDocument.
	*		@return boolean
	*		@see wt.epm.EPMDocument
	*/
	public static void renameEPMDocument(String number, String newName)
			throws WTException
	{
		EPMDocumentMaster em = findEPMDocumentMaster(number);	// Locate the EPMDocument.
		EPMDocumentMasterIdentity emIdentity;
		String oldName;
		
		// Check a EPMDocument was found.
		if (em != null) {
			emIdentity = (EPMDocumentMasterIdentity)em.getIdentificationObject();
			oldName = emIdentity.getName();
			
			// Check new name is not equal to old name & rename EPMDocument if not.
			if (!newName.equals(oldName)) {
				try {
					emIdentity.setNumber(number);
					emIdentity.setName(newName);
					IdentityHelper.service.changeIdentity(em, emIdentity);	// Change EPMDocumentMaster identity.
					PersistenceHelper.manager.refresh(em);
					
				}
				catch (WTPropertyVetoException pve) {
					throw new WTException(pve);
				}
			}
		}
	}
	
	/**
	*	Returns void, finds a WTDocumentMaster based on an item number parameter and renames it to a 
	*	name parameter.
	*
	*		@param number the item number of the WTDocument to be renamed.
	*		@param newName the new name of the WTDocument.
	*		@return boolean
	*		@see wt.doc.WTDocument
	*/
	public static void renameWTDocument(String number, String newName)
			throws WTException
	{
		WTDocumentMaster dm = findWTDocumentMaster(number);	// Locate the WTDocumentMaster.
		WTDocumentMasterIdentity dmIdentity;
		String oldName;
		
		// Check a WTDocumentMaster was found.
		if (dm != null) {
			dmIdentity = (WTDocumentMasterIdentity)dm.getIdentificationObject();
			oldName = dmIdentity.getName();
		
			// Check new name is not equal to old name & rename WTDocument if not.
			if (!newName.equals(oldName)) {
				try {
					dmIdentity.setNumber(number);
					dmIdentity.setName(newName);
					IdentityHelper.service.changeIdentity(dm, dmIdentity);	// Change WTDocumentMaster identity.
					PersistenceHelper.manager.refresh(dm);
					
				}
				catch (WTPropertyVetoException pve) {
					throw new WTException(pve);
				}
			}
		}
	}
	
	/**
	*	Returns void, finds a WTPartMaster based on an item number parameter and renames it to a 
	*	name parameter.
	*
	*		@param number the item number of the WTPart to be renamed.
	*		@param newName the new name of the WTPart.
	*		@return boolean
	*		@see wt.part.WTPart
	*/
	public static void renameWTPart(String number, String newName)
			throws WTException
	{
		WTPartMaster pm = findWTPartMaster(number);	// Locate the WTPartMaster.
		WTPartMasterIdentity pmIdentity;
		String oldName;
		
		// Check a WTPartMaster was found.
		if (pm != null) {
			pmIdentity = (WTPartMasterIdentity)pm.getIdentificationObject();
			oldName = pmIdentity.getName();
		
			// Check new name is not equal to old name & rename WTPart if not.
			if (!newName.equals(oldName)) {
				try {
					pmIdentity.setNumber(number);
					pmIdentity.setName(newName);
					IdentityHelper.service.changeIdentity(pm, pmIdentity);	// Change WTPart identity.
					PersistenceHelper.manager.refresh(pm);
					
				}
				catch (WTPropertyVetoException pve) {
					throw new WTException(pve);
				}
			}
		}
	}
	
	/**
	*	Returns void, finds an EPMDocumentMaster based on an item number parameter and renumbers it to a 
	*	new number parameter.
	*
	*		@param oldNumber the old number of the EPMDocument.
	*		@param newNumber the new number of the EPMDocument.
	*		@return boolean
	*		@see wt.epm.EPMDocument
	*/
	public static void renumberEPMDocument(String oldNumber, String newNumber)
			throws WTException
	{
		boolean renumbered = false;
		EPMDocumentMaster em = findEPMDocumentMaster(oldNumber);	// Locate the EPMDocumentMaster
		EPMDocumentMaster emCheck = findEPMDocumentMaster(newNumber);	// Attempt to locate an EPMDocumentMaster under new number.
		EPMDocumentMasterIdentity emIdentity;
		String name;
		
		// Check EPMDocumentMaster was found and no EPMDocumentMaster was found under new number - will not renumber if exists.
		if (em != null && emCheck == null) {
			try {
				emIdentity = (EPMDocumentMasterIdentity)em.getIdentificationObject();
				name = emIdentity.getName();
				emIdentity.setNumber(newNumber);
				emIdentity.setName(name);
				IdentityHelper.service.changeIdentity(em, emIdentity);	// Change EPMDocument identity.
				PersistenceHelper.manager.refresh(em);
			
			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}
	}
	
	/**
	*	Returns void, finds a WTDocumentMaster based on an item number parameter and renumbers it to a 
	*	new number parameter.
	*
	*		@param oldNumber the old number of the WTDocument.
	*		@param newNumber the new number of the WTDocument.
	*		@return boolean
	*		@see wt.doc.WTDocument
	*/
	public static void renumberWTDocument(String oldNumber, String newNumber)
			throws WTException
	{

		WTDocumentMaster dm = findWTDocumentMaster(oldNumber);	// Locate the WTDocumentMaster
		WTDocumentMaster dmCheck = findWTDocumentMaster(newNumber);	// Attempt to locate an WTDocumentMaster under new number.
		WTDocumentMasterIdentity dmIdentity;
		String name;
		
		// Check WTDocumentMaster was found and no WTDocumentMaster was found under new number - will not renumber if exists.
		if (dm != null && dmCheck == null) {
			try {
				dmIdentity = (WTDocumentMasterIdentity)dm.getIdentificationObject();
				name = dmIdentity.getName();
				dmIdentity.setNumber(newNumber);
				dmIdentity.setName(name);
				IdentityHelper.service.changeIdentity(dm, dmIdentity);	// Change WTDocumentMaster identity.
				PersistenceHelper.manager.refresh(dm);
			
			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}

	}
	
	/**
	*	Returns void, finds a WTPartMaster based on an item number parameter and renumbers it to a 
	*	new number parameter.
	*
	*		@param oldNumber the old number of the WTPart.
	*		@param newNumber the new number of the WTPart.
	*		@return boolean
	*		@see wt.part.WTPart
	*/
	public static void renumberWTPart(String oldNumber, String newNumber)
			throws WTException
	{
		WTPartMaster pm = findWTPartMaster(oldNumber);	// Locate the WTPartMaster
		WTPartMaster pmCheck = findWTPartMaster(newNumber);	// Attempt to locate an WTPartMaster under new number.
		WTPartMasterIdentity pmIdentity;
		String name;
		
		// Check WTPartMaster was found and no WTPartMaster was found under new number - will not renumber if exists.
		if (pm != null && pmCheck == null) {
			try {
				pmIdentity = (WTPartMasterIdentity)pm.getIdentificationObject();
				name = pmIdentity.getName();
				pmIdentity.setNumber(newNumber);
				pmIdentity.setName(name);
				IdentityHelper.service.changeIdentity(pm, pmIdentity);	// Change WTPartMaster identity.
				PersistenceHelper.manager.refresh(pm);

			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}
	}
	
	/**
	*	Returns void, finds an EPMDocumentMaster based on an item number parameter, renumbers it to a new number 
	*	parameter and renames it to a new name parameter.
	*
	*		@param oldNumber the old number of the EPMDocument.
	*		@param newNumber the new number of the EPMDocument.
	*		@param newName the new name of the EPMDocument.
	*		@return boolean
	*		@see wt.epm.EPMDocumentMaster
	*/
	public static void updateEPMDocumentIdentity(String oldNumber, String newNumber, String newName)
			throws WTException, NullPointerException
	{
		EPMDocumentMaster em;
		EPMDocumentMaster emCheck;
		EPMDocumentMasterIdentity emIdentity;
		
		if (oldNumber == null || newNumber == null) {
			throw new NullPointerException("Old Number & New Number are required");
		}
		
		em = findEPMDocumentMaster(oldNumber);	// Locate the EPMDocumentMaster
		emCheck = findEPMDocumentMaster(newNumber);	// Attempt to locate an EPMDocumentMaster under new number.
		
		// Check EPMDocumentMaster was found and no EPMDocumentMaster was found under new number - will not renumber if exists.
		if (em != null && emCheck == null) {
			try {
				emIdentity = (EPMDocumentMasterIdentity)em.getIdentificationObject();
				emIdentity.setNumber(newNumber);
				if (newName != null) {
					emIdentity.setName(newName);
				}
				IdentityHelper.service.changeIdentity(em, emIdentity);	// Change EPMDocumentMaster identity.
				PersistenceHelper.manager.refresh(em);
			
			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}
	}
	
	/**
	*	Returns void, finds a WTDocumentMaster based on an item number parameter, renumbers it to a new number 
	*	parameter and renames it to a new name parameter.
	*
	*		@param oldNumber the old number of the WTDocument.
	*		@param newNumber the new number of the WTDocument.
	*		@param newName the new name of the WTDocument.
	*		@return boolean
	*		@see wt.doc.WTDocumentMaster
	*/
	public static void updateWTDocumentIdentity(String oldNumber, String newNumber, String newName)
			throws WTException, NullPointerException
	{
		WTDocumentMaster dm;
		WTDocumentMaster dmCheck;
		WTDocumentMasterIdentity dmIdentity;
		
		if (oldNumber == null || newNumber == null) {
			throw new NullPointerException("Old Number & New Number are required");
		}
		
		// Check WTDocumentMaster was found and no WTDocumentMaster was found under new number - will not renumber if exists.
		dm = findWTDocumentMaster(oldNumber);	// Locate the WTDocumentMaster
		dmCheck = findWTDocumentMaster(newNumber);	// Attempt to locate an WTDocumentMaster under new number.
		
		if (dm != null && dmCheck == null) {
			try {
				dmIdentity = (WTDocumentMasterIdentity)dm.getIdentificationObject();
				dmIdentity.setNumber(newNumber);
				if (newName != null) {
					dmIdentity.setName(newName);
				}
				IdentityHelper.service.changeIdentity(dm, dmIdentity);	// Change WTDocumentMaster identity.
				PersistenceHelper.manager.refresh(dm);
			
			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}
	}
	
	/**
	*	Returns void, finds a WTPartMaster based on an item number parameter, renumbers it to a new number 
	*	parameter and renames it to a new name parameter.
	*
	*		@param oldNumber the old number of the WTPart.
	*		@param newNumber the new number of the WTPart.
	*		@param newName the new name of the WTPart.
	*		@see wt.part.WTPartMaster
	*/
	public static void updateWTPartIdentity(String oldNumber, String newNumber, String newName)
			throws WTException, NullPointerException
	{
		WTPartMaster pm;
		WTPartMaster pmCheck;
		WTPartMasterIdentity pmIdentity;
		
		if (oldNumber == null || newNumber == null) {
			throw new NullPointerException("Old Number & New Number are required");
		}
		
		// Check WTPartMaster was found and no WTPartMaster was found under new number - will not renumber if exists.
		pm = findWTPartMaster(oldNumber);	// Locate the WTPartMaster
		pmCheck = findWTPartMaster(newNumber);	// Attempt to locate an WTPartMaster under new number.
		
		if (pm != null && pmCheck == null) {
			try {
				pmIdentity = (WTPartMasterIdentity)pm.getIdentificationObject();
				pmIdentity.setNumber(newNumber);
				if (newName != null) {
					pmIdentity.setName(newName);
				}
				IdentityHelper.service.changeIdentity(pm, pmIdentity);	// Change WTPartMaster identity.
				PersistenceHelper.manager.refresh(pm);

			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
								
			}
		}
	}
	
	/**
	*	Returns void, renames all of the EPMDocuments in a QueryResult parameter so that they match their 
	*	corresponding WTPart name.
	*
	*		@param qr the QueryResult containing a number of objects - that may or may not be EPMDocuments.
	*		@return List<EPMDocument>
	*		@see wt.epm.EPMDocument
	*		@see wt.part.WTPart
	*/
	public static void syncPartEPMNames(QueryResult qr)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();

		Persistable per;
		EPMDocument epm;
		String epmNum;
		String partNum;
		WTPartMaster pm;
		String name;
		WTPartMasterIdentity pmIdentity;
		
		Sync:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			// Verify result object is an EPMDocument.
			if (per instanceof EPMDocument){
				// Retrieve EPMDocument number.
				epm = (EPMDocument)per;
				epmNum = epm.getNumber();
				partNum = epmNum;
				
				// Remove EPMDocument extension from Part number.
				if (epmNum.indexOf(".") > -1) {
					partNum = partNum.substring(0, partNum.length() - 4);
				}
				
				// Locate WTPartMaster.
				pm = null;
				try {
					pm = findWTPartMaster(partNum);
				} catch (WTException wte) {
					e.add(wte);
					continue Sync;
				}
				
				if (pm != null) {	// Check WTPartMaster is found.
					// Retrieve WTPartMaster name.
					name = null;
					try {
						pmIdentity = (WTPartMasterIdentity)pm.getIdentificationObject();
						name = pmIdentity.getName();
					
						// Rename EPMDocumentMaster.
						renameEPMDocument(epmNum, name);
						
					} catch (WTException wte) {
						e.add(wte);
						continue Sync;
					}
				}
			}
		}
		
		if (e.size() > 0) {
			throw new WTException(new StringBuilder(e.size())
				.append(" names failed to be synchronised")
				.toString());
		}
	}
	
	public static boolean isNumberValid(WTPart part, String reg)
	{	
		return GeneralUtils.regex(part.getNumber(), reg);
	}
}