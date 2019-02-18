package com.gemfire.authorization;

import org.apache.geode.LogWriter;
import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import java.util.Properties;

public class GemfireAuthenticator implements AuthInitialize {

    public static GemfireAuthenticator create(){
        return new GemfireAuthenticator();
    }

    @Override
    public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {

    }

    @Override
    public void init() {

    }

    @Override
    public Properties getCredentials(Properties securityProps, DistributedMember server, boolean isPeer) throws AuthenticationFailedException {
        Properties prop = new Properties();
        prop.put("security-username", "user");
        prop.put("security-password", "passord");
        prop.put("security-client-auth-init", "hello.GemfireAuthenticator");
        return prop;

    }

    @Override
    public Properties getCredentials(Properties securityProps) {
        return getCredentials(null, null, false);
    }

    @Override
    public void close() {

    }

    @Override
    public void init(Properties props) {

    }
//
//    @Override
    public void initialize(Cache cache, Properties properties) {

    }
}
