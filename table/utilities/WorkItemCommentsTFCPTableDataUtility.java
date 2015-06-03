package ext.site.table.utilities.WorkItemCommentsTFCPTableUtility;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.DefaultDataUtility;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;
import wt.workflow.work.WorkItem;

import wt.util.WTException;

/**
 *	
 */
public class WorkitemCommentsTFCPTableDataUtility
		extends DefaultDataUtility
{
	/**
	 *	
	 */
	@Override
	public Object getDataValue(String id, Object datum, ModelContext mc)
			throws WTException
	{
		String comments = "";
		WorkItem wi;
		TextDisplayComponent tdc;

		if (datum.getClass().isAssignableFrom(WorkItem.class)) {		// if it is a workitem
			wi = (WorkItem)datum;
			if (wi.isComplete()) {										// if it is complete
				comments = wi.getContext().getTaskComments();
			}			
		}
		
		tdc = new TextDisplayComponent("Work Item Comments");
		tdc.setValue(comments);
		
		return tdc;
	}

}
