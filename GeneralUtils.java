package ext.hydratight;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wt.util.WTProperties;

import wt.util.WTException;

/**
 *	Container for Generic Windchill helper functions.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class GeneralUtils
{

	/**
	 *	Retrieve the value of a Windchill property - do not catch Exceptions if<br />
	 *	property is required.
	 *
	 *		@param prop the name of the property to retrieve
	 *		@param req whether the property is required
	 *		@return the value of the property
	 */
	public static String getWindchillProperty(String prop, boolean req)
		throws WTException
	{
		String val = null;
		try {
			val = WTProperties.getLocalProperties().getProperty(prop);
		} catch (Throwable t) {
			t.printStackTrace();
			if (req) {
				throw new WTException(t);
			}
		}

		return val;
	}

	/**
	 *	Retrieve the value of a Windchill Property.
	 *
	 *		@param prop the name of the property to retrieve
	 *		@return the value of the property
	 */
	public static String getWindchillProperty(String prop)
	{
		String val = null;
		try {
			val = getWindchillProperty(prop, false);
		} catch (WTException wte) {
			// Do nothing...
		}
		return val;
	}

	/**
	 *	Compare a String to a regular expression, return result.
	 *
	 *		@param str the String to be compared to the regular expression
	 *		@param reg the regulst expression
	 *		@return the result of the comparison
	 */
	public static boolean regex(String str, String reg)
	{
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(str);

		boolean found = false;
		while (m.find()) {
			found = true;
		}

		return found;
	}

}