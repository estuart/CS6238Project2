package com.cs6238.project2.s2dr.server.config.authentication;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class UserAuthFilter extends AuthenticatingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticatingFilter.class);

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        LOG.info("Creating Token");

        // TODO figure out where this is used (if it is).
        return null;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        LOG.info("Checking if access allowed");

        Subject currentUser = SecurityUtils.getSubject();

        boolean isAllowed = currentUser.isAuthenticated();

        if (isAllowed) {
            LOG.info("Access allowed for subject {}", currentUser);
        } else {
            LOG.info("Access not allowed for subject {}", currentUser);
        }

        return isAllowed;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        LOG.info("Access Denied");

        // return status 401
        WebUtils.toHttp(response)
                .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login to gain access.");

        return false;
    }
}
