package com.kq;

import javassist.bytecode.DuplicateMemberException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {
    private final Map<String, Object> singletonComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> prototypalComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> lazyComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> singletonDefinitions = new ConcurrentHashMap<>();

    public Map<String, Class<?>> getSingletonDefinitions() {
        return singletonDefinitions;
    }

    public Map<String, Object> getSingletonComponents() {
        return singletonComponents;
    }

    public Map<String, Class<?>> getLazyComponents() {
        return lazyComponents;
    }

    public Object getComponent(String name)
            throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!singletonComponents.containsKey(name) && lazyComponents.containsKey(name)) {
            addSingleton(lazyComponents.get(name), name);
        }

        if (singletonComponents.containsKey(name)) {
            return singletonComponents.get(name);
        }

        if (prototypalComponents.containsKey(name)) {
            return createPrototype(prototypalComponents.get(name), name, new ArrayDeque<>());
        }

        throw new IllegalArgumentException("[Component with %s] not part of the context.".formatted(name));
    }

    public void addSingleton(Class<?> clazz, String name)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, DuplicateMemberException {
        if (clazz == null) {
            throw new IllegalArgumentException("[Component with %s] not registered".formatted(name));
        }

        if (singletonComponents.containsKey(name)) {
            return;
        }

        Object instance = createSingleton(clazz, name, new ArrayDeque<>());
        singletonComponents.putIfAbsent(name, instance);
    }

    public void registerPrototype(Class<?> clazz, String name) {
        prototypalComponents.put(name, clazz);
    }

    public String getComponents() {
        return "\n\n[Singleton] %s\n[Prototypal] %s\n\n".formatted(singletonComponents.values(), prototypalComponents.values());
    }

    private Object createSingleton(Class<?> clazz, String name, Deque<String> path)
            throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

            return constructor.newInstance(args);
        } finally {
            path.removeLast();
        }
    }

    private Object createPrototype(Class<?> clazz, String name, Deque<String> path)
            throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
            throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Optional<Object> singletonMatch = singletonComponents.values()
                .stream()
                .filter(s -> paramType.isAssignableFrom(s.getClass()))
                .findFirst();

        if (singletonMatch.isPresent()) {
            return singletonMatch.get();
        }

        Optional<Map.Entry<String, Class<?>>> singletonDefinitionMatch = singletonDefinitions.entrySet()
                .stream()
                .filter(entry -> paramType.isAssignableFrom(entry.getValue()))
                .findFirst();

        if (singletonDefinitionMatch.isPresent()) {
            String dependencyName = singletonDefinitionMatch.get().getKey();
            Class<?> dependencyClass = singletonDefinitionMatch.get().getValue();
            Object dependency = createSingleton(dependencyClass, dependencyName, path);
            singletonComponents.putIfAbsent(dependencyName, dependency);
            return singletonComponents.get(dependencyName);
        }

        Optional<Map.Entry<String, Class<?>>> prototypeMatch = prototypalComponents.entrySet()
                .stream()
                .filter(entry -> paramType.isAssignableFrom(entry.getValue()))
                .findFirst();

        if (prototypeMatch.isPresent()) {
            return createPrototype(prototypeMatch.get().getValue(), prototypeMatch.get().getKey(), path);
        }

        Optional<Map.Entry<String, Class<?>>> lazyMatch = lazyComponents.entrySet()
                .stream()
                .filter(entry -> paramType.isAssignableFrom(entry.getValue()))
                .findFirst();

        if (lazyMatch.isPresent()) {
            String dependencyName = lazyMatch.get().getKey();
            Class<?> dependencyClass = lazyMatch.get().getValue();
            Object dependency = createSingleton(dependencyClass, dependencyName, path);
            singletonComponents.putIfAbsent(dependencyName, dependency);
            return singletonComponents.get(dependencyName);
        }

        throw new IllegalArgumentException("[Component with %s] not registered".formatted(paramType));
    }

    private void checkCircularDependency(String name, Deque<String> path) {
        if (path.contains(name)) {
            StringBuilder cycle = new StringBuilder();
            for (String item : path) {
                cycle.append(item).append(" -> ");
            }
            cycle.append(name);
            throw new IllegalStateException("Circular dependency detected: " + cycle);
        }
    }

    private Constructor<?> getConstructor(Class<?> clazz) throws DuplicateMemberException {
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length > 1) {
            throw new DuplicateMemberException("[Component with %s] has multiple constructors.".formatted(clazz));
        }
        return declaredConstructors[0];
    }
}