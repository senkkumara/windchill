package ext.hydratight.obj;

import ext.hydratight.obj.IdentityUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.vc.wip.Workable;
import wt.vc.wip.WorkInProgressHelper;

import wt.util.WTException;

/**
 *	This class provides help with life cycles during Workflows.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class LifeCycleUtils
{
	
	static final private String DESIGN_STATE = "DESIGN";
	static final private String PRE_PRODUCTION_CHANGE_STATE = "PREPRODUCTIONCHANGE";
	static final private String PRE_PRODUCTION_STATE = "PREPRODUCTION";
	static final private String PRODUCTION_CHANGE_STATE = "PRODUCTIONCHANGE";
	static final private String PRODUCTION_STATE = "PRODUCTION";
	static final private String CANCELLED_STATE = "CANCELLED";
	static final private String OBSOLETE_STATE = "OBSOLETE";
	static final private String LOCK_STATE = "UNDERREVIEW";
	static final private List<String> STATES = Arrays.asList(new String[]{
		DESIGN_STATE,
		PRE_PRODUCTION_CHANGE_STATE,
		PRE_PRODUCTION_STATE,
		PRODUCTION_CHANGE_STATE,
		PRODUCTION_STATE,
		CANCELLED_STATE,
		OBSOLETE_STATE,
		LOCK_STATE
	});
	
	/**
	 *	Returns the State of a parameter WTObject.<br />
	 *	<br />
	 *	Returns null if the parameter object is not "LifeCycleManaged".
	 *
	 *		@param obj the WTObject for which the state is to be retrieved from.
	 *		@return State
	 */
	public static State getState(WTObject obj)
	{
		State state = null;
		LifeCycleManaged lm;
		
		if (obj instanceof LifeCycleManaged) {
			state = ((LifeCycleManaged)obj).getLifeCycleState();
		}
		
		return state;
	}
	
	/**
	 *	Returns a map of WTObjects and their state retrieved from a QueryResult parameter.
	 *
	 *		@param qr the QueryResult containing the object(s)
	 *		@return Map<WTObject, State>	
	 */
	public static Map<WTObject, State> getStates(QueryResult qr)
	{
		Map<WTObject, State> states = new HashMap<WTObject, State>();
		
		WTObject obj;
		State state;
		
		Get:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			
			if (obj instanceof LifeCycleManaged) {
				states.put(obj, getState(obj));
			}
		}
		
		return states;
	}
	
	/**
	 *	Returns a String containing the state of a WTObject argument.
	 *	<br />
	 *	Returns null if the parameter object is not "LifeCycleManaged".
	 *
	 *		@param obj the WTObject for which the state is to be retrieved from.
	 *		@return String
	 */
	public static String getStateStr(WTObject obj)
	{
		String stateStr = null;
		State state = getState(obj);
		
		if (state != null) {		// Will be null if object is not lifecycle managed.
			stateStr = state.toString();
		}

		return stateStr;
	}
	
	/**
	 *	Returns a String containing the internal state name of a WTObject argument.
	 *
	 *		@param obj the String containing the user-facing name of a state.
	 *		@return String
	 */
	public static String getInternalStateStr(WTObject obj)
	{
		String state = getStateStr(obj);
		return getInternalStateStr(state);	// Remove spaces & capitalise.
	}
	
	/**
	 *	Returns a String containing the internal state name of a String argument.
	 *
	 *		@param state the String containing the user-facing name of a state.
	 *		@return String
	 */
	public static String getInternalStateStr(String state)
	{
		return state.replaceAll("\\s","").replaceAll("-","").toUpperCase();	// Remove spaces & capitalise.
	}
	
	/**
	 *	Returns a List of WTObjects that are in the argument state.
	 *
	 *		@param qr the QueryResult containing the WTObjects.
	 *		@param state a String containing the internal name of the state.
	 *		@return List<WTObject>
	 */
	public static List<WTObject> getObjectsByState(QueryResult qr, String state)
	{
		return getObjectsByState(qr, state, true);
	}
	
	/**
	 *	Returns a List of WTObjects that depending on a boolean argument are/are not in the argument state.
	 *
	 *		@param qr the QueryResult containing the WTObjects.
	 *		@param state a String containing the internal name of the state.
	 *		@param match a Boolean regarding whether you want matches (true) or mis-matches (false)
	 *		@return List<WTObject>
	 */
	public static List<WTObject> getObjectsByState(QueryResult qr, String state, boolean match)
	{
		List<WTObject> objs = new ArrayList<WTObject>();
		
		Get:
		while (qr.hasMoreElements()) {
			Persistable per = (Persistable)qr.nextElement();
			
			if (per instanceof LifeCycleManaged) {
				WTObject obj = (WTObject)per;
				String objState = getStateStr(obj);
				
				if (objState.equals(state)) {			// State matches parameter State.
					if (match) {						// Want to retrieve matches.
						objs.add(obj);
					}
				}
				else {								// State doesn't match parameter State.				
					if (! match) {						// Want to retrieve miss-matches.
						objs.add(obj);
					}
				}
			}
		}
		
		return objs;
	}
	
	/**
	 *	Returns a Map of WTObjects and states that depending that failed to be set to the state specified.
	 *
	 *		@param qr the QueryResult containing the WTObjects.
	 *		@param state a String containing the internal name of the state.
	 *		@return Map<ObjectReference, String>
	 */
	public static Map<ObjectReference, String> setStates(QueryResult qr, String nState)
			throws WTException
	{
		Map<ObjectReference, String> f = new HashMap<ObjectReference, String>();
		
		State state = State.toState(nState);
		WTObject obj;
		LifeCycleManaged lm;
		String oState;
		
		SetState:
		while (qr.hasMoreElements()){
			obj = (WTObject)qr.nextElement();

			if (! (obj instanceof LifeCycleManaged)) {
				continue SetState;
			}

			lm = (LifeCycleManaged)obj;
			oState = lm.getLifeCycleState().toString();
			
			if (nState != oState) {		// Only set state if not already in state.
				try {
					LifeCycleHelper.service.setLifeCycleState(lm, state);		// Set object's state.
					
				}
				catch (WTException wte) {
					f.put(ObjectReference.newObjectReference((Persistable)obj), nState);
					wte.printStackTrace();
					continue SetState;	// Continue setting the state of the remaining objects.
				}
			}
		}
		
		return f;
	}
	
	/**
	 *	Sets the state of the Object referenced by a parameter ObjectReference to a parameter State.
	 *
	 *		@param ref the ObjectReference of the Object
	 *		@param stateStr the name of the state to set the object to
	 *		@throws WTException
	 */
	public static void setState(ObjectReference ref, String stateStr)
			throws WTException
	{
		WTObject obj = (WTObject)ref.getObject();
		LifeCycleManaged lm;
		State state;
		
		if (!(obj instanceof LifeCycleManaged)) {
			return;
		}
		
		state = State.toState(stateStr);
		LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged)obj, state);
	}
	
	public static Map<ObjectReference, String> setStates(Map<ObjectReference, String> objs)
			throws WTException
	{
		Map<ObjectReference, String> f = new HashMap<ObjectReference, String>();
		
		ObjectReference ref;
		String stateStr;
		
		Set:
		for (Object o : objs.keySet()) {
			ref = (ObjectReference)o;
			stateStr = (String)objs.get(o);
		
			try {
				setState(ref, stateStr);
				
			}
			catch (WTException wte) {
				f.put(ref, stateStr);
				wte.printStackTrace();
				continue Set;
				
			}
			
		}

		return f;
	}
	
	public static Map<ObjectReference, String> lock(QueryResult qr)
			throws WTException
	{
		Map<ObjectReference, String> map = new HashMap<ObjectReference, String>();
		WTObject obj = null;
		List<ObjectReference> chk = new ArrayList<ObjectReference>();
		LifeCycleManaged lm;
		String stateStr;
		ObjectReference ref;
		
		Check:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			if (WorkInProgressHelper.isCheckedOut((Workable) obj) || WorkInProgressHelper.isWorkingCopy((Workable) obj)) {
				chk.add(ObjectReference.newObjectReference((Persistable)obj));
			}
		}
		
		if (chk.size() > 0) {
			throw new WTException(new StringBuilder("The following object(s) are checked out:\n")
				.append(IdentityUtils.getIdentities(chk))
				.toString());
		}
		
		qr.reset();
		
		Lock:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			
			if (! (obj instanceof LifeCycleManaged)) {
				continue Lock;
			}
			
			lm = (LifeCycleManaged)obj;
			stateStr = lm.getState().toString();
			ref = ObjectReference.newObjectReference((Persistable)obj);
			map.put(ref, stateStr);
			LifeCycleHelper.service.setLifeCycleState(lm, State.toState(LOCK_STATE));
			
		}
		
		return map;
	}
	
	public static Map<ObjectReference, String> changeState(QueryResult qr, String target)
			throws WTException
	{
		
		Map<ObjectReference, String> f = new HashMap<ObjectReference, String>();
		String state = null;

		target = LifeCycleUtils.getInternalStateStr(target);
		
		if (target.equals(PRODUCTION_STATE)) state = PRODUCTION_CHANGE_STATE;
		if (target.equals(PRE_PRODUCTION_STATE)) state = PRE_PRODUCTION_CHANGE_STATE;
		if (target.equals(OBSOLETE_STATE)) state = target;
		
		if (state != null) {
			f = LifeCycleUtils.setStates(qr, state);
		}
		
		return f;
	}
	
	public static Map<ObjectReference, String> revertRemovedObjectState(
				Map<ObjectReference, String> orig, QueryResult qr)
				
			throws WTException
	{
		Map<ObjectReference, String> f = new HashMap<ObjectReference, String>();
		ObjectReference ref;
		WTObject obj;
		WTObject tmp;
		String stateStr;
		State state;
		
		Set:
		for (Object o : orig.keySet()) {
			ref = (ObjectReference)o;
			obj = (WTObject)ref.getObject();
			
			Find:
			while (qr.hasMoreElements()) {
				tmp = (WTObject)qr.nextElement();
				if (obj == tmp) {
					continue Set;
				}
			}
			
			qr.reset();
					
			if (!(obj instanceof LifeCycleManaged)) {
				continue Set;
			}
			
			stateStr = (String)orig.get(o);
			state = State.toState(stateStr);
			
			try {
				LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged)obj, state);
				
			}
			catch (WTException wte) {
				f.put(ref, stateStr);
				wte.printStackTrace();
				continue Set;
				
			}
			
		}
		
		return f;
	}
	
}