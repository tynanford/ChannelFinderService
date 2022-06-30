/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Purpose of class to intercept rest calls and handle logging in uniformed manner.
 *
 * @author <a href="mailto:zoltan.runyo@ess.eu">Zoltan Runyo</a>
 * @author Lars Johansson
 */
@Component
public class RestLogFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RestLogFilter.class.getName());

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class LogEntry {
        // fields to be public for logging to work
        // com.fasterxml.jackson.databind.ObjectMapper#writeValueAsString

        /**
         * Method for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String method;
        /**
         * Path for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String path;
        /**
         * Path info for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String pathInfo;
        /**
         * Query string for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String queryString;
        /**
         * Remote address for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String remoteAddress;
        /**
         * Status code for rest call.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public int statusCode;
        /**
         * Time to complete rest call, in milliseconds.
         */
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public long time;

        /**
         * Store method for rest call in log data.
         *
         * @param method method for rest call
         */
        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * Store path for rest call in log data.
         *
         * @param path path for rest call
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * Store path info for rest call in log data.
         *
         * @param pathInfo path info for rest call
         */
        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }

        /**
         * Store query string for rest call in log data.
         *
         * @param queryString query string for rest call
         */
        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        /**
         * Store remote address for rest call in log data.
         *
         * @param remoteAddress remote address for rest call
         */
        public void setRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        /**
         * Store status code for rest call in log data.
         *
         * @param statusCode status code for rest call
         */
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        /**
         * Store time to complete rest call in log data.
         *
         * @param time time to complete rest call, in milliseconds
         */
        public void setTime(long time) {
            this.time = time;
        }
    }

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = null;
        if (request instanceof HttpServletRequest) {
            req = (HttpServletRequest)request;
        }

        LogEntry logLine = new LogEntry();
        boolean restRequest = false;

        if(req != null) {
            restRequest = isRestRequest(req.getServletPath());

            logLine.setMethod(req.getMethod());
            logLine.setPath(req.getServletPath());
            logLine.setPathInfo(req.getPathInfo());
            logLine.setQueryString(req.getQueryString());
            logLine.setRemoteAddress(RequestUtilities.getIP(req));
        }

        try {
            long startTime = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            long endTime = System.currentTimeMillis();
            logLine.setTime(endTime - startTime);

            HttpServletResponse resp = null;
            if (response instanceof HttpServletResponse) {
                resp = (HttpServletResponse) response;
            }

            if (resp != null) {
                logLine.setStatusCode(resp.getStatus());
            }
        } finally {
            if (restRequest) {
                LOGGER.log(Level.INFO, mapper.writeValueAsString(logLine));
            }
        }
    }

    @Override
    public void destroy() { }

    private boolean isRestRequest(String pathInfo) {
        return StringUtils.isNotEmpty(pathInfo)
                && !StringUtils.endsWithIgnoreCase(pathInfo,".ico")
                && !StringUtils.startsWithIgnoreCase(pathInfo,"/swagger")
                && !StringUtils.endsWithIgnoreCase(pathInfo,".png")
                && !StringUtils.equals(pathInfo.trim(), "/");
    }

}

