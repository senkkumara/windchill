package ext.hydratight.obj.iba.value;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import wt.fc.WTObject;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueDBServiceInterface;
import wt.iba.value.service.IBAValueHelper;
import wt.part.WTPart;
import wt.services.Manager;
import wt.services.ManagerServiceFactory;

import java.rmi.RemoteException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class ValueUtils
{	
	/**
	 *	Returns the attirbute container for the parameter IBAHolder.
	 *
	 *		@param hdr the IBAHolder to retrieve the attribute container from
	 *		@throws WTException
	 *		@return DefaultAttributeContainer
	 */
	public static DefaultAttributeContainer getContainer(IBAHolder hdr)
			throws WTException
	{	
		DefaultAttributeContainer cont = (DefaultAttributeContainer)hdr.getAttributeContainer();

		if (cont == null) {
			try {
				hdr = IBAValueHelper.service.refreshAttributeContainer(hdr, null, null, null);
			}
			catch (RemoteException rme) {
				throw new WTException(rme);
			}
			
			cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
		}
		
		try {
			cont.setConstraintGroups(new Vector());
		}
		catch (WTPropertyVetoException pve) {
			throw new WTException(pve);
		}
		
		return cont;
	}
	
	/**
	 *	Returns an IBA database service.
	 *
	 *		@throws WTException
	 *		@return IBAValueDBServiceInterface
	 */
	public static IBAValueDBServiceInterface getIBADBService()
			throws WTException
	{
		Manager manager = ManagerServiceFactory.getDefault().getManager(IBAValueDBService.class);

		if (manager == null) {
			throw new WTException("Cannot get manager for IBAValueDBService");
		}
		
		return (IBAValueDBServiceInterface)manager;
	}
	
	/**
	 *	Updates the database with the changes made to an IBAHolder parameter.
	 *
	 *		@param hdr the IBAHolder to be updated in the database
	 *		@throws WTException
	 */
	public static void updateDB(IBAHolder hdr)
			throws WTException
	{
		IBAValueDBServiceInterface dbService = getIBADBService();
		updateDB(hdr, dbService);
	}
	
	/**
	 *	Updates the database with the changes made to an IBAHolder parameter using a database interface parameter.
	 *
	 *		@param hdr the IBAHolder to be updated in the database
	 *		@param dbService the database service interface used to update the database
	 *		@throws WTException
	 */
	public static void updateDB(IBAHolder hdr, IBAValueDBServiceInterface dbService)
			throws WTException
	{
		DefaultAttributeContainer cont = (DefaultAttributeContainer)hdr.getAttributeContainer();
		
		Object constraints = null;
		if (cont != null) {
			constraints = cont.getConstraintParameter();
		}
		
		cont = (DefaultAttributeContainer)dbService.updateAttributeContainer(hdr, constraints, null, null);
	}
	
	/**
	 *	Updates the database with the changes made to an list of IBAHolder parameter.
	 *
	 *		@param hdrs the list of IBAHolder to be updated in the database
	 *		@throws WTException
	 */
	public static List<IBAHolder> updateDB(List<IBAHolder> hdrs)
			throws WTException
	{
		int e = 0;
		List<IBAHolder> f = new ArrayList<IBAHolder>();
		IBAValueDBServiceInterface dbService = getIBADBService();
		
		Update:
		for (IBAHolder hdr : hdrs) {
			try {
				updateDB(hdr, dbService);
			}
			catch (WTException ex) {
				f.add(hdr);
				ex.printStackTrace();
				e++;
			}
		}

		if (e > 0) {
			throw new WTException(new StringBuilder("Failed to update ")
				.append(e)
				.append(" IBAHolders")
				.toString());
		}
		
		return f;	
	}
}