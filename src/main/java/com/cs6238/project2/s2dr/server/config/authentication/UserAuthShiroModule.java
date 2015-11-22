package com.cs6238.project2.s2dr.server.config.authentication;

import com.google.inject.Key;
import com.google.inject.Singleton;
import org.apache.shiro.guice.web.ShiroWebModule;

import javax.servlet.ServletContext;

public class UserAuthShiroModule extends ShiroWebModule {

    public UserAuthShiroModule(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected void configureShiroWeb() {
        bindRealm().to(UserAuthRealm.class);

        // allow anonymous users to reach the login endpoint without authenticating
        addFilterChain("/s2dr/login", ANON);

        // filter all requests through the authorization filter
        addFilterChain("/**", Key.get(UserAuthFilter.class));
    }
}
