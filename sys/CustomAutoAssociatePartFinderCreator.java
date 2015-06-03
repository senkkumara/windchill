package ext.hydratight.sys;

import java.util.Locale;
import com.ptc.windchill.uwgm.common.autoassociate.DefaultAutoAssociatePartFinderCreator;
import wt.epm.EPMDocument;
import wt.epm.workspaces.EPMWorkspace;
import wt.org.WTOrganization;
import wt.part.PartType;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.type.Typed;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTProperties;

import wt.pom.UniquenessException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlException;

/**
 *	
 */
public class HydratightAutoAssociatePartFinderCreator
		extends DefaultAutoAssociatePartFinderCreator
{

	public WTPart createNewWTPart(EPMDocument epmDoc, String partNumber, String partName, PartType partType,
			String folderOID, EPMWorkspace currentWS, WTOrganization org, Source source,
			QuantityUnit defaultUnits)

			throws WTException, WTPropertyVetoException, VersionControlException,
				UniquenessException
	{
		WTPart part = super.createNewWTPart(epmDoc,partNumber,partName,partType,folderOID,currentWS,org,source,defaultUnits);

		try{
			String targetType = WTProperties.getLocalProperties().getProperty("ext.cadx.PartType","wt.part.WTPart");
			((Typed)part).setTypeDefinitionReference(TypedUtilityServiceHelper.service.getTypeDefinitionReference(targetType));
			((WTPartMaster)part.getMaster()).setOrganization(epmDoc.getOrganization());
			((WTPartMaster)part.getMaster()).setEffPropagationStop(false);
			
		}
		catch (Exception schade){
		      throw new WTException(schade);
		}
		
		return part;
	}

	private void setItemTemplate()
	{

	}

	private void setItemType()
	{

	}

	private void setCountryOfOrigin()
	{

	}

	private void setWeightUOM()
	{

	}
}
