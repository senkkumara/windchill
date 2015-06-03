package ext.hydratight.sys;

import ext.site.sys.DefaultModeledAttributesDelegate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeRecord2;
import wt.change2.IncludedInIfc;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.maturity.PromotionNotice;
import wt.vc.VersionControlHelper;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

import wt.util.WTException;

/**
 *
 */
public class HydratightModeledAttributesDelegate extends DefaultModeledAttributesDelegate
{

	private static final String DRAWING_TYPE = "CADDRAWING";
	private static final int ECN_ROW_COUNT = 8;
	private static final String ECN_NUMBER_PARAM = "HT_ECN_NUMBER";
	private static final String ECN_DATE_PARAM = "HT_ECN_DATE";
	private static final String ECN_REVISION_PARAM = "HT_ECN_REVISION";
	private static final String ECN_DESCRIPTION_PARAM = "HT_ECN_DESCRIPTION";
	private static final String ECN_REVISED_BY_PARAM = "HT_ECN_REVISED_BY";
	private static final String ECN_CHECKED_BY_PARAM = "HT_ECN_CHECKED_BY";
	private static final String[] ECN_PARAMS = new String[]{
		ECN_NUMBER_PARAM,
		ECN_DATE_PARAM,
		ECN_REVISION_PARAM,
		ECN_DESCRIPTION_PARAM,
		ECN_REVISED_BY_PARAM,
		ECN_CHECKED_BY_PARAM
	};

	/**
	 *	Adds attributes that are specific to Hydratight.
	 *
	 *		@param attrs a map of the existing ("standard") attributes to add to
	 */
	@SuppressWarnings("unchecked")
	public static void addAttrs(HashMap attrs)
	{

		LoopRows:
		for (int i = 1; i < ECN_ROW_COUNT; i++) {

			LoopParams:
			for (int j = 0; j < ECN_PARAMS.length; j++) {
				attrs.put(new StringBuilder(ECN_NUMBER_PARAM)
					.append("_")
					.append(i)
					.toString(), String.class);
			}
		}

	}

	/**
	 *	Add modeled attributes that are specific to Hydratight.
	 *
	 *		@param docs a collection of documents to possibly add parameters to
	 *		@param results a map of the results from the "standard" attributes
	 */
	@SuppressWarnings("unchecked")
	public static void addModeledAttrs(Collection docs, HashMap results)
			throws WTException
	{

		EPMDocument doc = null;
		Map<String, String> data = null;
		IterateEPM:
		for (Iterator it = docs.iterator(); it.hasNext(); ) {
			doc = (EPMDocument)it.next();
			data = new HashMap<String, String>();
			addECNData(doc, data);
			addPromotionData(doc, data);
			removeUnwanted(data, null);

			if (results.containsKey(doc)) {
				results.put(doc, merge(data, (Map<String, String>)results.get(doc), true));
				continue IterateEPM;
			}
			else {
				results.put(doc, data);
			}
		}

	}

	private static void addPromotionData(EPMDocument doc, Map<String, String> data)
			throws WTException
	{
		return;
	}

	private static void addECNData(EPMDocument doc, Map<String, String> data)
			throws WTException
	{

		EPMDocumentMaster mas = (EPMDocumentMaster)doc.getMaster();

		if (mas.getDocType().toString().equals(DRAWING_TYPE)) {
			int revs = 0;

			Map<String, String> attrs = new HashMap<String, String>();
			QueryResult qr = VersionControlHelper.service.allVersionsOf(mas);
			EPMDocument epm = null;
			Vector<WTChangeOrder2> ecns = null;
			int count = 0;
			LoopVersions:
			while (qr.hasMoreElements()) {
				epm = (EPMDocument)qr.nextElement();
				ecns = getECN(epm);
				count = ecns.size();

				if (count > 1) {
					throw new WTException(new StringBuilder("CAD Document '")
						.append(epm.getNumber())
						.append("' has more than one associated ECN")
						.toString());
				}
				else if (count == 1) {
					revs++;
					WorkItem wi = null;
					String approver = null;
					String approved = null;
					WTChangeOrder2 ecn = ecns.get(0);
					QueryResult wis = WorkflowHelper.service.getWorkItems(ecn);

					GetApproverDetails:
					while (wis.hasMoreElements()) {
						wi = (WorkItem)wis.nextElement();

						if (wi.isComplete()) {
							SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yy");
							approver = fmt.format(wi.getModifyTimestamp());
							approved = wi.getCompletedBy();
						}
					}
					String num = ecn.getNumber();
					String dsc = ecn.getDescription();
					String rev = VersionControlHelper.getVersionIdentifier(epm).getSeries().getValue();
					String revBy = ecn.getCreatorName();

					attrs.put(ECN_NUMBER_PARAM, num);
					attrs.put(ECN_DATE_PARAM, approved);
					attrs.put(ECN_REVISION_PARAM, rev);	
					attrs.put(ECN_DESCRIPTION_PARAM, dsc);
					attrs.put(ECN_REVISED_BY_PARAM, revBy);
					attrs.put(ECN_CHECKED_BY_PARAM, approver);			
				}
				else {
					return;	// No ECN
				}
			}

			int j = 0;
			FillECNParams:
			while (j < revs) {
				LoopECNParams:
				for (String s : ECN_PARAMS) {
					data.put(
						new StringBuilder(s)
						.append("_")
						.append(j + 1)
						.toString(),
						attrs.get(new StringBuilder(s)
						.append("_")
						.append(revs - j)
						.toString()));
				}
				j++;
			}

			j++;
			BackFillECNParams:
			while (j < ECN_ROW_COUNT + 1) {
				LoopECNParams:
				for (String s : ECN_PARAMS) {
					data.put(new StringBuilder(s)
						.append("_")
						.append(j)
						.toString(), "");
				}
				j++;
			}

		}

	}

	private static Vector<WTChangeOrder2> getECN(EPMDocument epm)
			throws WTException
	{

		Vector<WTChangeOrder2> ecns = new Vector<WTChangeOrder2>();
		Vector<WTChangeActivity2> cas = new Vector<WTChangeActivity2>();
		WTChangeActivity2 ca = null;

		QueryResult qr = null;

		qr = PersistenceHelper.manager.navigate(epm, ChangeRecord2.CHANGE_ACTIVITY2_ROLE,
				ChangeRecord2.class, true);

		if ((qr != null) && (qr.size() > 0)) {
			GetECT:
			while (qr.hasMoreElements()) {
				ca = (WTChangeActivity2)qr.nextElement();
				cas.add(ca);
			}
		}

		QueryResult res = null;
		LoopECT:
		for (WTChangeActivity2 c : cas) {
			res = PersistenceHelper.manager.navigate(ca, IncludedInIfc.CHANGE_ORDER_IFC_ROLE,
					IncludedInIfc.class, true);

			if ((res != null) && (res.size() > 0)) {
				WTChangeOrder2 ecn = null;
				GetECN:
				while (res.hasMoreElements()) {
					ecn = (WTChangeOrder2)res.nextElement();
					ecns.add(ecn); 
				}
			}
		}

		return ecns;

	}

	private static void removeUnwanted(Map<String, String> data, List<String> attrs)
	{
		return;
	}

	private static Map<String, String> merge(Map<String, String> src, Map<String, String> dst, boolean overwrite)
	{

		Merge:
		for (Map.Entry<String, String> srcEntry : src.entrySet()) {
			if (overwrite || (! dst.containsKey(srcEntry.getKey()))) {
				dst.put(srcEntry.getKey(), srcEntry.getValue());
			}
		}

		return dst;

	}

	private static Map<String, String> merge(Map<String, String> src, Map<String, String> dst)
	{
		return merge(src, dst, false);
	}

}