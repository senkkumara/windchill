package ext.site.table.views.CustomChangeWorkItemsTableBuilder;

import com.ptc.core.htmlcomp.createtableview.Attribute;
import com.ptc.core.htmlcomp.tableview.TableColumnDefinition;
import com.ptc.netmarkets.work.NmWorkItemCommands;
import com.ptc.windchill.enterprise.change2.tableViews.ChangeWorkitemTableViews;
import java.util.List;
import java.util.Locale;

import wt.util.WTException;

/**
 *	
 */
public class CustomChangeWorkitemTableViews
		extends ChangeWorkitemTableViews
{
	/**
	 *	
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List getSpecialTableColumnsAttrDefinition(Locale lcl)
	{	
		List superColumns = super.getSpecialTableColumnsAttrDefinition(lcl);
		superColumns.add(new Attribute.TextAttribute("workitem_comment_TFCPTable", "Comments", lcl));				
		return superColumns;	
	}	
}