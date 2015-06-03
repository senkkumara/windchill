package ext.hydratight.obj.type;

import ext.hydratight.esi.HistoryUtils;
import ext.hydratight.obj.CollectUtils;
import ext.hydratight.obj.IdentityUtils;
import ext.hydratight.obj.VersionUtils;
import ext.hydratight.obj.build.ChangeBuilder;
import ext.hydratight.obj.iba.AttributeUtils;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import wt.change2.Changeable2;
import wt.change2.ChangeHelper2;
import wt.change2.RelevantRequestData2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;

import java.rmi.RemoteException;
import wt.change2.ChangeException2;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * This class provides help with Change object Workflows.
 *
 */
public class ChangeUtils
{

	static final private String DESIGN_STATE = "DESIGN";
	static final private String PRE_PRODUCTION_STATE = "PREPRODUCTION";
	static final private String PRE_PRODUCTION_CHANGE_STATE = "PREPRODUCTIONCHANGE";
	static final private String PRODUCTION_STATE = "PRODUCTION";
	static final private String PRODUCTION_CHANGE_STATE = "PRODUCTIONCHANGE";
	static final private String UNDER_REVIEW_STATE = "UNDERREVIEW";
	static final private String REWORK_STATE = "REWORK";
	static final private String OBSOLETE_STATE = "OBSOLETE";
	static final private String PROPOSAL_STATE = "PROPOSAL";
	static final private String BEFORE_KEY = "BEFORE";
	static final private String AFTER_KEY = "AFTER";
	
	public static List<ObjectReference> validateRevisions(WTChangeOrder2 cn)
			throws WTException
	{
		List<ObjectReference> nr = new ArrayList<ObjectReference>();
		QueryResult qr = ChangeHelper2.service.getChangeablesBefore(cn);
		
		Affected:
		while (qr.hasMoreElements()) {
			WTObject obj = (WTObject)qr.nextElement();
			String id = null;
			if (obj instanceof Versioned) {
				if (! isRevised((Versioned)obj, cn)) {
					nr.add(ObjectReference.newObjectReference((Persistable)obj));
					continue Affected;
				}
			}			
		}
		
		return nr;
	}
	
	private static boolean isRevised(Versioned v, WTChangeOrder2 cn)
			throws WTException
	{		
		String aff;
		String res;
		String state = ((LifeCycleManaged)v).getLifeCycleState().toString();
		Class<?> cls;

		try {
			if (v instanceof WTPart) {
				cls = Class.forName("wt.part.WTPart");
				aff = ((WTPart)v).getNumber();
			}
			else if (v instanceof EPMDocument) {
				cls = Class.forName("wt.doc.WTDocument");
				aff = ((EPMDocument)v).getNumber();
			}
			else if (v instanceof WTDocument) {
				cls = Class.forName("wt.part.WTPart");
				aff = ((WTDocument)v).getNumber();
			}
			else {
				return false;
			}

			if (state.equals(OBSOLETE_STATE)) {
				return false;
			}
		}
		catch (ClassNotFoundException cnf) {
			throw new WTException(cnf);
		}

		QueryResult resulting = ChangeHelper2.service.getChangeablesAfter(cn);
		Persistable per;
		
		Resulting:
		while (resulting.hasMoreElements()) {
			per = (Persistable)resulting.nextElement();
			res = null;
			if (per.getClass().isInstance(cls)) {
				if (per instanceof WTPart) {
					aff = ((WTPart)per).getNumber();
				}
				else if (per instanceof EPMDocument) {
					aff = ((EPMDocument)per).getNumber();
				}
				else if (per instanceof WTDocument) {
					aff = ((WTDocument)per).getNumber();
				}
				else {
					continue Resulting;
				}

				if (aff.equals(res)) {
					return true;
				}
			}
		}
		
		return false;
	}


	public static List<ObjectReference> validateStates(WTChangeRequest2 cr, String target)
			throws WTException
	{
		QueryResult qr = ChangeHelper2.service.getChangeables(cr);
		List<String> states = new ArrayList<String>();
		
		if (target.equals(PRE_PRODUCTION_STATE)) {
			states.add(DESIGN_STATE);
			
		}
		else if (target.equals(PRODUCTION_STATE)) {
			states.add(DESIGN_STATE);
			states.add(PRE_PRODUCTION_STATE);
			 
		}
		else if (target.equals(OBSOLETE_STATE)) {
			states.add(PRODUCTION_STATE);
			states.add(PRODUCTION_CHANGE_STATE);
			
		}
		else {
			throw new WTException("Invalid target state");
		}
		
		return validateStates(qr, states, true);
	}
	
	public static Map<String, List<ObjectReference>> validateStates(WTChangeOrder2 cn)
			throws WTException
	{
		Map<String, List<ObjectReference>> invalid = new HashMap<String, List<ObjectReference>>();
		List<ObjectReference> before = validateStatesBefore(cn);
		List<ObjectReference> after = validateStatesAfter(cn);
		
		if (before.size() > 0) {
			invalid.put(BEFORE_KEY, before);
		}
		
		if (after.size() > 0) {
			invalid.put(AFTER_KEY, after);
		}
		
		return invalid;
	}
	
	private static List<ObjectReference> validateStatesBefore(WTChangeOrder2 cn)
			throws WTException
	{
		QueryResult qr = ChangeHelper2.service.getChangeablesBefore(cn);
		List<String> states = Arrays.asList(new String[]{
			PRE_PRODUCTION_CHANGE_STATE,
			PRODUCTION_CHANGE_STATE
		});
		
		return validateStates(qr, states, true);
	}
	
	private static List<ObjectReference> validateStatesAfter(WTChangeOrder2 cn)
			throws WTException
	{
		QueryResult qr = ChangeHelper2.service.getChangeablesAfter(cn);
		List<String> states = Arrays.asList(new String[]{
			PRE_PRODUCTION_STATE,
			PRE_PRODUCTION_STATE,
			UNDER_REVIEW_STATE,
			REWORK_STATE,
			OBSOLETE_STATE,
			PROPOSAL_STATE
		});
		
		return validateStates(qr, states, true);
	}
	
	private static List<ObjectReference> validateStates(QueryResult qr, List<String> states,
				boolean match)
				
			throws WTException
	{
		List<ObjectReference> invalid = new ArrayList<ObjectReference>();
		
		WTObject obj;
		LifeCycleManaged lm;
		String state;
		
		Find:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			
			if (obj instanceof LifeCycleManaged) {
				lm = (LifeCycleManaged)obj;
				state = lm.getLifeCycleState().toString();
				
				Check:
				for (String st : states) {
					if (match && st.equals(state)) {
						if (obj instanceof WTPart) {
							invalid.add(ObjectReference.newObjectReference((Persistable)obj));
						}
						else if (obj instanceof EPMDocument) {
							invalid.add(ObjectReference.newObjectReference((Persistable)obj));
						}
						else if (obj instanceof WTDocument) {
							invalid.add(ObjectReference.newObjectReference((Persistable)obj));
						}
						
						continue Find;
					}
				}
			}
		}
		
		return invalid;
	}
	
	public static List<ObjectReference> reactivate(List<ObjectReference> refs)
			throws WTException
	{
	
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		List<WTObject> objs = new ArrayList<WTObject>();
		WTObject obj;
		QueryResult cas;
		WTChangeActivity2 ca;
	
		Set:
		for (ObjectReference ref : refs) {
			obj = (WTObject)ref.getObject();
			
			try {
				obj = reactivate(obj);
			
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference((Persistable)obj));
				wte.printStackTrace();
				continue Set;
			}
			
			if (obj != null) {
				objs.add(obj);
			}
			
		}
		
		return f;
	}
	
	public static List<ObjectReference> reactivate(WTChangeOrder2 cn)
			throws WTException
	{
		QueryResult qr = ChangeHelper2.service.getChangeablesBefore(cn);
		List<WTObject> objs = new ArrayList<WTObject>();
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		WTObject obj;
		Collection<WTObject> col;
		
		Set:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();

			try {
				obj = reactivate(obj);
			
			}
			catch (WTException wte) {
				f.add(ObjectReference.newObjectReference((Persistable)obj));
				wte.printStackTrace();
				continue Set;
			}
			
			if (obj != null) {
				objs.add(obj);
			}
			
		}
		
		col = VersionUtils.revise(objs);
		cas = ChangeHelper2.service.getChangeActivities(cn);
		ca = (WTChangeActivity2)cas.nextElement();

		ChangeConstr.addResultingToChangeActivity(ca, new Vector<WTObject>(col));
		
		return f;
	}
	
	public static WTObject reactivate(WTObject obj)
			throws WTException
	{
		LifeCycleManaged lm;
		String state;
		QueryResult iter;
		Collection act;

		if (! (obj instanceof LifeCycleManaged)) {
			return null;
		}
			
		lm = (LifeCycleManaged)obj;
		state = lm.getLifeCycleState().toString();
			
		if (! state.equals(OBSOLETE_STATE)) {
			return null;		// Doesn't need reactivating!
		}

		if (! VersionUtils.isVersionNumeric(obj)) {
			LifeCycleHelper.service.setLifeCycleState(lm, State.toState(PRODUCTION_CHANGE_STATE));
			return obj;
		}
			
		iter = VersionControlHelper.service.allIterationsFrom((Iterated)obj);
		while (iter.hasMoreElements()) {
			act = HistoryUtils.getActivities((Persistable)obj);
			if (act.size() > 0) {
				// Numeric revision, released
				LifeCycleHelper.service.setLifeCycleState(lm, State.toState(PRE_PRODUCTION_CHANGE_STATE));
				return obj;
			}
		}
			
		// Numeric revision, not released
		LifeCycleHelper.service.setLifeCycleState(lm, State.toState(DESIGN_STATE));
		
		return obj;
	}
	
	public static Map<ObjectReference, Map<ObjectReference, String>> collect(WTChangeRequest2 cr,
				String state)
				
			throws WTException
	{
		QueryResult qr = ChangeHelper2.service.getChangeables(cr);
		
		List<String> collectStates = Arrays.asList(new String[] { DESIGN_STATE, PROPOSAL_STATE });
		List<String> invalidStates = Arrays.asList(new String[] { OBSOLETE_STATE });
		List<String> waitStates = Arrays.asList(new String[] { PRE_PRODUCTION_CHANGE_STATE, 
					PRODUCTION_CHANGE_STATE, UNDER_REVIEW_STATE, REWORK_STATE });
					
		//collectStates.addAll(invalidStates);
		//collectStates.addAll(waitStates);
					
		Map<ObjectReference, Map<ObjectReference, String>> col = CollectUtils.collect(qr, collectStates);
		
		Set keys = col.keySet();
		System.out.println(keys.size());
		for (Object o : keys) {
			System.out.println((ObjectReference)o);
		}

		return col;
	}
	
	
	public static Set<ObjectReference> remove(WTChangeRequest2 cr, Set<ObjectReference> refs, List<String> states,
				boolean match)
				
	{
		Set<ObjectReference> f = new HashSet<ObjectReference>();	// Returns objects that failed
		
		Remove:
		for (ObjectReference ref : refs) {
			try {
				remove(cr, ref, states, match);
				
			}
			catch (WTException wte){	// Catches ChangeException2 & WTException
				f.add(ref);
				wte.printStackTrace();
				continue Remove;
				
			}
		}
		
		return f;
	}
	
	private static void remove(WTChangeRequest2 cr, ObjectReference ref, List<String> states, boolean match)
			throws WTException
	{
		WTObject obj = (WTObject)ref.getObject();
		String state;

		if (! ((obj instanceof LifeCycleManaged) && (obj instanceof Changeable2))) {
			return;	// Would fail casting
		}
			
		state = ((LifeCycleManaged)obj).getLifeCycleState().toString();
		if (! (match && states.contains(state))) {
			return;	// Object does not need to be removed
		}
		
		ChangeHelper2.service.unattachChangeable((Changeable2)obj, cr, RelevantRequestData2.class,
				RelevantRequestData2.CHANGEABLE2_ROLE);
	}
	
}