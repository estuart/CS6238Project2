package com.cs6238.project2.s2dr.server.config.authentication;

import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.User;
import com.cs6238.project2.s2dr.server.config.GuiceServletConfig;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
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
        return token instanceof X509Token;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        X509Token x509Token = (X509Token) token;
        LOG.info("Checking for authentication info using token {}", token);

        User user;
        try {
            user = AuthenticationDao.getUser(x509Token);

        } catch (SQLException | NoQueryResultsException | TooManyQueryResultsException e) {
            throw new AuthenticationException(
                    "Unable to authenticate the user based on the given username/password combo", e);
        }

        LOG.info("Successfully authenticated {}", user);

        CurrentUser currentUser = GuiceServletConfig.injector.getInstance(CurrentUser.class);
        currentUser.setCurrentUser(user);

        return new SimpleAuthenticationInfo(
                user, x509Token.getCredentials(), this.getName());
    }
}
