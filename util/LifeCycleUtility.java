package ext.hydratight.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTList;
import wt.feedback.StatusFeedback;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.method.MethodContext;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import wt.lifecycle.LifeCycleException;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;

/**
 *	Life Cycle Utility<br />
 *	Allows the reassigning of life cycles and setting of states.
 */
public class LifeCycleUtility
		extends Utility
		implements RemoteAccess
{
	private static final String TEMPLATE_ARG = "-template";
	private static final String STATE_ARG = "-state";
	private static final String COMMENTS_ARG = "-comments";
	private static final String TEMPLATE_HEAD = "template";
	private static final String STATE_HEAD = "state";
	private static final String COMMENTS_HEAD = "comments";
	private static final String ORG_CLASS = "/wt.inf.container.OrgContainer/";
	private static final String MISSING_ARG = "Missing Required Argument: One of {0}, {1} or {2} is required";
	private static final String CONFLICTING_ARG = "Conflicting Arguments: {0}, {1} and {2} are mutually exclusive arguments";
	private static final String MISSING_COMPLEMENTARY_ARG_1 = "Missing Required Argument: If using {0}, then {1} and {2} are also required";
	private static final String MISSING_COMPLEMENTARY_ARG_2 = "Missing Required Argument: If using {0}, then {1} is also required";
	private static final String INVALID_LIFE_CYCLE_NAME = "Invalid life cycle name: No life cycle by the name '{0}' exists! Try specifying an organisation";
	private static final String INVALID_STATE_NAME = "Invalid state name: No state by the name '{0}' exists!";
	private static final String NO_DATA = "Empty Input File: The provided CSV file is empty";
	private static final String MISSING_COL = "Missing Required Column: One of {0} or {1} is required, along with {2}";
	private static final String CONFLICTING_COL = "Conflicting Arguments: {0} and {1} are mutually exclusive columns";
	private static final String MISSING_COMPLEMENTARY_COL = "";
	private static final String MISSING_DATA = "";
	private static final String MALFORMED_DATA = "";
	private static String templateStr;
	private static String stateStr;
	private static LifeCycleTemplateReference template;
	private static State state;
	private static String comments;
	static {
		CLASS_NAME = "ext.hydratight.util.LifeCycleUtility";
		UTILITY_NAME = "Life Cycle Utility";
	}

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
		idType = 1;
		switch (idType) {
		case 0:
			//processFile();
			break;
			
		case 1:
			process(findByNumber());
			break;
			
		case 2:
			//process(findByOid());
			break;
		}
		
		return Boolean.TRUE;
	
	}
	
	protected static void processArgs(String as[])
			throws WTException
	{
	
		file = getStringArg(as, FILE_ARG);
		number = getStringArg(as, NUMBER_ARG);
		revision = getStringArg(as, REVISION_ARG);
		oid = getStringArg(as, OID_ARG);
		type = getStringArg(as, TYPE_ARG);
		stateStr = getStringArg(as, STATE_ARG);
		orgStr = getStringArg(as, ORG_ARG);
		templateStr = getStringArg(as, TEMPLATE_ARG);
		stateStr = getStringArg(as, STATE_ARG);
		comments = getStringArg(as, COMMENTS_ARG);
		log = getBooleanArg(as, LOG_ARG);
		
		validateArgs();

	}

	/**
	 *	Validates the arguments provided are sufficient for the utility to function in one of<br />
	 *	its possible modes.<br />
	 *	<br />
	 *	There are three modes in which this utility can be run:
	 *	<ol>
	 *		<li>File - set life cycles on multiple objects based on a file</li>
	 *		<li>Number - set life cycle of single object found by number</li>
	 *		<li>OID - set life cycle of single object found by OID</li>
	 *	</ol>
	 *	File Mode:<br />
	 *	The <i>required</i> options are -
	 *	<ul>
	 *		<li>File</li>
	 *	</ul>
	 *	The <i>optional</i> options are -
	 *	<ul>
	 *		<li>Revision</li>
	 *		<li>Iteration (requires "Revision")</li>
	 *		<li>Template (requires "Organisation")</li>
	 *		<li>Organisation (only required if using "Template"</li>
	 *		<li>State<li>
	 *		<li>Comments</li>
	 *		<li>Log</li>
	 *	</ul>
	 *	Number Mode:<br />
	 *	The <i>required</i> options are -
	 *	<ul>
	 *		<li>Number</li>
	 *		<li>Type</li>
	 *	</ul>
	 *	The <i>optional</i> options are -
	 *	<ul>
	 *		<li>Revision</li>
	 *		<li>Iteration (requires "Revision")</li>
	 *		<li>Template (requires "Organisation")</li>
	 *		<li>Organisation (only required if using "Template"</li>
	 *		<li>State<li>
	 *		<li>Comments</li>
	 *		<li>Log</li>
	 *	</ul>
	 *	OID Mode:<br />
	 *	The <i>required</i> options are -
	 *	<ul>
	 *		<li>OID</li>
	 *	</ul>
	 *	The <i>optional</i> options are -
	 *	<ul>
	 *		<li>Type (if <i>full</i> OID is provided)</li>
	 *		<li>Template (requires "Organisation")</li>
	 *		<li>Organisation (only required if using "Template"</li>
	 *		<li>State<li>
	 *		<li>Comments</li>
	 *		<li>Log</li>
	 *	</ul>
	 */
	protected static void validateArgs()
			throws IllegalArgumentException
	{
		try {
			if (file == null && number == null && oid == null) {
				throw new IllegalArgumentException(MessageFormat.format(MISSING_ARG, new Object[] {
					FILE_ARG, NUMBER_ARG, OID_ARG
				}));
			}
			
			if ((file != null && number != null) ||
					(file != null && oid != null) ||
					(number != null && oid != null)) {
					
				throw new IllegalArgumentException(MessageFormat.format(CONFLICTING_ARG, new Object[] {
					FILE_ARG, NUMBER_ARG, OID_ARG
				}));
			}

			if (stateStr != null) {
				try {
					state = getState(stateStr);
				}
				catch (WTInvalidParameterException ex) {
					throw new IllegalArgumentException(MessageFormat.format(INVALID_STATE_NAME,
						new Object[] {
							stateStr
						}
					));
				}	
			}
			
			if (file != null) {
				idType = 0;
				return;
			}
			
			if (number != null) {
				if (revision != null && orgStr != null) {
					idType = 1;
				} else {
					idType = 1;
					//throw new IllegalArgumentException(MessageFormat.format(MISSING_COMPLEMENTARY_ARG_1, new Object[] {
					//	NUMBER_ARG, REVISION_ARG, ORG_ARG
					//}));
				}
			}
			
			if (oid != null) {
				if (orgStr != null) {
					idType = 2;
				} else {
					throw new IllegalArgumentException(MessageFormat.format(MISSING_COMPLEMENTARY_ARG_2, new Object[] {
						NUMBER_ARG, ORG_ARG
					}));
				}
			}
			
			if (orgStr != null) {
				try {
					org = getOrganization(orgStr);
					if (org == null) {
						throw new WTException();
					}

				}
				catch (WTException ex) {
					throw new IllegalArgumentException(MessageFormat.format(INVALID_ORG_NAME,
							new Object[] {
								orgStr
							}
						));
				}
			}

			if (templateStr != null) {
				try {
					template = getLifeCycleTemplateRef(orgStr, templateStr);
					if (template == null) {
						throw new LifeCycleException();
					}
				}
				catch (WTException ex) {
					throw new IllegalArgumentException(MessageFormat.format(INVALID_LIFE_CYCLE_NAME,
						new Object[] {
							templateStr
						}
					));
				}
			}

		}
		catch (IllegalArgumentException ex) {
			print(ex.getMessage());
			System.exit(0);
		}
	}

	/**
	 *	Sets templates based on details passed at the command line.
	 */
	private static void process(QueryResult qr)
			throws WTException
	{
	
		WTObject obj = null;
		WTList list = new WTArrayList();
		
		while (qr.hasMoreElements()) {
			obj = (WTObject)qr.nextElement();
			
			if (obj instanceof LifeCycleManaged) {
				list.add(obj);
			}
		}
		
		set(list, template, state, comments);
		
	}

	private static void set(WTList objs, LifeCycleTemplateReference lctRef, State st, String cmm)
			throws WTException
	{
		if (stateStr != null) {
			LifeCycleHelper.service.reassign(objs, lctRef, null, st, cmm);
		}
		else {
			LifeCycleHelper.service.reassign(objs, lctRef, null, true, cmm);
		}
		
	}
	
	/**
	 *	Get the life cycle template reference based on the name of the template and organisation.
	 */
	private static LifeCycleTemplateReference getLifeCycleTemplateRef(String og, String tp)
			throws WTException, LifeCycleException
	{
		if (og == null) {							// No organisation specifed ...
			return getLifeCycleTemplateRef(tp);
		}

		if (og.indexOf(ORG_CLASS) < 0) {			// Check if has full Container path or just organisation name
			og = new StringBuilder().append(ORG_CLASS).append(og).toString();
		}
		
		WTContainerRef cont = WTContainerHelper.service.getByPath(og);

		return LifeCycleHelper.service.getLifeCycleTemplateReference(tp, cont);
	}

	/**
	 *	Get the life cycle template reference based on the name of the template only.
	 */
	private static LifeCycleTemplateReference getLifeCycleTemplateRef(String tp)
			throws WTException, LifeCycleException
	{
		return LifeCycleHelper.service.getLifeCycleTemplateReference(tp);
	}

	/**
	 *
	 */
	private static State getState(String st)
			throws WTInvalidParameterException
	{
		return State.toState(st.toUpperCase());
	}
	
	protected static void printHelp()
	{
	
		print("Usage: java ext.hydratight.util.LifeCycleUtility [args]\n" +
		"Args:\n" +
		"\t" + USERNAME_ARG + " [REQUIRED] <username of user to authenticate as>\n" +
		"\t" + PASSWORD_ARG + " [REQUIRED] <password of user to authenticate as>\n" +
		"\t" + FILE_ARG + " <full filepath of input csv file>\n" +
		"\t" + TEMPLATE_ARG + " <new lifecycle template to set object(s) to>\n" +
		"\t" + STATE_ARG + " <new state to set object(s) to>\n" +
		"\t" + ORG_ARG + " <name of the organisation container>\n" +
		"\t" + NUMBER_ARG + " <number of object to set lifecycle template on>\n" +
		"\t" + REVISION_ARG + " <revision of the object to set lifecycle template on>\n" +
		"\t" + OID_ARG + " <id of object to set lifecycle template on>\n" +
		"\t" + TYPE_ARG + " <type of the object to set lifecycle template on>\n" +
		"\t" + LOG_ARG + " <full filepath of log file>\n");
		
	}
}