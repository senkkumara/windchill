// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   ChangeRevisionLabelUtility.java

package wt.vc;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.*;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.familytable.EPMSepFamilyTable;
import wt.epm.familytable.EPMSepFamilyTableMaster;
import wt.fc.*;
import wt.feedback.StatusFeedback;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerService;
import wt.method.MethodContext;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
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

// Referenced classes of package wt.vc:
//            Versioned, Mastered, VersionIdentifier, VersionControlHelper, 
//            VersionControlService

public class ChangeRevisionLabelUtility
    implements RemoteAccess {

            private static final String CLASS_NAME = wt/vc/ChangeRevisionLabelUtility.getName();
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
            private static final Map getNumberMap = new HashMap();


            public static void main(String args[]) throws Throwable {
/* 145*/        WTContext.init(args);
/* 148*/        if (args == null || args.length == 0 || getBooleanArg(args, "-help")) {
/* 150*/            printHelp();
                } else {
/* 153*/            Object obj = null;
/* 155*/            print("Please log in as a site administrator...  ");
/* 157*/            try {
/* 157*/                processCredentials(args);
/* 158*/                Boolean boolean1 = (Boolean)RemoteMethodServer.getDefault().invoke("remoteMain", CLASS_NAME, null, new Class[] {
/* 158*/                    [Ljava/lang/String;
                        }, new Object[] {
/* 158*/                    args
                        });
/* 159*/                if (!boolean1.booleanValue()) {
/* 160*/                    print("Error: invalid log in.");
                        }
                    }
/* 163*/            catch (Exception exception) {
/* 164*/                obj = exception;
                    }
/* 168*/            if (obj != null) {
/* 169*/                for (boolean flag = true; flag;) {
/* 171*/                    if (obj instanceof InvocationTargetException) {
/* 172*/                        obj = ((InvocationTargetException)obj).getCause();
                            } else
/* 174*/                    if (obj instanceof RemoteException) {
/* 175*/                        obj = ((RemoteException)obj).getCause();
                            } else {
/* 178*/                        flag = false;
                            }
                        }

/* 182*/                if (test) {
/* 183*/                    throw obj;
                        }
/* 185*/                ((Throwable) (obj)).printStackTrace();
                    }
                }
/* 189*/        if (!test) {
/* 190*/            System.exit(0);
                }
            }

            public static Boolean remoteMain(String as[]) throws Throwable {
/* 202*/        if (!WTContainerHelper.service.isAdministrator(WTContainerHelper.getExchangeRef(), SessionHelper.manager.getPrincipal(), true)) {
/* 203*/            return Boolean.FALSE;
                }
/* 207*/        processVersionArgs(as);
/* 209*/        switch (idType) {
/* 211*/        case 0: // '\0'
/* 211*/            setRevByName();
                    break;

/* 214*/        case 1: // '\001'
/* 214*/            setRevByOid();
                    break;
                }
/* 218*/        return Boolean.TRUE;
            }

            private static void processCredentials(String as[]) throws Exception {
/* 233*/        userName = getStringArg(as, "-username");
/* 234*/        password = getStringArg(as, "-password");
/* 235*/        if (userName != null && password != null) {
/* 236*/            RemoteMethodServer remotemethodserver = RemoteMethodServer.getDefault();
/* 237*/            remotemethodserver.setUserName(userName);
/* 238*/            remotemethodserver.setPassword(password);
                }
/* 240*/        SessionHelper.manager.getPrincipal();
            }

            private static void processVersionArgs(String as[]) throws WTException {
/* 252*/        versionName = getStringArg(as, "-name");
/* 253*/        objectId = getStringArg(as, "-oid");
/* 254*/        oldRevision = getStringArg(as, "-oldRev");
/* 255*/        if (versionName == null && objectId == null) {
/* 256*/            throw new IllegalArgumentException(MessageFormat.format("Missing Required Argument: One of {0} or {1} is required.", new Object[] {
/* 256*/                "-name", "-oid"
                    }));
                }
/* 258*/        if (versionName != null && objectId != null) {
/* 259*/            throw new IllegalArgumentException(MessageFormat.format("Conflicting Arguments: {0} and {1} are mutually exclusive arguments.", new Object[] {
/* 259*/                "-name", "-oid"
                    }));
                }
/* 261*/        if (versionName != null) {
/* 262*/            if (oldRevision == null) {
/* 263*/                throw new IllegalArgumentException(MessageFormat.format("Missing Required Argument: If using {0} argument, then {1} is also required. ", new Object[] {
/* 263*/                    "-name", "-oldRev"
                        }));
                    }
/* 265*/            idType = 0;
                } else {
/* 268*/            idType = 1;
                }
/* 272*/        String s = getStringArg(as, "-org");
/* 273*/        if (s != null) {
/* 274*/            filterOrg = getOrganization(s);
/* 275*/            if (filterOrg == null) {
/* 276*/                throw new WTException(MessageFormat.format("Invalid org name: {0} does not refer to any existing organization.", new Object[] {
/* 276*/                    s
                        }));
                    }
                }
/* 281*/        newRevision = getStringArg(as, "-newRev");
            }

            private static void setRevByName() throws Throwable {
/* 290*/        HashSet hashset = new HashSet();
/* 291*/        HashSet hashset1 = new HashSet();
/* 292*/        ArrayList arraylist = new ArrayList();
/* 293*/        Object obj = null;
/* 294*/        Object obj1 = null;
/* 299*/        Class aclass[] = {
/* 299*/            wt/part/WTPart, wt/doc/WTDocument, wt/epm/EPMDocument, wt/epm/familytable/EPMSepFamilyTable
                };
/* 300*/        Class aclass1[] = {
/* 300*/            wt/part/WTPartMaster, wt/doc/WTDocumentMaster, wt/epm/EPMDocumentMaster, wt/epm/familytable/EPMSepFamilyTableMaster
                };
/* 301*/        String as[] = {
/* 301*/            "name", "name", "name", "name"
                };
/* 304*/label0:
/* 304*/        for (int k = 0; k < aclass.length; k++) {
/* 305*/            Class class1 = aclass[k];
/* 306*/            Class class2 = aclass1[k];
/* 307*/            String s = as[k];
/* 314*/            QuerySpec queryspec = new QuerySpec();
/* 315*/            int i = queryspec.appendClassList(class1, true);
/* 316*/            int j = queryspec.appendClassList(class2, true);
/* 317*/            queryspec.appendWhere(new SearchCondition(class2, s, "=", versionName), new int[] {
/* 317*/                j
                    });
/* 318*/            queryspec.appendAnd();
/* 319*/            queryspec.appendWhere(new SearchCondition(class1, "masterReference.key.id", class2, "thePersistInfo.theObjectIdentifier.id"), new int[] {
/* 319*/                i, j
                    });
/* 320*/            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
/* 322*/            do {
/* 322*/                if (!queryresult.hasMoreElements()) {
/* 323*/                    continue label0;
                        }
/* 323*/                Object aobj[] = (Object[])(Object[])queryresult.nextElement();
/* 324*/                Versioned versioned = (Versioned)aobj[0];
/* 326*/                WTOrganization wtorganization = WTContainerHelper.getContainer((WTContained)versioned).getOrganization();
/* 327*/                if ((filterOrg == null || filterOrg.equals(wtorganization)) && oldRevision.equals(versioned.getVersionIdentifier().getVersionId())) {
/* 334*/                    hashset.add(versioned);
/* 335*/                    Mastered mastered = (Mastered)aobj[1];
/* 336*/                    hashset1.add(mastered);
                        }
                    } while (true);
                }

/* 339*/        arraylist.addAll(hashset);
/* 340*/        setRev(arraylist, hashset1);
            }

            public static void setRevByOid() throws Throwable {
/* 349*/        HashSet hashset = new HashSet();
/* 350*/        HashSet hashset1 = new HashSet();
/* 351*/        ArrayList arraylist = new ArrayList();
/* 352*/        String s = "";
/* 355*/        wt.fc.Persistable persistable = null;
/* 360*/        try {
/* 360*/            ObjectReference objectreference = ObjectReference.newObjectReference(ObjectIdentifier.newObjectIdentifier(objectId));
/* 361*/            persistable = objectreference.getObject();
                }
/* 363*/        catch (Exception exception) { }
/* 365*/        if (persistable != null) {
                    Class class1;
                    Class class2;
/* 367*/            if (persistable instanceof WTPart) {
/* 368*/                versionName = ((WTPart)persistable).getName();
/* 369*/                class1 = wt/part/WTPart;
/* 370*/                class2 = wt/part/WTPartMaster;
                    } else
/* 372*/            if (persistable instanceof WTDocument) {
/* 373*/                versionName = ((WTDocument)persistable).getName();
/* 374*/                class1 = wt/doc/WTDocument;
/* 375*/                class2 = wt/doc/WTDocumentMaster;
                    } else
/* 377*/            if (persistable instanceof EPMDocument) {
/* 378*/                versionName = ((EPMDocument)persistable).getName();
/* 379*/                class1 = wt/epm/EPMDocument;
/* 380*/                class2 = wt/epm/EPMDocumentMaster;
                    } else
/* 382*/            if (persistable instanceof EPMSepFamilyTable) {
/* 383*/                versionName = ((EPMSepFamilyTable)persistable).getName();
/* 384*/                class1 = wt/epm/familytable/EPMSepFamilyTable;
/* 385*/                class2 = wt/epm/familytable/EPMSepFamilyTableMaster;
                    } else {
/* 388*/                throw new WTException(MessageFormat.format("Invalid oid: {0} does not refer to a WTPart, WTDocument, EPMDocument or EPMSepFamilyTable", new Object[] {
/* 388*/                    objectId
                        }));
                    }
/* 391*/            Versioned versioned = (Versioned)persistable;
/* 392*/            long l = versioned.getMaster().getPersistInfo().getObjectIdentifier().getId();
/* 393*/            String s1 = versioned.getVersionIdentifier().getVersionId();
/* 401*/            QuerySpec queryspec = new QuerySpec();
/* 402*/            int i = queryspec.appendClassList(class1, true);
/* 403*/            int j = queryspec.appendClassList(class2, true);
/* 404*/            queryspec.appendWhere(new SearchCondition(class2, "thePersistInfo.theObjectIdentifier.id", "=", l), new int[] {
/* 404*/                j
                    });
/* 405*/            queryspec.appendAnd();
/* 406*/            queryspec.appendWhere(new SearchCondition(class1, "masterReference.key.id", class2, "thePersistInfo.theObjectIdentifier.id"), new int[] {
/* 406*/                i, j
                    });
/* 407*/            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
/* 409*/            do {
/* 409*/                if (!queryresult.hasMoreElements()) {
/* 410*/                    break;
                        }
/* 410*/                Object aobj[] = (Object[])(Object[])queryresult.nextElement();
/* 411*/                Versioned versioned1 = (Versioned)aobj[0];
/* 413*/                WTOrganization wtorganization = WTContainerHelper.getContainer((WTContained)versioned1).getOrganization();
/* 414*/                if ((filterOrg == null || filterOrg.equals(wtorganization)) && s1.equals(versioned1.getVersionIdentifier().getVersionId()) && versioned.getPersistInfo().getObjectIdentifier().getId() != versioned1.getPersistInfo().getObjectIdentifier().getId()) {
/* 426*/                    hashset.add(versioned1);
/* 427*/                    Mastered mastered = (Mastered)aobj[1];
/* 428*/                    hashset1.add(mastered);
                        }
                    } while (true);
/* 432*/            arraylist.add(versioned);
/* 433*/            arraylist.addAll(hashset);
                }
/* 436*/        setRev(arraylist, hashset1);
            }

            private static void setRev(ArrayList arraylist, Collection collection) throws Throwable {
/* 449*/        if (arraylist.size() == 0) {
/* 450*/            print("\nNo accessible versions found.");
/* 451*/            return;
                }
/* 455*/        print("\nStatus Before Update:");
/* 456*/        print("\nVersions");
/* 457*/        print("Oid, Name, Number, Revision, Container");
                String s;
                String s2;
                String s4;
                String s6;
/* 458*/        for (Iterator iterator = arraylist.iterator(); iterator.hasNext(); print((new StringBuilder()).append(s).append(", ").append(versionName).append(", ").append(s2).append(", ").append(s4).append(", ").append(s6).toString())) {
/* 459*/            Versioned versioned = (Versioned)iterator.next();
/* 460*/            s = versioned.getPersistInfo().getObjectIdentifier().getStringValue();
/* 461*/            s2 = getNumber(versioned);
/* 462*/            s4 = VersionControlHelper.getIterationDisplayIdentifier(versioned).getLocalizedMessage(SessionHelper.getLocale());
/* 463*/            s6 = IdentityFactory.getDisplayIdentifier(WTContainerHelper.getContainer((WTContained)versioned)).getLocalizedMessage(SessionHelper.getLocale());
                }

/* 467*/        if (newRevision == null) {
/* 468*/            print("\nNo revision id specified. No update attempted.");
                } else {
/* 472*/            if (collection.size() > 1) {
/* 473*/                print("\nToo many applicable masters: Can only update objects with one associated master.\n");
/* 474*/                throw new WTException("Too many applicable masters: Can only update objects with one associated master.");
                    }
/* 484*/            VersionControlHelper.service.changeRevision((Versioned)arraylist.get(0), newRevision, true);
/* 487*/            print("\n\nStatus After Update:");
/* 488*/            print("\nVersions");
/* 489*/            print("Oid, Name, Number, Revision, Container");
                    String s1;
                    String s3;
                    String s5;
                    String s7;
/* 490*/            for (Iterator iterator1 = arraylist.iterator(); iterator1.hasNext(); print((new StringBuilder()).append(s1).append(", ").append(versionName).append(", ").append(s3).append(", ").append(s5).append(", ").append(s7).toString())) {
/* 491*/                Versioned versioned1 = (Versioned)iterator1.next();
/* 492*/                versioned1 = (Versioned)PersistenceHelper.manager.refresh(versioned1);
/* 493*/                s1 = versioned1.getPersistInfo().getObjectIdentifier().getStringValue();
/* 494*/                s3 = getNumber(versioned1);
/* 495*/                s5 = VersionControlHelper.getIterationDisplayIdentifier(versioned1).getLocalizedMessage(SessionHelper.getLocale());
/* 496*/                s7 = IdentityFactory.getDisplayIdentifier(WTContainerHelper.getContainer((WTContained)versioned1)).getLocalizedMessage(SessionHelper.getLocale());
                    }

                }
            }

            private static String getStringArg(String as[], String s) {
/* 512*/        String s1 = null;
/* 513*/        for (int i = 0; i < as.length; i++) {
/* 514*/            String s2 = as[i];
/* 515*/            if (s2.equalsIgnoreCase(s) && i + 1 < as.length) {
/* 516*/                s1 = as[i + 1];
                    }
                }

/* 520*/        return s1;
            }

            private static boolean getBooleanArg(String as[], String s) {
/* 533*/        boolean flag = false;
/* 534*/        for (int i = 0; i < as.length; i++) {
/* 535*/            String s1 = as[i];
/* 536*/            if (s1.equalsIgnoreCase(s)) {
/* 537*/                flag = true;
                    }
                }

/* 541*/        return flag;
            }

            private static WTOrganization getOrganization(String s) throws WTException {
/* 552*/        WTOrganization wtorganization = null;
/* 553*/        if (s != null) {
/* 554*/            wt.org.DirectoryContextProvider directorycontextprovider = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null, (String[])null);
/* 555*/            wtorganization = OrganizationServicesHelper.manager.getOrganization(s, directorycontextprovider);
                }
/* 558*/        return wtorganization;
            }

            private static String getNumber(Versioned versioned) {
/* 569*/        String s = "";
/* 572*/        ObjectReference objectreference = versioned.getMasterReference();
/* 573*/        if (objectreference == null) {
/* 574*/            return s;
                }
/* 579*/        try {
/* 579*/            Mastered mastered = (Mastered)objectreference.getObject();
/* 580*/            Class class1 = mastered.getClass();
/* 583*/            if (!getNumberMap.containsKey(class1)) {
/* 585*/                try {
/* 585*/                    getNumberMap.put(class1, class1.getMethod("getNumber", (Class[])null));
                        }
/* 587*/                catch (Exception exception1) {
/* 588*/                    getNumberMap.put(class1, null);
                        }
                    }
/* 593*/            Method method = (Method)getNumberMap.get(class1);
/* 594*/            if (method != null) {
/* 595*/                Object obj = method.invoke(mastered, (Object[])null);
/* 596*/                if (obj instanceof String) {
/* 597*/                    s = (String)obj;
                        }
                    }
                }
/* 601*/        catch (Exception exception) { }
/* 603*/        return s;
            }

            private static void printHelp() {
/* 610*/        print("Usage: java wt.vc.ChangeRevisionLabelUtility  [args]\nArgs:\n   -name <name of the versioned object>\n     REQUIRED in absence of '-oid' argument.\n     The argument '-oldRev' is required in conjunction with this argument.\n   -oldRev <value of the old revision label of the versioned object>\n     REQUIRED if the '-name' is specified as an argument.\n   -oid <object identifier of the versioned object>\n     REQUIRED in absence of '-name' argument.\n     Example Value: wt.part.WTPart:10001\n   -newRev <value to set the revision label of the versioned object to>\n     OPTIONAL : If not present the utility will display information about\n                the versioned objects specified by the '-name' or '-oid'\n                argument, including the current revision label.\n   -org <name of the org container the versioned object exists in>\n     OPTIONAL : If present the utility will only consider objects contained\n                in the org.\n   -username <name of the user to authenticate as>\n     OPTIONAL : If present in conjunction with the '-password' argument\n                the utility will attempt authenticate as the user, and no\n                prompt will be made if successful.  Otherwise there will be\n                a login prompt.\n   -password <password of the user to authenticate as>\n     OPTIONAL : If present in conjunction with the '-username' argument\n                the utility will attempt authenticate with the password,\n                and no prompt will be made if successful.  Otherwise there\n                will be a login prompt.\n   -help\n     OPTIONAL : If present, displays this usage message.");
            }

            private static void print(String s) {
/* 648*/        if (RemoteMethodServer.ServerFlag && MethodContext.getActiveCount() > 0) {
/* 650*/            MethodContext.getContext().sendFeedback(new StatusFeedback(s));
                } else {
/* 653*/            System.out.println(s);
                }
            }

}
