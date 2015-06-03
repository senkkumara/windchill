package com.ptc.cust.wsdelete;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import wt.doc.WTDocument;
import wt.epm.EPMApplicationType;
import wt.epm.EPMContextHelper;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentHelper;
import wt.epm.EPMDocumentManager;
import wt.epm.workspaces.EPMBaselineHelper;
import wt.epm.workspaces.EPMBaselineService;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.epm.workspaces.EPMWorkspaceManager;
import wt.fc.ObjectVectorIfc;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTSet;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.FolderService;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.ObjectIsStaleException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.LogFile;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class DeleteWorkspaces
{
  private ArrayList userNamesList = new ArrayList();
  private WTPrincipal authenticationObject;
  private static final String WS_NAME_SEPARATOR = ":";

  public static void main(String[] paramArrayOfString)
  {
    DeleteWorkspaces localDeleteWorkspaces = new DeleteWorkspaces();
    try {
      LogFile.init("DeleteWorkspaces.log", true, true);
      localDeleteWorkspaces.parseArgs(paramArrayOfString);
      localDeleteWorkspaces.authenticate();
      localDeleteWorkspaces.processDelete();
    }
    catch (DeleteWorkspacesException localDeleteWorkspacesException) {
      System.out.println(localDeleteWorkspacesException.getLocalizedMessage());
    }
    catch (Exception localException) {
      localException.printStackTrace();
    }
    System.out.println("\n\n INFO:: Exiting workspace delete utility.");
    System.exit(0);
  }

  private void authenticate() throws DeleteWorkspaces.DeleteWorkspacesException {
    if (this.authenticationObject != null) {
      return;
    }

    try
    {
      this.authenticationObject = SessionHelper.getPrincipal();
      EPMContextHelper.setApplication(EPMApplicationType.getEPMApplicationTypeDefault());
    }
    catch (WTPropertyVetoException localWTPropertyVetoException) {
      throw new DeleteWorkspacesException(localWTPropertyVetoException.getLocalizedMessage());
    }
    catch (WTException localWTException) {
      throw new DeleteWorkspacesException(localWTException.getLocalizedMessage());
    }
  }

  private void parseArgs(String[] paramArrayOfString) throws DeleteWorkspaces.DeleteWorkspacesException
  {
    if ((paramArrayOfString == null) || (paramArrayOfString.length < 1)) {
      usage();
    }
    for (int i = 0; i < paramArrayOfString.length; i++)
      this.userNamesList.add(paramArrayOfString[i]);
  }

  private void usage() throws DeleteWorkspaces.DeleteWorkspacesException
  {
    throw new DeleteWorkspacesException("Usage : " + getClass().getName() + " <user> [ <user> ...]\n\nWhen prompted to login authenticate using an Administrator privileged user\n");
  }

  private void processDelete() throws DeleteWorkspaces.DeleteWorkspacesException {
    try {
      Iterator localIterator1 = this.userNamesList.iterator();
      while (localIterator1.hasNext()) {
        String str = (String)localIterator1.next();
        System.out.println("\n INFO:: ******* Processing user " + str + "******* \n");
        WTUser localWTUser = getWorkspaceOwners(str);
        if (localWTUser != null)
        {
          System.out.println("INFO:: Getting all the workspaces owned by " + str);
          WTSet localWTSet = EPMWorkspaceHelper.manager.getWorkspaces(localWTUser, null);
          if (localWTSet.size() == 0) {
            System.out.println("WARNING:: Could not find any workspaces owned by name : " + str);
          }
          else {
            System.out.println("SUCCESS:: Found " + localWTSet.size() + " workspaces owned by " + str);
            for (Iterator localIterator2 = localWTSet.persistableIterator(); localIterator2.hasNext(); ) {
              EPMWorkspace localEPMWorkspace = (EPMWorkspace)localIterator2.next();
              System.out.println("INFO:: About to delete workspace: " + localEPMWorkspace.getName());
              purgeWorkspace(localEPMWorkspace);
              localEPMWorkspace = (EPMWorkspace)PersistenceHelper.manager.refresh(localEPMWorkspace);
              try {
                PersistenceHelper.manager.delete(localEPMWorkspace);
              }
              catch (ObjectIsStaleException localObjectIsStaleException)
              {
                PersistenceHelper.manager.delete(localEPMWorkspace);
              }
              catch (WTException localWTException2)
              {
                if ((localWTException2.getNestedThrowable() instanceof ObjectIsStaleException)) {
                  PersistenceHelper.manager.delete(localEPMWorkspace);
                }
                else {
                  throw localWTException2;
                }
              }
              System.out.println("SUCCESS:: Successfully deleted " + localEPMWorkspace.getName() + "\n");
            }
            System.out.println("SUCCESS:: ******* Successfully deleted all workspaces of user " + str + "******* \n");
          }
        }
      }
    } catch (WTException localWTException1) { localWTException1.printStackTrace();
      throw new DeleteWorkspacesException(localWTException1.getLocalizedMessage()); }
  }

  private void purgeWorkspace(EPMWorkspace paramEPMWorkspace) throws WTException
  {
    if (paramEPMWorkspace == null) {
      return;
    }
    QueryResult localQueryResult = FolderHelper.service.findFolderContents(paramEPMWorkspace.getFolder());
    System.out.println("INFO:: Number of objects in workspace folder  : " + localQueryResult.size());

    WTArrayList localWTArrayList = new WTArrayList();
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();

    String str1 = paramEPMWorkspace.getFolder().getFolderPath();

    System.out.println("INFO:: WorkspaceFolderPath : " + str1);

    while (localQueryResult.hasMoreElements()) {
      localObject1 = (FolderEntry)localQueryResult.nextElement();
      System.out.println("INFO:: Object from workspace folder : " + localObject1);
      if (((localObject1 instanceof Workable)) && (WorkInProgressHelper.isWorkingCopy((Workable)localObject1))) {
        localWTArrayList.add((Persistable)localObject1);
      }
      else {
        String str2 = FolderHelper.getLocation((FolderEntry)localObject1);
        System.out.println("INFO:: Location : " + str2);

        if (str1.equals(str2)) {
          if (((localObject1 instanceof EPMDocument)) || ((localObject1 instanceof WTDocument)) || ((localObject1 instanceof WTPart)))
            localArrayList1.add(localObject1);
          else {
            localArrayList2.add(localObject1);
          }
        }
      }
    }
    System.out.println("INFO:: Number of working copies : " + localWTArrayList.size());
    System.out.println("INFO:: Number of new objects : " + localArrayList1.size());
    System.out.println("INFO:: Number of other objects : " + localArrayList2.size());

    if (localWTArrayList.size() > 0) {
      localObject1 = (Workable[])localWTArrayList.toArray(new Workable[localWTArrayList.size()]);
      System.out.println("INFO:: Undo checkout : " + localObject1.length + " objects.");
      try {
        EPMWorkspaceHelper.manager.undoCheckout(paramEPMWorkspace, localWTArrayList);
      }
      catch (WTException localWTException1)
      {
        localWTException1.printStackTrace();
      }
      System.out.println("SUCCESS:: Done with undo checkout ...");
    }

    System.out.println("INFO:: Deleting " + localArrayList1.size() + " objects.");
    EPMDocumentHelper.service.deleteObjects(localArrayList1);

    System.out.println("INFO:: Deleting " + localArrayList2.size() + " objects.");
    Object localObject1 = localArrayList2.iterator();
    while (((Iterator)localObject1).hasNext()) {
      localObject2 = (Persistable)((Iterator)localObject1).next();
      try {
        PersistenceHelper.manager.delete((Persistable)localObject2);
      }
      catch (WTException localWTException2)
      {
        localWTException2.printStackTrace();
      }

    }

    Object localObject2 = EPMBaselineHelper.service.getBaselineItems(paramEPMWorkspace);
    System.out.println("INFO:: Baseline Items Count : " + ((QueryResult)localObject2).size());
    try {
      EPMBaselineHelper.service.remove(paramEPMWorkspace, ((QueryResult)localObject2).getObjectVectorIfc().getVector());
    }
    catch (WTException localWTException3)
    {
      localWTException3.printStackTrace();
    }

    System.out.println("SUCCESS:: Done with removing objects from workspace ...");
  }

  private WTUser getWorkspaceOwners(String paramString) throws WTException {
    QuerySpec localQuerySpec = new QuerySpec(WTUser.class);
    SearchCondition localSearchCondition = new SearchCondition(WTUser.class, "name", "=", paramString);
    localQuerySpec.appendWhere(localSearchCondition, 0, -1);

    QueryResult localQueryResult = PersistenceHelper.manager.find(localQuerySpec);

    if (localQueryResult.size() == 1) {
      WTUser localWTUser = (WTUser)localQueryResult.nextElement();
      System.out.println("SUCCESS:: Found user : " + paramString);
      return localWTUser;
    }if (localQueryResult.size() == 0) {
      System.out.println("WARNING:: No user with user name : " + paramString + " found....");
    } else {
      System.out.println("WARNING:: More than one user with user name : " + paramString + " found....");
      System.out.println("WARNING:: No processing done for user : " + paramString);
    }
    return null;
  }

  private class DeleteWorkspacesException extends Exception
  {
    public DeleteWorkspacesException() {
    }

    public DeleteWorkspacesException(String arg2) {
      super();
    }
  }
}