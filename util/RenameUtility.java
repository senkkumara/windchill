package ext.hydratight.util;

import java.io.*;
import java.util.*;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.*;
import wt.load.LoadServerHelper;
import wt.method.RemoteAccess;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.applicationcontext.implementation.ServiceProperties;
import wt.util.*;

public class MassRename
    implements RemoteAccess
{

    public MassRename()
    {
    }

    public static String getValue(Hashtable hashtable, String s, boolean flag)
        throws WTException
    {
        if(VERBOSE)
            LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : getValue()");
        String s1 = (String)hashtable.get(s);
        if(s1 == null)
        {
            String as[] = {
                s
            };
            String s2 = WTMessage.getLocalizedMessage("wt.part.partResource", "10", as);
            throw new WTException(s2);
        }
        if(flag && s1.equals(""))
            throw new WTException((new StringBuilder()).append("ERROR: value for ").append(s).append("is REQUIRED ").toString());
        if(!flag && s1.equals(""))
            s1 = null;
        return s1;
    }

    public static WTDocumentMaster getWTDocumentMaster(String s)
    {
        if(VERBOSE)
            LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : getWTDocumentMaster()");
        WTDocumentMaster wtdocumentmaster = null;
        Object obj = null;
        try
        {
            QuerySpec queryspec = new QuerySpec(wt/doc/WTDocumentMaster);
            SearchCondition searchcondition = new SearchCondition(wt/doc/WTDocumentMaster, "number", "=", s);
            queryspec.appendWhere(searchcondition);
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
            if(queryresult.size() > 0)
                wtdocumentmaster = (WTDocumentMaster)queryresult.nextElement();
        }
        catch(WTException wtexception)
        {
            wtexception.printStackTrace();
        }
        return wtdocumentmaster;
    }

    public static boolean renameWTDocument(Hashtable hashtable, Hashtable hashtable1, Vector vector)
    {
        String s;
        String s6;
        String s7;
        String s8;
        LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : renameWTDocument()");
        LoadServerHelper.printMessage((new StringBuilder()).append("hashtable :").append(hashtable).toString());
        s = "RenameWTDocument.log";
        boolean flag = false;
        Object obj = null;
        s6 = null;
        s7 = null;
        s8 = null;
        Object obj1 = null;
        Object obj2 = null;
        try
        {
            s6 = getValue(hashtable, "orgNumber", true).toUpperCase().trim();
            s7 = getValue(hashtable, "newNumber", true).toUpperCase().trim();
            s8 = getValue(hashtable, "newName", true).trim();
            if(VERBOSE)
                addToLogs((new StringBuilder()).append("Original Number: ").append(s6).append(", New Number: ").append(s7).append(", New Name: ").append(s8).toString(), s);
        }
        catch(WTException wtexception)
        {
            addToLogs((new StringBuilder()).append("\nInput Data Error for ").append(hashtable).append("  ").append(wtexception.toString()).toString(), s);
            return true;
        }
        WTDocumentMaster wtdocumentmaster;
        wtdocumentmaster = getWTDocumentMaster(s6);
        if(wtdocumentmaster == null)
        {
            String s1 = (new StringBuilder()).append("ERROR: Can not find WTDocument: ").append(s6).toString();
            addToLogs(s1, s);
            return true;
        }
        WTDocumentMaster wtdocumentmaster1 = getWTDocumentMaster(s7);
        if(wtdocumentmaster1 != null)
        {
            String s2 = (new StringBuilder()).append("ERROR: WTDocument already exists in database: ").append(s7).toString();
            addToLogs(s2, s);
            return true;
        }
        try
        {
            try
            {
                WTDocumentMasterIdentity wtdocumentmasteridentity = (WTDocumentMasterIdentity)wtdocumentmaster.getIdentificationObject();
                s6 = wtdocumentmasteridentity.getNumber();
                wtdocumentmasteridentity.setNumber(s7);
                wtdocumentmasteridentity.setName(s8);
                IdentityHelper.service.changeIdentity(wtdocumentmaster, wtdocumentmasteridentity);
                PersistenceHelper.manager.refresh(wtdocumentmaster);
                String s3 = (new StringBuilder()).append("SUCCESS: Renamed successfully: ").append(s6).append(" to ").append(s7).toString();
                addToLogs(s3, s);
            }
            catch(WTException wtexception1)
            {
                String s4 = (new StringBuilder()).append("FAIL: Rename error: ").append(s6).append(" to ").append(s7).append(": ").append(wtexception1).toString();
                addToLogs(s4, s);
                return true;
            }
        }
        catch(Exception exception)
        {
            String s5 = (new StringBuilder()).append("FAIL: Rename error: ").append(s6).append(" to ").append(s7).append(": ").append(exception).toString();
            addToLogs(s5, s);
        }
        return true;
    }

    public static WTPartMaster getWTPartMaster(String s)
    {
        if(VERBOSE)
            LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : getWTPartMaster()");
        WTPartMaster wtpartmaster = null;
        Object obj = null;
        try
        {
            QuerySpec queryspec = new QuerySpec(wt/part/WTPartMaster);
            SearchCondition searchcondition = new SearchCondition(wt/part/WTPartMaster, "number", "=", s);
            queryspec.appendWhere(searchcondition);
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
            if(queryresult.size() > 0)
                wtpartmaster = (WTPartMaster)queryresult.nextElement();
        }
        catch(WTException wtexception)
        {
            wtexception.printStackTrace();
        }
        return wtpartmaster;
    }

    public static boolean renameWTPart(Hashtable hashtable, Hashtable hashtable1, Vector vector)
    {
        String s;
        String s6;
        String s7;
        String s8;
        LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : renameWTPart()");
        LoadServerHelper.printMessage((new StringBuilder()).append("hashtable :").append(hashtable).toString());
        s = "RenameWTPart.log";
        boolean flag = false;
        Object obj = null;
        s6 = null;
        s7 = null;
        s8 = null;
        Object obj1 = null;
        Object obj2 = null;
        try
        {
            s6 = getValue(hashtable, "orgNumber", true).toUpperCase().trim();
            s7 = getValue(hashtable, "newNumber", true).toUpperCase().trim();
            s8 = getValue(hashtable, "newName", true).trim();
            addToLogs((new StringBuilder()).append("Original Number: ").append(s6).append(", New Number: ").append(s7).append(", New Name: ").append(s8).toString(), s);
        }
        catch(WTException wtexception)
        {
            addToLogs((new StringBuilder()).append("\nInput Data Error for ").append(hashtable).append("  ").append(wtexception.toString()).toString(), s);
            return true;
        }
        WTPartMaster wtpartmaster;
        wtpartmaster = getWTPartMaster(s6);
        if(wtpartmaster == null)
        {
            String s1 = (new StringBuilder()).append("ERROR: Can not find WTPart: ").append(s6).toString();
            addToLogs(s1, s);
            return true;
        }
        WTPartMaster wtpartmaster1 = getWTPartMaster(s7);
        if(wtpartmaster1 != null)
        {
            String s2 = (new StringBuilder()).append("ERROR: WTPart already exists in database: ").append(s7).toString();
            addToLogs(s2, s);
            return true;
        }
        try
        {
            try
            {
                WTPartMasterIdentity wtpartmasteridentity = (WTPartMasterIdentity)wtpartmaster.getIdentificationObject();
                s6 = wtpartmasteridentity.getNumber();
                wtpartmasteridentity.setNumber(s7);
                wtpartmasteridentity.setName(s8);
                IdentityHelper.service.changeIdentity(wtpartmaster, wtpartmasteridentity);
                PersistenceHelper.manager.refresh(wtpartmaster);
                String s3 = (new StringBuilder()).append("SUCCESS: Renamed successfully: ").append(s6).append(" to ").append(s7).toString();
                addToLogs(s3, s);
            }
            catch(WTException wtexception1)
            {
                String s4 = (new StringBuilder()).append("FAIL: Rename error: ").append(s6).append(" to ").append(s7).append(": ").append(wtexception1).toString();
                addToLogs(s4, s);
                return true;
            }
        }
        catch(Exception exception)
        {
            String s5 = (new StringBuilder()).append("FAIL: Rename error: ").append(s6).append(" to ").append(s7).append(": ").append(exception).toString();
            addToLogs(s5, s);
        }
        return true;
    }

    public static void addToLogs(String s, String s1)
    {
        if(VERBOSE)
            LoadServerHelper.printMessage("Inside Class : ext.load.MassRename & Method : addToLogs()");
        String s2 = null;
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            s2 = wtproperties.getProperty("wt.home");
        }
        catch(Exception exception)
        {
            System.out.println((new StringBuilder()).append("Error getting WTHOME: ").append(exception).toString());
        }
        String s3 = (new StringBuilder()).append(s2).append(File.separator).append("logs").append(File.separator).append(s1).toString();
        Object obj = null;
        File file = new File(s3);
        try
        {
            FileOutputStream fileoutputstream;
            if(file.exists())
            {
                fileoutputstream = new FileOutputStream(file.getAbsolutePath(), true);
            } else
            {
                file.createNewFile();
                fileoutputstream = new FileOutputStream(file.getAbsolutePath());
            }
            PrintWriter printwriter = new PrintWriter(fileoutputstream, true);
            printwriter.println(s);
            printwriter.flush();
            printwriter.close();
            printwriter = null;
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    private static PrintWriter _pwriter = null;
    public static final String CLASSNAME = "ext.load.MassRename";
    public static boolean VERBOSE = false;

    static 
    {
        try
        {
            ServiceProperties serviceproperties = ServiceProperties.getServiceProperties("WTServiceProviderFromProperties");
            String s = serviceproperties.getProperty("ext.load.massrename.verbose");
            if(s.trim().equals("true"))
                VERBOSE = true;
        }
        catch(Exception exception)
        {
            VERBOSE = false;
        }
    }
}