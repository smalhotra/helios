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
package org.helios.jmx.dynamic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Title: JMXNotification</p>
 * <p>Description: Defines a notification that is supported by the MODB</p> 
 * <p>Company: Helios Development Group</p>
 * @author Whitehead (whitehead.nicholas@gmail.com)
 * @version $LastChangedRevision$
 * $HeadURL$
 * $Id$
 */
@Target(value={ElementType.TYPE,ElementType.ANNOTATION_TYPE} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JMXNotification {
	
	/**
	 * A description of the notification
	 * @return the description of the notification
	 */
	String description() default "MODB Notification";
	
	/**
	 * The name of the notification
	 * @return the name
	 */
	String name() default "javax.management.Notification";
	
	/**
	 * An array of notification types.
	 * @return types
	 */
	JMXNotificationType[] types();
	
	/**
	 * Directs the modb reflector to instrospect the notification name.
	 * @return
	 */
	boolean instrospectName() default false;
	
	/**
	 * Directs the modb reflector to instrospect the notification description.
	 * @return
	 */
	boolean instrospectDescription() default false;
	
	/**
	 * Directs the modb reflector to instrospect the notification types.
	 * @return
	 */
	boolean instrospectTypes() default false;
	
	
	
	
	
}
