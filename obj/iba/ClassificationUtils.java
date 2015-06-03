package ext.hydratight.obj.iba;

import ext.hydratight.obj.iba.value.StringValueUtils;
import com.ptc.core.components.beans.TypeInstanceBean;
import com.ptc.core.meta.common.AttributeTypeIdentifier;
import com.ptc.core.meta.common.IdentifierFactory;
import com.ptc.core.meta.common.RemoteWorkerHandler;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.impl.GetTypeInstanceIdentifierRemoteWorker;
import com.ptc.core.meta.container.common.AttributeContainerSpec;
import com.ptc.core.meta.type.common.TypeInstance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.services.applicationcontext.implementation.DefaultServiceProvider;

import com.ptc.core.meta.common.IllegalFormatException;
import wt.util.WTException;

/**
 *	This class provides help with the classification of Parts within Windchill.<br />
 *	<br />
 *	This class has been developed and verified for use in Windchill 10.2 M020.
 *
 *		@author Toby Pettit
 *		@version 1.0
 **/
public class ClassificationUtils
{

	final static private String MAJOR_PRODUCT_GROUP_IBA = "Major_Product_Group";
	final static private String PRODUCT_SUB_GROUP_IBA = "Product_Sub_Group";
	final static private String PRODUCT_LINE_IBA = "Product_Line";
	final static private String ASSET_TYPE_IBA = "Asset_Type";
	final static private String HARMONIZED_CODE_IBA = "Harmonized_Code";
	final static private String INTRASTAT_CODE_IBA = "Intrastat_Code";
	final static private String CLASSIFICATION_ID = "classification.id";
	final static private List<String> attrs = Arrays.asList(new String[]{ MAJOR_PRODUCT_GROUP_IBA, 
			PRODUCT_SUB_GROUP_IBA, PRODUCT_LINE_IBA, ASSET_TYPE_IBA, HARMONIZED_CODE_IBA, INTRASTAT_CODE_IBA });
	
	/**
	 *	Returns boolean depending on whether the argument Part has been classified.<br />
	 *
	 *		@param part the WTPart to be checked.
	 *		@throws WTException
	 *		@return boolean returns "true" if the Part has been classified.
	 */
	public static boolean isClassified(WTPart part)
			throws WTException
	{
		Boolean classified = false;
		TypeInstance ti;
		ReferenceFactory rf = new ReferenceFactory();
		WTReference ref = rf.getReference(part);
		TypeInstanceIdentifier tii;
		TypeIdentifier tid;
		IdentifierFactory idFact;
		AttributeTypeIdentifier ati;
		AttributeTypeIdentifier[] atis;
		AttributeContainerSpec acs;
		TypeInstanceBean bean;
		List<TypeInstance> tiList;
		Object obj;

		try {
			tii = (TypeInstanceIdentifier)RemoteWorkerHandler.handleRemoteWorker(new 
					GetTypeInstanceIdentifierRemoteWorker(), ref);
					
		}
		catch (Exception ex) {
			throw new WTException(ex);
		}
			
		tid = (TypeIdentifier)tii.getDefinitionIdentifier();
		idFact = (IdentifierFactory)DefaultServiceProvider.getService(IdentifierFactory.class, "logical");
 
		try {
			ati = ((AttributeTypeIdentifier)idFact.get(CLASSIFICATION_ID, tid));
	  
		}
		catch (IllegalFormatException ex) {					
			throw new WTException(ex);
		}
	
		atis = new AttributeTypeIdentifier[1];
		atis[0] = ati;

		acs = new AttributeContainerSpec();
		acs.putEntry(ati);

		bean = new TypeInstanceBean();
		bean.setRowData(Collections.singletonList(tii));
		bean.setInflateTypeInstanceRows(true);
		bean.setFilter(acs);
		
		tiList = bean.getTypeInstances();
		
		if (tiList != null) {
			ti = (TypeInstance)tiList.get(0);

			obj = ti.get(ati);
			if (obj != null) {
				classified = true;
			}
		}
		
		return classified;
	}
	
	/**
	 *	Returns boolean depending on whether the argument Part contains the required classification<br />
	 *	attributes.<br />
	 *	<br />
	 *	This method should be applied to Parts that have not been classifed to verify whether the<br />
	 *	attributes have been populated by other means - e.g. the Part was migrated / copied from a<br />
	 *	migrated part.
	 *
	 *		@param part the WTPart to be checked
	 *		@return boolean returns "true" if the Part contains the classification attributes
	 */
	public static boolean hasClassificationAttributes(WTPart part)
			throws WTException
	{
		boolean hasAttr = false;		// Assumed not to be valid
		
		// Attempt to retrieve the value of each attribute
		Map<String, String> values = StringValueUtils.getStringValues((WTObject)part, attrs);
				
		if (values.size() == attrs.size()) {		// If a value could not be retrieved the size of the 
												// hashmap will be smaller than the input String array
			hasAttr = true;
			
			// Check that all of the classification attributes have been populated by the user
			Check:
			for (String attr : values.values()) {
				if (attr.length() == 0) {
					hasAttr = false;	// Attribute is present but has value ""
					break Check;
				}
			}
		}
		
		return hasAttr;
	}
}