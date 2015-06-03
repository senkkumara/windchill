package ext.hydratight.obj.iba;

import com.ptc.wvs.server.util.PublishUtils;
import com.ptc.wvs.server.util.WVSContentHelper;
import ext.hydratight.obj.iba.value.StringValueUtils;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.content.ContentRoleType;
import wt.epm.EPMDocument;
import wt.epm.navigator.CollectItem;
import wt.epm.navigator.EPMNavigateHelper;
import wt.epm.navigator.relationship.UIRelationships;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.part.WTPart;
import wt.representation.Representation;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.VersionIdentifier;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlException;

public class DrawingAttributeUtils
{
	static final private String DRAWING_URL_IBA = "Drawing_URL";
	static final private String DRAWING_NUMBER_IBA = "Drawing_Number";
	static final private String DRAWING_REVISION_IBA = "Drawing_Revision";
	static final private String PRE_PRODUCTION_STATE = "PREPRODUCTION";
	static final private String PRODUCTION_STATE = "PRODUCTION";
	static final private String PRODUCTION_REVISION = "A";
	static final private String DOC_TYPE = "DRAWING";
	static final private String EXT = ".drw";
	
	public static Map<String, String> getDrawingAttributeInfo(WTPart part)
			throws WTException
	{
		return getDrawingAttributeInfo(part, false);
	}

	public static Map<String, String> getDrawingAttributeInfo(WTPart part, boolean inc)
			throws WTException
	{
		Map<String, String> attrs = new HashMap<String, String>();
		
		WTSet epms = 
				EPMNavigateHelper.navigate(part, UIRelationships.newAssociatedCADDocs(), 
				CollectItem.OTHERSIDE).getResults(new WTHashSet());
		
		EPMDocument epm;
		String type;
		String partNumber;
		Representation rep;
		String num = "";
		String url = "";
		String rev = "";
		VersionIdentifier vi;
		
		Search:
		for (Object obj : epms) {
			epm = (EPMDocument)((ObjectReference)obj).getObject();
			type = epm.getDocType().toString();
			if (type.indexOf(DOC_TYPE) > 0) {
				// Drawing number must match Part number - ignore PE, INSP etc
				partNumber = part.getNumber();
				num = epm.getNumber();
				
				if (! (num.equals(new StringBuilder(partNumber)
					.append(EXT.toUpperCase())
					.toString()))) {
					continue Search;
				}
				
				// Retrieve "Drawing URL" value
				rep = PublishUtils.getRepresentation((Persistable)epm);
				
				if (rep != null) {
					url = WVSContentHelper.getDownloadURLForType(rep, ContentRoleType.SECONDARY);
				}
				
				// Retrieve "Drawing Number" value
				if (num.toLowerCase().indexOf(EXT) > -1) {
					num = num.substring(0, num.length() - 4);
				}
				
				//Retrieve "Drawing Revision" value
				try {
					vi = VersionControlHelper.getVersionIdentifier((Versioned)part);
					
					if (inc) {
						try {
							vi = VersionControlHelper.nextVersionId((Versioned)part);
						}
						catch (WTPropertyVetoException pve) {
							throw new WTException(pve);
						}
					}
					
					rev = vi.getValue();
					
				}
				catch (VersionControlException vc) {
					throw new WTException(vc);
				}
				
				// Create HashMap of IBA names and values
				Map<String, String> values = new HashMap<String, String>();
				attrs.put(DRAWING_URL_IBA, url);
				attrs.put(DRAWING_NUMBER_IBA, num);
				attrs.put(DRAWING_REVISION_IBA, rev);

			}
		}
		
		return attrs;
	}
	
	public static Map<ObjectReference, Map<String, String>> getDrawingAttributes(QueryResult qr)
			throws WTException
	{
		Map<ObjectReference, Map<String, String>> map = 
					new HashMap<ObjectReference, Map<String, String>>();
		
		List<String> attrs = Arrays.asList(new String[] { DRAWING_URL_IBA, DRAWING_NUMBER_IBA,
				DRAWING_REVISION_IBA });

		Persistable per;
		WTPart part;
		ObjectReference ref;
		Map<String, String> values;
		
		Get:
		while (qr.hasMoreElements()) {
			per = (Persistable)qr.nextElement();
			if (!(per instanceof WTPart)) {
				continue Get;
			}
			
			part = (WTPart)per;
			ref = ObjectReference.newObjectReference(per);
			values = StringValueUtils.getStringValues(part, attrs);

			ReplaceNulls:
			for (String attr : attrs) {
				if (values.get(attr) == null) {
					values.put(attr, "");
				}
			}
			
			map.put(ref, values);
		}
		
		return map;
	}
	
	private static void setDrawingAttributes(WTPart part, String rev, boolean inc)
			throws WTException
	{
		Map<String, String> attrs = getDrawingAttributeInfo(part, inc);
		
		if (attrs.size() > 0) {
			if (rev != null) {
				attrs.put(DRAWING_REVISION_IBA, rev);
			}
			StringValueUtils.setStringValues(part, attrs);
		}
	}
	
	public static void setDrawingAttributes(WTPart part)
			throws WTException
	{
		setDrawingAttributes(part, null);
	}
	
	public static void setDrawingAttributes(WTPart part, String rev)
			throws WTException
	{
		setDrawingAttributes(part, rev, false);
	}
	
	public static void setDrawingAttributes(WTPart part, boolean inc)
			throws WTException
	{
		setDrawingAttributes(part, null, true);
	}
	
	private static List<ObjectReference> setDrawingAttributes(QueryResult qr, String rev, boolean inc)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		Persistable per;
		
		Set:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			
			if (!(per instanceof WTPart)) {
				continue Set;
			}
			
			try {
				setDrawingAttributes((WTPart)per, rev, inc);
			
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference(per));
				wte.printStackTrace();
				continue Set;
			}
		}
		
		return f;
	}
	
	public static List<ObjectReference> setDrawingAttributes(QueryResult qr) 
			throws WTException
	{
		return setDrawingAttributes(qr, null, false);	
	}
	
	public static List<ObjectReference> setDrawingAttributes(QueryResult qr, String rev)
			throws WTException
	{
		return setDrawingAttributes(qr, rev, false);
	}
	
	public static List<ObjectReference> setDrawingAttributes(QueryResult qr, boolean inc)
			throws WTException
	{
		return setDrawingAttributes(qr, null, inc);
	}
	
	private static List<ObjectReference> setDrawingAttributes(List<ObjectReference> refs, String rev, boolean inc)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		WTObject obj;
		WTPart part;
		
		Set:
		for (ObjectReference ref : refs) {
			obj = (WTObject)ref.getObject();
			
			if (! (obj instanceof WTPart)) {
				continue Set;
			}
			
			try {
				setDrawingAttributes((WTPart)obj, rev, inc);
			
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference((Persistable)obj));
				wte.printStackTrace();
				continue Set;
			}
		}
		
		return f;
	}
	
	public static List<ObjectReference> setDrawingAttributes(List<ObjectReference> refs)
			throws WTException
	{
		return setDrawingAttributes(refs, null, false);
	}
	
	public static List<ObjectReference> setDrawingAttributes(List<ObjectReference> refs, String rev)
			throws WTException
	{
		return setDrawingAttributes(refs, rev, false);
	}
	
	public static List<ObjectReference> setDrawingAttributes(List<ObjectReference> refs, boolean inc)
			throws WTException
	{
		return setDrawingAttributes(refs, null, inc);
	}
	
	public static Map<ObjectReference, Map<String, String>> setDrawingAttributes(
				Map<ObjectReference, Map<String, String>> map)

	{
		Map<ObjectReference, Map<String, String>> f =
				new HashMap<ObjectReference, Map<String, String>>();
		
		WTObject obj = null;
		WTPart part = null;
		Map<String, String> attrs = null;
		
		Set:
		for (ObjectReference ref : map.keySet()) {
			obj = (WTObject)ref.getObject();
			if (! (obj instanceof WTPart)) {
				continue Set;
			}
			
			part = (WTPart)obj;			
			attrs = map.get(ref);
			
			try {
				StringValueUtils.setStringValues(part, attrs);
			
			}
			catch (WTException wte) {
				f.put(ref, attrs);
				wte.printStackTrace();
				continue Set;
			}

		}
		
		return f;
	}
	
	public static List<ObjectReference> setAnticipatedDrawingAttributes(QueryResult qr, String state)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		if (state.equals(PRE_PRODUCTION_STATE)) {
			f = setDrawingAttributes(qr, true);
		}
		
		if (state.equals(PRODUCTION_STATE)) {
			f = setDrawingAttributes(qr, PRODUCTION_REVISION);
		}
		
		return f;
	}
	
	public static List<ObjectReference> setAnticipatedDrawingAttributes(List<ObjectReference> refs, String state)
			throws WTException
	{
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		if (state.equals(PRE_PRODUCTION_STATE)) {
			f = setDrawingAttributes(refs, true);
		}
		
		if (state.equals(PRODUCTION_STATE)) {
			f = setDrawingAttributes(refs, PRODUCTION_REVISION);
		}
		
		return f;
	}
	
	public static Map<ObjectReference, Map<String, String>> resetDrawingAttributes(
				Map<ObjectReference, Map<String, String>> objs)
				
	{
		Map<ObjectReference, Map<String, String>> f =
				new HashMap<ObjectReference, Map<String, String>>();
		
		ObjectReference ref;
		WTObject obj;
		Map<String, String> values;
		String name;
		String value;
		Map<String, String> tmp;
		
		LoopObjects:
		for (Object o : objs.keySet()) {
			ref = (ObjectReference)o;
			obj = (WTObject)ref.getObject();
			values = (HashMap<String, String>)objs.get(o);
			
			LoopAttributes:
			for (Object iba : values.keySet()) {
				name = (String)iba;
				value = values.get(iba);
				
				try {
					StringValueUtils.setStringValue(obj, name, value);
				
				}
				catch (WTException wte) {
					wte.printStackTrace();
					
					if (! f.containsKey(ref)) {
						tmp = new HashMap<String, String>();
						tmp.put(name, value);
						f.put(ref, tmp);
						
					}
					
					tmp = f.get(ref);
					tmp.put(name, value);
					
					continue LoopAttributes;
				}
			}
		}
		
		return f;
	}
}