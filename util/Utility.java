package ext.hydratight.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.familytable.EPMSepFamilyTable;
import wt.epm.familytable.EPMSepFamilyTableMaster;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.feedback.StatusFeedback;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.method.MethodContext;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

/**
 *	This class provides help with custom Windchill utilities.
 */ 
public class Utility
	implements RemoteAccess
{
	protected static String CLASS_NAME = "ext.hydratight.util.Utility";
	protected static String UTILITY_NAME = "Utility";
	protected static final String METHOD_NAME = "remoteMain";
	protected static final String HELP_ARG = "-help";
	protected static final String USERNAME_ARG = "-username";
	protected static final String PASSWORD_ARG = "-password";
	protected static final String FILE_ARG = "-file";
	protected static final String NUMBER_ARG = "-number";
	protected static final String REVISION_ARG = "-revision";
	protected static final String ITERATION_ARG = "-iteration";
	protected static final String TYPE_ARG = "-type";
	protected static final String OID_ARG = "-oid";
	protected static final String ORG_ARG = "-org";
	protected static final String LOG_ARG = "-log";
	protected static final String DEBUG_ARG = "-debug";
	private static final String UTILITIES_ARG = "-utilities";
	protected static final String NUMBER_HEAD = "number";
	protected static final String REVISION_HEAD = "revision";
	protected static final String ITERATION_HEAD = "iteration";
	protected static final String TYPE_HEAD = "type";
	protected static final String OID_HEAD = "oid";
	protected static final String ORG_HEAD = "org";
	private static final String UTILITIES_HEAD = "utilities";
	protected static final String VERSION_CONDITION = "versionInfo.identifier.versionId";
	protected static final String ITERATION_CONDITION = "iterationInfo.identifier.iterationId";
	protected static String file;
	protected static String number;
	protected static String revision;
	protected static String iteration;
	protected static String type;
	protected static String oid;
	protected static WTOrganization org;
	protected static String orgStr;
	protected static QueryResult qr;
	private static String utilities;
	protected static int idType;
	protected static String logFile;
	protected static String errFile;
	protected static boolean log = false;
	protected static boolean debug = false;
	private static String userName;
	private static String password;
	protected static final String INVALID_ORG_NAME = "Invalid organisation name: No organisation by the name '{0}' exists!";
	private static final String LOG_IN_MSG = "Logged in as {0}...";
	
	public static void main(String [] args)
			throws Exception, Throwable
	{
		WTContext.init(args);
		if (args == null || args.length == 0 || getBooleanArg(args, HELP_ARG)) {
			printHelp();
		}
		else {
			Object obj = null;
			print("Entering '" + UTILITY_NAME + "'...  ");
			try {
				processCredentials(args);
				router(args);
					
			}
			catch (Exception ex) {
				obj = ex;
			}
			
			if (obj != null) {
				for (boolean flag = true; flag;) {
					if (obj instanceof InvocationTargetException) {
						obj = ((InvocationTargetException)obj).getCause();
						
					}
					else if (obj instanceof RemoteException) {
						obj = ((RemoteException)obj).getCause();
					
					}
					else {
						flag = false;
						
					}
				}

				if (debug) {
					throw (Exception)obj;
				}
				
				((Throwable) (obj)).printStackTrace();
			}
		}
		
		if (!debug) {
			System.exit(0);
		}
	}
	
	public static Boolean router(String as[])
			throws Throwable
	{
		if (!WTContainerHelper.service.isAdministrator(WTContainerHelper.getExchangeRef(),
				SessionHelper.manager.getPrincipal(), true)) {
				
			return Boolean.FALSE;
		}

		processArgs(as);
		print("I don't really do anything at the moment... Sorry!");
		// Do something with them!
		
		return Boolean.TRUE;
	}
	
	protected static void processArgs(String as[])
			throws WTException
	{
	
	}

	protected static void validateArgs()
	{
		
	}
	
	protected static String getStringArg(String[] arguments, String stringArg)
	{
		String str = null;
		for (int i = 0; i < arguments.length; i++) {
			String str2 = arguments[i];
			if ((str2.equalsIgnoreCase(stringArg)) && (i + 1 < arguments.length)) {
				str = arguments[(i + 1)];
			}
		}

		return str;
	}
	
	protected static boolean getBooleanArg(String[] arguments, String boolArg)
	{
	
		boolean bool = false;
		for (int i = 0; i < arguments.length; i++) {
			String str = arguments[i];
			if (str.equalsIgnoreCase(boolArg)) {
				bool = true;
			}
		}

		return bool;
	}
	
	protected static void processCredentials(String as[] )
			throws Exception
	{
	
		print("Please log in as a site administrator...  ");
		userName = getStringArg(as, USERNAME_ARG);
		password = getStringArg(as, PASSWORD_ARG);
		if (userName != null && password != null) {
			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			rms.setUserName(userName);
			rms.setPassword(password);
		}
		
		SessionHelper.manager.getPrincipal();
		print(MessageFormat.format(LOG_IN_MSG, new Object[] {
				userName
			}));
		
	}
	
	protected static QueryResult findByNumber()
			throws WTException
	{
		
		Map<String, Class> c = new HashMap<String, Class>();
		c.put("WTPart", WTPart.class);
		c.put("EPMDocument", EPMDocument.class);
		c.put("WTDocument", WTDocument.class);
		
		Map<String, String> f = new HashMap<String, String>();
		f.put("WTPart", WTPart.NUMBER);
		f.put("EPMDocument", EPMDocument.NUMBER);
		f.put("WTDocument", WTDocument.NUMBER);
		
		if (! (c.containsKey(type) && f.containsKey(type))) {
			throw new WTException("something");
		}
		
		QuerySpec qs = new QuerySpec(c.get(type));
		qs.appendWhere(new SearchCondition(c.get(type), f.get(type), SearchCondition.EQUAL, number), new int[] {0,1});
		LatestConfigSpec lcs = new LatestConfigSpec();
		
		if (revision != null) {
			qs.appendAnd();
			SearchCondition rsc = new SearchCondition(c.get(type), VERSION_CONDITION, SearchCondition.EQUAL, revision);
			qs.appendWhere(rsc, new int[] {0});
			
			if (iteration != null) {
				qs.appendAnd();
				SearchCondition isc = new SearchCondition(c.get(type), ITERATION_CONDITION, SearchCondition.EQUAL, iteration);		
				qs.appendWhere(isc, new int[] {0});
			}
			
		}
		else {
			qs = lcs.appendSearchCriteria(qs);
			
		}
		
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
		
		if (revision != null) {
			qr = lcs.process(qr);
		}
		
		return qr;
		
	}
	
	protected static List<ObjectReference> filterNumber(QueryResult qr)
	{
		
		return null;
		
	}
	
	protected static QueryResult findByOid()
			throws WTException
	{
		
		QueryResult qr = null;
		Class cls;
		Class clsMs;
		ObjectReference ref = null;
		Persistable per = null;
		Versioned v = null;
		
		try {
			ref = ObjectReference.newObjectReference(ObjectIdentifier.newObjectIdentifier(oid));
			per = ref.getObject();
		
		}
		catch (WTException wte) {
		
		}
		
		if (per != null) {
			if (per instanceof WTPart) {
				cls = WTPart.class;
				clsMs = WTPartMaster.class;
			
			}
			else if (per instanceof EPMDocument) {
				cls = EPMDocument.class;
				clsMs = EPMDocumentMaster.class;
				
			}
			else if (per instanceof EPMSepFamilyTable) {
				cls = EPMSepFamilyTable.class;
				clsMs = EPMSepFamilyTableMaster.class;
				
			}
			else if (per instanceof WTDocument) {
				cls = WTDocument.class;
				clsMs = WTDocumentMaster.class;
				
			}
			else {
				throw new WTException();
			}
			
			v = (Versioned)per;
			long id = v.getMaster().getPersistInfo().getObjectIdentifier().getId();
			String vid = v.getVersionIdentifier().getValue();	// Check this works!!
			
			QuerySpec qs = new QuerySpec();
			int i = qs.appendClassList(cls, true);
			int j = qs.appendClassList(clsMs, true);
			qs.appendWhere(new SearchCondition(clsMs, "thePersistInfo.theObjectIdentifier.id",
					SearchCondition.EQUAL, id), new int[] {
							j
						});
						
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(cls, "masterReference.key.id",
					clsMs, "thePersistInfo.theObjectIdentifier.id"), new int[] {
							i, j
						});
			
			qr = PersistenceHelper.manager.find((StatementSpec)qs);
		}
		
		return qr;

	}
	
	protected static List<ObjectReference> filterOid(Versioned v1, QueryResult qr, WTOrganization org)
			throws WTException
	{
	
		Set hsv = new HashSet();
		Set hsm = new HashSet();
		List<ObjectReference> li = new ArrayList<ObjectReference>();
		String s1 = null; // Temp
		
		Versioned v = null;
		Mastered m = null;
		WTOrganization org2 = null;
		while (qr.hasMoreElements()) {
			Object o[] = (Object[])(Object[])qr.nextElement();
			v = (Versioned)o[0];
			org2 = WTContainerHelper.getContainer((WTContained)v).getOrganization();
			
			if ((org == null || org.equals(org2)) &&
				s1.equals(v.getVersionIdentifier().getValue()) && 
				v1.getPersistInfo().getObjectIdentifier().getId() != v.getPersistInfo().getObjectIdentifier().getId()) {
					hsv.add(v);
					m = (Mastered)o[1];
					hsm.add(m);
			}
						
		}
		
		return li;
	}
	
	protected static WTOrganization getOrganization(String s)
			throws WTException
	{
	
		WTOrganization org = null;
		if (s != null) {
			DirectoryContextProvider dcp =
					OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null,
					(String[])null);
					
			org = OrganizationServicesHelper.manager.getOrganization(s, dcp);
		}
		
		return org;

	}
	
	protected static void print(String str)
	{
	
		if ((RemoteMethodServer.ServerFlag) && (MethodContext.getActiveCount() > 0)) {
			MethodContext.getContext().sendFeedback(new StatusFeedback(str));
		
		}
		else {
			System.out.println(str);
			
		}
	}

	protected static void printHelp()
	{
		print("I am helpful");
	}

	protected static void printHelp(Map<String, String> args)
	{
		String txt;

	}
	
	private static void write(String line, String file)
			throws IOException
	{
		
		FileWriter fstream = new FileWriter(file, true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(line);
		out.newLine();
		out.close();
		
	}
	
	protected static void writeLog(String line)
			throws IOException
	{
		write(line, logFile);
	}
	
	protected static void writeError(String line)
			throws IOException
	{
		write(line, errFile);
	}
	
	protected static void close(Reader r) 
    { 
        try { 
            if (r != null) { 
                r.close(); 
            } 
        }
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
    }

}