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
package org.helios.ot.agent.impl.netty.handler;

import org.helios.ot.agent.protocol.impl.HeliosProtocolResponse;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * <p>Title: InvocationResponseListener</p>
 * <p>Description: Defines a listener that listens on invocation responses inbound from the server</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.ot.agent.impl.netty.handler.InvocationResponseListener</code></p>
 */

public interface InvocationResponseListener extends ProtocolListener {
	/**
	 * Callback from the asynch response handler when a response is received
	 * @param response The asynch response
	 * @param ctx The channel handler context (useful if we need to zip something back to the server etc.)
	 * @param message The original message event (useful if we need info from the event itself.
	 */
	public void onInvocationResponse(HeliosProtocolResponse response, ChannelHandlerContext ctx, MessageEvent message);
}
