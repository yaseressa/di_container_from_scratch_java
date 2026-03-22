package com.kq.core;

import com.kq.core.exceptions.CircularDependencyException;
import com.kq.core.exceptions.DIException;
import com.kq.core.exceptions.MissingComponentException;
import com.kq.core.exceptions.MultipleConstructorsException;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.kq.core.enums.Scope.PROTOTYPE;
import static com.kq.core.enums.Scope.SINGLETON;

public class ApplicationContext {
    private final Map<String, Object> singletonComponents = new ConcurrentHashMap<>();
    private List<ComponentDefinition> definitions = new CopyOnWriteArrayList<>();


    public void setDefinitions(List<ComponentDefinition> definitions) {
        this.definitions = definitions;
    }

    public void populateContext() throws Exception {
            for (var definition : definitions) {
                if (definition.getScope() == SINGLETON) {
                    if (definition.isLazy()) continue;
                    Object singleton = createSingleton(definition.getType(), definition.getName(), new ArrayDeque<>());
                    singletonComponents.putIfAbsent(definition.getName(), singleton);
                }

            }

    }

    public Object getComponent(String name)
            throws Exception {

        if (singletonComponents.containsKey(name)) {
            return singletonComponents.get(name);
        }

        var definition = definitions.stream().filter(d -> d.getName().equals(name)).findFirst().orElse(null);
        if (Objects.nonNull(definition)) {
            if (definition.isLazy() && definition.getScope() == SINGLETON) {
                Object lazySingleton = createSingleton(definition.getType(), name, new ArrayDeque<>());
                singletonComponents.putIfAbsent(definition.getName(), lazySingleton);
                return lazySingleton;
            }
            if (definition.getScope() == PROTOTYPE)
                return createPrototype(definition.getType(), name, new ArrayDeque<>());
        }
        throw new MissingComponentException("[Component %s] not part of the context.".formatted(name));
    }


    public String getMembers() {
        return "\n\nContext %s\n\n".formatted(
                definitions.stream().map(ComponentDefinition::getName).collect(Collectors.toList()));
    }

    private Object createSingleton(Class<?> clazz, String name, Deque<String> path)
            throws Exception {
        if (singletonComponents.containsKey(name)) {
            return singletonComponents.get(name);
        }

        checkCircularDependency(name, path);
        path.addLast(name);

        try {
            Constructor<?> constructor = getConstructor(clazz);
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = resolveDependency(paramTypes[i], path);
            }

            Object newInstance = constructor.newInstance(args);
            singletonComponents.putIfAbsent(name, newInstance);
            return newInstance;
        } finally {
            path.removeLast();
        }
    }

    private Object createPrototype(Class<?> clazz, String name, Deque<String> path)
            throws Exception {
        checkCircularDependency(name, path);
        path.addLast(name);

        try {
            Constructor<?> constructor = getConstructor(clazz);
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = resolveDependency(paramTypes[i], path);
            }

            return constructor.newInstance(args);
        } finally {
            path.removeLast();
        }
    }

    private Object resolveDependency(Class<?> paramType, Deque<String> path)
            throws Exception {
        Optional<Object> singletonMatch = singletonComponents.values()
                .stream()
                .filter(s -> paramType.isAssignableFrom(s.getClass()))
                .findFirst();

        if (singletonMatch.isPresent()) {
            return singletonMatch.get();
        }


        Optional<ComponentDefinition> componentDefinition = definitions.stream().filter(def -> paramType.isAssignableFrom(def.getType()))
                .findFirst();

        if (componentDefinition.isPresent()) {
            String dependencyName = componentDefinition.get().getName();
            Class<?> dependencyClass = componentDefinition.get().getType();
            if (componentDefinition.get().getScope() == SINGLETON) {
                Object dependency = createSingleton(dependencyClass, dependencyName, path);
                singletonComponents.putIfAbsent(dependencyName, dependency);
                return singletonComponents.get(dependencyName);
            }
            if (componentDefinition.get().getScope() == PROTOTYPE) {
                return createPrototype(dependencyClass, dependencyName, path);
            }

        }

        throw new MissingComponentException("[Component %s] not registered".formatted(paramType));
    }

    private void checkCircularDependency(String name, Deque<String> path) {
        if (path.contains(name)) {
            StringBuilder cycle = new StringBuilder();
            for (String item : path) {
                cycle.append(item).append(" -> ");
            }
            cycle.append(name);
            throw new CircularDependencyException("Circular dependency detected: " + cycle);
        }
    }

    private Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length > 1) {
            throw new MultipleConstructorsException("[Component %s] has multiple constructors.".formatted(clazz));
        }
        return declaredConstructors[0];
    }
}