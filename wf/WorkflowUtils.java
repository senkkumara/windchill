package ext.hydratight.wf;

import wt.workflow.engine.ProcessData;
import wt.workflow.work.WfAssignedActivity;

/**
 *	This class provides general help with Workflows.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class WorkflowUtils
{

	/**
	 *	Returns a String containing the comments entered on a task by the assignee.
	 *	
	 *		@param act the Activity containing the comments to be retrieved.
	 *		@return String
	 */
	public static String getTaskComments(WfAssignedActivity act)
	{	
		ProcessData data = act.getContext();
		return data.getTaskComments();
	}

}