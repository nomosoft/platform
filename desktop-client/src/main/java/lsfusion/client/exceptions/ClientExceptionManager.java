package lsfusion.client.exceptions;

import lsfusion.base.ExceptionUtils;
import lsfusion.client.Log;
import lsfusion.client.Main;
import lsfusion.client.SwingUtils;
import lsfusion.client.form.RmiQueue;
import lsfusion.interop.exceptions.FatalHandledRemoteException;
import lsfusion.interop.exceptions.RemoteAbandonedException;
import lsfusion.interop.exceptions.RemoteClientException;
import lsfusion.interop.exceptions.RemoteServerException;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import static lsfusion.client.ClientResourceBundle.getString;

public class ClientExceptionManager {
    private final static Logger logger = Logger.getLogger(ClientExceptionManager.class);
    private final static List<Throwable> unreportedThrowables = new ArrayList<>();
    
    public static Throwable fromDesktopClientToAppServer(Throwable e) {
        return e; // assuming that here should be only swing exceptions, so server has all this exceptions and will be able do deserialize them 
    }

    public static void handle(final Throwable e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logger.error("Client error: ", e);
                
                Throwable remote = getRemoteExceptionCause(e);
                if(remote == null) {
                    Log.error(getString("errors.internal.client.error"), e);
                    reportClientThrowable(fromDesktopClientToAppServer(e));
                }
                if (remote instanceof RemoteServerException) {
                    Log.error((RemoteServerException) remote);
                    reportServerRemoteThrowable((RemoteServerException) remote);
                }
                if(remote instanceof FatalHandledRemoteException) {
                    reportClientHandledRemoteThrowable((RemoteClientException) remote);
                }
                if(remote instanceof RemoteException) { // unhandled
                    RemoteException unhandled = (RemoteException) remote;
                    RmiQueue.handleNotRetryableRemoteException(unhandled);
                    reportClientUnhandledRemoteThrowable(unhandled);
                }
                if(remote instanceof RemoteAbandonedException) {                    
                }                    
            }
        });
    }

    public static Throwable getRemoteExceptionCause(Throwable e) {
        for (Throwable ex = e; ex != null && ex != ex.getCause(); ex = ex.getCause()) {
            if (ex instanceof RemoteException || ex instanceof RemoteServerException || ex instanceof RemoteClientException || ex instanceof RemoteAbandonedException) {                
                assert !(ex instanceof RemoteClientException) || ex instanceof FatalHandledRemoteException;  
                return ex;
            }
        }
        return null;
    }

    public static void reportClientThrowable(Throwable t) { // не содержит remoteException 
        reportThrowable(t);
    }
    public static void reportServerRemoteThrowable(RemoteServerException t) {
        reportThrowable(t);
    }
    public static void reportClientHandledRemoteThrowable(RemoteClientException t) {
        reportThrowable(t);
    }
    public static void reportClientUnhandledRemoteThrowable(RemoteException t) {
        reportThrowable(t);
    }

    public static void reportThrowable(Throwable e) {
        SwingUtils.assertDispatchThread();
        
        logger.error("Reporting throwable : " + e, e);
        
        if (!(e instanceof ConcurrentModificationException && ExceptionUtils.getStackTrace(e).contains("bibliothek.gui.dock.themes.basic.action.buttons.ButtonPanel.setForeground"))) {
            synchronized (unreportedThrowables) {
                boolean reported = false;
                try {
                    reported = Main.clientExceptionLog("Client error", e);
                } catch (ConnectException ex) {
                    logger.error("Error reporting client connect exception: " + e, ex);
                } catch (Throwable ex) {
                    logger.error("Error reporting client exception: " + e, ex);
                }
                if(!reported)
                    unreportedThrowables.add(e);
            }
        }
    }

    public static void flushUnreportedThrowables() {
        synchronized (unreportedThrowables) {
            for (Iterator<Throwable> iterator = unreportedThrowables.iterator(); iterator.hasNext(); ) {
                Throwable t = iterator.next();
                boolean reported = false;
                try {
                    reported = Main.clientExceptionLog("Unreported client error", t);
                } catch (Throwable e) {
                    logger.error("Error reporting unreported client exception: " + t, e);
                }
                if(reported)
                    iterator.remove();
            }
        }
    }
}
