package com.kq.core;

import com.kq.core.enums.Scope;

public class ComponentDefinition {
    Class<?> type;
    String name;
    Scope scope;
    boolean lazy ;

    public ComponentDefinition(Class<?> type, String name, Scope scope, boolean lazy) {
        this.type = type;
        this.name = name;
        this.scope = scope;
        this.lazy = lazy;
    }

    public ComponentDefinition() {
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public static ComponentDefinitionBuilder builder() {
        return new ComponentDefinitionBuilder();
    }

    public static class ComponentDefinitionBuilder {
        private final ComponentDefinition INSTANCE;

        public ComponentDefinitionBuilder() {
            this.INSTANCE = new ComponentDefinition();
        }

        public ComponentDefinitionBuilder name(String name) {
            INSTANCE.setName(name);
            return this;
        }
        public ComponentDefinitionBuilder scope(Scope scope) {
            INSTANCE.setScope(scope);
            return this;
        }
        public ComponentDefinitionBuilder lazy(boolean lazy) {
            INSTANCE.setLazy(lazy);
            return this;
        }
        public ComponentDefinitionBuilder type(Class<?> type) {
            INSTANCE.setType(type);
            return this;
        }

        public ComponentDefinition build(){
            return INSTANCE;
        }

    }
}
