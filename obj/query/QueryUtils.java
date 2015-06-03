package ext.hydratight.obj.query;

import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.maturity.PromotionNotice;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.baseline.ManagedBaseline;

import wt.util.WTException;

public class QueryUtils
{
	private static QueryResult find(String value, Class cls, String field)
			throws WTException
	{
		return find(value, cls, field, false);
	}
	
	private static QueryResult find(String value, Class cls, String field, boolean like)
			throws WTException
	{
		String cond = SearchCondition.EQUAL;
		QuerySpec qs;
		
		if (like) {
			cond = SearchCondition.LIKE;
		}
	
		qs = new QuerySpec(cls);
		qs.appendWhere(new SearchCondition(cls, field, cond, value), new int[] {0});
				
		return PersistenceHelper.manager.find((StatementSpec)qs);
	}

	public static WTChangeOrder2 findChangeNotice(String number)
			throws WTException
	{
		WTChangeOrder2 cn = null;
		QueryResult qr = find(number, WTChangeOrder2.class, WTChangeOrder2.NUMBER, false);
		
		if (qr.size() > 0) {
			cn = (WTChangeOrder2)qr.nextElement();
		}
		
		return cn;
	}
	
	public static WTChangeRequest2 findChangeRequest(String number)
			throws WTException
	{
		WTChangeRequest2 cr = null;
		QueryResult qr = find(number, WTChangeRequest2.class, WTChangeRequest2.NUMBER, false);
		
		if (qr.size() > 0) {
			cr = (WTChangeRequest2)qr.nextElement();
		}
		
		return cr;
	}
	
	public static WTChangeActivity2 findChangeActivity(String number)
			throws WTException
	{
		WTChangeActivity2 ca = null;
		QueryResult qr = find(number, WTChangeActivity2.class, WTChangeActivity2.NUMBER, false);
		
		if (qr.size() > 0) {
			ca = (WTChangeActivity2)qr.nextElement();
		}
		
		return ca;
	}
	
	public static ManagedBaseline findBaseline(String number)
			throws WTException
	{
		ManagedBaseline bl = null;
		QueryResult qr = find(number, ManagedBaseline.class, ManagedBaseline.NUMBER, false);
		
		if (qr.size() > 0) {
			bl = (ManagedBaseline)qr.nextElement();
		}
		
		return bl;
	}
	
	public static PromotionNotice findPromotionRequest(String number)
			throws WTException
	{
	
		return null;
	
	}
}