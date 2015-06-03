/*
Renold change lifecycle template command line tool - version 2
Author: Richard Morrow, INNEO - 10th February 2014
Usage:
Open Windchill shell
Compile java source into <WTHOME>/codebase/com/renold/util
Run this command  in folder <WTHOME>/codebase/com/renold/util:
		windchill com.renold.util.lcUpdate <username> <password> <full filepath> <folder for log files> <new lifecycle template name> <org_name>
where <full filepath> is a 2 column csv list : <Object Number>,<Version>,<Type> where Type is one of EPMDocument, WTPart or WTDocument
If <Version> is null, the latest version will be used
Use an administrative user so all data can be accessed
*/
package ext.hydratight.util;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.fc.*;
import wt.fc.collections.*;
import wt.folder.*;
import wt.inf.container.*;
import wt.lifecycle.*;
import wt.query.*;
import wt.doc.*;
import wt.doc.WTDocument;
import wt.epm.*;
import wt.epm.EPMDocument;
import wt.part.*;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.org.WTPrincipal;
import wt.util.*;
import wt.vc.baseline.*;
import wt.vc.baseline.ManagedBaseline;
import wt.pds.*;
import wt.series.*;
import wt.series.MultilevelSeries;
import wt.vc.*;
import wt.vc.config.*;
import java.util.*;
import java.io.BufferedReader; 
import java.io.BufferedWriter; 
import java.io.File; 
import java.io.FileReader; 
import java.io.FileWriter; 
import java.io.IOException; 
import java.io.FileInputStream; 
import java.io.DataInputStream; 
import java.io.LineNumberReader;
import java.io.InputStreamReader; 
import java.io.Reader; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.Scanner; 
import java.util.StringTokenizer; 
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class lcUpdate{

public static String logFile = null;
public static String errFile = null;

public static void main(String [] args) throws Exception
{
RemoteMethodServer rms = RemoteMethodServer.getDefault();
rms.setUserName(args[0]);
rms.setPassword(args[1]);
System.out.println("Command line utility for updating the folders of objects in a csv file....");
String fileList = (args[2]);
  FileInputStream fstream = new FileInputStream(fileList);
  DataInputStream in = new DataInputStream(fstream);
  BufferedReader br = new BufferedReader(new InputStreamReader(in));
  Map<String,Map<String,String>> tokens = tokenize(br);  
String logFolder = args[3];
if (!(logFolder.endsWith("\\"))) logFolder = logFolder + "\\";
Calendar currentDate = Calendar.getInstance();
SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
String dateNow = formatter.format(currentDate.getTime());
logFile = logFolder + "\\LCTemplateUpdateLog-" + dateNow + ".csv";
errFile = logFolder + "\\LCTemplateUpdateErrors-" + dateNow + ".csv";
System.out.println("Log files: " + logFile + "," + errFile);
String orgPath = args[5];
WTContainerRef wtcr = WTContainerHelper.service.getByPath("/wt.inf.container.OrgContainer/" + orgPath);
String lcTemplate = args[4];
LifeCycleTemplateReference lctr = LifeCycleHelper.service.getLifeCycleTemplateReference(lcTemplate, wtcr);
System.out.println("lifeCycleTemplateRef = " + lctr);
changeLCTemplate(tokens, lctr);
}
public static void changeLCTemplate(Map<String,Map<String,String>>tokens, LifeCycleTemplateReference lctr) throws WTPropertyVetoException, WTException, IOException, LifeCycleException {	
for (Map.Entry<String,Map<String,String>> entry : tokens.entrySet()) {
	String key = entry.getKey();
	String objClass = tokens.get(key).get("class");
	System.out.println("Processing " + key);
	String objVersion = tokens.get(key).get("version");
	String number = key.split(":")[0];
	String logLine = null;
	PDMLinkProduct context = null;
         EPMDocument epm = null;
		 WTPart part = null;
		 WTDocument doc = null;
		 Folder newFolder = null;
		 QuerySpec qs = null;
		 QueryResult qr = null;
		if (objClass.equals("EPMDocument")){
		qs = new QuerySpec(EPMDocument.class);
        qs.appendWhere(new SearchCondition(EPMDocument.class, EPMDocument.NUMBER, SearchCondition.EQUAL, number), new int[] {0,1});
		LatestConfigSpec lcs = new LatestConfigSpec();
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qs = lcs.appendSearchCriteria(qs);
		}
		else
		{
		String[] spl = objVersion.split("[.]");
		String rev = spl[0];
		String iter = spl[1];
		qs.appendAnd();
		SearchCondition rsc = new SearchCondition(EPMDocument.class, "versionInfo.identifier.versionId", SearchCondition.EQUAL, rev);
		qs.appendWhere(rsc,new int[] {0});
		qs.appendAnd();
		SearchCondition isc = new SearchCondition(EPMDocument.class, "iterationInfo.identifier.iterationId", SearchCondition.EQUAL, iter);
		qs.appendWhere(isc,new int[] {0});		
		}
        qr = PersistenceHelper.manager.find(((StatementSpec)qs));
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qr = lcs.process(qr);
		}
        if (qr.hasMoreElements()) {
            epm = (EPMDocument) qr.nextElement();
					 try {
						WTList list = new WTArrayList();
						list.add(epm);
						list = LifeCycleHelper.service.reassign(list,lctr,null,true,null);
						logLine = key + ",successfully changed lifecycle template for " + epm;
						System.out.println(logLine);
						writeLog(logLine);
						} catch (WTException e) {
						System.out.println(">>>>Error: " + e.getLocalizedMessage());
						logLine = key + "," + e.getLocalizedMessage();
						System.out.println(logLine);		
						} 
			}
				else {
				System.out.println(key + ": Object not found");
				logLine = key + ",Object not found";
				writeErr(logLine);		
				}
		}
		else if (objClass.equals("WTPart")){
		qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number), new int[] {0,1});
		LatestConfigSpec lcs = new LatestConfigSpec();
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qs = lcs.appendSearchCriteria(qs);
		}
		else
		{
		String[] spl = objVersion.split("[.]");
		String rev = spl[0];
		String iter = spl[1];
		qs.appendAnd();
		SearchCondition rsc = new SearchCondition(WTPart.class, "versionInfo.identifier.versionId", SearchCondition.EQUAL, rev);
		qs.appendWhere(rsc,new int[] {0});
		qs.appendAnd();
		SearchCondition isc = new SearchCondition(WTPart.class, "iterationInfo.identifier.iterationId", SearchCondition.EQUAL, iter);
		qs.appendWhere(isc,new int[] {0});		
		}
        qr = PersistenceHelper.manager.find(((StatementSpec)qs));
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qr = lcs.process(qr);
		}
        if (qr.hasMoreElements()) {
            part = (WTPart) qr.nextElement();
					 try {
						WTList list = new WTArrayList();
						list.add(part);
						list = LifeCycleHelper.service.reassign(list,lctr,null,true,null);
						logLine = key + ",successfully changed lifecycle template for " + part;
						System.out.println(logLine);
						writeLog(logLine);
						} catch (WTException e) {
						System.out.println(">>>>Error: " + e.getLocalizedMessage());
						logLine = key + "," + e.getLocalizedMessage();
						System.out.println(logLine);		
						} 
			}
				else {
				System.out.println(key + ": Object not found");
				logLine = key + ",Object not found";
				writeErr(logLine);		
				}
		
		
		
		}
		else if (objClass.equals("WTDocument")){
		qs = new QuerySpec(WTDocument.class);
        qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, number), new int[] {0,1});
		LatestConfigSpec lcs = new LatestConfigSpec();
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qs = lcs.appendSearchCriteria(qs);
		}
		else
		{
		String[] spl = objVersion.split("[.]");
		String rev = spl[0];
		String iter = spl[1];
		qs.appendAnd();
		SearchCondition rsc = new SearchCondition(WTDocument.class, "versionInfo.identifier.versionId", SearchCondition.EQUAL, rev);
		qs.appendWhere(rsc,new int[] {0});
		qs.appendAnd();
		SearchCondition isc = new SearchCondition(WTDocument.class, "iterationInfo.identifier.iterationId", SearchCondition.EQUAL, iter);
		qs.appendWhere(isc,new int[] {0});		
		}
        qr = PersistenceHelper.manager.find(((StatementSpec)qs));
		if (objVersion==null||objVersion==""||objVersion.length()==0){
		qr = lcs.process(qr);
		}
        if (qr.hasMoreElements()) {
            doc = (WTDocument) qr.nextElement();
					 try {
						WTList list = new WTArrayList();
						list.add(doc);
						list = LifeCycleHelper.service.reassign(list,lctr,null,true,null);
						logLine = number + "," + objClass + "," + ",successfully changed lifecycle template for " + doc;
						System.out.println(logLine);
						writeLog(logLine);
						} catch (WTException e) {
						System.out.println(">>>>Error: " + e.getLocalizedMessage());
						logLine = key + "," + e.getLocalizedMessage();
						System.out.println(logLine);		
						} 
			}
				else {
				System.out.println(key + ": Object not found");
				logLine = key + ",Object not found";
				writeErr(logLine);		
				}
		
		
		
		}
		else {
		System.out.println(key + ": Object class not supported: " + objClass);
		logLine = key + ",Object class not supported," + objClass;
		writeErr(logLine);		
		}
	}
System.out.println("Update complete.");	
}
public static void writeLog(String line) throws IOException{
	  FileWriter fstream = new FileWriter(logFile, true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(line);
		out.newLine();
		out.close();
}
public static void writeErr(String line) throws IOException{
	  FileWriter fstream = new FileWriter(errFile, true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(line);
		out.newLine();
		out.close();
}
    public static Map<String,Map<String,String>> tokenize(BufferedReader br) throws IOException 
    { 
    Map<String,Map<String,String>> numberKey  = new LinkedHashMap<String,Map<String,String>>();
        try 
        { 
			String line;
			while ((line = br.readLine()) != null)   {
				Map<String,String> newValues = new HashMap<String, String>();
                String[] st = line.split(",");
				newValues.put("version", st[1]);
				newValues.put("class", st[2]);
				numberKey.put(st[0] + ":" + st[2],newValues);
            }
        } 
        finally 
        { 
            close(br); 
        }
        return numberKey; 
    } 
    public static void close(Reader r) 
    { 
        try 
        { 
            if (r != null) 
            { 
                r.close(); 
            } 
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
    }	
}