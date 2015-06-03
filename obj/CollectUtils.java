package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import wt.epm.EPMDocument;
import wt.epm.navigator.CollectItem;
import wt.epm.navigator.EPMNavigateHelper;
import wt.epm.navigator.relationship.UIRelationships;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.lifecycle.LifeCycleManaged;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;

import wt.util.WTException;

public class CollectUtils
{
	static final private String CAD_ASSEMBLY_TYPE = "CADASSEMBLY";
	static final private String CAD_DRAWING_TYPE = "CADDRAWING";
	static final private String EPM_TO_EPM_LINK_TYPE = "EPM to EPM";		// EPM uses
	static final private String EPM_TO_PART_LINK_TYPE = "EPM to Part";		// EPM describes
	static final private String PART_TO_PART_LINK_TYPE = "Part to Part";	// Part uses (BOM)
	static final private String PART_TO_EPM_LINK_TYPE = "Part to EPM";		// Related part
	static private Integer limit = 200;
	static private Boolean match = true;
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr)
			throws WTException
	{
		return collect(qr, null, true, null);
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr,
				boolean recursive)
				
			throws WTException
	{
		return collect(qr, null, recursive, null);
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr,
				List<String> states, boolean recursive)
				
			throws WTException
	{
		return collect(qr, states, recursive, null);
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr,
				List<String> states)
				
			throws WTException
	{
		return collect(qr, states, true, null);
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr,
				List<String> states, Integer localLimit)
			
			throws WTException
	{
		return collect(qr, states, true, localLimit);
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(QueryResult qr,
				List<String> states, boolean recursive, Integer localLimit)
				
			throws WTException
	{
		Map<ObjectReference, Map<ObjectReference, String>> refs =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
		
		WTObject obj;
		WTPart part;
		EPMDocument epm;
		ObjectReference ref;
		WTCollection parents;
		WTCollection parts = new WTArrayList();
		Map<ObjectReference, Map<ObjectReference, String>> tmp;
		List<Persistable> ls = convert(qr);

		if (localLimit != null) {
			limit = localLimit;
		}
		
		Collect:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
						
			if (obj instanceof WTPart) {
				part = (WTPart)obj;
				parts.add(part);
				tmp = collectRelatedEPM(part, ls, null, states);
				refs = merge(refs, tmp);
				continue Collect;
			}
			
			if (obj instanceof EPMDocument) {
				epm = (EPMDocument)obj;
				tmp = collectRelatedParts(epm, ls, null, states);
				refs = merge(refs, tmp);
				tmp = collectEPMStructure(epm, ls, null, states);
				refs = merge(refs, tmp);
				continue Collect;
			}
		}
		
		parents = WTPartHelper.service.filterPartsWithChildren(parts);

		Collect:
		for (Iterator j = parents.referenceIterator(); j.hasNext();) {
			ref = (ObjectReference) j.next();
			part = (WTPart) ref.getObject();
			tmp = collectPartStructure(part, ls, null, states);
			refs = merge(refs, tmp);
		}
		
		if (refs.size() > 0 && recursive) {
			if (refs.size() < limit) {
				tmp = collect(refs, ls, states, limit);
				refs = merge(refs, tmp);
			}
		}
		
		return refs;
	}
	
	private static Map<ObjectReference, Map<ObjectReference, String>> collect(
				Map<ObjectReference, Map<ObjectReference, String>> refs, List<Persistable> ls, List<String> states,
				Integer localLimit)
				
			throws WTException
	{			
		Map<ObjectReference, Map<ObjectReference, String>> col =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
				
		Map<ObjectReference, Map<ObjectReference, String>> tmp =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
				
		ObjectReference ref;
		WTObject obj;
		WTPart part;
		EPMDocument epm;
		WTCollection parts = new WTArrayList();
		WTCollection parents;
		
		if (localLimit != null) {
			limit = localLimit;
		}
		
		Collect:
		for (Entry<ObjectReference, Map<ObjectReference, String>> entry : refs.entrySet()) {
			ref = entry.getKey();
			obj = (WTObject)ref.getObject();
			if (obj instanceof WTPart) {
				part = (WTPart)obj;
				parts.add(part);
				tmp = collectRelatedEPM(part, ls, null, states);
				col = merge(col, tmp);
				continue Collect;
			}
			
			if (obj instanceof EPMDocument) {
				epm = (EPMDocument)obj;
				col = merge(col, tmp);
				tmp = collectEPMStructure(epm, ls, null, states);
				col = merge(col, tmp);
				continue Collect;
			}
		}
		
		parents = WTPartHelper.service.filterPartsWithChildren(parts);
		
		Collect:
		for (Iterator j = parents.referenceIterator(); j.hasNext();) {
			ref = (ObjectReference) j.next();
			part = (WTPart) ref.getObject();
			tmp = collectPartStructure(part, ls, null, states);
			col = merge(col, tmp);
		}
		
		if (col.size() > 0) {
			if ((col.size() + refs.size()) < limit) {
				tmp = collect(col, ls, states, localLimit);
				col = merge(col, tmp);
			}
		}
				
		return merge(refs, col);
	}
	
	private static List<Persistable> convert(QueryResult qr)
	{
		List<Persistable> ls = new ArrayList<Persistable>();

		GetObjects:
		while (qr.hasMoreElements()) {
			ls.add((Persistable)qr.nextElement());
		}
		qr.reset();
		
		return ls;
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collectPartStructure(WTPart part,
				List<Persistable> ls, Map<ObjectReference, Map<ObjectReference, String>> refs, List<String> states)
				
			throws WTException
	{
		Map<ObjectReference, Map<ObjectReference, String>> col =
				new HashMap<ObjectReference, Map<ObjectReference,String>>();
		
		ConfigSpec cs = new LatestConfigSpec();
		QueryResult uses = WTPartHelper.service.getUsesWTParts(part , cs);
		Persistable[] per;
		WTPart child;
		ObjectReference ref;
		ObjectReference ref2 = ObjectReference.newObjectReference((Persistable)part);
		String state;
		
		Collect:
		while (uses.hasMoreElements()) {
			per = (Persistable[])uses.nextElement();
			child = (WTPart) per[1];
			ref = ObjectReference.newObjectReference((Persistable)child);
			if (states == null) {
				col = add(ref, ref2, PART_TO_PART_LINK_TYPE, col);
				continue Collect;
			}
			state = ((LifeCycleManaged)child).getLifeCycleState().toString();
			if (states.contains(state)) {
				if (! present(ref, ref2, PART_TO_PART_LINK_TYPE, ls, refs)) {
					col = add(ref, ref2, PART_TO_PART_LINK_TYPE, col);
				}
			}
			
		}
		
		return col;
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collectEPMStructure(EPMDocument epm,
				List<Persistable> ls, Map<ObjectReference, Map<ObjectReference, String>> refs, List<String> states)
			
			throws WTException
	{
		Map<ObjectReference, Map<ObjectReference, String>> col =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
		
		String type = epm.getDocType().toString();
		QueryResult rels = null;
		Persistable[] p;
		EPMDocument rel;
		String state;
		ObjectReference ref;
		ObjectReference ref2;

		if (type.equals(CAD_ASSEMBLY_TYPE)) {
			rels = EPMStructureHelper.service.navigateReferencesToIteration(epm, null, false, new LatestConfigSpec());
		}
		
		if (type.equals(CAD_DRAWING_TYPE)) {
			rels = EPMStructureHelper.service.navigateUsesToIteration(epm, null, false, new LatestConfigSpec());
		}
		
		if (rels != null) {
			ref2 = ObjectReference.newObjectReference((Persistable)epm);
			
			Collect:
			while (rels.hasMoreElements()) {
				p = (Persistable[])rels.nextElement();
				rel = (EPMDocument) p[1];
				ref = ObjectReference.newObjectReference((Persistable)rel);
				if (states == null) {
					col = add(ref, ref2, EPM_TO_EPM_LINK_TYPE, col);
					continue Collect;
				}
				state = ((LifeCycleManaged)rel).getLifeCycleState().toString();
				if (states.contains(state)) {
					ref = ObjectReference.newObjectReference((Persistable)rel);
					if (! present(ref, ref2, EPM_TO_EPM_LINK_TYPE, ls, refs)) {
						col = add(ref, ref2, EPM_TO_EPM_LINK_TYPE, col);
					}
				}
			}
		}
		
		return col;
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collectRelatedEPM(WTPart part,
				List<Persistable> ls, Map<ObjectReference, Map<ObjectReference, String>> refs, List<String> states)
			
			throws WTException
	{
		Map<ObjectReference, Map<ObjectReference, String>> col =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
		
		WTSet epms = 
				EPMNavigateHelper.navigate(part, UIRelationships.newAssociatedCADDocs(), 
				CollectItem.OTHERSIDE).getResults(new WTHashSet());
		
		EPMDocument epm;
		ObjectReference ref;
		ObjectReference ref2 = ObjectReference.newObjectReference((Persistable)part);
		String type;
		String state;
		
		Collect:
		for (Object o : epms) {
			epm = (EPMDocument)((ObjectReference)o).getObject();
			ref = ObjectReference.newObjectReference((Persistable)epm);
			type = epm.getDocType().toString();
			if (states == null) {
				col = add(ref, ref2, EPM_TO_PART_LINK_TYPE, col);
				continue Collect;
			}
			state = ((LifeCycleManaged)epm).getLifeCycleState().toString();
			if (states.contains(state)) {
				ref = ObjectReference.newObjectReference((Persistable)epm);
				if (! present(ref, ref2, EPM_TO_PART_LINK_TYPE, ls, refs)) {
					col = add(ref, ref2, EPM_TO_PART_LINK_TYPE, col);
				}
			}

		}
		
		return col;
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collectRelatedParts(EPMDocument epm,
				List<Persistable> ls, Map<ObjectReference, Map<ObjectReference, String>> refs, List<String> states)
			
			throws WTException
	{
		Map<ObjectReference, Map<ObjectReference, String>> col =
				new HashMap<ObjectReference, Map<ObjectReference, String>>();
		
		throw new WTException("Not implemented!");
		//refs = merge(refs, col);
		
		//return refs;
	}
	
	private static boolean present(ObjectReference ref, ObjectReference ref2, String linkType, List<Persistable> ls,
				Map<ObjectReference, Map<ObjectReference, String>> refs)
				
			throws WTException
	{
		boolean found = false;
		Map<ObjectReference, String> links;
		String type;
		ObjectReference tmp;
		
		if (refs != null) {
			links = new HashMap<ObjectReference, String>();
			if (refs.containsKey(ref)) {
				links = refs.get(ref);
				type = null;
				if (links.containsKey(ref2)) {
					type = links.get(ref2);
					found = type.equals(linkType);
				}
			}

			if (found) {
				return found;
			}
		}
		
		Search:
		for (Persistable per : ls) {
			tmp = ObjectReference.newObjectReference(per);
			
			if (tmp == ref) {
				found = true;
				break Search;
			}
		}
		
		return found;
	}
	
	private static Map<ObjectReference, Map<ObjectReference, String>> merge (
				Map<ObjectReference, Map<ObjectReference, String>> map1,
				Map<ObjectReference, Map<ObjectReference, String>> map2)
				
	{
		Map<ObjectReference, String> links = new HashMap<ObjectReference, String>();
		Map<ObjectReference, String> links2;
		ObjectReference ref;
		ObjectReference ref2;
		String linkTypes2;

		AddDependent:
		for (Entry<ObjectReference, Map<ObjectReference, String>> entry : map2.entrySet()) {
			ref = entry.getKey();
			links2 = entry.getValue();
			
			if (! map1.containsKey(ref)) {
				map1.put(ref, links2);
				continue AddDependent;
			}
			
			links = map1.get(ref);
			
			AddLink:
			for (Entry<ObjectReference, String> link : links2.entrySet()) {
				ref2 = link.getKey();
				linkTypes2 = link.getValue();
				
				if (! links.containsKey(ref2)) {
					links.put(ref2, linkTypes2);
					continue AddLink;
				}
				
			}
			
		}
		
		return map1;
	}
	
	private static Map<ObjectReference, Map<ObjectReference, String>> add(ObjectReference ref,
				ObjectReference ref2, String linkType, Map<ObjectReference, Map<ObjectReference, String>> map)
	
	{
		Map<ObjectReference, String> links = new HashMap<ObjectReference, String>();
		if (! map.containsKey(ref)) {
			links.put(ref2, linkType);
			map.put(ref, links);
			return map;
		}
		
		links = map.get(ref);
		
		if (! links.containsKey(ref2)) {
			links.put(ref2, linkType);
		}
		
		return map;
	}
	
}