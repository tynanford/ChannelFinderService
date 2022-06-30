/*
 * Copyright (C) 2021 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.channelfinder;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting specific information from a {@link HttpServletRequest}.
 *
 * @author Lars Johansson
 */
public class RequestUtilities {

    /** The name of header set by proxy server, alternative source of IP address */
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * This class is not to be instantiated.
     */
    private RequestUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieve ip address for (origin of) request.
     *
     * <p>
     * In descending order:
     * <ol>
     * <li>IP address of request header {@link RequestUtilities#HEADER_X_FORWARDED_FOR}
     * <li>IP address of the client or last proxy that sent the request
     * </ol>
     *
     * @param httpServletRequest request
     * @return ip address for (origin of) request
     */
    public static String getIP(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            // retrieve ip address from, in descending order
            //     1. HEADER_X_FORWARDED_FOR
            //     2. client or last proxy that sent request

            String xForwardedFor = httpServletRequest.getHeader(RequestUtilities.HEADER_X_FORWARDED_FOR);
            String remoteAddr = httpServletRequest.getRemoteAddr();

            final String ip;
            if (xForwardedFor != null) {
                ip = xForwardedFor;
            } else {
                ip = remoteAddr;
            }

            return ip;
        }
        return null;
    }

}
