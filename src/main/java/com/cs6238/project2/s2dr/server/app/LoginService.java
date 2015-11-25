package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.config.authentication.X509Token;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

public class LoginService {

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
            addNewUser(token);
        }

        currentUser.login(token);
    }

    public void addNewUser(X509Token token) throws SQLException {
        loginDao.addNewUser(token);
    }
}
