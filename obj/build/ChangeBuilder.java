package ext.hydratight.obj.build;

import java.util.Vector;
import wt.change2.AffectedActivityData;
import wt.change2.Category;
import wt.change2.Changeable2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeItem;
import wt.change2.ChangeNoticeComplexity;
import wt.change2.ChangeRecord2;
import wt.change2.IssuePriority;
import wt.change2.ReportedAgainst;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeIssue;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleState;
import wt.org.WTOrganization;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;

import wt.change2.ChangeException2;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class provides methods that help with the creation of Change objects.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 9.1 M070.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class ChangeBuilder
{
	static final private String DEFAULT_CREATOR = "wcadmin";
	static final private String DEFAULT_ECN_DESCRIPTION = "Initial release";
	static final private String DEFAULT_ECT_DESCRIPTION = "Initial release activity";
	
	public static WTChangeOrder2 createChangeNotice(String name, String description, WTOrganization org, 
				WTContainer cont, LifeCycleState state, ChangeNoticeComplexity complexity, String type, WTUser creator,
				boolean store, WTChangeRequest2 cr)
				
			throws WTException, IllegalArgumentException
	{	
		WTChangeOrder2 cn = WTChangeOrder2.newWTChangeOrder2();
		TypeDefinitionReference tdr;

		if (creator != null) {
			SessionHelper.manager.setAuthenticatedPrincipal(creator.getName());
		}
		
		if (name == null || org == null || cont == null) {
			throw new IllegalArgumentException("Name, Organisation & Container must not be null!");
		}
		
		if (name.length() > 256) {
			throw new IllegalArgumentException(new StringBuilder("Name too long, maximum 256 (")
				.append(name.length())
				.append(")")
				.toString());
		}
		
		if (description == null) {
			description = DEFAULT_ECN_DESCRIPTION;
		}
		
		if (description.length() > 4000) {
			throw new IllegalArgumentException(new StringBuilder("Description too long (")
				.append(description.length())
				.append(")")
				.toString());
		}
		
		try {			
			cn.setName(name);
			cn.setDescription(description);
			cn.setOrganization(org);
			cn.setContainer(cont);
			
			if (state != null) {
				cn.setState(state);		// Set state if provided
			}
			
			cn.setChangeNoticeComplexity(ChangeNoticeComplexity.SIMPLE);
			if (complexity != null) {
				cn.setChangeNoticeComplexity(complexity);	// Set complexity if provided
			}
			
			if (type != null) {
				tdr = TypedUtility.getTypeDefinitionReference(type);
				cn.setTypeDefinitionReference(tdr);		// Create as soft type if provided
			}
			
		}
		catch (WTPropertyVetoException ex) {				
			throw new WTException(ex);
		}
		
		if (store) {
			if (cr != null) {
				storeChangeNotice(cr, cn);
			}
			else {
				storeChangeNotice(cn);
			}
		}
		
		return cn;
	}
	
	public static WTChangeActivity2 createChangeActivity(String name, String description,
				WTOrganization org, WTContainer cont, LifeCycleState state, String type, WTUser creator, boolean store,
				WTChangeOrder2 cn)
				
			throws WTException
	{	
		WTChangeActivity2 ca = WTChangeActivity2.newWTChangeActivity2();
		TypeDefinitionReference tdr;

		if (creator != null) {
			SessionHelper.manager.setAuthenticatedPrincipal(creator.getName());
		}
		
		if (name == null || org == null || cont == null) {
			throw new IllegalArgumentException("Name, Organisation & Container must not be null!");
		}
		
		if (name.length() > 256) {
			throw new IllegalArgumentException(new StringBuilder("Name too long, maximum 256 (")
				.append(name.length())
				.append(")")
				.toString());
		}
		
		if (description == null) {
			description = DEFAULT_ECT_DESCRIPTION;
		}
		
		if (description.length() > 4000) {
			throw new IllegalArgumentException(new StringBuilder("Description too long (")
				.append(description.length())
				.append(")")
				.toString());
		}
		
		try {			
			ca.setName(name);
			ca.setDescription(description);
			ca.setOrganization(org);
			ca.setContainer(cont);
			
			if (state != null) {
				ca.setState(state);		// Set state if provided
			}
			
			if (type != null) {
				tdr = TypedUtility.getTypeDefinitionReference(type);
				ca.setTypeDefinitionReference(tdr);	// Create as soft type
			}
			
		}
		catch (WTPropertyVetoException e) {
			throw new WTException(e);
		}
		
		if (store && cn != null) {
			storeChangeActivity(cn, ca);
		}
		
		return ca;
	}
	
	private static WTChangeOrder2 storeChangeNotice(WTChangeOrder2 cn)
			throws WTException
	{
		try {
			cn = (WTChangeOrder2)ChangeHelper2.service.saveChangeOrder(cn);
			
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
			
		}
		
		return cn;
	}
	
	private static WTChangeOrder2 storeChangeNotice(WTChangeRequest2 cr, WTChangeOrder2 cn)
			throws WTException
	{
		try {
			cn = (WTChangeOrder2)ChangeHelper2.service.saveChangeOrder(cr, cn);
			
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
			
		}
		
		return cn;
	}
	
	private static WTChangeOrder2 storeChangeNotice(WTChangeOrder2 cn, WTChangeRequest2 cr)
			throws WTException
	{
		try {
			cn = (WTChangeOrder2)ChangeHelper2.service.saveChangeOrder(cr, cn);
			
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
			
		}
		
		return cn;
	}
	
	private static WTChangeActivity2 storeChangeActivity(WTChangeOrder2 cn, WTChangeActivity2 ca)
			throws WTException
	{
		try {
			ca = (WTChangeActivity2)ChangeHelper2.service.saveChangeActivity(cn, ca);
			
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
			
		}
		
		return ca;
	}
	
	private static void associate(ChangeItem c, Vector<WTObject> os, Class cls)
			throws WTException
	{
		RemoveInvalid:		// Check that all objects are valid types
		for (WTObject o : os) {
			if (!(o instanceof Changeable2)) {
				os.remove(o);
			}
		}
		
		try {
			if (os.size() > 0) {
				ChangeHelper2.service.storeAssociations(cls, c, os);
			}
			
		}
		catch (ChangeException2 ce) {
			throw new WTException(ce);
			
		}
	}
	
	public static void addResultingToChangeActivity(WTChangeActivity2 ca, Vector<WTObject> os)
			throws WTException
	{
		associate(ca, os, ChangeRecord2.class);
	}
	
	public static void addAffectedToChangeActivity(WTChangeActivity2 ca, Vector<WTObject> os)
			throws WTException
	{
		associate(ca, os, AffectedActivityData.class);
	}
	
	public static WTChangeIssue createProblemReport(String name, String description, WTUser creator,
				WTContainer cont, WTOrganization org, LifeCycleState state, String type,
				String category, String priority, Boolean store)
			
			throws WTException
	{
		WTChangeIssue ci = WTChangeIssue.newWTChangeIssue();
		TypeDefinitionReference tdr;

		if (creator != null) {
			SessionHelper.manager.setAuthenticatedPrincipal(creator.getName());
		}
		
		if (name == null || org == null || cont == null) {
			throw new IllegalArgumentException("Name, Organisation & Container must not be null!");
		}
		
		if (name.length() > 256) {
			throw new IllegalArgumentException(new StringBuilder("Name too long, maximum 256 (")
				.append(name.length())
				.append(")")
				.toString());
		}
		
		if (description.length() > 4000) {
			throw new IllegalArgumentException(new StringBuilder("Description too long (")
				.append(description.length())
				.append(")")
				.toString());
		}
		
		try {
			// Populate CI Details
			ci.setName(name);
			ci.setDescription(description);
			ci.setOrganization(org);
			ci.setContainer(cont);
			
			if (state != null) {
				ci.setState(state);		// Set state if provided
			}
			
			ci.setCategory(Category.OTHER);
			if (category != null) {
				ci.setCategory(Category.toCategory(category));	// Set category if provided
			}
			
			ci.setIssuePriority(IssuePriority.MEDIUM);
			if (priority != null) {
				ci.setIssuePriority(IssuePriority.toIssuePriority(priority));	// Set priority if provided
			}
			
			if (type != null) {
				tdr = TypedUtility.getTypeDefinitionReference(type);
				ci.setTypeDefinitionReference(tdr);		// Create as soft type if provided
			}
			
		}
		catch (WTPropertyVetoException e) {				
			throw new WTException(e);
		}
		
		if (store) {
			store(ci);
		}
		
		return ci;
	}
	
	public static void store(WTChangeIssue ci)
			throws WTException
	{
		try {
			ci = (WTChangeIssue)ChangeHelper2.service.saveChangeIssue(ci);
		
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
		}
	}
	
	public static void addToProblemReport(WTChangeIssue ci, Vector<WTObject> os)
			throws WTException
	{
		RemoveInvalid:
		for (WTObject o : os) {
			if(! (o instanceof Changeable2)) {
				os.remove(o);
			}
		}
		
		try {
			if (os.size() > 0) {
				associate(ci, os, ReportedAgainst.class);
			}
		
		}
		catch (ChangeException2 e) {
			throw new WTException(e);
		}
	}

}