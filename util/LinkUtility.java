package ext.hydratight.util;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;

import com.infoengine.SAK.Task;
import com.infoengine.SAK.IeService;
import com.infoengine.au.NamingService;

public class MassAssociate {

    public static void main(String args[]) throws Exception,IOException {
	BufferedReader in = new BufferedReader(new FileReader(args[0]));
	Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("outfile.txt"), "UTF8"));

      IeService ie = new IeService();
      ie.setCredentials(args[1],args[2]);

      String remoteTask="http://" + args[3] + "/Windchill/servlet/IE/tasks/ext/kohler/kps/loader/linkParttoEPM.xml";

      String workspacename = args[4];

	javax.servlet.http.HttpServletRequest request;

	//write out the headers for the output file
	out.write("proefilename, proerev, linktype, wtpartnumber, wtpartrev, status\n");
	
	if(!args[0].equals(null))
	{
		String str;

		int i=0;
		while ((str = in.readLine()) != null) {
			i++;
			StringTokenizer st = new StringTokenizer(str,",");
			String proefilename = st.nextToken();
			String proerev = st.nextToken();
			String linktype = st.nextToken();
			String wtpartnumber = st.nextToken();
			String wtpartrev = st.nextToken();
			String containerRef = st.nextToken();
			String serial = String.valueOf(System.currentTimeMillis());
			String taskstring = remoteTask+"?proefilename=" + proefilename + "&proerev=" + proerev + "&linktype=" + linktype + "&wtpartnumber=" + wtpartnumber  + "&workspacename=" + workspacename+ "&wtpartrev=" + wtpartrev + "&containerRef=" + containerRef + "&username=" + args[1] + "&passwd=" + args[2] + "&linenum="+String.valueOf(i)+ "&serial="+serial;

			System.out.println("Line " + i + "," + taskstring);
		boolean found=false;
			ie.beginRequest(taskstring);
         	//while(!found){	
				ie.updateCollection();
			
			//Set result
			String status = ie.getAttributeValue("status",0,"STATUS");
			out.write("Line " + i + "," + proefilename + "," + proerev + "," +  linktype + "," + wtpartnumber + "," + wtpartrev + "," + status + "\n");
			out.flush();
			String linenum = ie.getAttributeValue("status",0,"LINENUM");
			//System.out.println("Expected: " + String.valueOf(i) +" Found: "+linenum);
			//try{
			//Thread.sleep(500);
			//}
			//catch (Exception e){};
			
			//if (String.valueOf(i).equals(linenum)){
			//found=true;
			//}
			//}
		}
	in.close();
	out.close();
	}
	else{
		System.out.println("Usage: windchill ext.kohler.kps.loader.BulkLoadCADPartLinks <input file name> <username> <password> <servername> <workspacename>");
	}

    }

}



////////////////////////////////////////////////////////////////////////////////////////////////////


package ext.hydratight.util;

import com.infoengine.SAK.IeService;
import java.io.*;
import java.util.StringTokenizer;

public class ValidateAssociation
{

    public ValidateAssociation()
    {
    }

    public static void main(String args[])
        throws Exception, IOException
    {
        BufferedReader bufferedreader = new BufferedReader(new FileReader(args[0]));
        BufferedWriter bufferedwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("verification.txt"), "UTF8"));
        IeService ieservice = new IeService();
        ieservice.setCredentials(args[1], args[2]);
        String s = (new StringBuilder()).append("http://").append(args[3]).append("/Windchill/servlet/IE/tasks/ext/kohler/kps/loader/QueryCADPartLinks.xml").toString();
        bufferedwriter.write("Line,wtpartnumber, wtpartrev\n");
        if(!args[0].equals(null))
        {
            int i = 0;
            do
            {
                String s1;
                if((s1 = bufferedreader.readLine()) == null)
                    break;
                i++;
                StringTokenizer stringtokenizer = new StringTokenizer(s1, ",");
                String s2 = stringtokenizer.nextToken();
                String s3 = stringtokenizer.nextToken();
                String s4 = stringtokenizer.nextToken();
                String s5 = stringtokenizer.nextToken();
                String s6 = stringtokenizer.nextToken();
                String s7 = (new StringBuilder()).append(s).append("?proefilename=").append(s2).append("&proerev=").append(s3).append("&linktype=").append(s4).append("&wtpartnumber=").append(s5).append("&wtpartrev=").append(s6).append("&username=").append(args[1]).append("&passwd=").append(args[2]).toString();
                System.out.println((new StringBuilder()).append("Line ").append(i).append(",").append(s7).toString());
                ieservice.beginRequest(s7);
                ieservice.updateCollection();
                if(s4.equals("Owner"))
                    if(ieservice.getElementCount("ownerlinks") > 0)
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("LINK FOUND").append("\n").toString());
                    else
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("NO LINK FOUND").append("\n").toString());
                if(s4.equals("Image"))
                    if(ieservice.getElementCount("imagelinks") > 0)
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("LINK FOUND").append("\n").toString());
                    else
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("NO LINK FOUND").append("\n").toString());
                if(s4.equals("Content"))
                    if(ieservice.getElementCount("contentlinks") > 0)
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("LINK FOUND").append("\n").toString());
                    else
                        bufferedwriter.write((new StringBuilder()).append("Line ").append(i).append(",").append(s2).append(",").append(s3).append(",").append(s4).append(",").append(s5).append(",").append(s6).append(",").append("NO LINK FOUND").append("\n").toString());
            } while(true);
            bufferedreader.close();
            bufferedwriter.close();
        } else
        {
            System.out.println("Usage: windchill ext.kohler.kps.loader.VerifyCADPartLinks <input file name> <username> <password> <servername>");
        }
    }
}
