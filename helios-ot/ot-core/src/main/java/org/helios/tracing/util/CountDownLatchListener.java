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
package org.helios.tracing.util;

/**
 * <p>Title: CountDownLatchListener</p>
 * <p>Description: Defines a class that listens on the countdown event of a CountDownLatchBroadcaster.</p> 
 * <p>Company: Helios Development Group</p>
 * @author Whitehead (whitehead.nicholas@gmail.com)
 * @version $LastChangedRevision: 1058 $
 * $HeadURL: https://helios.dev.java.net/svn/helios/helios-opentrace-pre-maven/branches/DEV-0.1/src/org/helios/tracing/util/CountDownLatchListener.java $
 * $Id: CountDownLatchListener.java 1058 2009-02-18 17:33:54Z nwhitehead $
 */
public interface CountDownLatchListener {
	/**
	 * Callback from a CountDownLatchBroadcaster when it counts down.
	 * @param serial The unqiue id of the countdown.
	 */
	public void countDownComplete(long serial);
	/**
	 * Callback from a CountDownLatchBroadcaster indicating it timed out before completion.
	 * @param serial The unqiue id of the countdown.
	 */	
	public void countDownTimedOut(long serial);
	
	/**
	 * Callback from a CountDownLatchBroadcaster indicating the watching thread was interrupted before completion.
	 * @param serial The unqiue id of the countdown.
	 */	
	public void countDownInterrupted(long serial);
	
}
