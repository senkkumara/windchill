package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.List;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.part.WTPart;

import wt.util.WTException;

/**
 *	
 */
public class TypeUtils
{

	/**
	 *	
	 */
	public static boolean isPart(WTObject obj)
	{
		return (obj instanceof WTPart);
	}
	
	/**
	 *	
	 */
	public static boolean isEPM(WTObject obj)
	{
		return (obj instanceof EPMDocument);
	}
	
	/**
	 *	
	 */
	public static boolean isWTDocument(WTObject obj)
	{
		return (obj instanceof WTDocument);
	}
	
	/**
	 *	
	 */
	public static boolean hasParts(QueryResult qr)
	{
		WTObject obj;

		Find:
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			if (isPart(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 *	
	 */
	public static boolean hasEPM(QueryResult qr)
	{
		Find:
		while (qr.hasMoreElements()) {
			WTObject obj = (WTObject)qr.nextElement();
			if (isEPM(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 *	
	 */
	public static boolean hasWTDocuments(QueryResult qr)
	{
		Find:
		while (qr.hasMoreElements()) {
			WTObject obj = (WTObject)qr.nextElement();
			if (isWTDocument(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 *	
	 */
	public static Persistable getObject(String oid)
			throws WTException
	{
		return new ReferenceFactory().getReference(oid).getObject();
	}
	
}