package ext.hydratight.esi;

import ext.hydratight.obj.VersionUtils;
import ext.hydratight.obj.iba.AttributeUtils;
import ext.hydratight.obj.build.ChangeBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeNoticeComplexity;
import wt.change2.ChangeRecord2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.org.WTOrganization;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.vc.VersionControlHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 *	This class helps with ESI releases.<br />
 *	<br / >
 *	Its primary function is to facilitate the release of items to ERP.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class ReleaseUtils
{
	static final String ECN_TYPE = "com.actuant.pdm.Hydratight.HTReleaseChangeNotice";
	static final String ECT_TYPE = "com.actuant.pdm.Hydratight.HTReleaseChangeActivity";
	static final String ECN_NAME_PREFIX = "Release ECN - ";
	static final String ECT_NAME = "Release ECT";
	static final String ECT_DESCRIPTION = "ECT created automatically for the release of objects.";
	static final String STATE = "RESOLVED";
	static final int MAX_ECN_NAME_LENGTH = 50;
	
	// static {
		// try {
			// WTProperties wtprops = WTProperties.getLocalProperties();
			// VERBOSE = wtprops.getProperty(LOG_PROP, false);
		// } catch (Throwable t) {
			// t.printStackTrace();
		// }
	// }
	
	/**
	 * 	Creates an ECN and ECT to release Parts attached to a Release Request argument.
	 *
	 *		@param cr the Change Request object containing the Parts to be released.
	 *		@param approver the User whom approved the Change Request.
	 *		@throws WTException
	 *		@return WTChangeOrder2
	 *		@see WTChangeRequest2, WTUser
	 */
	public static WTChangeOrder2 release(WTChangeRequest2 cr, WTUser approver)
			throws WTException
	{	
		WTChangeOrder2 cn = null;
		String name;
		String description;
		WTUser creator;
		WTContainer cont;
		WTOrganization org;
		LifeCycleState state;
		WTChangeActivity2 ca;
		QueryResult qr = ChangeHelper2.service.getChangeables(cr);
		Collection<WTObject> revised = VersionUtils.revise(qr);
		Vector<WTObject> aff = new Vector<WTObject>();
		Vector<WTObject> res = new Vector<WTObject>();
	
		qr.reset();
		FindAffected:		// Retrieve AFFECTED data
		while (qr.hasMoreElements()) {
			WTObject obj = (WTObject)qr.nextElement();
			if (! (isReleaseable(obj))) {		// Accepts WTParts, WTDocuments & EPMDocuments
				continue FindAffected;
			}
			aff.add(obj);
		}
	
		FindResulting:		// Retrieve RESULTING data
		for (WTObject obj : revised) {
			if (! (isReleaseable(obj))) {		// Accepts WTParts, WTDocuments & EPMDocuments
				continue FindResulting;
			}
			res.add(obj);
		}
		
		if (res.size() > 0) {
			name = ECN_NAME_PREFIX + cr.getName();
			if (name.length() > MAX_ECN_NAME_LENGTH) {
				name = name.substring(0, MAX_ECN_NAME_LENGTH);
			}
			description = cr.getDescription();
			creator = (WTUser)cr.getCreator().getPrincipal();
			cont = cr.getContainer();
			org = cont.getOrganization();
			
			// Create State for ECN & ECT - both go straight to "Resolved" state
			state = new LifeCycleState();
			try {
				state.setState(State.toState(STATE));
				
			}
			catch (WTPropertyVetoException pve) {
				throw new WTException(pve);
			}
			
			// Create change objects
			cn = ChangeBuilder.createChangeNotice(name, description, org, cont, null,
						ChangeNoticeComplexity.SIMPLE, ECN_TYPE, creator, true, cr);		// Create ECN				
			
			ca = ChangeBuilder.createChangeActivity(ECT_NAME, ECT_DESCRIPTION, org, cont,
						state, ECT_TYPE, creator, true, cn);		// Create ECT
			
			// Associate data
			ChangeBuilder.addAffectedToChangeActivity(ca, aff);
			ChangeBuilder.addResultingToChangeActivity(ca, res);
			
			if (cn == null) {
				throw new WTException("Failed to create release ECN... :`( ");
			}
			
			AttributeUtils.setApprover(cn, approver);
			AttributeUtils.setRequester(cr, cn);		// Set requester IBA on ECN - to ensure
														// all ECNs have a requester value
						
		}
		
		return cn;
	}
	
	/**
	 *	Returns boolean if the WTObject parameter is of a type that is releaseable.
	 *
	 *		@param obj the WTObject to be evaluated
	 *		@return boolean
	 */
	private static boolean isReleaseable(WTObject obj)
	{
		return (obj instanceof WTPart)||(obj instanceof EPMDocument)||(obj instanceof WTDocument);
	}
	
}