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
package org.helios.editors;

import java.beans.PropertyEditorSupport;

import org.apache.log4j.Level;

/**
 * <p>Title: Log4JLevelEditor</p>
 * <p>Description: A property editor for Log4J levels</p> 
 * <p>Company: Helios Development Group</p>
 * @author Whitehead (whitehead.nicholas@gmail.com)
 * @version $LastChangedRevision$
 * $HeadURL$
 * $Id$
 */
public class Log4JLevelEditor extends PropertyEditorSupport {
	
	/**
	 * Converts the passed String to a Log4J Level and applies. 
	 * @param text The string to convert and apply.
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(final String text) {
		try {
		      Level level = Level.toLevel(text);
		      setValue(level);			
		} catch (Exception e) {
			throw new IllegalArgumentException("The value [" + text + "] is not a valid Level", e);
		}
	}
	
	/**
	 * Converts the Level back to a string. 
	 * @return A string representation of the Level.
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText()   {
		Level level = (Level) getValue();
		return level.toString();
	}
}
