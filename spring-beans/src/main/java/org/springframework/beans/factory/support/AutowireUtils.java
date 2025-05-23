// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 工具类，包含各种对实现支持自动装配的bean工厂有用的方法。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 1.1.2
 * @see AbstractAutowireCapableBeanFactory
 */
abstract class AutowireUtils {

    public static final Comparator<Executable> EXECUTABLE_COMPARATOR = (e1, e2) -> {
        int result = Boolean.compare(Modifier.isPublic(e2.getModifiers()), Modifier.isPublic(e1.getModifiers()));
        return result != 0 ? result : Integer.compare(e2.getParameterCount(), e1.getParameterCount());
    };

    /**
     * 对给定的构造函数进行排序，优先选择公共构造函数和具有最大参数数量的“贪婪”构造函数。结果将首先包含公共构造函数，然后是参数数量递减的构造函数，接着是非公共构造函数，同样也是参数数量递减的。
     * @param constructors 要排序的构造函数数组
     */
    public static void sortConstructors(Constructor<?>[] constructors) {
        Arrays.sort(constructors, EXECUTABLE_COMPARATOR);
    }

    /**
     * 对给定的工厂方法进行排序，优先选择公共方法和参数数量最多的“贪婪”方法。结果将首先包含公共方法，然后是参数数量逐渐减少的方法，接着是非公共方法，同样也是参数数量逐渐减少的方法。
     * @param factoryMethods 要排序的工厂方法数组
     */
    public static void sortFactoryMethods(Method[] factoryMethods) {
        Arrays.sort(factoryMethods, EXECUTABLE_COMPARATOR);
    }

    /**
     * 确定给定的bean属性是否被排除在依赖检查之外。
     * <p>此实现排除由CGLIB定义的属性。
     * @param pd bean属性的属性描述符
     * @return 是否排除该bean属性
     */
    public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
        Method wm = pd.getWriteMethod();
        if (wm == null) {
            return false;
        }
        if (!wm.getDeclaringClass().getName().contains("$$")) {
            // 这不是CGLIB方法，所以没问题。
            return false;
        }
        // 这是由CGLIB声明的，但我们仍然可能希望自动装配它
        // 如果它实际上是由超类声明的。
        Class<?> superclass = wm.getDeclaringClass().getSuperclass();
        return !ClassUtils.hasMethod(superclass, wm);
    }

    /**
     * 返回给定 bean 属性的 setter 方法是否定义在任何给定的接口中。
     * @param pd bean 属性的 PropertyDescriptor
     * @param interfaces 接口（Class 对象）的集合
     * @return setter 方法是否由接口定义
     */
    public static boolean isSetterDefinedInInterface(PropertyDescriptor pd, Set<Class<?>> interfaces) {
        Method setter = pd.getWriteMethod();
        if (setter != null) {
            Class<?> targetClass = setter.getDeclaringClass();
            for (Class<?> ifc : interfaces) {
                if (ifc.isAssignableFrom(targetClass) && ClassUtils.hasMethod(ifc, setter)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解析给定的自动装配值与给定的所需类型，例如将一个 {@link ObjectFactory} 值解析为其实际的对象结果。
     * @param autowiringValue 要解析的值
     * @param requiredType 将结果分配到的类型
     * @return 解析后的值
     */
    public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
        if (autowiringValue instanceof ObjectFactory<?> factory && !requiredType.isInstance(autowiringValue)) {
            if (autowiringValue instanceof Serializable && requiredType.isInterface()) {
                autowiringValue = Proxy.newProxyInstance(requiredType.getClassLoader(), new Class<?>[] { requiredType }, new ObjectFactoryDelegatingInvocationHandler(factory));
            } else {
                return factory.getObject();
            }
        }
        return autowiringValue;
    }

    /**
     * 确定给定泛型工厂方法的泛型返回类型的目标类型，其中形式类型变量在给定方法本身上声明。
     * 例如，给定以下签名的工厂方法，如果使用反射的 `createProxy()` 方法和包含 `MyService.class` 的 `Object[]` 数组调用 `resolveReturnTypeForFactoryMethod()`，则 `resolveReturnTypeForFactoryMethod()` 将推断目标返回类型为 `MyService`。
     * <pre class="code">{@code public static <T> T createProxy(Class<T> clazz)}</pre>
     * <h4>可能的返回值</h4>
     * <ul>
     * <li>如果可以推断，则为目标返回类型</li>
     * <li>如果给定 `method` 没有声明任何 `Method#getTypeParameters()` 形式类型变量，则为 `Method#getReturnType()` 标准返回类型</li>
     * <li>如果目标返回类型无法推断（例如，由于类型擦除），则为 `Method#getReturnType()` 标准返回类型</li>
     * <li>如果给定参数数组的长度小于给定方法的 `Method#getGenericParameterTypes()` 形式参数列表的长度，则为 `null`</li>
     * </ul>
     * @param method 要进行反射的方法（从不为 `null`）
     * @param args 在方法调用时将提供给方法的参数（从不为 `null`）
     * @param classLoader 如果需要，则用于解析类名的 ClassLoader（从不为 `null`）
     * @return 解析后的目标返回类型或标准方法返回类型
     * @since 3.2.5
     */
    public static Class<?> resolveReturnTypeForFactoryMethod(Method method, Object[] args, @Nullable ClassLoader classLoader) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(args, "Argument array must not be null");
        TypeVariable<Method>[] declaredTypeVariables = method.getTypeParameters();
        Type genericReturnType = method.getGenericReturnType();
        Type[] methodParameterTypes = method.getGenericParameterTypes();
        Assert.isTrue(args.length == methodParameterTypes.length, "Argument array does not match parameter count");
        // 确保在方法上直接声明类型变量（例如，T）
        // 它自身（例如，通过 <T>），而不是在封装类或接口上。
        boolean locallyDeclaredTypeVariableMatchesReturnType = false;
        for (TypeVariable<Method> currentTypeVariable : declaredTypeVariables) {
            if (currentTypeVariable.equals(genericReturnType)) {
                locallyDeclaredTypeVariableMatchesReturnType = true;
                break;
            }
        }
        if (locallyDeclaredTypeVariableMatchesReturnType) {
            for (int i = 0; i < methodParameterTypes.length; i++) {
                Type methodParameterType = methodParameterTypes[i];
                Object arg = args[i];
                if (methodParameterType.equals(genericReturnType)) {
                    if (arg instanceof TypedStringValue typedValue) {
                        if (typedValue.hasTargetType()) {
                            return typedValue.getTargetType();
                        }
                        try {
                            Class<?> resolvedType = typedValue.resolveTargetType(classLoader);
                            if (resolvedType != null) {
                                return resolvedType;
                            }
                        } catch (ClassNotFoundException ex) {
                            throw new IllegalStateException("Failed to resolve value type [" + typedValue.getTargetTypeName() + "] for factory method argument", ex);
                        }
                    } else if (arg != null && !(arg instanceof BeanMetadataElement)) {
                        // 仅当参数类型是简单值时才考虑它...
                        return arg.getClass();
                    }
                    return method.getReturnType();
                } else if (methodParameterType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    for (Type typeArg : actualTypeArguments) {
                        if (typeArg.equals(genericReturnType)) {
                            if (arg instanceof Class<?> clazz) {
                                return clazz;
                            } else {
                                String className = null;
                                if (arg instanceof String name) {
                                    className = name;
                                } else if (arg instanceof TypedStringValue typedValue) {
                                    String targetTypeName = typedValue.getTargetTypeName();
                                    if (targetTypeName == null || Class.class.getName().equals(targetTypeName)) {
                                        className = typedValue.getValue();
                                    }
                                }
                                if (className != null) {
                                    try {
                                        return ClassUtils.forName(className, classLoader);
                                    } catch (ClassNotFoundException ex) {
                                        throw new IllegalStateException("Could not resolve class name [" + arg + "] for factory method argument", ex);
                                    }
                                }
                                // 考虑添加逻辑以确定 typeArg 的类型，如果可能的话。
                                // 目前，只是回退...
                                return method.getReturnType();
                            }
                        }
                    }
                }
            }
        }
        // 回退...
        return method.getReturnType();
    }

    /**
     * 用于懒加载当前目标对象的反射性 {@link InvocationHandler}。
     */
    @SuppressWarnings("serial")
    private static class ObjectFactoryDelegatingInvocationHandler implements InvocationHandler, Serializable {

        private final ObjectFactory<?> objectFactory;

        ObjectFactoryDelegatingInvocationHandler(ObjectFactory<?> objectFactory) {
            this.objectFactory = objectFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch(method.getName()) {
                // 仅当代理对象完全相同时才考虑相等。
                case "equals" ->
                    (proxy == args[0]);
                // 使用代理的 hashCode。
                case "hashCode" ->
                    System.identityHashCode(proxy);
                case "toString" ->
                    this.objectFactory.toString();
                default ->
                    {
                        try {
                            yield method.invoke(this.objectFactory.getObject(), args);
                        } catch (InvocationTargetException ex) {
                            throw ex.getTargetException();
                        }
                    }
            };
        }
    }
}
