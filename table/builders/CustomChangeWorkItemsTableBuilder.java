package ext.site.table.builders.CustomChangeWorkItemsTableBuilder;

import com.ptc.core.htmlcomp.tableview.ConfigurableTable;
import com.ptc.mvc.components.OverrideComponentBuilder;
import com.ptc.windchill.enterprise.change2.mvc.builders.tables.ChangeWorkItemsTableBuilder;
import ext.site.table.views.CustomChangeWorkItemTableViews;

import wt.util.WTException;

/**
 *	
 */
@OverrideComponentBuilder
public class CustomChangeWorkItemsTableBuilder
		extends ChangeWorkItemsTableBuilder
{
	/**
	 *	
	 */
	@Override
	public ConfigurableTable buildConfigurableTable(String arg0)
			throws WTException
	{
		return new CustomChangeWorkItemTableViews();
	}
}
