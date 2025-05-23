// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据Apache许可证2.0版（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValueResolver;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingBiFunction;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.util.function.ThrowingSupplier;

/**
 *  专门化的 {@link InstanceSupplier}，提供用于实例化底层 bean 实例的工厂 {@link Method}，如果有的话。如果需要，透明地处理 {@link AutowiredArguments} 的解析。通常用于 AOT-处理的应用程序中，作为基于反射注入的有针对性的替代方案。
 *
 * <p>如果没有提供任何 {@code generator}，则使用反射来实例化 bean 实例，并贡献完整的 {@link ExecutableMode#INVOKE 调用} 提示。支持多种生成器回调样式：
 * <ul>
 *  <li>一个具有注册的 bean 和解析的 {@code arguments} 的函数，用于需要解析参数的执行程序。添加了一个 {@link ExecutableMode#INTROSPECT 内省} 提示，以便可以读取参数注解</li>
 *  <li>一个仅具有注册的 bean 的函数，用于更简单的无需解析参数的情况</li>
 *  <li>当可以使用方法引用时，使用一个 supplier</li>
 * </ul>
 * 生成器回调处理检查异常，以便调用者无需处理它们。
 *
 *  @author Phillip Webb
 *  @author Stephane Nicoll
 *  @since 6.0
 *  @param <T> 由此供应商提供的实例类型
 *  @see AutowiredArguments
 */
public final class BeanInstanceSupplier<T> extends AutowiredElementResolver implements InstanceSupplier<T> {

    private final ExecutableLookup lookup;

    @Nullable
    private final ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generator;

    @Nullable
    private final String[] shortcuts;

    private BeanInstanceSupplier(ExecutableLookup lookup, @Nullable ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generator, @Nullable String[] shortcuts) {
        this.lookup = lookup;
        this.generator = generator;
        this.shortcuts = shortcuts;
    }

    /**
     * 创建一个 {@link BeanInstanceSupplier}，用于解析指定bean构造函数的参数。
     * @param <T> 提供的实例的类型
     * @param parameterTypes 构造函数的参数类型
     * @return 一个新的 {@link BeanInstanceSupplier} 实例
     */
    public static <T> BeanInstanceSupplier<T> forConstructor(Class<?>... parameterTypes) {
        Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
        Assert.noNullElements(parameterTypes, "'parameterTypes' must not contain null elements");
        return new BeanInstanceSupplier<>(new ConstructorLookup(parameterTypes), null, null);
    }

    /**
     * 创建一个新的 {@link BeanInstanceSupplier}，该实例用于解析指定工厂方法的参数。
     * @param <T> 提供的实例的类型
     * @param declaringClass 声明工厂方法的类
     * @param methodName 工厂方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 一个新的 {@link BeanInstanceSupplier} 实例
     */
    public static <T> BeanInstanceSupplier<T> forFactoryMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        Assert.notNull(declaringClass, "'declaringClass' must not be null");
        Assert.hasText(methodName, "'methodName' must not be empty");
        Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
        Assert.noNullElements(parameterTypes, "'parameterTypes' must not contain null elements");
        return new BeanInstanceSupplier<>(new FactoryMethodLookup(declaringClass, methodName, parameterTypes), null, null);
    }

    ExecutableLookup getLookup() {
        return this.lookup;
    }

    /**
     * 返回一个新的 {@link BeanInstanceSupplier} 实例，该实例使用指定的
     * {@code generator} 双参数函数来实例化底层的 bean。
     * @param generator 一个使用 {@link RegisteredBean} 和解析后的 {@link AutowiredArguments}
     * 来实例化底层 bean 的 {@link ThrowingBiFunction}
     * @return 一个具有指定生成器的新的 {@link BeanInstanceSupplier} 实例
     */
    public BeanInstanceSupplier<T> withGenerator(ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generator) {
        Assert.notNull(generator, "'generator' must not be null");
        return new BeanInstanceSupplier<>(this.lookup, generator, this.shortcuts);
    }

    /**
     * 返回一个新的 {@link BeanInstanceSupplier} 实例，该实例使用指定的
     * {@code generator} 函数来实例化底层的 bean。
     * @param generator 一个使用 {@link RegisteredBean} 实例化底层 bean 的
     * {@link ThrowingFunction}
     * @return 一个具有指定生成器的新的 {@link BeanInstanceSupplier} 实例
     */
    public BeanInstanceSupplier<T> withGenerator(ThrowingFunction<RegisteredBean, T> generator) {
        Assert.notNull(generator, "'generator' must not be null");
        return new BeanInstanceSupplier<>(this.lookup, (registeredBean, args) -> generator.apply(registeredBean), this.shortcuts);
    }

    /**
     * 返回一个新的 {@link BeanInstanceSupplier} 实例，该实例使用指定的
     * {@code generator} 供应商来实例化底层的 bean。
     * @param generator 用于实例化底层 bean 的一个 {@link ThrowingSupplier}
     * @return 一个带有指定生成器的新的 {@link BeanInstanceSupplier} 实例
     * @deprecated 替代方案为 {@link #withGenerator(ThrowingFunction)}
     */
    @Deprecated(since = "6.0.11", forRemoval = true)
    public BeanInstanceSupplier<T> withGenerator(ThrowingSupplier<T> generator) {
        Assert.notNull(generator, "'generator' must not be null");
        return new BeanInstanceSupplier<>(this.lookup, (registeredBean, args) -> generator.get(), this.shortcuts);
    }

    /**
     * 返回一个新的 {@link BeanInstanceSupplier} 实例
     * 该实例使用直接 bean 名称注入快捷方式来指定特定参数。
     * @param beanNames 要用作快捷方式的 bean 名称（与构造函数或工厂方法参数对齐）
     * @return 一个新的、使用快捷方式的 {@link BeanInstanceSupplier} 实例
     */
    public BeanInstanceSupplier<T> withShortcuts(String... beanNames) {
        return new BeanInstanceSupplier<>(this.lookup, this.generator, beanNames);
    }

    @Override
    public T get(RegisteredBean registeredBean) throws Exception {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        Executable executable = this.lookup.get(registeredBean);
        AutowiredArguments arguments = resolveArguments(registeredBean, executable);
        if (this.generator != null) {
            return invokeBeanSupplier(executable, () -> this.generator.apply(registeredBean, arguments));
        }
        return invokeBeanSupplier(executable, () -> instantiate(registeredBean.getBeanFactory(), executable, arguments.toArray()));
    }

    private T invokeBeanSupplier(Executable executable, ThrowingSupplier<T> beanSupplier) {
        if (!(executable instanceof Method method)) {
            return beanSupplier.get();
        }
        Method priorInvokedFactoryMethod = SimpleInstantiationStrategy.getCurrentlyInvokedFactoryMethod();
        try {
            SimpleInstantiationStrategy.setCurrentlyInvokedFactoryMethod(method);
            return beanSupplier.get();
        } finally {
            SimpleInstantiationStrategy.setCurrentlyInvokedFactoryMethod(priorInvokedFactoryMethod);
        }
    }

    @Nullable
    @Override
    public Method getFactoryMethod() {
        if (this.lookup instanceof FactoryMethodLookup factoryMethodLookup) {
            return factoryMethodLookup.get();
        }
        return null;
    }

    /**
     * 解决指定已注册bean的参数。
     * @param registeredBean 已注册的bean
     * @return 解析后的构造函数或工厂方法参数
     */
    AutowiredArguments resolveArguments(RegisteredBean registeredBean) {
        Assert.notNull(registeredBean, "'registeredBean' must not be null");
        return resolveArguments(registeredBean, this.lookup.get(registeredBean));
    }

    private AutowiredArguments resolveArguments(RegisteredBean registeredBean, Executable executable) {
        Assert.isInstanceOf(AbstractAutowireCapableBeanFactory.class, registeredBean.getBeanFactory());
        int startIndex = (executable instanceof Constructor<?> constructor && ClassUtils.isInnerClass(constructor.getDeclaringClass())) ? 1 : 0;
        int parameterCount = executable.getParameterCount();
        Object[] resolved = new Object[parameterCount - startIndex];
        Assert.isTrue(this.shortcuts == null || this.shortcuts.length == resolved.length, () -> "'shortcuts' must contain " + resolved.length + " elements");
        ConstructorArgumentValues argumentValues = resolveArgumentValues(registeredBean);
        Set<String> autowiredBeanNames = new LinkedHashSet<>(resolved.length * 2);
        for (int i = startIndex; i < parameterCount; i++) {
            MethodParameter parameter = getMethodParameter(executable, i);
            DependencyDescriptor descriptor = new DependencyDescriptor(parameter, true);
            String shortcut = (this.shortcuts != null ? this.shortcuts[i - startIndex] : null);
            if (shortcut != null) {
                descriptor = new ShortcutDependencyDescriptor(descriptor, shortcut);
            }
            ValueHolder argumentValue = argumentValues.getIndexedArgumentValue(i, null);
            resolved[i - startIndex] = resolveArgument(registeredBean, descriptor, argumentValue, autowiredBeanNames);
        }
        registerDependentBeans(registeredBean.getBeanFactory(), registeredBean.getBeanName(), autowiredBeanNames);
        return AutowiredArguments.of(resolved);
    }

    private MethodParameter getMethodParameter(Executable executable, int index) {
        if (executable instanceof Constructor<?> constructor) {
            return new MethodParameter(constructor, index);
        }
        if (executable instanceof Method method) {
            return new MethodParameter(method, index);
        }
        throw new IllegalStateException("Unsupported executable: " + executable.getClass().getName());
    }

    private ConstructorArgumentValues resolveArgumentValues(RegisteredBean registeredBean) {
        ConstructorArgumentValues resolved = new ConstructorArgumentValues();
        RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
        if (beanDefinition.hasConstructorArgumentValues() && registeredBean.getBeanFactory() instanceof AbstractAutowireCapableBeanFactory beanFactory) {
            BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(beanFactory, registeredBean.getBeanName(), beanDefinition, beanFactory.getTypeConverter());
            ConstructorArgumentValues values = beanDefinition.getConstructorArgumentValues();
            values.getIndexedArgumentValues().forEach((index, valueHolder) -> {
                ValueHolder resolvedValue = resolveArgumentValue(valueResolver, valueHolder);
                resolved.addIndexedArgumentValue(index, resolvedValue);
            });
        }
        return resolved;
    }

    private ValueHolder resolveArgumentValue(BeanDefinitionValueResolver resolver, ValueHolder valueHolder) {
        if (valueHolder.isConverted()) {
            return valueHolder;
        }
        Object value = resolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
        ValueHolder resolvedHolder = new ValueHolder(value, valueHolder.getType(), valueHolder.getName());
        resolvedHolder.setSource(valueHolder);
        return resolvedHolder;
    }

    @Nullable
    private Object resolveArgument(RegisteredBean registeredBean, DependencyDescriptor descriptor, @Nullable ValueHolder argumentValue, Set<String> autowiredBeanNames) {
        TypeConverter typeConverter = registeredBean.getBeanFactory().getTypeConverter();
        if (argumentValue != null) {
            return (argumentValue.isConverted() ? argumentValue.getConvertedValue() : typeConverter.convertIfNecessary(argumentValue.getValue(), descriptor.getDependencyType(), descriptor.getMethodParameter()));
        }
        try {
            return registeredBean.resolveAutowiredArgument(descriptor, typeConverter, autowiredBeanNames);
        } catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(null, registeredBean.getBeanName(), descriptor, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private T instantiate(ConfigurableBeanFactory beanFactory, Executable executable, Object[] args) {
        if (executable instanceof Constructor<?> constructor) {
            try {
                return (T) instantiate(constructor, args);
            } catch (Exception ex) {
                throw new BeanInstantiationException(constructor, ex.getMessage(), ex);
            }
        }
        if (executable instanceof Method method) {
            try {
                return (T) instantiate(beanFactory, method, args);
            } catch (Exception ex) {
                throw new BeanInstantiationException(method, ex.getMessage(), ex);
            }
        }
        throw new IllegalStateException("Unsupported executable " + executable.getClass().getName());
    }

    private Object instantiate(Constructor<?> constructor, Object[] args) throws Exception {
        Class<?> declaringClass = constructor.getDeclaringClass();
        if (ClassUtils.isInnerClass(declaringClass)) {
            Object enclosingInstance = createInstance(declaringClass.getEnclosingClass());
            args = ObjectUtils.addObjectToArray(args, enclosingInstance, 0);
        }
        ReflectionUtils.makeAccessible(constructor);
        return constructor.newInstance(args);
    }

    private Object instantiate(ConfigurableBeanFactory beanFactory, Method method, Object[] args) throws Exception {
        Object target = getFactoryMethodTarget(beanFactory, method);
        ReflectionUtils.makeAccessible(method);
        return method.invoke(target, args);
    }

    @Nullable
    private Object getFactoryMethodTarget(BeanFactory beanFactory, Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        return beanFactory.getBean(declaringClass);
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        if (!ClassUtils.isInnerClass(clazz)) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            ReflectionUtils.makeAccessible(constructor);
            return constructor.newInstance();
        }
        Class<?> enclosingClass = clazz.getEnclosingClass();
        Constructor<?> constructor = clazz.getDeclaredConstructor(enclosingClass);
        return constructor.newInstance(createInstance(enclosingClass));
    }

    private static String toCommaSeparatedNames(Class<?>... parameterTypes) {
        return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
    }

    /**
     * 执行对 {@link Executable} 的查找。
     */
    static abstract class ExecutableLookup {

        abstract Executable get(RegisteredBean registeredBean);
    }

    /**
     * 执行对构造函数的查找。
     */
    private static class ConstructorLookup extends ExecutableLookup {

        private final Class<?>[] parameterTypes;

        ConstructorLookup(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        @Override
        public Executable get(RegisteredBean registeredBean) {
            Class<?> beanClass = registeredBean.getBeanClass();
            try {
                Class<?>[] actualParameterTypes = (!ClassUtils.isInnerClass(beanClass)) ? this.parameterTypes : ObjectUtils.addObjectToArray(this.parameterTypes, beanClass.getEnclosingClass(), 0);
                return beanClass.getDeclaredConstructor(actualParameterTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("%s cannot be found on %s".formatted(this, beanClass.getName()), ex);
            }
        }

        @Override
        public String toString() {
            return "Constructor with parameter types [%s]".formatted(toCommaSeparatedNames(this.parameterTypes));
        }
    }

    /**
     * 执行对工厂方法的查找。
     */
    private static class FactoryMethodLookup extends ExecutableLookup {

        private final Class<?> declaringClass;

        private final String methodName;

        private final Class<?>[] parameterTypes;

        FactoryMethodLookup(Class<?> declaringClass, String methodName, Class<?>[] parameterTypes) {
            this.declaringClass = declaringClass;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public Executable get(RegisteredBean registeredBean) {
            return get();
        }

        Method get() {
            Method method = ReflectionUtils.findMethod(this.declaringClass, this.methodName, this.parameterTypes);
            Assert.notNull(method, () -> "%s cannot be found".formatted(this));
            return method;
        }

        @Override
        public String toString() {
            return "Factory method '%s' with parameter types [%s] declared on %s".formatted(this.methodName, toCommaSeparatedNames(this.parameterTypes), this.declaringClass);
        }
    }
}
