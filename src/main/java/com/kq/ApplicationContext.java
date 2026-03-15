package com.kq;

import javassist.bytecode.DuplicateMemberException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class ApplicationContext {
    private final Map<String, Object> singletonComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> prototypalComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> lazyComponents = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> singletonDefinitions = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Map<String, Class<?>>>> circularSolver = new ConcurrentHashMap<>();

    public Map<String, Class<?>> getSingletonDefinitions() {
        return singletonDefinitions;
    }

    public Map<String, Object> getSingletonComponents() {
        return singletonComponents;
    }

    public Map<String, Class<?>> getPrototypalComponents() {
        return prototypalComponents;
    }

    public Map<String, Class<?>> getLazyComponents() {
        return lazyComponents;
    }

    public Object getComponent(String name) throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (lazyComponents.containsKey(name)) {
            Object lazySingleton = addComponent(lazyComponents.get(name), name);
            singletonComponents.put(name, lazySingleton);
            return lazySingleton;
        }
        if (singletonComponents.containsKey(name)) return singletonComponents.get(name);
        else if (prototypalComponents.containsKey(name)) return addComponent(prototypalComponents.get(name).getClass(),name);
        throw new IllegalArgumentException("[Component with %s] not part of the context.");
    }

    public Object addComponent(Class<?> clazz, String name) throws InvocationTargetException, InstantiationException, IllegalAccessException, DuplicateMemberException {
        var constructor = getConstructor(clazz);
        var paramTypes = constructor.getParameterTypes();
        var len = paramTypes.length;
        var args = new Object[len];
        for (int i = 0; i < len; i++) {
            var paramType = paramTypes[i];
            Optional<Object> singletonMatch = singletonComponents.values().stream().filter((s) -> paramType.isAssignableFrom(s.getClass())).findFirst();
            Optional<Class<?>> prototypeMatch = prototypalComponents.values().stream().filter(paramType::isAssignableFrom).findFirst();

            if (singletonMatch.isPresent())
                args[i] = singletonMatch.get();
            else if(getSingletonComponents().containsValue(paramType)){
                if(circularSolver.containsKey(paramType))
                    circularSolver.get(paramType).add(Map.of(name, paramType));
                else
                    circularSolver.put(paramType, List.of(Map.of(name, paramType)));
                return null;
            }
            else if (prototypeMatch.isPresent())
                args[i] = addComponent(prototypeMatch.get(), null);

            else throw new IllegalArgumentException("[Component with %s] not registered".formatted(paramType));
        }


        Object newInstance = constructor.newInstance(args);

        if (circularSolver.containsKey(clazz)){
            for (var dep: circularSolver.get(clazz)){
                singletonComponents.put(dep.keySet().stream().findFirst().orElse(null), dep.values().stream().findFirst());
            }
        }
        return newInstance;
    }

    public String getComponents() {
        return "\n\n[Singleton] %s\n[Prototypal] %s\n\n".formatted(singletonComponents.values(), prototypalComponents.values());
    }

    private Constructor<?> getConstructor(Class<?> clazz) throws DuplicateMemberException {
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length > 1)
            throw new DuplicateMemberException("[Component with %s] has multiple constructors.".formatted(clazz));

        return clazz.getDeclaredConstructors()[0];
    }


}
