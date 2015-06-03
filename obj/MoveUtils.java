package ext.hydratight.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import wt.util.WTException;

public class MoveUtils
{

	static private Map<String, String> DEFAULT_MAP = new HashMap<String, String>();
	static {
		DEFAULT_MAP.put("wt.epm.EPMDocument", "CAD");
	}
	
	public static void move(WTObject obj, String containerName, String folderName)
			throws WTException
	{
		SubFolder folder = getFolder(folderName, containerName);
		if (folderName != null) {
			FolderHelper.service.changeFolder((FolderEntry) obj, folder);

		}
		else {
			throw new WTException(new StringBuilder("Folder for ")
				.append(obj.getDisplayIdentifier().toString())
				.append(" not found!")
				.toString());
		}
	}
	
	public static List<ObjectReference> synchContainers(QueryResult qr)
			throws WTException
	{	
		List<ObjectReference> f = new ArrayList<ObjectReference>();
		Persistable per;
		
		Sync:
		while (qr.hasMoreElements()) {
			per = (Persistable)qr.nextElement();

			if (per instanceof WTPart) {
				try {
					synchContainers((WTPart)per, DEFAULT_MAP);
				
				}
				catch (WTException wte) {
					f.add(ObjectReference.newObjectReference(per));
					wte.printStackTrace();
					continue Sync;
				}
			}
		
		}
			
		return f;
	}
	
	public static void synchContainers(WTPart part, Map<String, String> map)
			throws WTException
	{
		int err = 0;
		QueryResult relatedDocs = WTPartHelper.service.getDescribedByDocuments(part);
		WTObject obj;

		Move:
		while (relatedDocs.hasMoreElements()) {
			obj = (WTObject) relatedDocs.nextElement();
				
			try {
				synchContainers(part, obj, map);
				
			}
			catch (WTException wte) {
				err++;
				wte.printStackTrace();
				continue Move;
			}
			
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to move ")
				.append(err)
				.append(" related document(s)")
				.toString());
		}
	}
	
	public static void synchContainers(WTPart part, WTObject obj, Map<String, String> map)
			throws WTException
	{
		String contStr = part.getContainerName();
		Class<?> cls;
		String clsStr;
		String fldStr = null;
		Folder fdr;

		if (map != null) {
			GetFolder:
			for (Map.Entry<String, String> entry : map.entrySet()) {
				try {
					cls = Class.forName(entry.getKey());
				}
				catch (ClassNotFoundException cnf) {
					throw new WTException(cnf);
				}

				if (obj.getClass().isInstance(cls)) {
					fldStr = entry.getValue();
					break GetFolder;
				}
			}
		}

		if (fldStr != null) {
			fdr = FolderHelper.service.getFolder((FolderEntry) obj);
			if (!fdr.getContainerName().equals(contStr) || !fdr.getName().equals(fldStr)) {
				if (WorkInProgressHelper.isCheckedOut((Workable) obj) || WorkInProgressHelper.isWorkingCopy((Workable) obj)) {
					throw new WTException(new StringBuilder("Object ")
						.append(obj.getDisplayIdentifier().toString())
						.append(" is checked out, it cannot be moved!")
						.toString());
					
				}
				else {
					move(obj, contStr, fldStr);
					
				}
			}
		}
	}

	private static SubFolder getFolder(String folderName, String containerName)
			throws WTException
	{
		SubFolder subfolder = null;
		QuerySpec qs = new QuerySpec(SubFolder.class);
		qs.appendWhere(new SearchCondition(SubFolder.class, SubFolder.NAME, SearchCondition.EQUAL,
					folderName), null);

		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

		Filter:
		while (qr.hasMoreElements()) {
			subfolder = (SubFolder) qr.nextElement();
			if (subfolder.getContainerName().equals(containerName)) {
				break Filter;
			}
		}
		
		return subfolder;
	}

}
