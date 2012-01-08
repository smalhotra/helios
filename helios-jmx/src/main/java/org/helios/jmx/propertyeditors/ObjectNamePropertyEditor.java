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
package org.helios.jmx.propertyeditors;

import java.beans.PropertyEditorSupport;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;



/**
 * <p>Title: ObjectNamePropertyEditor</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group</p>
 * @author Whitehead (whitehead.nicholas@gmail.com)
 * @version $LastChangedRevision$
 * $HeadURL$
 * $Id$
 */
public class ObjectNamePropertyEditor extends PropertyEditorSupport {

	/**
	 * 
	 */
	public ObjectNamePropertyEditor() {		
	}

	/**
	 * @param source
	 */
	public ObjectNamePropertyEditor(Object source) {
		super(source);
	}
	
    public void setAsText(final String text) {
	      setValue(text);
	}
    
    public Object getValue() {
    	try {
    		return new ObjectName(getAsText());
    	} catch (MalformedObjectNameException e) {
    		throw new RuntimeException(e);
        }
    }
    
	

}
