package com.teliolabs.corba.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;
import org.tmforum.mtnm.emsSession.EmsSession_I;
import org.tmforum.mtnm.nmsSession.NmsSession_I;
import org.tmforum.mtnm.nmsSession.NmsSession_IPOA;
import org.tmforum.mtnm.session.Session_I;

import java.time.Instant;

@Log4j2
@Getter
@Setter
@RequiredArgsConstructor
public class CorbaSession extends NmsSession_IPOA {
    private EmsSession_I emsSession;
    private NmsSession_I nmsSession;
    private String user;
    private NameComponent[] nameComponents;
    private ORB orb;
    private POA poa;

    @Override
    public void eventLossOccurred(String startTime, String notificationId) {
        log.debug("eventLossOccurred (startTime) - {}, (notificationId) - {}", startTime, notificationId);
    }

    @Override
    public void eventLossCleared(String endTime) {
        log.info("eventLossCleared - {}", endTime);
    }

    @Override
    public void alarmLossOccurred(String startTime, String notificationId) {
        log.debug("alarmLossOccurred (startTime) - {}, (notificationId) - {}", startTime, notificationId);
    }

    @Override
    public Session_I associatedSession() {
        log.debug("associatedSession");
        return null;
    }

    @Override
    public void ping() {
        Instant now = Instant.now();
        log.debug("Ping received at {}", now);
        try {
            if (getEmsSession() != null && !getEmsSession()._non_existent()) {
                getEmsSession().ping();
                log.debug("Ping replied at {}", now);
            }
        } catch (Exception ex) {
            //do nothing
            log.error("Error sending ping reply");
        }
    }

    @Override
    public void endSession() {
        log.info("endSession triggered");
    }
}
