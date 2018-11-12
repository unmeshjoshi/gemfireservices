package com.gemfire.authorization;

import org.apache.geode.internal.logging.LogService;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;

import java.io.Serializable;
import java.util.Properties;

public class OnlyFunctionCallsSecurityManager implements SecurityManager, Serializable {
    @Override
    public void init(Properties securityProps) {
        LogService.getLogger().info("Init ");

    }

    @Override
    public Object authenticate(Properties credentials) throws AuthenticationFailedException {
        LogService.getLogger().info("Authenticate ");
        return new User("test", "test");
    }

    @Override
    public boolean authorize(Object principal, ResourcePermission permission) {
        LogService.getLogger().info("Authorize " + principal + permission);
        return true;
    }

    @Override
    public void close() {
        System.out.println("close");

    }

    public static class User implements Serializable {
        String password;
        String name;

        public User(String password, String name) {
            this.password = password;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }
    }

}


