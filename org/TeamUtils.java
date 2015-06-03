package ext.hydratight.org;

import ext.hydratight.org.PrincipalUtils;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import wt.fc.WTObject;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.org.StandardOrganizationServicesManager;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.project.Role;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.TeamTemplate;
import wt.team.TeamTemplateReference;

import wt.util.WTException;

/**
 *	This provides help with Teams during workflows.
 *
 *		@author Toby Pettit
 *		@version 1.0
 */
public class TeamUtils
{

	static final String ADMIN_GROUP_NAME = "Hydratight Organisation Administrators";

	/**
	 *	This method adds a Principal argument into an argument Role in an agument Team.
	 *	
	 *		@param team the Team which the target role belongs to.
	 *		@param role the role in the target team into which the principal is to be added.
	 *		@param pr the Principal to be added into the role in the target team.
	 */
	public static void addParticipant(Team team, Role role, WTPrincipal pr)
			throws WTException
	{
		team.addPrincipal(role, pr);		// Add participant	
	}
	
	/**
	 *	This method removes a Principal argument from an argument Role in an agument Team.
	 *	
	 *		@param team the Team which the target role belongs to.
	 *		@param role the role in the target team from which the principal is to be removed.
	 *		@param pr the Principal to be removed infromto the role in the target team.
	 */
	public static void removeParticipant(Team team, Role role, WTPrincipal pr)
			throws WTException
	{
		team.deletePrincipalTarget(role, pr);		// Remove participant
	}
	
	/**
	 *	This method removes all of the principals from a Role argument in the team of a argument
	 *	object.
	 *
	 *		@param obj the Object to which the team belongs to.
	 *		@param roleStr a String containing the name of the role.
	 */
	public static void removeAllParticipants(TeamManaged tm, String roleStr)
			throws WTException
	{
		int err = 0;
		Team team = TeamHelper.service.getTeam(tm);							// Get the team
		Role role = Role.toRole(roleStr);									// Get role
		Enumeration<?> participants = team.getPrincipalTarget(role);		// Get participants
		WTPrincipalReference ref;
		WTPrincipal pr;
		
		Remove:
		while (participants.hasMoreElements()) {
			ref = (WTPrincipalReference)participants.nextElement();
			try {
				pr = ref.getPrincipal();		// Get participant
				removeParticipant(team, role, pr);			// Remove participant
			}
			catch (WTException wte) {
				wte.printStackTrace();
				err++;
			}
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to remove ")
				.append(err)
				.append(" participants from ")
				.append(roleStr)
				.append(" role...")
				.toString());
		}
	}
	
	public static void addParticipants(TeamManaged tm, String roleStr, Vector<WTPrincipalReference> participants)
			throws WTException
	{	
		setParticipants(tm, roleStr, participants, false);	
	}
	
	/**
	 *	This method sets participants in a Role argument to that of a Vector argument -
	 *	removing those not in the Vector in the process.
	 *
	 *		@param obj the Object to which the team belongs to.
	 *		@param roleStr a String containing the name of the role.
	 *		@param participants a Vector containing the participants to be in the role.
	 */
	public static void setParticipants(TeamManaged tm, String roleStr, Vector<WTPrincipalReference> refs,
				boolean removeExisting)
				
			throws WTException
	{
		int err = 0;
		Team team = TeamHelper.service.getTeam(tm);		// Get team
		Role role = Role.toRole(roleStr);		// Get role
		WTPrincipal pr;
		
		if (removeExisting) {
			removeAllParticipants(tm, roleStr);		// Remove all existing.
		}
		
		
		// Cycle through each participant and add them to the role.
		AddParticipants:
		for (WTPrincipalReference ref : refs) {
			try {
				pr = ref.getPrincipal();
				addParticipant(team, role, pr);
				
			}
			catch (WTException wte) {
				wte.printStackTrace();
				err++;
				continue AddParticipants;
			}
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to add ")
				.append(err)
				.append(" participants to ")
				.append(roleStr)
				.append(" role.")
				.toString());
		}
	}
	
	/**
	 *	Returns a vector containing the Principals in a role argument in the team of an Object
	 *	argument.
	 *
	 *		@param obj the Object to which the team belongs to.
	 *		@param roleStr a String containing the name of the role.
	 *		@return java.util.Vector
	 */
	public static Vector<WTPrincipalReference> getParticipants(TeamManaged tm, String roleStr)
			throws WTException
	{
		Vector<WTPrincipalReference> refs = new Vector<WTPrincipalReference>();
		WTPrincipalReference ref;
		Team team;
		Role role;
		int count = countParticipants(tm, roleStr);
		Enumeration<?> participants;
		
		// Only applicable if there is more than one participant in the given role.
		if (count > 1) {
			team = TeamHelper.service.getTeam(tm);
			role = Role.toRole(roleStr);
			participants = team.getPrincipalTarget(role);

			Get:
			while (participants.hasMoreElements()) {
				ref = (WTPrincipalReference)participants.nextElement();
				
				if (ref != null) {
					refs.add(ref);
				}
			}
		}
		
		return refs;
	}
	
	/**
	 *	Returns integer count of the number of participants are in an argument Role in the Team
	 *	associated to an argument Object.
	 *
	 *		@param obj the Object containing the Team.
	 *		@param roleStr a String containing the name of the Role.
	 *		@return int
	 */
	public static int countParticipants(TeamManaged tm, String roleStr)
			throws WTException
	{
		int count = 0;
		Team team = TeamHelper.service.getTeam(tm);
		Role role = Role.toRole(roleStr);
		WTPrincipalReference ref;
		Enumeration<?> participants = team.getPrincipalTarget(role);

		Count:
		while (participants.hasMoreElements()) {
			ref = (WTPrincipalReference)participants.nextElement();
			count++;
		}
		
		return count;
	}
	
	public static void removeParticipantMatch(TeamManaged tm, String roleAStr, String roleBStr)
			throws WTException
	{
		compareParticipants(tm, roleAStr, roleBStr, true);
	}
	
	public static void removeParticipantMisMatch(TeamManaged tm, String roleAStr, String roleBStr)
			throws WTException
	{
		compareParticipants(tm, roleAStr, roleBStr, false);
	}
	
	/**
	 *	This method compares the sets of participants in two Role areguments in the same team.<br />
	 *	It will then remove matches or mismatches - depending on the boolean arguments provided.
	 *
	 *		@param obj the Object containing the Team
	 *		@param RoleAStr a String containing the name of the comparative role.
	 *		@param RoleBStr a String containing the name of the target role - the role from which principals may be removed.
	 *		@param match boolean determining whether matches should be removed.
	 */
	public static void compareParticipants(TeamManaged tm, String roleAStr, String roleBStr, boolean match)
			throws WTException
	{
		int err = 0;
		Team team = TeamHelper.service.getTeam(tm);
		Role roleA = Role.toRole(roleAStr);				// Benchmark role - participants are unaffected.
		Role roleB = Role.toRole(roleBStr);				// Role where matches / mis-matches are removed from.

		// Remove matches OR mis-matches - not both.
		Enumeration<?> principalsA = team.getPrincipalTarget(roleA);
		Enumeration<?> principalsB = team.getPrincipalTarget(roleB);
		WTPrincipalReference refA;
		WTPrincipalReference refB;
		WTPrincipal prA;
		WTPrincipal prB;
		
		// Cycle through each participant in role "A".
		LoopA:
		while (principalsA.hasMoreElements()) {
			refA = (WTPrincipalReference)principalsA.nextElement();
			
			try {
				prA = refA.getPrincipal();
			}
			catch (WTException wte) {
				wte.printStackTrace();
				err++;
				continue LoopA;
			}
			
			// Cycle through each participant in role "B".
			LoopB:
			while (principalsB.hasMoreElements()) {
				refB = (WTPrincipalReference)principalsB.nextElement();
				
				try {
					prB = refB.getPrincipal();
				}
				catch (WTException wte) {
					wte.printStackTrace();
					err++;
					continue LoopB;
				}
				
				// Compare particpant in role "A" and "B".
				if (match && prA.equals(prB)) {
					// Match found and match to be removed.
					try {
						removeParticipant(team, roleB, prB);
					}
					catch (WTException wte) {
						wte.printStackTrace();
						err++;
					}
					
					continue LoopB;
				}
			}
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to carry out team actions (")
				.append(err)
				.append(")")
				.toString());
		}
	}
	
	/**
	 *	This method randomly selects a participant in a Role argument in the Team of an Object argument; 
	 *	removing all other participants other than it from the Role.
	 *
	 *		@param obj the Object containing the Team.
	 *		@param roleStr a String containing the name of the role.
	 */
	public static void selectRandomParticipant(TeamManaged tm, String roleStr)
			throws WTException
	{
		Team team = TeamHelper.service.getTeam(tm);
		Role role = Role.toRole(roleStr);
		Vector<WTPrincipalReference> participants = getParticipants(tm, roleStr);
		WTPrincipalReference ref;
		WTPrincipal pr;
		Random gen;
		int rand = 0;
		int size = participants.size();
		
		// Only applicable if there is more than one participant in the given role.
		if (size > 1) {
			size--;		// Indices of a vector are 0-based, size is 1-based.
			gen = new Random();
			rand = gen.nextInt(size);		// Select a random index number.
		
			// Retrieve random participant.
			ref = participants.get(rand);
			pr = ref.getPrincipal();
		
			try {
				removeAllParticipants(tm, roleStr);		// Remove all participants from role.
				
			} catch (WTException wte) {
				wte.printStackTrace();
				resetTeam(tm);
				throw new WTException(wte);
				
			}
			
			addParticipant(team, role, pr);		// Re-add random participant into role.
			
		}
	}
	
	public static void addGroupToRole(TeamManaged tm, WTOrganization org, String groupName, String roleStr)
			throws WTException
	{
		addGroupToRole(tm, org, groupName, roleStr, false);
	}
	
	/**
	 *	This method adds all of the members of a Group argument into a Role argument - removing all of 
	 *	existing participants that are not in the role.
	 *
	 *	@param obj the Object containing the Team.
	 *	@param org the Organisation containing the Group.
	 *	@param groupName a String containing the name of the Group.
	 *	@param roleStr a String containing the name of the Role.
	 */
	public static void addGroupToRole(TeamManaged tm, WTOrganization org, String groupName,
				String roleStr, boolean remove)
		
			throws WTException
	{
		int err = 0;
		Team team = TeamHelper.service.getTeam(tm);
		Role role = Role.toRole(roleStr);
		WTPrincipal pr;
		WTGroup group;

		removeAllParticipants(tm, roleStr);		// Remove all existing participants.
	
		group = PrincipalUtils.getGroup(org, groupName);
		
		if (group != null) {
			Enumeration<?> mmb = group.members();
			
			Add:
			while (mmb.hasMoreElements()) {
				pr = (WTPrincipal)mmb.nextElement();
				if (pr != null) {
					try {	
						addParticipant(team, role, pr);
						
					} catch (WTException wte) {
						wte.printStackTrace();
						err++;
						continue Add;
						
					}
				}
			}
		}
		
		if (err != 0) {
			throw new WTException(new StringBuilder("Failed to add ")
				.append(err)
				.append(" participants to team")
				.toString());
		}
	}
	
	public static void setTeamTemplate(TeamManaged tm, String templateName)
			throws WTException
	{

		WTContained contObj = (WTContained)tm;
		@SuppressWarnings("unchecked")
		Vector<TeamTemplate> templates = TeamHelper.service.findCandidateTeamTemplates(		// Get team templates
				WTContainerRef.newWTContainerRef(contObj.getContainer()));
		
		String name;
		boolean set = false;
		
		FindTemplate:		// Find team template from valid templates
		for (TeamTemplate template : templates) {
			name = template.getName();
			if (name.equals(templateName)) {
				// Found specified Team Template
				setTeamTemplate(tm, template);		// Set specified Team Template
				set = true;
				break FindTemplate;
			}
		}
		
		if (! set) {
			throw new WTException("Template not found!");
		}

	}
	
	/**
	 *	Returns oolean attempt to set team template of parameter Team Managed to parameter team template.
	 *
	 *		@param tm the TeamManaged object containing the Team to set.
	 *		@param template a String containing the name of the template to set.
	 */
	public static void setTeamTemplate(TeamManaged tm, TeamTemplate template)
			throws WTException
	{

		TeamHelper.service.reteam(tm, TeamTemplateReference.newTeamTemplateReference(template));
		
	}
	
	/**
	 *	Returns boolean attempt to reset team - by setting the team template back to the default.
	 *
	 *		@param tm the TeamManaged object containing the Team to be reset.
	 *		@return boolean result of attempt to reset team.
	 */
	public static void resetTeam(TeamManaged tm)
			throws WTException
	{
		TeamHelper.service.reteam(tm, tm.getTeamTemplateId());	
	}
	
	/**
	 *	Returns boolean whether a participant in a Role argument is in a Group argument.
	 *
	 *		@param obj the Object containing the Team.
	 *		@param org the Organisation containing the Group.
	 *		@param groupName a String containing the name of the Group.
	 *		@param roleStr a String containing the name of the Role.
	 *		@return Boolean
	 */
	public static boolean hasGroupMember(TeamManaged tm, WTOrganization org, String groupName,
				String roleStr)
			
			throws WTException
	{
		
		boolean match = false;
		WTGroup group = PrincipalUtils.getGroup(org, groupName);
		Team team;
		Role role;
		WTPrincipalReference ref;
		WTPrincipal pr;
		
		if (group != null) {
			team = TeamHelper.service.getTeam(tm);
			role = Role.toRole(roleStr);
		
			Enumeration<?> participants = team.getPrincipalTarget(role);
			
			Search:
			while (participants.hasMoreElements()) {
				ref = (WTPrincipalReference)participants.nextElement();
				pr = ref.getPrincipal();
				if (group.isMember(pr)) {
					match = true;
					break Search;
				}
			}
		}
		
		return match;
	}

	/**
	 *
	 */
	public static boolean isAdministrator(WTPrincipal usr, String groupName)
			throws WTException
	{
		StandardOrganizationServicesManager sosm = 
				StandardOrganizationServicesManager.newStandardOrganizationServicesManager();
				
		WTOrganization org = sosm.getOrganization(usr);
		WTGroup group = PrincipalUtils.getGroup(org, ADMIN_GROUP_NAME);
		
		return group.isMember(usr);
	}

	/**
	 *
	 */
	public static boolean isAdministrator(WTPrincipal usr)
			throws WTException
	{
		return isAdministrator(usr, ADMIN_GROUP_NAME);	
	}
	
}