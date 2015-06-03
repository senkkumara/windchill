package ext.hydratight.esi;

import com.ptc.windchill.esi.txn.ESITransactionUtility;
import com.ptc.windchill.esi.txn.ReleaseActivity;
import java.util.Collection;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.BaselineHelper;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

import com.ptc.windchill.esi.utl.ESIException;
import wt.util.WTException;

/**
 *	This class helps with ESI activity history.<br />
 *	<br />
 *	Its primary function is to facilitate the deletion of ESI release activities.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 9.1 M070.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class HistoryUtils
{
	
	/**
	 *	<strong>USE WITH CAUTION!</strong><br/>
	 *	</br>
	 *	Deletes <strong>ALL</strong> of the release activities referencing the Part(s) attached to a Baseline <br />
	 *	parameter. Nothing will be done with baselineables that are not of type "WTPart".<br />
	 *	<br />
	 *	<b>WARNING:</b> This includes deleting the release activities referencing the release of parent BOM.
	 *
	 *		@param bl the Baseline containing the Part(s) which require their release activities deleting.
	 *		@throws WTException
	 */
	public static void deleteActivities(Baseline bl)
			throws WTException
	{
		// Retrieve Baselineables
		QueryResult qr = BaselineHelper.service.getBaselineItems(bl);
		deleteActivities(qr);
	}
	
	/**
	 *	<strong>USE WITH CAUTION!</strong><br/>
	 *	</br>
	 *	Deletes <strong>ALL</strong> of the release activities referencing the Part(s) in a QueryResult <br />
	 *	parameter. Nothing will be done with baselineables that are not of type "WTPart".<br />
	 *	<br />
	 *	<b>WARNING:</b> This includes deleting the release activities referencing the release of parent BOM.
	 *
	 *		@param qr the QueryResult containing the Part(s) which require their release activities deleting.
	 *		@throws WTException
	 */
	public static void deleteActivities(QueryResult qr)
			throws WTException
	{
		int err = 0;		// Count exceptions
		Persistable per;
		WTPart part;
		
		// Loop Baselineables
		Delete:
		while (qr.hasMoreElements()) {
			per = (Persistable) qr.nextElement();
			if(per instanceof WTPart){		// Only WTParts can be released
				part = (WTPart)per;
				
				try {
					deleteActivities(part);		// Remove release activites for Part
				}
				catch (WTException wte) {
					err++;
					wte.printStackTrace();
					continue Delete;
				}
			}
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to delete ESI activities on ")
				.append(err)
				.append(" object(s)...")
				.toString());
		}
	}
	
	/**
	 *	Deletes <strong>ALL</strong> the ESI activities in a Collection parameter.
	 *
	 *		@param col the Collection containing the ESI activities.
	 *		@throws WTException
	 */
	public static void deleteActivities(Collection<ReleaseActivity> col)
			throws WTException
	{
		int err = 0;		// Count exceptions
		String id;
		
		Delete:
		for (ReleaseActivity ra : col) {		
			id = ra.toString();	// Get String rep for reporting
			
			try {
				deleteActivity(ra);		// Delete activity
			
			}
			catch (WTException wte) {
				err++;
				wte.printStackTrace();
				continue Delete;
			
			}

		}
		
		if (err != 0) {
			throw new WTException("Failed to delete " + err + " ESI activities...");
		}
	}
	
	/**
	 *	<strong>USE WITH CAUTION!</strong><br/>
	 *	</br>
	 *	Deletes <strong>ALL</strong> of the release activities referencing the Part parameter - all iterations, not<br />
	 *	revisions.
	 *	<br />
	 *	<b>WARNING:</b> This includes deleting the release activities referencing the release of parent BOM.
	 *
	 *		@param part the WTPart to have its release activities deleted.
	 *		@throws WTException
	 */
	public static void deleteActivities(WTPart part)
		throws WTException
	{
		int err = 0;
		QueryResult iter = VersionControlHelper.service.allIterationsFrom((Iterated)part);
		Persistable per;
		QueryResult trans;
		int count = 0;
		Collection<ReleaseActivity> acts;
		
		// Delete all activities for each iteration
		Delete:
		while (iter.hasMoreElements()){
			per = (Persistable) iter.nextElement();
			
			// Retrieve Iteration's ESI Transactions
			try {
				trans = getTransactions(per);
				
			}
			catch (WTException wte) {
				err++;
				wte.printStackTrace();
				continue Delete;
			}
			
			// Retrieve Iteration's ESI Activities
			try {
				acts = getActivities(per);
			
			}
			catch (WTException wte) {
				err++;
				wte.printStackTrace();
				continue Delete;
			}
			
			// Delete all Release Activities for iteration
			if (acts.size() > 0){		// Part is associated to ESI activities
				try {
					deleteActivities(acts);
					
				}
				catch (WTException wte) {
					err++;
					wte.printStackTrace();
					continue Delete;
				}
			}
		}
		
		// Report failure
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to delete ")
				.append(err)
				.append(" activities for ")
				.append(part.getNumber())
				.append("...")
				.toString());
		}
	}
	
	/**
	 *	Returns QueryResult containing ESI transactions for a parameter Persistable.
	 *
	 *		@param per the Peristable containing ESI transactions
	 *		@return QueryResult
	 *		@throws WTException
	 */
	public static QueryResult getTransactions(Persistable per)
			throws ESIException
	{
		return (new ESITransactionUtility().findTransactions(per));		// Get ESI transactions
	}
	
	/**
	 *	Returns Collection containing ESI activities for a parameter Persistable.
	 *
	 *		@param per the Peristable containing ESI activities
	 *		@return Collection
	 *		@throws WTException
	 */
	public static Collection<ReleaseActivity> getActivities(Persistable per)
			throws WTException
	{
		return (new ESITransactionUtility().getActivities(per));		// Get ESI activities
	}
	
	/**
	 *	Returns a String containing the details of the parameter ReleaseActivity, namely:
	 *	<ol>
	 *		<li>Action</li>
	 *		<li>Creator</li>
	 *		<li>Status</li>
	 *		<li>Description</li>
	 *		<li>Releaseable</li>
	 *	</ol>
	 *
	 *		@param ra the ReleaseActivity from which the details are extracted
	 *		@return String
	 */
	public static String getActivityDetails(ReleaseActivity ra)
	{
		
		String action = ra.getAction().toString();
		String creator = ra.getCreator();
		String status = ra.getStatus().toString();
		String description = ra.getDescription();
		String releaseable = ra.getReleasableObject().toString();
		
		return (new StringBuilder())
				.append("Object: ")
				.append(releaseable)
				.append(", Action: ")
				.append(action)
				.append(", Status: ")
				.append(status)
				.append(", Description: ")
				.append(description)
				.append(", Creator: ")
				.append(creator).toString();
	}
	
	/**
	 *	Returns a String containing the details of the parameter ReleaseActivities, namely:
	 *	<ol>
	 *		<li>Action</li>
	 *		<li>Creator</li>
	 *		<li>Status</li>
	 *		<li>Description</li>
	 *		<li>Releaseable</li>
	 *	</ol>
	 *
	 *		@param acts the Collection of ReleaseActivity from which the details are extracted
	 *		@return String
	 */
	public static String getActivityDetails(Collection<ReleaseActivity> acts)
	{
	
		StringBuilder sb = new StringBuilder();
		
		for (ReleaseActivity ra : acts) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			
			sb.append(getActivityDetails(ra));
		}
		
		return sb.toString();
		
	}
	
	/**
	 *	Deletes an ESI activity parameter.
	 *
	 *		@param ra the Release Activity to be deleted.
	 *		@throws WTException
	 */
	public static void deleteActivity(ReleaseActivity ra)
			throws WTException
	{
		ESITransactionUtility util = new ESITransactionUtility();
		util.deleteReleaseActivity(ra);
	}
}