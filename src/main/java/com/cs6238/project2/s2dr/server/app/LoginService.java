package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.config.authentication.X509Token;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;

public class LoginService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

    private final LoginDao loginDao;

    @Inject
    public LoginService(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    public void login(X509Certificate certificate) throws SQLException {
        byte[] certificateSignature = certificate.getSignature();

        X500Principal principal = certificate.getSubjectX500Principal();

        Subject currentUser = SecurityUtils.getSubject();

        X509Token token = new X509Token(principal, certificateSignature);
        try {
            currentUser.login(token);
        } catch (AuthenticationException e) {
            LOG.info("Unable to authenticate based on credentials {}.", token);

            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
            addNewUser(token, new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getPublicExponent()));

            LOG.info("Added credentials {} as a new user. Will re-attempt login", token);
        }

        currentUser.login(token);
    }

    public void addNewUser(X509Token token, RSAPublicKeySpec publicKeySpec) throws SQLException {
        loginDao.addNewUser(token, publicKeySpec);
    }
}
