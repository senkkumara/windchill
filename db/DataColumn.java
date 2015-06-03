package ext.hydratight.db;

import java.util.LinkedList;

public class DataColumn
	extends LinkedList<String>
{

	private String name = "id";
	private String delim = ", ";

	public DataColumn(String n)
	{
		this.name = n;
	}

	public DataColumn setName(String n)
	{
		this.name = n;
		return this;
	}

	public String getName()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return this.toString(null, this.delim);
	}

	public String toString(DataColumn idCol)
	{
		return this.toString(idCol, this.delim);
	}

	public String toString(String dl)
	{
		return this.toString(null, dl);
	}

	public String toString(DataColumn idCol, String dl)
	{
		StringBuilder sb = new StringBuilder();

		if (idCol != null && idCol.size() != this.size()) {
			return null;
		}

		if (idCol != null) {
			CreateHeader:
			for (String str : idCol) {
				sb.append(str);
				sb.append(dl);
			}

			if (sb.length() > 0) {
				sb.delete(sb.length() - dl.length(), sb.length() - 1);		// remove trailing ", "
			}

			sb.append("\n");
		}

		CreateBody:
		for (String str : this) {
			sb.append(str);
			sb.append(dl);
		}

		if (sb.length() > 0) {
			sb.delete(sb.length() - dl.length(), sb.length() - 1);		// remove trailing ", "
		}

		return sb.toString();
	}

	public String toHTML()
	{
		return this.toHTML(null);
	}

	public String toHTML(DataColumn idCol)
	{
		StringBuilder sb = new StringBuilder();

		if (idCol != null && idCol.size() != this.size()) {
			return null;
		}

		if (idCol != null) {
			sb.append("<table>");
			sb.append("<tr>");
			CreateHeader:
			for (String str : idCol) {
				sb.append("<th>");
				sb.append(str);
				sb.append("</th>");
			}
			sb.append("</tr>");
		}

		sb.append("<tr>");
		CreateBody:
		for (String str : this) {
			sb.append("<td>");
			sb.append(str);
			sb.append("</td>");
		}
		sb.append("</tr>");

		if (idCol != null) {
			sb.append("</table>");
		}

		return sb.toString();
	}
}