package ext.hydratight.obj.type;

import java.util.Vector;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleState;
import wt.org.WTOrganization;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.vc.baseline.Baselineable;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.ManagedBaseline;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides methods that help with the creation of Baseline objects.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 9.1 M070.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class BaselineBuilder
{
	static final private String DEFAULT_CREATOR = "wcadmin";
	static final private int MAX_NAME_LENGTH = 200;
	static final private int MAX_DESCRIPTION_LENGTH = 4000;
	
	/**
	 *	Creates a ManagedBaseline, stores it in the database and adds any items passed in a Vector<br />
	 *	parameter to it.
	 *
	 *		@param name the name of the new Baseline
	 *		@param description the description of the new Baseline (optional)
	 *		@param cont the WTContainer in which the new Baseline is to be stored
	 *		@param state the LifeCycleState in which the Baseline will be created (optional)
	 *		@param type the Soft Type of ManagedBaseline to be created (optional)
	 *		@param items a Vector containing the object(s) to be attached to the new Baseline (optional)
	 *		@param store a boolean determing whether to persist the new Baseline.
	 *		@throws WTException
	 *		@return ManagedBaseline
	 */
	public static ManagedBaseline createBaseline(String name, String description, WTContainer cont,
				LifeCycleState state, String type, Vector<WTObject> items, Boolean store)
				
			throws WTException, IllegalArgumentException
	{
		ManagedBaseline bl = ManagedBaseline.newManagedBaseline();
		Baselineable b;
		TypeDefinitionReference tdr;
		
		if (name == null || cont == null) {
			throw new IllegalArgumentException("Name & Container must not be null!");
		}
		
		if (name.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(new StringBuilder("Name too long, maximum 200 (")
				.append(name.length())
				.append(" max)")
				.toString());
		}
		
		if (description != null) {
			if (description.length() > MAX_DESCRIPTION_LENGTH) {
				throw new IllegalArgumentException(new StringBuilder("Description too long (")
					.append(description.length())
					.append(" max)")
					.toString());
			}
		}
				
		try {
			bl.setName(name);
			bl.setDescription(description);
			bl.setContainer(cont);
			
			if (state != null) {
				bl.setState(state);
			}
			
			if (type != null) {
				tdr = TypedUtility.getTypeDefinitionReference(type);
				bl.setTypeDefinitionReference(tdr);	// Create as soft type
			}
			
		}
		catch (WTPropertyVetoException e) {
			throw new WTException(e);
		}
		
		if (store) {
			storeBaseline(bl);
		}
		
		if (items != null) {
			if (items.size() > 0) {
				Add:	// Add items to baseline
				for (WTObject item : items) {
					if (item instanceof Baselineable) {
						b = (Baselineable)item;
						BaselineHelper.service.addToBaseline(b, bl);
					}
				}
			}
		}
		
		return bl;
	}
	
	/**
	 *	Creates a ManagedBaseline and stores it in the database.
	 *
	 *		@param name the name of the new Baseline
	 *		@param description the description of the new Baseline (optional)
	 *		@param cont the WTContainer in which the new Baseline is to be stored
	 *		@param state the LifeCycleState in which the Baseline will be created (optional)
	 *		@param type the Soft Type of ManagedBaseline to be created (optional)
	 *		@throws WTException
	 *		@return ManagedBaseline
	 */
	public static ManagedBaseline createBaseline(String name, String description, WTContainer cont,
				LifeCycleState state, String type)
				
			throws WTException
	{
		return createBaseline(name, description, cont, state, type, null, true);
	}
	
	/**
	 *	Persists (stores) a Baseline parameter in the database.
	 *
	 *		@param bl the ManagedBaseline to be stored
	 *		@throws WTException
	 *		@return ManagedBaseline
	 */
	private static ManagedBaseline storeBaseline(ManagedBaseline bl)
			throws WTException
	{
		return (ManagedBaseline)PersistenceHelper.manager.save(bl); 
	}
}