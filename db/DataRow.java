package ext.hydratight.db;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataRow
	extends LinkedHashMap<String, String>
{
	private String delim = ", ";

	public DataRow setDelimiter(String dl)
	{
		this.delim = dl;
		return this;
	}

	public String getDelimiter()
	{
		return delim;
	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(String dl)
	{
		return this.toString(false, dl);
	}

	public String toString(boolean inc)
	{
		return this.toString(inc, this.delim);
	}

	public String toString(boolean incHead, String dl)
	{
		StringBuilder sb = new StringBuilder();
		String col, val;
		Set<String> cols;
		Iterator it;

		if (incHead) {
			cols = this.keySet();
			it = cols.iterator();
			CollectHeaders:
			while (it.hasNext()) {
				col = (String)it.next();
				sb.append(col);
				sb.append(dl);

			}

			if (sb.length() > 0) {
				sb.delete(sb.length() - dl.length(), sb.length() - 1);		// remove trailing ", "
			}

			sb.append("\n");
		}

		CollectValues:
		for (Map.Entry<String, String> entry : this.entrySet()) {
			val = entry.getValue();
			sb.append(val);
			sb.append(dl);
		}

		if (sb.length() > 0) {
			sb.delete(sb.length() - dl.length(), sb.length());		// remove trailing ", "
		}

		return sb.toString();
	}

	/**
	 *	Returns a formated HTML table row ONLY - not wrapped in "&lt;table&gt;" tags.
	 *
	 *		return String
	 */
	public String toHTML()
	{
		return this.toHTML(false, null, null);
	}

	public String toHTML(boolean incHead)
	{
		return this.toHTML(incHead, null, null);
	}

	public String toHTML(String[] bStyle, String[] hStyle)
	{
		return this.toHTML(false, bStyle, hStyle);
	}

	/** 
	 *	Returns formatted HTML table of data. If "incHead" is true, the data is wrapped<br />
	 *	in a "&lt;table&gt;" tag and includes a header row for the column names.
	 *
	 *		@return String
	 */
	public String toHTML(boolean incHead, String[] bStyle, String[] hStyle)
	{
		StringBuilder sb = new StringBuilder();
		String str, col, val;
		Set<String> cols;
		Iterator it;
		int i = 0;
		int j = 0;

		if (incHead) {
			sb.append("<table>");
			sb.append("<tr>");
			cols = this.keySet();
			it = cols.iterator();
			CollectHeaders:
			while (it.hasNext()) {
				col = (String)it.next();
				sb.append("<th");
				if (hStyle != null && i < hStyle.length && hStyle[i] != null) {
					sb.append(" style=\"");
					sb.append(hStyle[i]);
					sb.append("\"");
				}
				sb.append(">");
				sb.append(col);
				sb.append("</th>");
				i++;
			}
			sb.append("</tr>");
		}

		sb.append("<tr>");
		CollectValues:
		for (Map.Entry<String, String> entry : this.entrySet())
		{
			val = entry.getValue();
			sb.append("<td");
			if (bStyle != null && j < bStyle.length && bStyle[j] != null) {
				sb.append(" style=\"");
				sb.append(bStyle[j]);
				sb.append("\"");
			}
			sb.append(">");
			sb.append(val);
			sb.append("</td>");
			j++;
		}
		sb.append("</tr>");

		if (incHead) {
			sb.append("</table>");
		}

		return sb.toString();
	}
}