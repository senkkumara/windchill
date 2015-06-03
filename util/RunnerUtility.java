package ext.hydratight.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *	
 */
public class RunnerUtility
{

	public static void main(String[] args)
	{
		String reg = "^(?:(?:(?:SWE[\\w-]+)|(?:(?:[A-Z]\\d{4}){2}[A-Z]{2})|(?:[A-Z]{2}S[A-Z]{2}\\d{7})|(?:\\d{3}[A-Z]{2}\\d{7})|(?:G\\d{4}[ABCDHKLMX]\\d{4}(?:CR|PG|XX))|(?:[AC]\\d{4}[A-Z][\\dSM]\\d{3}[-A-Z]{2}))(?:[MPWU](?:_\\d+)?)?|(?:S[\\dM]\\d{3}[A-Z]\\d{4}[A-Z]{2})|(?:S-(?:(?:\\d{4}-\\d{2}-(?:[1-3]A|3G))|(?:M\\d{3}(?:-\\d{3})?)|(?:A\\d{3}-\\d{2}-(?:XX|2C|2G)))-[LR]H-[A-Z]-[A-Z]{2})|(?:OSP_[A-Z0-9-]+(?:_\\d+)?)|R\\d{4}(?:(?:FR(?:-\\d{4}){2}-\\d{3})|(?:[RFHS]B-\\d{4}-[MI])|FS-(?:\\d|G)\\d{3}-\\d{4}-[MI]|T(?:S[B-F]|H|R[A-T]?)(?:-\\d{4}){2}-[MI]|TF[A-E]?-(\\d{4})(?:-\\1-[MI]){2}|TG[A-P]?(?:-\\d{4}){2}-[MI]|P(?:S[A-D]?|A[A-B])-\\d{4}(?:-[A-Z0-9]{4})-[MI]|(?:AN|CHA|BEA)-(?:E|\\d{4})(?:-\\d{4}){2}-[MI]|BP-\\d{4}-0000-000|PL-\\d{4}-[MI]|SH-(?:(?:G\\d{3})|\\d{4}-[MI]))-([A-F]|X)\\2{1}3(?:1|2))$";

		Pattern p = Pattern.compile(reg, Pattern.COMMENTS);
		Matcher m = p.matcher(args[0]);

		boolean found = false;
		while (m.find()) {
			found = true;
		}

		System.out.println(found);
	}

}

/*

^(?:(?#														Hydratight PART Regex
	)(?:(?#														Assemblies & Components
		)(?:SWE[\w-]+)|(?#											Sweeney Torque Multipliers
		)(?:(?:[A-Z]\d{4}){2}[A-Z]{2})|(?#							Fully Intelligent
		)(?:[A-Z]{2}S[A-Z]{2}\d{7})|(?#								Special Assembly
		)(?:\d{3}[A-Z]{2}\d{7})|(?#									Special Component
		)(?:G\d{4}[ABCDHKLMX]\d{4}(?:CR|PG|XX))|(?#					Leak Sealing
		)(?:[AC]\d{4}[A-Z][\dSM]\d{3}[-A-Z]{2})(?#					Tension / Torque
		))(?:[MPWU](?:_\d+)?)?(?#									Process Modifiers
	)|(?#

	)(?:S[\dM]\d{3}[A-Z]\d{4}[A-Z]{2})|(?#						Studbolt
	)(?:S-(?#													All-Thread
		)(?:(?#														Forms
			)(?:\d{4}-\d{2}-(?:[1-3]A|3G))|(?#							Imperial
			)(?:M\d{3}(?:-\d{3})?)|(?#									Metric
			)(?:A\d{3}-\d{2}-(?:XX|2C|2G))(?#							ACME
		))-[LR]H(?#													Right / Left Hand
		)-[A-Z](?#													Plating / Coating
		)-[A-Z]{2}(?# 												Material & Certification
	))|(?#

	)(?:OSP_[A-Z0-9-]+(?:_\d+)?)|(?#							Outside Processes

	)R(?#														Raw Material
		)\d{4}(?#													Material Code
		)(?:(?#														Forms
			)(?:FR(?:-\d{4}){2}-\d{3})|(?#								Forged Ring
			)(?:[RFHS]B-\d{4}-[MI])|(?#									Bar Stock
			)FS-(?:\d|G)\d{3}-\d{4}-[MI]|(?#							Rectangular Flat Strip
			)T(?:S[B-F]|H|R[A-T]?)(?:-\d{4}){2}-[MI]|(?#				Tube: Round, Square, Hex
			)TF[A-E]?-(\d{4})(?:-\1-[MI]){2}|(?#						Tube: Rectangular
			)TG[A-P]?(?:-\d{4}){2}-[MI]|(?#								Tube: Gauge
			)P(?:S[A-D]?|A[A-B])-\d{4}(?:-[A-Z0-9]{4})-[MI]|(?#			Pipe
			)(?:AN|CHA|BEA)-(?:E|\d{4})(?:-\d{4}){2}-[MI]|(?#			Angles & Sections
			)BP-\d{4}-0000-000|(?#										Boiler Plate
			)PL-\d{4}-[MI]|(?#											Plate
			)SH-(?:(?:G\d{3})|\d{4}-[MI])(?#							Sheet
		))-([A-F]|X)\2(?#											Charpy Requirements
		){1}3(?:1|2)(?#												Inspection Requirements
	))(?#
)$

^(?:(?#														Hydratight CAD Regex
	)(?:(?#														Assemblies & Components
		)(?:SWE[\w-]+)|(?#											Sweeney Torque Multipliers
		)(?:(?:[A-Z]\d{4}){2}[A-Z]{2})|(?#							Fully Intelligent
		)(?:[A-Z]{2}S[A-Z]{2}\d{7})|(?#								Special Assembly
		)(?:\d{3}[A-Z]{2}\d{7})|(?#									Special Component
		)(?:G\d{4}[ABCDHKLMX]\d{4}(?:CR|PG|XX))|(?#					Leak Sealing
		)(?:[AC]\d{4}[A-Z][\dSM]\d{3}[-A-Z]{2})(?#					Tension / Torque
		))(?:[MPWU](?:_\d+)?)?(?#									Process Modifiers
		)(?:-(?:INSP|PE))?(?#										Variants
	)|(?#

	)(?:S[\dM]\d{3}[A-Z]\d{4}[A-Z]{2})|(?#						Studbolt
	)(?:S-(?#													All-Thread
		)(?:(?#														Forms
			)(?:\d{4}-\d{2}-(?:[1-3]A|3G))|(?#							Imperial
			)(?:M\d{3}(?:-\d{3})?)|(?#									Metric
			)(?:A\d{3}-\d{2}-(?:XX|2C|2G))(?#							ACME
		))-[LR]H(?#													Right / Left Hand
		)-[A-Z](?#													Plating / Coating
		)-[A-Z]{2}(?# 												Material & Certification
	))|(?#

	)(?:OSP_[A-Z0-9-]+(?:_\d+)?)|(?#							Outside Processes

	)R(?#														Raw Material
		)\d{4}(?#													Material Code
		)(?:(?#														Forms
			)(?:FR(?:-\d{4}){2}-\d{3})|(?#								Forged Ring
			)(?:[RFHS]B-\d{4}-[MI])|(?#									Bar Stock
			)FS-(?:\d|G)\d{3}-\d{4}-[MI]|(?#							Rectangular Flat Strip
			)T(?:S[B-F]|H|R[A-T]?)(?:-\d{4}){2}-[MI]|(?#				Tube: Round, Square, Hex
			)TF[A-E]?-(\d{4})(?:-\1-[MI]){2}|(?#						Tube: Rectangular
			)TG[A-P]?(?:-\d{4}){2}-[MI]|(?#								Tube: Gauge
			)P(?:S[A-D]?|A[A-B])-\d{4}(?:-[A-Z0-9]{4})-[MI]|(?#			Pipe
			)(?:AN|CHA|BEA)-(?:E|\d{4})(?:-\d{4}){2}-[MI]|(?#			Angles & Sections
			)BP-\d{4}-0000-000|(?#										Boiler Plate
			)PL-\d{4}-[MI]|(?#											Plate
			)SH-(?:(?:G\d{3})|\d{4}-[MI])(?#							Sheet
		))-([A-F]|X)\2(?#											Charpy Requirements
		){1}3(?:1|2)(?#												Inspection Requirements
	)|(?#

	)(?:(?:(?(?AM|APP)_\d{8})|CP|CMP|GEN|MAS)_\w+))(?#
).(?:DRW|PRT|ASM)$

*/