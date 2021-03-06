/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.collectors.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.helios.collectors.jmx.identifiers.AbstractMBeanServerIdentifier;
import org.helios.collectors.jmx.identifiers.DefaultMBeanServerIdentifier;
import org.helios.collectors.jmx.identifiers.IMBeanServerIdentifier;
import org.helios.helpers.JMXHelper;
import org.helios.jmx.dynamic.ManagedObjectDynamicMBean;
import org.helios.jmx.dynamic.annotations.JMXAttribute;
import org.helios.jmx.dynamic.annotations.JMXManagedObject;
import org.helios.jmx.dynamic.annotations.JMXNotification;
import org.helios.jmx.dynamic.annotations.JMXNotificationType;
import org.helios.jmx.dynamic.annotations.JMXNotifications;
import org.helios.jmx.dynamic.annotations.JMXOperation;
import org.helios.jmx.dynamic.annotations.options.AttributeMutabilityOption;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>Title: AbstractMBeanServerConnectionFactory</p>
 * <p>Description: Abstract base class for concrete MBeanServerConnectionFactories</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>org.helios.collectors.jmx.AbstractMBeanServerConnectionFactory</code></p>
 */
@JMXManagedObject(annotated=true, declared=true)
@JMXNotifications(notifications={
        @JMXNotification(description="Notification indicating error connecting to a remote MBeanServer", types={
                @JMXNotificationType(type="org.helios.remote.mbean.state.notification")
        }),
        @JMXNotification(description="Notification indicating change in active domains on remote MBeanServer", types={
                @JMXNotificationType(type="org.helios.remote.mbean.domain.notification")
        })
})
public abstract class AbstractMBeanServerConnectionFactory extends ManagedObjectDynamicMBean implements IMBeanServerConnectionFactory, ApplicationContextAware, MBeanServerConnection {
	/**  */
	private static final long serialVersionUID = 2931250432886706976L;
	/** A serial number factory for designating IDs for unidentifiable VMs */
	protected static final AtomicLong UNKNOWN_SERIAL = new AtomicLong();
	/** A connection pool of MBeanServerConnections */
	protected MBeanServerConnectionPool connectionPool;
	/** The bean name of the mbean server connection pool prototype factory. Defaults to <b><code>MBeanServerConnectionPoolFactory</code></b>. */
	protected String factoryName = DEFAULT_FACTORY_NAME;
	/** The application context */
	protected ApplicationContext applicationContext = null;
	/** The default prototype bean name providing an MBeanServerConnection Pool */
	public static final String DEFAULT_FACTORY_NAME = "MBeanServerConnectionPoolFactory";
	
	/** The configured class name of the server identifier for this connection factory */
	protected volatile String serverIdentifierClassName = null;

	/** The server identifier for this connection factory */
	protected volatile IMBeanServerIdentifier serverIdentifier = null;
	
	/** the host Id that this factory connects to  */
	protected String hostId = null;	
	/** the vm Id that this factory connects to  */
	protected String vmId = null;
	/** the JMX default domain name of the MBeanServer that this factory connects to  */
	protected String defaultDomain = null;
	/** The permanent MBeanServer Connection */
	protected volatile MBeanServerConnection mBeanConnection = null;
	/** The HSPProtocol definition */
	protected final HSPProtocol hspProtocol;
	
	/** The JMXServiceURL template for local in-vm proxy URLs */
	public static final String LOCAL_JMX_SERVICE_TEMPLATE = "service:jmx:hsp://{0}/{1}/{2}?shared={3}";
	
        // service:jmx:rmi://njwub810/jndi/rmi://localhost:8003/jmxrmi?host=njwub810&vmid=default&defaultdomain=jboss
	/** Maximum time (milliseconds) to wait for a remote MBean Server for a new connection */
	protected long timeout = 3000l;
	
	/**  Indicate whether detailed stack trace needs to be printed out in logs*/
	protected boolean printStackTrace = false;	
	
	protected Logger log = null;
	
	protected boolean connected = false;
	protected int retrialsSinceOffline = 0;
	protected List<String> liveDomains = null;
	private boolean everConnected = false;
	
	
	/**
	 * Creates a new AbstractMBeanServerConnectionFactory
	 * @param hspProtocol The HSP protocol used to communicate with this MBeanServer proxy
	 */
	public AbstractMBeanServerConnectionFactory(HSPProtocol hspProtocol) {
		super();
		this.hspProtocol = hspProtocol;
	}


	/**
	 * Registers a management interface and a JMX Server Proxy for this connection factory
	 * @throws Exception
	 */
	public void start() throws Exception {
		try{
			getConnection();
		}catch(MBeanServerConnectionFactoryException e){
			// The remote JMX server to be monitored is offline 
			// so don't register it to Helios MBean Server yet!
			// Subsequent requests to getConnection would take care
			// of it when that instance will come online
		}		
	}


	/**
	 * 
	 */
	private void registerToHeliosServer() {
		String[] ids = getIds();
		hostId = ids[0];
		vmId = ids[1];
		defaultDomain = ids[2];		
		createPool();
		reflectObject(this);
		reflectObject(connectionPool);
		objectName = JMXHelper.objectName(new StringBuilder(CONNECTION_MBEAN_DOMAIN + ":host=").append(hostId).append(",vm=").append(vmId).append(",domain=").append(defaultDomain));
		try {
			JMXHelper.getHeliosMBeanServer().registerMBean(this, objectName);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Unregisters the management interface and the JMX Server Proxy for this connection factory
	 */
	public void stop() {
		try { connectionPool.close(); } catch (Exception e) {log.debug(e.getMessage());}
		try { JMXHelper.getHeliosMBeanServer().unregisterMBean(objectName); } catch (Exception e) {}
	}
	
	/**
	 * Returns a default (shared connection) JMXServiceURL for a proxy connection to this MBeanServer proxy. 
	 * @return a JMXServiceURL
	 */
	@JMXAttribute(name="JMXServiceURL", description="Returns a default (shared connection) JMXServiceURL for a proxy connection to this MBeanServer proxy.", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getJMXServiceURL() {
		return getServiceURL(true).toString();
	}
	
	/**
	 * Returns a dedicated connection JMXServiceURL for a proxy connection to this MBeanServer proxy. 
	 * @return a JMXServiceURL
	 */
	@JMXAttribute(name="DedicatedJMXServiceURL", description="Returns a default (shared connection) JMXServiceURL for a proxy connection to this MBeanServer proxy.", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getDedicatedJMXServiceURL() {
		return getServiceURL(false).toString();
	}
	
	/**
	 * Creates and returns a JMXServiceURL for a proxy connection to this proxy MBeanServer
	 * @param shared true for shared physical connections, false for a dedicated connection
	 * @return JMXServiceURL
	 */
	protected JMXServiceURL getServiceURL(boolean shared) {
		String url = null;
		try {
			//url = MessageFormat.format(LOCAL_JMX_SERVICE_TEMPLATE, hostId, vmId, defaultDomain, shared);
			url = hspProtocol.formatServiceURL(objectName, shared);
			return new JMXServiceURL(url);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create LocalServiceURL for [" + url + "]", e);
		}		
	}
	
	/**
	 * Creates the configured pool. If the config is not set or an exception is thrown 
	 * creating the configured pool, the default pool is created.
	 */
	protected void createPool() {
		try {
			connectionPool = (MBeanServerConnectionPool)
				applicationContext.getBean(factoryName, this); 
		} catch (Exception e) {
			connectionPool = new MBeanServerConnectionPool(this);
		}
	}
	
	/**
	 * Returns the permanent connection to the target MBeanServer
	 * @return MBeanServerConnection
	 * @throws MBeanServerConnectionFactoryException
	 */
	@JMXOperation(name="getConnection", description="Acquires an MBeanServerConnection from this factory")
	public MBeanServerConnection getConnection() throws MBeanServerConnectionFactoryException {
		try {
			//If the connection is already created, return that to the caller
			synchronized(this) {
				if(mBeanConnection==null) {
					mBeanConnection = _getConnection();
					if(!everConnected){
						everConnected = true;
						registerToHeliosServer();
					}
				}
			}
			setConnectionStatus(true);
			return mBeanConnection;
		} catch (Exception e) {
			setConnectionStatus(false);
			if(printStackTrace)
				log.error(e.getMessage(),e);
			throw new MBeanServerConnectionFactoryException("Failed to acquire permanent MBeanServer Connection", e);
		}
	}

	@JMXOperation(name="setConnectionStatus", description="Setter for connection status")
	public synchronized void setConnectionStatus(boolean isConnected) {
		//reporting a status that we already know - so return right away
		if (isConnected == connected){
			if(!this.connected)
				retrialsSinceOffline++;
			return;
		}			
		if(isConnected){
			// Connection to remote MBean server is back online now.  
			if(!connected){
				connected = true;
				retrialsSinceOffline = 0;
				this.sendNotification(new Notification("org.helios.remote.mbean.connection.state",this,notificationSequence.incrementAndGet(),System.currentTimeMillis(),"Connection created to remote MBean server with object name: " + this.objectName));
			}
		}else{
			retrialsSinceOffline++;
			if(connected){
				connected = false;
				this.sendNotification(new Notification("org.helios.remote.mbean.connection.state",this,notificationSequence.incrementAndGet(),System.currentTimeMillis(),"Connection lost to remote MBean server with object name: " + this.objectName));
			}
		}
	}
	
	/**
	 * Returns a new connection
	 * @return An MBeanConnection.
	 * @throws MBeanServerConnectionFactoryException
	 * @throws IOException 
	 */
	@JMXOperation(name="getNewConnection", description="Acquires a new MBeanServerConnection from this factory")
	public MBeanServerConnection getNewConnection() throws MBeanServerConnectionFactoryException {
		try {
			MBeanServerConnection connection = _getConnection();
			setConnectionStatus(true);
			return connection;
		} catch (Exception e) {
			setConnectionStatus(false);
			throw new MBeanServerConnectionFactoryException("Failed to create a new connection" , e);
		}
	}
	
	/**
	 * Detects any domain changes from the previous poll and send notification if there are any changes
	 * 
	 * @param oName
	 * @param domains
	 */
	@JMXOperation(name="processDomains", description="Detects any domain changes from the previous poll and send notification if there are any changes")
	public void processDomains(ArrayList<String> domains){
		if(liveDomains == null){
			liveDomains = domains;
		}else{
			List<String> offlineDomains = (List<String>)ListUtils.subtract(liveDomains, domains);
			List<String> newDomains = (List<String>)ListUtils.subtract(domains, liveDomains);
			if(!(offlineDomains.isEmpty()) || !(newDomains.isEmpty()) ){
				log.debug("There are domain changes for this remote MBean Server: " + objectName);
				if(!(newDomains.isEmpty())){
					// Some domain(s) are new so send out notification for each one of 'em
					for(String newD: newDomains){
						this.sendNotification(new Notification("org.helios.remote.mbean.domain.notification", this, notificationSequence.incrementAndGet(),System.currentTimeMillis(), "Domain came online: " + newD));
						log.info("Notification sent out for new Domain: " + newD);
					}
				}
				
				if(!(offlineDomains.isEmpty())){
					// Some domain(s) have gone offline so send out notification for each one of 'em
					for(Object offlineD: offlineDomains){
						this.sendNotification(new Notification("org.helios.remote.mbean.domain.notification", this, notificationSequence.incrementAndGet(),System.currentTimeMillis(), "Domain went offline: " + offlineD));
						log.info("Notification sent out for dead Domain: " + offlineD);
					}
				}
				liveDomains = domains;
			}else{
				log.debug("There are no domain changes for this remote MBean Server: " + objectName);
			}
		}
	}	
	
	/**
	 * Returns a pooled connection
	 * @return An MBeanConnection.
	 * @throws MBeanServerConnectionFactoryException
	 */
	@JMXOperation(name="getPooledConnection", description="Acquires a pooled MBeanServerConnection from this factory's pool")
	public MBeanServerConnection getPooledConnection() throws MBeanServerConnectionFactoryException {
		try {
			return connectionPool.borrowObject();
		} catch (Exception e) {
			throw new MBeanServerConnectionFactoryException("Failed to create new connection" , e);
		}
	}
	
	/**
	 * Returns a pooled connection to the pool
	 * @param connection the MBeanServerConnection to return to the pool
	 * @throws Exception
	 */
	@JMXOperation(name="returnPooledConnection", description="Returns a pooled MBeanServerConnection to this factory's pool")
	public void returnPooledConnection(MBeanServerConnection connection) throws Exception {
		connectionPool.returnObject(connection);
	}
	
	
	/**
	 * Resets the permanent connection
	 * @return the permanent connection
	 * @throws MBeanServerConnectionFactoryException
	 */
	@JMXOperation(name="resetConnection", description="Resets the permanent connection")
	public MBeanServerConnection resetConnection() throws MBeanServerConnectionFactoryException {
		mBeanConnection = null;
		return getConnection();
	}
	
	
	/**
	 * The internal getConnection implementation
	 * @return an MBeanServerConnection
	 * @throws IOException
	 */
	protected abstract MBeanServerConnection _getConnection() throws Exception;	
	
	/**
	 * Returns the host Id that this factory connects to 
	 * @return the host Id that this factory connects to
	 */
	@JMXAttribute(name="HostId", description="the host Id that this factory connects to", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getHostId() {
		return hostId;
	}
	
	/**
	 * Returns the vm Id that this factory connects to 
	 * @return the vm Id that this factory connects to
	 */
	@JMXAttribute(name="VMId", description="the VM Id that this factory connects to", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getVMId() {
		return vmId;
	}
	
	/**
	 * Returns the JMX default domain name of the MBeanServer that this factory connects to
	 * @return the JMX default domain name of the MBeanServer that this factory connects to
	 */
	@JMXAttribute(name="DefaultDomain", description="the JMX default domain name of the MBeanServer that this factory connects to", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getDefaultDomain() {
		return defaultDomain;
	}
	
	
	
	/**
	 * Returns the host and vm ids for the VM that this connection factory connects to
	 * @return A String array with the host id in [0] and the VM id in [1].
	 */	
	public synchronized String[] getIds() {
		if(serverIdentifier==null) {
			if(serverIdentifierClassName==null) {
				serverIdentifier = new DefaultMBeanServerIdentifier();
			} else {
				serverIdentifier = AbstractMBeanServerIdentifier.getInstance(serverIdentifierClassName);
			}
		}	
		MBeanServerConnection connection = null;
		try {
			connection = this.getConnection();
			return serverIdentifier.getHostVMId(connection);
		} catch (Exception e) {
			return new String[]{"UnknownHost#" + UNKNOWN_SERIAL.incrementAndGet(), "UnknownVM#" + UNKNOWN_SERIAL.incrementAndGet(), "UnknownDomain#" + UNKNOWN_SERIAL.incrementAndGet() };
		} finally {
			// How do we close the connection ?
		}
	}
	
	
	/**
	 * Returns the configured class name of the server identifier for this connection factory 
	 * @return the serverIdentifierClassName
	 */
	public String getServerIdentifierClassName() {
		return serverIdentifierClassName;
	}
	
	/**
	 * Sets the configured class name of the server identifier for this connection factory
	 * @param serverIdentifierClassName the serverIdentifierClassName to set
	 */
	public void setServerIdentifierClassName(String serverIdentifierClassName) {
		this.serverIdentifierClassName = serverIdentifierClassName;
	}


	/**
	 * No Op
	 * @param obj
	 * @throws Exception
	 */
	@Override
	public void activateObject(Object obj) throws Exception {
		
	}

	/**
	 * Destroys an MBeanServerConnection
	 * @param obj an MBeanServerConnection
	 * @throws Exception
	 */
	@Override
	public void destroyObject(Object obj) throws Exception {
		if(obj!=null && obj instanceof Closeable) {
			try { ((Closeable)obj).close(); } catch (Exception e) {}
		}
	}

	/**
	 * Creates a new MBeanServerConnection
	 * @return a new MBeanServerConnection
	 * @throws Exception
	 */
	@Override
	public MBeanServerConnection makeObject() throws Exception {
		return getNewConnection();
	}

	/**
	 * No Op
	 * @param obj
	 * @throws Exception
	 */
	@Override
	public void passivateObject(Object obj) throws Exception {
		
	}

	/**
	 * Validates an MBeanServerConnection
	 * @param connection
	 * @return
	 */
	@Override
	public boolean validateObject(Object connection) {
		if(connection==null || !(connection instanceof MBeanServerConnection)) return false;
		try { 
			((MBeanServerConnection)connection).getMBeanCount(); 
			return true; 
		} catch (Exception e) {
			return false;
		}		
	}

	/**
	 * Sets the application context
	 * @param applicationContext the applicationContext to set
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Returns the bean name of the mbean server connection pool prototype factory
	 * @return the factoryName
	 */
	public String getFactoryName() {
		return factoryName;
	}

	/**
	 * Sets the bean name of the mbean server connection pool prototype factory
	 * @param factoryName the factoryName to set
	 */
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	/**
	 * Indicates if the permanent MBeanServer connection is connected
	 * @return true if the permanent MBeanServer connection is connected
	 */
	@JMXAttribute(name="Connected", description="Indicates if the permanent MBeanServer connection is connected", mutability=AttributeMutabilityOption.READ_ONLY)
	public boolean getConnected() {
		return connected;
	}

	
	/**
	 * Checks the permanent connection and acquires it if it is null.
	 */
	protected void validateConn() {
		if(mBeanConnection==null) {
			try {
				getConnection();
			}  catch (Exception e) {
				throw new RuntimeException("Failed to invoke against Permanent MBeanServerConnection", e);
			}
		}		
	}

	/**
	 * Adds a notification listener to the permanent MBeanServer connection
	 * @param objectName
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="addNotificationListener", description="Adds a notification listener to the permanent MBeanServer connection")
	public void addNotificationListener(ObjectName objectName, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, IOException {
		validateConn();
		mBeanConnection.addNotificationListener(objectName, listener, filter, handback);
	}
	
	/**
	 * Adds a notification listener to the permanent MBeanServer connection
	 * @param objectName
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="addNotificationListener", description="Adds a notification listener to the permanent MBeanServer connection")
	public void addNotificationListener(ObjectName objectName, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, IOException {
		validateConn();
		mBeanConnection.addNotificationListener(objectName, listener, filter, handback);
	}
	


	/**
	 * Instantiates and registers an MBean in the MBean server.
	 * @param className
	 * @param objectName
	 * @return the ObjectInstance representing the created MBean
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="createMBean", description="Instantiates and registers an MBean in the MBean server.")
	public ObjectInstance createMBean(String className, ObjectName objectName) throws ReflectionException, InstanceAlreadyExistsException,MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.createMBean(className, objectName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Instantiates and registers an MBean in the MBean server.
	 * @param className
	 * @param objectName
	 * @param loaderName
	 * @return the ObjectInstance representing the created MBean
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 * @throws InstanceNotFoundException
	 */
	@Override
	@JMXOperation(name="createMBean", description="Instantiates and registers an MBean in the MBean server.")
	public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException,MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException, InstanceNotFoundException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.createMBean(className, objectName, loaderName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}
	
	/**
	 * Instantiates and registers an MBean in the MBean server.
	 * @param className
	 * @param objectName
	 * @param param 
	 * @param signature
	 * @return the ObjectInstance representing the created MBean
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="createMBean", description="Instantiates and registers an MBean in the MBean server.")
	public ObjectInstance createMBean(String className, ObjectName objectName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException,MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.createMBean(className, objectName, params, signature);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}
	
	/**
	 * Instantiates and registers an MBean in the MBean server.
	 * @param className
	 * @param objectName
	 * @param loaderName
	 * @param param 
	 * @param signature
	 * @return the ObjectInstance representing the created MBean
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 * @throws InstanceNotFoundException
	 */
	@Override
	@JMXOperation(name="createMBean", description="Instantiates and registers an MBean in the MBean server.")
	public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException,MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException, InstanceNotFoundException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.createMBean(className, objectName, loaderName, params, signature);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Returns the value of an attribute
	 * @param objectName
	 * @param attributeName
	 * @return
	 * @throws MBeanException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="getAttribute", description="Returns the value of the passed attribute name")
	public Object getAttribute(ObjectName objectName, String attributeName) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.getAttribute(objectName, attributeName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Returns the values of the passed attribute names
	 * @param objectName
	 * @param attributeNames
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="getAttributes", description="Returns the values of the passed attribute names")
	public AttributeList getAttributes(ObjectName objectName, String[] attributeNames) throws InstanceNotFoundException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.getAttributes(objectName, attributeNames);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Returns the list of domains for target MBeanServer that was retrieved during 
	 * last connection check poll for this server
	 * @return an array of domain names
	 * @throws IOException
	 */
	@Override
	@JMXAttribute(name="Domains", description="The domains of the target MBeanServer", mutability=AttributeMutabilityOption.READ_ONLY)
	public String[] getDomains() {
		try {
			if(liveDomains==null)
				return getConnection().getDomains();
			else
				return liveDomains.toArray(new String[0]);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return null;
		} 
	}

	/**
	 * Returns the number of MBeans registered in the target MBeanServer
	 * @return
	 * @throws IOException
	 */
	@Override
	@JMXAttribute(name="MBeanCount", description="The number of MBeans registered in the target MBeanServer", mutability=AttributeMutabilityOption.READ_ONLY)
	public Integer getMBeanCount(){
		try{
			return getConnection().getMBeanCount();
		}catch(Exception ex){
			return -1;
		}
	}

	/**
	 * Returns the MBeanInfo for the passed ObjectName
	 * @param objectName
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IntrospectionException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="getMBeanInfo", description="Returns the MBeanInfo for the passed ObjectName")
	public MBeanInfo getMBeanInfo(ObjectName objectName) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.getMBeanInfo(objectName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Returns the ObjectInstance for the passed ObjectName
	 * @param objectName
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="getObjectInstance", description="Returns the ObjectInstance for the passed ObjectName")
	public ObjectInstance getObjectInstance(ObjectName objectName) throws InstanceNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.getObjectInstance(objectName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Invokes the named operation on the named MBean
	 * @param objectName
	 * @param opName
	 * @param params
	 * @param signature
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="invoke", description="Invokes the named operation on the named MBean")
	public Object invoke(ObjectName objectName, String opName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.invoke(objectName, opName, params, signature);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Determines if the named MBean is an instance of the passed class name
	 * @param objectName
	 * @param className
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="isInstanceOf", description="Determines if the named MBean is an instance of the passed class name")
	public boolean isInstanceOf(ObjectName objectName, String className) throws InstanceNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.isInstanceOf(objectName, className);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Determines if the passed ObjectName is registered
	 * @param objectName
	 * @return
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="isRegistered", description="Determines if the passed ObjectName is registered")
	public boolean isRegistered(ObjectName objectName) throws IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.isRegistered(objectName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Queries a set of MBean ObjectInstances from the passed ObjectName and Query expression
	 * @param objectNaNames 
	 * @param query
	 * @return
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="queryMBeans", description="Queries a set of MBean ObjectInstances from the passed ObjectName and Query expression")
	public Set<ObjectInstance> queryMBeans(ObjectName objectName, QueryExp query) throws IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.queryMBeans(objectName, query);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Queries a set of MBean ObjectNames from the passed ObjectName and Query expression
	 * @param objectName
	 * @param query
	 * @return
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="queryNames", description="Queries a set of MBean ObjectNames from the passed ObjectName and Query expression")
	public Set<ObjectName> queryNames(ObjectName objectName, QueryExp query) throws IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.queryNames(objectName, query);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Removes a registered notification listener
	 * @param objectName
	 * @param listener
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="removeNotificationListener", description="Removes a registered notification listener")
	public void removeNotificationListener(ObjectName objectName, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.removeNotificationListener(objectName, listener);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}		
	}

	/**
	 * Removes a registered notification listener
	 * @param objectName
	 * @param listener
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="removeNotificationListener", description="Removes a registered notification listener")
	public void removeNotificationListener(ObjectName objectName, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.removeNotificationListener(objectName, listener);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}		
	}

	/**
	 * Removes a registered notification listener
	 * @param objectName
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="removeNotificationListener", description="Removes a registered notification listener")
	public void removeNotificationListener(ObjectName objectName, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.removeNotificationListener(objectName, listener, filter, handback);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}		
	}

	/**
	 * Removes a registered notification listener
	 * @param objectName
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="removeNotificationListener", description="Removes a registered notification listener")
	public void removeNotificationListener(ObjectName objectName, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.removeNotificationListener(objectName, listener, filter, handback);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}		
	}

	/**
	 * Sets the value of an attribute
	 * @param objectName
	 * @param attribute
	 * @throws InstanceNotFoundException
	 * @throws AttributeNotFoundException
	 * @throws InvalidAttributeValueException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="setAttribute", description="Sets the value of an attribute")
	public void setAttribute(ObjectName objectName, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.setAttribute(objectName, attribute);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
		
	}

	/**
	 * Sets the value of a list of attributes
	 * @param objectName
	 * @param attributeList
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="setAttributes", description="Sets the value of a list of attributes")
	public AttributeList setAttributes(ObjectName objectName, AttributeList attributeList) throws InstanceNotFoundException, ReflectionException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			return conn.setAttributes(objectName, attributeList);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
	}

	/**
	 * Unregisters an MBean identified by the passed ObjectName
	 * @param objectName
	 * @throws InstanceNotFoundException
	 * @throws MBeanRegistrationException
	 * @throws IOException
	 */
	@Override
	@JMXOperation(name="unregisterMBean", description="Unregisters an MBean identified by the passed ObjectName")
	public void unregisterMBean(ObjectName objectName) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
		validateConn();
		MBeanServerConnection conn = null;
		try {
			conn = getPooledConnection();
			conn.unregisterMBean(objectName);
		} catch (MBeanServerConnectionFactoryException e) {
			throw new RuntimeException("Failed to get pooled connection", e);
		} finally {
			try { this.returnPooledConnection(conn); } catch (Exception e) {log.debug(e.getMessage());}
		}
		
	}
	
	/** Return timeout currently set (milliseconds)*/
	public long getTimeout() {
		return timeout;
	}
	
	/**
	 * Set timeout (milliseconds
	 * @param timeout
	 */
	@JMXOperation(name="setTimeout", description="Set timeout in milliseconds")
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@JMXAttribute(name="printStackTrace", description="Dictates whether detailed stack traces for exceptions should be printed out in logs", mutability=AttributeMutabilityOption.READ_WRITE)
	public boolean getPrintStackTrace() {
		return printStackTrace;
	}
	
	/**
	 * Dictates whether detailed stack traces for exceptions should be printed out in logs
	 * @param logStackTrace
	 */
	public void setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
	}

	/**
	 * @return the retrialsSinceOffline
	 */
	@JMXAttribute(name="retrialsSinceOffline", description="Number of attempts since the remote server went offline", mutability=AttributeMutabilityOption.READ_ONLY)
	public int getRetrialsSinceOffline() {
		return retrialsSinceOffline;
	}	
	
}
