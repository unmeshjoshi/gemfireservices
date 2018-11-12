package com.gemfire.authorization;

import org.apache.geode.security.ResourcePermission;

public class CustomResourcePermission extends ResourcePermission {
    private String resource = "functionexecutionScopedByVisibility";
    @Override public Resource getResource() {

        return Resource.DATA;


    }

    @Override
    public Operation getOperation() {
        return Operation.READ;
    }

    @Override
    public String toString() {
        return "mycustompermission";
    }
}
