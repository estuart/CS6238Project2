package com.cs6238.project2.s2dr.server.config.authentication;

import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class UserAuthRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizingRealm.class);

    @Override
    public boolean supports(AuthenticationToken token) {
        LOG.info("Checking if AuthenticationToken is supported");
        return token instanceof UsernamePasswordToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        LOG.info("Checking for Authentication Info");

        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;

        User user;
        try {
            user = AuthenticationDao.getUser(
                    usernamePasswordToken.getUsername(), new String(usernamePasswordToken.getPassword()));

        } catch (SQLException | NoQueryResultsException | TooManyQueryResultsException e) {
            throw new AuthenticationException(
                    "Unable to authenticate the user based on the given username/password combo", e);
        }

        LOG.info("Successfully authenticated {}", user);

        return new SimpleAuthenticationInfo(
                user, usernamePasswordToken.getCredentials(), this.getName());
    }
}
