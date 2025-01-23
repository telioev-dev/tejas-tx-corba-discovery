package com.teliolabs.corba.transport;

import com.teliolabs.corba.data.dto.Circle;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.tmforum.mtnm.common.Common_IHolder;
import org.tmforum.mtnm.emsMgr.EMSMgr_I;
import org.tmforum.mtnm.emsMgr.EMSMgr_IHelper;
import org.tmforum.mtnm.emsSession.EmsSession_I;
import org.tmforum.mtnm.emsSession.EmsSession_IHolder;
import org.tmforum.mtnm.emsSessionFactory.EmsSessionFactory_I;
import org.tmforum.mtnm.emsSessionFactory.EmsSessionFactory_IHelper;
import org.tmforum.mtnm.equipment.EquipmentInventoryMgr_I;
import org.tmforum.mtnm.equipment.EquipmentInventoryMgr_IHelper;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.managedElementManager.ManagedElementMgr_I;
import org.tmforum.mtnm.managedElementManager.ManagedElementMgr_IHelper;
import org.tmforum.mtnm.multiLayerSubnetwork.MultiLayerSubnetworkMgr_I;
import org.tmforum.mtnm.multiLayerSubnetwork.MultiLayerSubnetworkMgr_IHelper;
import org.tmforum.mtnm.nmsSession.NmsSession_I;
import org.tmforum.mtnm.nmsSession.NmsSession_IHelper;

import java.util.Objects;

@Log4j2
public final class CorbaConnection implements AutoCloseable {

    public static final String ME_MANAGER_NAME = "ManagedElement";
    public static final String EI_MANAGER_NAME = "EquipmentInventory";
    public static final String MLS_MANAGER_NAME = "MultiLayerSubnetwork";

    public static final String EMS_MANAGER_NAME = "EMS";
    public static final String PRT_MANAGER_NAME = "Protection";

    @Getter
    private boolean valid;

    @Getter
    private CorbaSession corbaSession;

    @Getter
    private ManagedElementMgr_I meManager;

    @Getter
    private EquipmentInventoryMgr_I eiManager;

    @Getter
    private EMSMgr_I emsManager;

    @Getter
    MultiLayerSubnetworkMgr_I mlsnManager;

    @Getter
    private Common_IHolder managerInterface;

    private CorbaConnection() {
    }


    private void initManagers() throws ProcessingFailureException {
        if (meManager == null) {
            managerInterface = new Common_IHolder();
            corbaSession.getEmsSession().getManager(ME_MANAGER_NAME, managerInterface);
            meManager = ManagedElementMgr_IHelper.narrow(managerInterface.value);
        }

        if (mlsnManager == null) {
            managerInterface = new Common_IHolder();
            corbaSession.getEmsSession().getManager(MLS_MANAGER_NAME, managerInterface);
            mlsnManager = MultiLayerSubnetworkMgr_IHelper.narrow(managerInterface.value);
        }

        if (emsManager == null) {
            managerInterface = new Common_IHolder();
            corbaSession.getEmsSession().getManager(EMS_MANAGER_NAME, managerInterface);
            emsManager = EMSMgr_IHelper.narrow(managerInterface.value);
        }

        if (eiManager == null) {
            managerInterface = new Common_IHolder();
            corbaSession.getEmsSession().getManager(EI_MANAGER_NAME, managerInterface);
            this.eiManager = EquipmentInventoryMgr_IHelper.narrow(managerInterface.value);
        }
    }

    // Singleton instance
    private static final CorbaConnection INSTANCE = new CorbaConnection();

    // Public method to get the instance
    private static CorbaConnection getInstance() {
        return INSTANCE;
    }

    public static CorbaConnection getConnection(Circle circle) throws Exception {

        log.debug("Creating connection for Circle: {}", circle);
        String[] orbArgs = {"-ORBInitRef", circle.getNameService().trim()};
        ORB orb = ORB.init(orbArgs, null);
        NamingContextExt nRef = NamingContextExtHelper.
                narrow(orb.resolve_initial_references("NameService"));

        NameComponent[] nameComponents = getNameComponents(circle);
        EmsSessionFactory_I sessionFact = EmsSessionFactory_IHelper.narrow(nRef.resolve(nameComponents));

        POA rpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rpoa.the_POAManager().activate();
        rpoa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);

        CorbaConnection corbaConnection = new CorbaConnection();

        corbaConnection.corbaSession = new CorbaSession();
        corbaConnection.corbaSession.setNameComponents(nameComponents);


        org.omg.CORBA.Object cObj = rpoa.servant_to_reference((Servant) corbaConnection.corbaSession);
        NmsSession_I nmsSession = NmsSession_IHelper.narrow(cObj);
        corbaConnection.corbaSession.setNmsSession(nmsSession);

        EmsSession_IHolder sessHolder = new EmsSession_IHolder();
        String version = sessionFact.getVersion();
        log.debug("NMS Version: {}", version);

        log.debug("User: {}, Password: {}", circle.getUserName(), circle.getPassword());
        log.debug("nmsSession: {}, sessHolder: {}", nmsSession, sessHolder);
        sessionFact.getEmsSession(circle.getUserName(), circle.getPassword(), nmsSession, sessHolder);
        EmsSession_I emsSession = sessHolder.value;

        corbaConnection.corbaSession.setEmsSession(emsSession);

        if (emsSession.associatedSession()._non_existent()) {
            log.error("Auth Fail or EmsSession not exist");
        } else {
            log.info("EMS Connected: {} on address: {}:{}", circle.getEms(), circle.getHost(), circle.getPort());
        }

        corbaConnection.corbaSession.setOrb(orb);
        corbaConnection.corbaSession.setPoa(rpoa);
        corbaConnection.initManagers();
        return corbaConnection;
    }

    private static NameComponent[] getNameComponents(Circle circle) {
        NameComponent tmfClass = new NameComponent("TMF_MTNM", "Class");
        NameComponent tmfVendor = new NameComponent(circle.getVendor(), "Vendor");
        NameComponent tmfEmsInstance = new NameComponent(circle.getEms(), "EmsInstance");
        NameComponent tmfVersion = new NameComponent(circle.getEmsVersion(), "Version");
        NameComponent tmfEntity = new NameComponent(circle.getEms(), "EmsSessionFactory_I");
        NameComponent[] nameComponents = {tmfClass, tmfVendor, tmfEmsInstance, tmfVersion, tmfEntity};
        return nameComponents;
    }

    @Override
    public void close() {
        log.debug("Closing Session...");
        if (Objects.isNull(corbaSession)) {
            log.warn("CorbaSession is null. No resources to close.");
            return;
        }

        try {
            closeNmsSession();
            closeEmsSession();
            destroyRootPoa();
            shutdownOrb();
            clearManagers();
            log.info("All sessions and resources have been successfully closed.");
            valid = false;
        } catch (Exception e) {
            log.error("Error occurred while closing sessions or resources.", e);
        }
    }

    private void clearManagers() {
        managerInterface = null;
        corbaSession = null;
        meManager = null;
    }

    private void closeNmsSession() {
        NmsSession_I nmsSession = corbaSession.getNmsSession();
        if (nmsSession != null) {
            try {
                nmsSession._release();
                nmsSession.endSession();
                corbaSession.setNmsSession(null);
                log.debug("NMS Session Closed");
                valid = false;
            } catch (Exception e) {
                log.error("Error closing NMS session.", e);
            }
        } else {
            log.warn("No NMS Session found.");
        }
    }

    private void closeEmsSession() {
        EmsSession_I emsSession = corbaSession.getEmsSession();
        if (emsSession != null) {
            try {
                emsSession._release();
                emsSession.endSession();
                corbaSession.setEmsSession(null);
                log.debug("EMS Session Closed");
                valid = false;
            } catch (Exception e) {
                log.error("Error closing EMS session.", e);
            }
        } else {
            log.warn("No EMS Session found.");
        }
    }

    private void destroyRootPoa() {
        POA rootPOA = corbaSession.getPoa();
        if (rootPOA != null) {
            try {
                rootPOA.destroy(true, true);
                corbaSession.setPoa(null);
                log.debug("Root POA destroyed");
                valid = false;
            } catch (Exception e) {
                log.error("Error destroying Root POA.", e);
            }
        } else {
            log.warn("No Root POA found.");
        }
    }

    private void shutdownOrb() {
        ORB orb = corbaSession.getOrb();
        if (orb != null) {
            try {
                orb.shutdown(true);
                orb.destroy();
                corbaSession.setOrb(null);
                log.debug("ORB Shutdown and Destroyed");
                valid = false;
            } catch (Exception e) {
                log.error("Error shutting down ORB.", e);
            }
        } else {
            log.warn("No ORB found.");
        }
    }
}
