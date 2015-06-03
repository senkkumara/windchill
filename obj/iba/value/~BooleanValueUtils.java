package ext.hydratight.iba.value;

import ext.hydratight.iba.value.ValueUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.BooleanValueDefaultView;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.service.IBAValueHelper;
import wt.part.WTPart;

import java.rmi.RemoteException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class BooleanValueUtils
{

//////////////////////////////////////////// GET ////////////////////////////////////////////
	
	public static Boolean getBooleanValue(IBAHolder hdr, String attr, boolean refresh)
			throws WTException
	{
		Boolean value = null;
		try {
			DefaultAttributeContainer cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
			if (PersistenceHelper.isPersistent(hdr) && refresh) {
				hdr = IBAValueHelper.service.refreshAttributeContainer(hdr, null, null, null);
				cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
			}
			
			if (cont == null) {
				return null;
			}
			
			AttributeDefDefaultView defView =
					IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attr);
			
			AbstractValueView[] absView = cont.getAttributeValues(defView);
			
			if (absView.length != 0) {
				if (!( absView[0] instanceof BooleanValueDefaultView)) {
					throw new WTException("Attribute " + attr + " is not of type Boolean");
				}
				return ((BooleanValueDefaultView) absView[0]).isValue();
			}
		
		} catch (Exception wte) {
			throw new WTException(wte);
		}
		
		return value;
	}
	
	public static Boolean getBooleanValue(WTObject obj, String attr, boolean refresh)
			throws WTException
	{
		return getBooleanValue((IBAHolder)obj, attr, refresh);
	}
	
	public static Boolean getBooleanValue(IBAHolder hdr, String attr)
			throws WTException
	{
		return getBooleanValue(hdr, attr, false);
	}
	
	public static Boolean getBooleanValue(WTObject obj, String attr)
			throws WTException
	{
		return getBooleanValue(obj, attr, false);
	}
	
	public static Hashtable<String, Boolean> getBooleanValues(WTObject obj, List<String> attrs, boolean refresh)
			throws WTException
	{
		Hashtable<String, Boolean> values = new Hashtable<String, Boolean>();
		for (String attr : attrs) {
			Boolean value = getBooleanValue(obj, attr, refresh);
			values.put(attr, value);
		}
		return values;
	}
	
	public static Hashtable<WTObject, Boolean> getBooleanValues(List<WTObject> objs, String attr, boolean refresh)
			throws WTException
	{
		Hashtable<WTObject, Boolean> values = new Hashtable<WTObject, Boolean>();
		for (WTObject obj : objs) {
			Boolean value = getBooleanValue(obj, attr, refresh);
			values.put(obj, value);
		}		
		return values;
	}
	
	public static Map<WTObject, Hashtable<String, Boolean>> getBooleanValues(Hashtable<WTObject,
				List<String>> objs, boolean refresh)
				
			throws WTException
	{
	
		Map<WTObject, Hashtable<String, Boolean>> values = new HashMap<WTObject,
				Hashtable<String, Boolean>>();
				
		for (WTObject obj : objs.keySet()) {
			List<String> attrs = objs.get(obj);
			Hashtable<String, Boolean> table = getBooleanValues(obj, attrs, refresh);
			values.put(obj, table);

		}
		return values;
	}
		
	public static Hashtable<String, Boolean> getBooleanValues(WTObject obj, List<String> attrs)
			throws WTException
	{
		return getBooleanValues(obj, attrs, false);
	}
	
	public static Hashtable<WTObject, Boolean> getBooleanValues(List<WTObject> objs, String attr)
			throws WTException
	{
		return getBooleanValues(objs, attr, false);
	}
	
	public static Map<WTObject, Hashtable<String, Boolean>> getBooleanValues(Hashtable<WTObject,
				List<String>> map)
				
			throws WTException
	{
		return getBooleanValues(map, false);
	}
	
	public static Boolean getBooleanValue(WTPart part, String attr)
			throws WTException
	{
		return getBooleanValue((WTObject)part, attr);
	}
	
	public static Hashtable<String, Boolean> getBooleanValues(WTPart part, List<String> attrs)
			throws WTException
	{
		Hashtable<String, Boolean> values = new Hashtable<String, Boolean>();
		for (String attr : attrs) {
			Boolean value = getBooleanValue((WTObject)part, attr);
			values.put(attr, value);
		}
		return values;
	}
	
	public static Hashtable<Boolean, List<WTObject>> getObjectsByBooleanValues(String attr,
				List<WTObject> objs, boolean refresh)
				
			throws WTException
	{
	
		Hashtable<Boolean, List<WTObject>> values = new Hashtable<Boolean, List<WTObject>>();
		for (WTObject obj : objs) {
			Boolean value = getBooleanValue(obj, attr, refresh);
			
			if (! values.containsKey(value)) {
				values.put(value, new ArrayList<WTObject>());
			}
			List<WTObject> list = values.get(value);
			list.add(obj);
		}		
		return values;
		
	}
	
	public static Hashtable<Boolean, List<WTObject>> getObjectsByBooleanValues(String attr, List<WTObject> objs)
			throws WTException
	{
		return getObjectsByBooleanValues(attr, objs, false);
	}
	
	
//////////////////////////////////////////// SET ////////////////////////////////////////////
	
	public static DefaultAttributeContainer updateContainerBooleanValue(IBAHolder hdr,
				String attr, Boolean value, DefaultAttributeContainer cont)
				
			throws WTException
	{
	
		try {
			AttributeDefDefaultView defView =
					IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attr);
			
			if (!(defView instanceof BooleanDefView)) {
				throw new WTException("IBA " + attr + " is not of type Boolean!");
			}
			
			BooleanDefView booView = (BooleanDefView)defView;
			AbstractValueView[] absView = cont.getAttributeValues(booView);
		
			if (absView.length == 0) {
				BooleanValueDefaultView newBooView = new BooleanValueDefaultView(booView, value);
				cont.addAttributeValue(newBooView);
			} else {
				BooleanValueDefaultView newBooView = (BooleanValueDefaultView)absView[0];
				newBooView.setValue(value);
				cont.updateAttributeValue(newBooView); 
			}
			
		} catch (RemoteException rme) {
			throw new WTException(rme);
		} catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		return cont;
		
	}
	
	public static IBAHolder setBooleanValue(IBAHolder hdr, String attr, Boolean value)
			throws WTException
	{
	
		DefaultAttributeContainer cont = ValueUtils.getContainer(hdr);
		updateContainerBooleanValue(hdr, attr, value, cont);			
		hdr.setAttributeContainer(cont);
		ValueUtils.updateDB(hdr);
		return hdr;
		
	}
	
	public static void setBooleanValue(WTObject obj, String attr, Boolean value)
			throws WTException
	{
		setBooleanValue((IBAHolder)obj, attr, value);
	}
	
	public static void setBooleanValues(WTObject obj, Hashtable<String, Boolean> attrs)
			throws WTException
	{

		List<WTException> e = new ArrayList<WTException>();
		IBAHolder hdr = (IBAHolder)obj;
		DefaultAttributeContainer cont = ValueUtils.getContainer(hdr);
		
		Set:
		for (String attr : attrs.keySet()) {
			Boolean value = attrs.get(attr);
			try {
				cont = updateContainerBooleanValue(hdr, attr, value, cont);
			} catch (WTException wte) {
				e.add(wte);
				continue Set;
			}
		}
		hdr.setAttributeContainer(cont);
		ValueUtils.updateDB(hdr);
		
		if (e.size()>0) {
			throw new WTException(e.size() +
					" attributes failed to be set on " + obj);
		}
		
	}
	
	public static void setBooleanValues(String attr, Boolean value, List<WTObject> objs)
			throws WTException
	{

		List<WTException> e = new ArrayList<WTException>();
		
		Set:
		for (WTObject obj : objs) {
			IBAHolder hdr = (IBAHolder)obj;
			try {
				setBooleanValue(hdr, attr, value);
				
			} catch (WTException wte) {
				e.add(wte);
				continue Set;
			}

		}
		
		if (e.size()>0) {
			throw new WTException(e.size() + 
					" attributes failed to be set");
		}
		
	}
	
	public static void setBooleanValues(Hashtable<String, Boolean> attrs, List<WTObject> objs)
			throws WTException
	{
	
		List<WTException> e = new ArrayList<WTException>();
		
		Set:
		for (WTObject obj : objs) {
			try {
				setBooleanValues(obj, attrs);
				
			} catch (WTException wte) {
				e.add(wte);
				continue Set;
			}
		}
		
		if (e.size()>0) {
			throw new WTException(e.size() + 
					" attributes failed to be set");
		}
		
	}
	
	public static void setBooleanValues(Map<WTObject, Hashtable<String, Boolean>> map)
			throws WTException
	{
		List<WTException> e = new ArrayList<WTException>();
		
		Set:
		for (Map.Entry<WTObject, Hashtable<String, Boolean>> entry : map.entrySet()) {
			WTObject obj = entry.getKey();
			Hashtable<String, Boolean> attrs = entry.getValue();
			
			try {
				setBooleanValues(obj, attrs);
				
			} catch (WTException wte) {
				e.add(wte);
				continue Set;
			}			

		}
		
		if (e.size() > 0) {
			throw new WTException("Failed to set values on " + e.size() + " objects.");
		}

	}
	
	public static void setBooleanValue(WTPart part, String attr, Boolean value)
			throws WTException
	{
		setBooleanValue((IBAHolder)part, attr, value);
	}
	
	public static void setBooleanValues(WTPart part, Hashtable<String, Boolean> attrs)
			throws WTException
	{
		setBooleanValues((WTObject)part, attrs);
	}
}