/*
 * Copyright (C) 2005 Les A. Hazlewood
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307
 * USA
 *
 * Or, you may view it online at
 * http://www.opensource.org/licenses/lgpl-license.php
 */
package org.jsecurity.session;

import java.io.Serializable;
import java.util.Calendar;
import java.security.Principal;
import java.net.InetAddress;

/**
 * A SessionHandle is a client-tier representation of a server side {@link Session Session}.
 * This implementation is basically a proxy to a server-side {@link SessionManager SessionManager},
 * which will return the proper results for each method call.
 *
 * <p>A <tt>SessionHandle</tt> will cache data when appropriate to avoid a remote method invocation,
 * only communicating with the server when necessary (for example, when determining if
 * {@link #isExpired() isExpired()}, which can only be accurately known by the server).
 *
 * <p>Of course, if used in-process with a SessionManager business POJO, as might be the case in a
 * web-based application where the web classes and server-side business pojos exist in the same
 * JVM, a remote method call will not be incurred.
 *
 * @since 1.0.0
 * @author Les Hazlewood
 */
public class SessionHandle implements Session {

    private Serializable sessionId = null;

    //cached fields to avoid a server-side method call if out-of-process:
    private Calendar startTimestamp = null;
    private Calendar stopTimestamp = null;
    private InetAddress hostAddress = null;

    /**
     * Handle to a server-side SessionManager.  See {@link #setSessionManager} for details.
     */
    private SessionManager sessionManager = null;


    public SessionHandle(){}

    public SessionHandle( SessionManager sessionManager, Serializable sessionId ) {
        setSessionManager( sessionManager );
        setSessionId( sessionId );
    }

    /**
     * Returns the {@link SessionManager SessionManager} used by this handle to invoke
     * all session-related methods.
     * @return the {@link SessionManager SessionManager} used by this handle to invoke
     * all session-related methods.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Sets the {@link SessionManager SessionManager} to which this <tt>SessionHandle</tt> will
     * delegate its method calls.  In a rich client environment, this <tt>SessionManager</tt> will
     * probably be a remoting proxy which executes remote method invocations.  In a single-process
     * environment (e.g. a web  application deployed in the same JVM of the application server),
     * the <tt>SessionManager</tt> can be the actual business POJO implementation.
     *
     * <p>You'll notice the {@link Session Session} interface and the {@link SessionManager}
     * interface are nearly identical.  This is to ensure the SessionManager can support
     * most method calls in the Session interface, via this handle/proxy technique.  The session
     * manager is implementated as a stateless business POJO, with the handle passing the
     * session id as necessary.
     *
     * @param sessionManager the <tt>SessionManager</tt> this handle will use when delegating
     * method calls.
     */
    public void setSessionManager( SessionManager sessionManager ) {
        this.sessionManager = sessionManager;
    }

    /**
     * Sets the sessionId used by this handle for all future {@link SessionManager SessionManager}
     * method invocations.
     * @param sessionId the <tt>sessionId</tt> to use for all <tt>SessionManager</tt> invocations.
     * @see #setSessionManager( SessionManager sessionManager )
     */
    public void setSessionId( Serializable sessionId ) {
        this.sessionId = sessionId;
    }

    /**
     * @see Session#getSessionId()
     */
    public Serializable getSessionId() {
        return sessionId;
    }

    /**
     * @see Session#getStartTimestamp()
     */
    public Calendar getStartTimestamp() {
        if ( startTimestamp == null ) {
            startTimestamp = sessionManager.getStartTimestamp( sessionId );
        }
        return startTimestamp;
    }

    /**
     * @see Session#getStopTimestamp()
     */
    public Calendar getStopTimestamp() {
        if ( stopTimestamp == null ) {
            stopTimestamp = sessionManager.getStopTimestamp( sessionId );
        }
        return stopTimestamp;
    }

    /**
     * @see org.jsecurity.session.Session#getLastAccessTime()
     */
    public Calendar getLastAccessTime() {
        //can't cache - only business pojo knows the accurate time:
        return sessionManager.getLastAccessTime( sessionId );
    }

    /**
     * @see org.jsecurity.session.Session#isExpired()
     */
    public boolean isExpired() {
        //can't cache - only business pojo knows the accurate time for expiration:
        return sessionManager.isExpired( sessionId );
    }

    /**
     * @see org.jsecurity.session.Session#getPrincipal()
     */
    public Principal getPrincipal() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @see org.jsecurity.session.Session#getHostAddress()
     */
    public InetAddress getHostAddress() {
        if ( hostAddress == null ) {
            hostAddress = sessionManager.getHostAddress( sessionId );
        }
        return hostAddress;
    }

    /**
     * @see org.jsecurity.session.Session#touch()
     */
    public void touch() throws ExpiredSessionException {
        sessionManager.touch( sessionId );
    }

    /**
     * @see org.jsecurity.session.Session#stop()
     */
    public void stop() throws ExpiredSessionException {
        sessionManager.stop( sessionId );
    }

    public Object getAttribute( Object key ) throws ExpiredSessionException {
        return sessionManager.getAttribute( sessionId, key );
    }

    public void setAttribute( Object key, Object value )
    throws ExpiredSessionException, IllegalArgumentException {
        sessionManager.setAttribute( sessionId, key, value );
    }

    public Object removeAttribute( Object key ) throws ExpiredSessionException {
        return sessionManager.removeAttribute( sessionId, key );
    }
}
