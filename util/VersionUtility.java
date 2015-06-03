package ext.hydratight.util;

import ext.hydratight.util.UtilityHelper;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.familytable.EPMSepFamilyTable;
import wt.epm.familytable.EPMSepFamilyTableMaster;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.feedback.StatusFeedback;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerService;
import wt.method.MethodContext;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.util.LocalizableMessage;
import wt.util.WTContext;
import wt.util.WTException;

import java.util.Iterator;
import wt.vc.Mastered;
import wt.vc.Versioned;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.StandardVersionControlService;

public class MassVersionControlUtility
  implements RemoteAccess
{
  private static final String CLASS_NAME = MassRevisionRelabelUtility.class.getName();
  private static final String NAME_ARG = "-name";
  private static final String OID_ARG = "-oid";
  private static final String NEWREV_ARG = "-newRev";
  private static final String ORG_ARG = "-org";
  private static final String USERNAME_ARG = "-username";
  private static final String PASSWORD_ARG = "-password";
  private static final String HELP_ARG = "-help";
  private static final String OLDREV_ARG = "-oldRev";
  private static final int NAME_TYPE = 0;
  private static final int OID_TYPE = 1;
  private static final String MISSING_NAME_OID = "Missing Required Argument: One of {0} or {1} is required.";
  private static final String MISSING_OLDREV = "Missing Required Argument: If using {0} argument, then {1} is also required. ";
  private static final String CONFLICTING_NAME_OID = "Conflicting Arguments: {0} and {1} are mutually exclusive arguments.";
  private static final String INVALID_ORG_NAME = "Invalid org name: {0} does not refer to any existing organization.";
  private static final String INVALID_OID = "Invalid oid: {0} does not refer to a WTPart, WTDocument, EPMDocument or EPMSepFamilyTable";
  private static final String TOO_MANY_MASTERS = "Too many applicable masters: Can only update objects with one associated master.";
  private static final String NO_REVISION_ID = "No revision id specified. No update attempted.";
  private static final String BEFORE = "Status Before Update:";
  private static final String AFTER = "Status After Update:";
  private static final String NO_VERSIONS_FOUND = "No accessible versions found.";
  private static final String VERSIONS = "Versions";
  private static final String OID = "Oid";
  private static final String NAME = "Name";
  private static final String NUMBER = "Number";
  private static final String REVISION = "Revision";
  private static final String CONTAINER = "Container";
  private static String versionName;
  private static String objectId;
  private static String newRevision;
  private static String oldRevision;
  private static WTOrganization filterOrg;
  private static String userName;
  private static String password;
  private static int idType;
  protected static boolean test = false;

  private static final Map<Class, Method> getNumberMap = new HashMap();

  public static void main(String[] args) throws Throwable {
		
		WTContext.init(args);

		if ((args == null) || (args.length == 0) || (UtilityHelper.getBooleanArg(args, "-help"))) {
			UtilityHelper.printHelp("Test");
			
		} else {
			Object obj = null;
			
			try {
				UtilityHelper.processCredentials(args);
				Boolean loggedOn = (Boolean)RemoteMethodServer.getDefault().invoke("remoteMain", CLASS_NAME, null, new Class[] { java.lang.String.class }, new Object[] { args });
				if (! loggedOn.booleanValue()) {
					UtilityHelper.print("Error: invalid log in.");
				}
				
			} catch (Exception ex) {
				obj = ex;
				
			}

			if (obj != null) {
				int i = 1;
				while (i != 0) {
					if ((obj instanceof InvocationTargetException)) {
						obj = ((InvocationTargetException)obj).getCause();
					
					} else if ((obj instanceof RemoteException)) {
						obj = ((RemoteException)obj).getCause();
					
					} else {
						i = 0;
						
					}
				}

				if (test) {
					throw ((Throwable)obj);
						
				}
				((Throwable)obj).printStackTrace();
			}
		}

		if (!test) {
			System.exit(0);
		}
	}

  public static Boolean remoteMain(String[] paramArrayOfString)
    throws Throwable
  {
    if (!WTContainerHelper.service.isAdministrator(WTContainerHelper.getExchangeRef(), SessionHelper.manager.getPrincipal(), true)) {
      return Boolean.FALSE;
    }

    processVersionArgs(paramArrayOfString);

    switch (idType) {
    case 0:
      setRevByName();
      break;
    case 1:
      setRevByOid();
    }

    return Boolean.TRUE;
  }

  private static void processVersionArgs(String[] paramArrayOfString)
    throws WTException
  {
    versionName = UtilityHelper.getStringArg(paramArrayOfString, "-name");
    objectId = UtilityHelper.getStringArg(paramArrayOfString, "-oid");
    oldRevision = UtilityHelper.getStringArg(paramArrayOfString, "-oldRev");
    if ((versionName == null) && (objectId == null)) {
      throw new IllegalArgumentException(MessageFormat.format("Missing Required Argument: One of {0} or {1} is required.", new Object[] { "-name", "-oid" }));
    }
    if ((versionName != null) && (objectId != null)) {
      throw new IllegalArgumentException(MessageFormat.format("Conflicting Arguments: {0} and {1} are mutually exclusive arguments.", new Object[] { "-name", "-oid" }));
    }
    if (versionName != null) {
      if (oldRevision == null) {
        throw new IllegalArgumentException(MessageFormat.format("Missing Required Argument: If using {0} argument, then {1} is also required. ", new Object[] { "-name", "-oldRev" }));
      }
      idType = 0;
    }
    else {
      idType = 1;
    }

    String str = UtilityHelper.getStringArg(paramArrayOfString, "-org");
    if (str != null) {
      filterOrg = getOrganization(str);
      if (filterOrg == null) {
        throw new WTException(MessageFormat.format("Invalid org name: {0} does not refer to any existing organization.", new Object[] { str }));
      }

    }

    newRevision = UtilityHelper.getStringArg(paramArrayOfString, "-newRev");
  }

  private static void setRevByName()
    throws Throwable
  {
    HashSet localHashSet1 = new HashSet();
    HashSet localHashSet2 = new HashSet();
    ArrayList localArrayList = new ArrayList();
    QuerySpec localQuerySpec = null;
    QueryResult localQueryResult = null;

    Class[] arrayOfClass1 = { WTPart.class, WTDocument.class, EPMDocument.class, EPMSepFamilyTable.class };
    Class[] arrayOfClass2 = { WTPartMaster.class, WTDocumentMaster.class, EPMDocumentMaster.class, EPMSepFamilyTableMaster.class };
    String[] arrayOfString = { "name", "name", "name", "name" };

    for (int k = 0; k < arrayOfClass1.length; k++) {
      Class localClass1 = arrayOfClass1[k];
      Class localClass2 = arrayOfClass2[k];
      String str = arrayOfString[k];

      localQuerySpec = new QuerySpec();
      int i = localQuerySpec.appendClassList(localClass1, true);
      int j = localQuerySpec.appendClassList(localClass2, true);
      localQuerySpec.appendWhere(new SearchCondition(localClass2, str, "=", versionName), new int[] { j });
      localQuerySpec.appendAnd();
      localQuerySpec.appendWhere(new SearchCondition(localClass1, "masterReference.key.id", localClass2, "thePersistInfo.theObjectIdentifier.id"), new int[] { i, j });
      localQueryResult = PersistenceHelper.manager.find(localQuerySpec);

      while (localQueryResult.hasMoreElements()) {
        Object[] arrayOfObject = (Object[])localQueryResult.nextElement();
        Versioned localVersioned = (Versioned)arrayOfObject[0];

		VersionIdentifier vi = null;
			StandardVersionControlService svcs =
					StandardVersionControlService.newStandardVersionControlService();
					
			vi = VersionControlHelper.nextVersionId(localVersioned);
			String version = vi.getValue();
		
        WTOrganization localWTOrganization = WTContainerHelper.getContainer((WTContained)localVersioned).getOrganization();
        if (((filterOrg == null) || (filterOrg.equals(localWTOrganization))) && 
			
          (oldRevision.equals(version)))
        {
          localHashSet1.add(localVersioned);
          Mastered localMastered = (Mastered)arrayOfObject[1];
          localHashSet2.add(localMastered);
        }
      }
    }
    localArrayList.addAll(localHashSet1);
    setRev(localArrayList, localHashSet2);
  }

  public static void setRevByOid()
    throws Throwable
  {
    HashSet localHashSet1 = new HashSet();
    HashSet localHashSet2 = new HashSet();
    ArrayList localArrayList = new ArrayList();
    String str = "";

    Persistable localPersistable = null;
    try
    {
      ObjectReference localObjectReference = ObjectReference.newObjectReference(ObjectIdentifier.newObjectIdentifier(objectId));
      localPersistable = localObjectReference.getObject();
    }
    catch (Exception localException) {
    }
    if (localPersistable != null)
    {
      Object localObject1;
      Object localObject2;
      if ((localPersistable instanceof WTPart)) {
        versionName = ((WTPart)localPersistable).getName();
        localObject1 = WTPart.class;
        localObject2 = WTPartMaster.class;
      }
      else if ((localPersistable instanceof WTDocument)) {
        versionName = ((WTDocument)localPersistable).getName();
        localObject1 = WTDocument.class;
        localObject2 = WTDocumentMaster.class;
      }
      else if ((localPersistable instanceof EPMDocument)) {
        versionName = ((EPMDocument)localPersistable).getName();
        localObject1 = EPMDocument.class;
        localObject2 = EPMDocumentMaster.class;
      }
      else if ((localPersistable instanceof EPMSepFamilyTable)) {
        versionName = ((EPMSepFamilyTable)localPersistable).getName();
        localObject1 = EPMSepFamilyTable.class;
        localObject2 = EPMSepFamilyTableMaster.class;
      }
      else {
        throw new WTException(MessageFormat.format("Invalid oid: {0} does not refer to a WTPart, WTDocument, EPMDocument or EPMSepFamilyTable", new Object[] { objectId }));
      }

      Versioned localVersioned1 = (Versioned)localPersistable;
      long l = localVersioned1.getMaster().getPersistInfo().getObjectIdentifier().getId();
	  
	  VersionIdentifier vi = null;
			StandardVersionControlService svcs =
					StandardVersionControlService.newStandardVersionControlService();
					
			vi = VersionControlHelper.nextVersionId(localVersioned1);
			String version1 = vi.getValue();
			
			
      str = version1;

      QuerySpec localQuerySpec = new QuerySpec();
      int i = localQuerySpec.appendClassList((Class)localObject1, true);
      int j = localQuerySpec.appendClassList((Class)localObject2, true);
      localQuerySpec.appendWhere(new SearchCondition((Class)localObject2, "thePersistInfo.theObjectIdentifier.id", "=", l), new int[] { j });
      localQuerySpec.appendAnd();
      localQuerySpec.appendWhere(new SearchCondition((Class)localObject1, "masterReference.key.id", (Class)localObject2, "thePersistInfo.theObjectIdentifier.id"), new int[] { i, j });
      QueryResult localQueryResult = PersistenceHelper.manager.find(localQuerySpec);

      while (localQueryResult.hasMoreElements()) {
        Object[] arrayOfObject = (Object[])localQueryResult.nextElement();
        Versioned localVersioned2 = (Versioned)arrayOfObject[0];

		vi = null;
			svcs =
					StandardVersionControlService.newStandardVersionControlService();
					
			vi = VersionControlHelper.nextVersionId(localVersioned2);
			String version2 = vi.getValue();
		
		
        WTOrganization localWTOrganization = WTContainerHelper.getContainer((WTContained)localVersioned2).getOrganization();
        if (((filterOrg == null) || (filterOrg.equals(localWTOrganization))) && 
          (str.equals(version2)) && 
          (localVersioned1.getPersistInfo().getObjectIdentifier().getId() != localVersioned2.getPersistInfo().getObjectIdentifier().getId()))
        {
          localHashSet1.add(localVersioned2);
          Mastered localMastered = (Mastered)arrayOfObject[1];
          localHashSet2.add(localMastered);
        }
      }

      localArrayList.add(localVersioned1);
      localArrayList.addAll(localHashSet1);
    }

    setRev(localArrayList, localHashSet2);
  }

  private static void setRev(ArrayList<Versioned> paramArrayList, Collection<Mastered> paramCollection)
    throws Throwable
  {
    if (paramArrayList.size() == 0) {
      UtilityHelper.print("\nNo accessible versions found.");
      return;
    }
	
	Versioned localVersioned;
    String str1;
    String str2;
    String str3;
    String str4;
    UtilityHelper.print("\nStatus Before Update:");
    UtilityHelper.print("\nVersions");
    UtilityHelper.print("Oid, Name, Number, Revision, Container");
    for (Iterator localIterator = paramArrayList.iterator(); localIterator.hasNext(); ) {
      localVersioned = (Versioned)localIterator.next();
      str1 = localVersioned.getPersistInfo().getObjectIdentifier().getStringValue();
      str2 = getNumber(localVersioned);
      str3 = VersionControlHelper.getIterationDisplayIdentifier(localVersioned).getLocalizedMessage(SessionHelper.getLocale());
      str4 = IdentityFactory.getDisplayIdentifier(WTContainerHelper.getContainer((WTContained)localVersioned)).getLocalizedMessage(SessionHelper.getLocale());
      UtilityHelper.print(str1 + ", " + versionName + ", " + str2 + ", " + str3 + ", " + str4);
    }

    if (newRevision == null) {
      UtilityHelper.print("\nNo revision id specified. No update attempted.");
    }
    else
    {
      if (paramCollection.size() > 1) {
        UtilityHelper.print("\nToo many applicable masters: Can only update objects with one associated master.\n");
        throw new WTException("Too many applicable masters: Can only update objects with one associated master.");
      }

      VersionControlHelper.service.changeRevision((Versioned)paramArrayList.get(0), newRevision, true);

      UtilityHelper.print("\n\nStatus After Update:");
      UtilityHelper.print("\nVersions");
      UtilityHelper.print("Oid, Name, Number, Revision, Container");
      for (Iterator localIterator = paramArrayList.iterator(); localIterator.hasNext(); ) {
        localVersioned = (Versioned)localIterator.next();
        localVersioned = (Versioned)PersistenceHelper.manager.refresh(localVersioned);
        str1 = localVersioned.getPersistInfo().getObjectIdentifier().getStringValue();
        str2 = getNumber(localVersioned);
        str3 = VersionControlHelper.getIterationDisplayIdentifier(localVersioned).getLocalizedMessage(SessionHelper.getLocale());
        str4 = IdentityFactory.getDisplayIdentifier(WTContainerHelper.getContainer((WTContained)localVersioned)).getLocalizedMessage(SessionHelper.getLocale());
        UtilityHelper.print(str1 + ", " + versionName + ", " + str2 + ", " + str3 + ", " + str4);
      }
    }
  }

  private static WTOrganization getOrganization(String paramString)
    throws WTException
  {
    WTOrganization localWTOrganization = null;
    if (paramString != null) {
      DirectoryContextProvider localDirectoryContextProvider = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null, (String[])null);
      localWTOrganization = OrganizationServicesHelper.manager.getOrganization(paramString, localDirectoryContextProvider);
    }

    return localWTOrganization;
  }

  private static String getNumber(Versioned paramVersioned)
  {
    String str = "";

    ObjectReference localObjectReference = paramVersioned.getMasterReference();
    if (localObjectReference == null) {
      return str;
    }

    try
    {
      Mastered localMastered = (Mastered)localObjectReference.getObject();
      Class localClass = localMastered.getClass();

      if (!getNumberMap.containsKey(localClass)) {
        try {
          getNumberMap.put(localClass, localClass.getMethod("getNumber", (Class[])null));
        }
        catch (Exception localException2) {
          getNumberMap.put(localClass, null);
        }

      }

      Method localMethod = (Method)getNumberMap.get(localClass);
      if (localMethod != null) {
        Object localObject = localMethod.invoke(localMastered, (Object[])null);
        if ((localObject instanceof String))
          str = (String)localObject;
      }
    }
    catch (Exception localException1)
    {
    }
    return str;
  }
}