// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“协议”）；除非遵守协议，否则您不得使用此文件。
* 您可以在以下地址获取协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在协议下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅协议以了解具体规定许可权和限制。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * BeanFactories使用的默认对象实例化策略。
 *
 * <p>如果容器需要通过实现<em>方法注入</em>来覆盖方法，则使用CGLIB动态生成子类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

    /**
     * CGLIB 回调数组中用于透传行为的索引，
     * 在这种情况下，子类不会覆盖原始类。
     */
    private static final int PASSTHROUGH = 0;

    /**
     * CGLIB 回调数组中索引，用于应该被重写以提供 <em>方法查找</em> 的方法。
     */
    private static final int LOOKUP_OVERRIDE = 1;

    /**
     * CGLIB 回调数组中用于应该使用通用<em>方法替换</em>功能覆盖的方法的索引。
     */
    private static final int METHOD_REPLACER = 2;

    @Override
    protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
        return instantiateWithMethodInjection(bd, beanName, owner, null);
    }

    @Override
    protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner, @Nullable Constructor<?> ctor, Object... args) {
        return new CglibSubclassCreator(bd, owner).instantiate(ctor, args);
    }

    @Override
    public Class<?> getActualBeanClass(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
        if (!bd.hasMethodOverrides()) {
            return super.getActualBeanClass(bd, beanName, owner);
        }
        return new CglibSubclassCreator(bd, owner).createEnhancedSubclass(bd);
    }

    /**
     * 为了历史原因创建的内部类，以避免在 3.2 版本之前的 Spring 版本中使用外部 CGLIB 依赖。
     */
    private static class CglibSubclassCreator {

        private static final Class<?>[] CALLBACK_TYPES = new Class<?>[] { NoOp.class, LookupOverrideMethodInterceptor.class, ReplaceOverrideMethodInterceptor.class };

        private final RootBeanDefinition beanDefinition;

        private final BeanFactory owner;

        CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
            this.beanDefinition = beanDefinition;
            this.owner = owner;
        }

        /**
         * 创建一个实现所需查找的动态生成的子类的新实例。
         * @param ctor 要使用的构造函数。如果这是 {@code null}，则使用无参构造函数（无参数化，或设置器注入）
         * @param args 要用于构造函数的参数。
         * 如果 {@code ctor} 参数为 {@code null}，则忽略这些参数。
         * @return 动态生成的子类的新实例
         */
        public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
            Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
            Object instance;
            if (ctor == null) {
                instance = BeanUtils.instantiateClass(subclass);
            } else {
                try {
                    Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
                    instance = enhancedSubclassConstructor.newInstance(args);
                } catch (Exception ex) {
                    throw new BeanInstantiationException(this.beanDefinition.getBeanClass(), "Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
                }
            }
            // SPR-10785: 直接在实例上设置回调，而不是在
            // 通过 Enhancer（增强器）增强的类，以避免内存泄漏。
            Factory factory = (Factory) instance;
            factory.setCallbacks(new Callback[] { NoOp.INSTANCE, new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner), new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner) });
            return instance;
        }

        /**
         * 使用CGLIB创建一个增强的子类，该子类基于提供的bean定义。
         */
        public Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(beanDefinition.getBeanClass());
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setAttemptLoad(true);
            if (this.owner instanceof ConfigurableBeanFactory cbf) {
                ClassLoader cl = cbf.getBeanClassLoader();
                enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
            }
            enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
            enhancer.setCallbackTypes(CALLBACK_TYPES);
            return enhancer.createClass();
        }
    }

    /**
     * 提供CGLIB所需的hashCode和equals方法，以确保CGLIB不会为每个bean生成一个不同的类。
     * 身份基于类和bean定义。
     */
    private static class CglibIdentitySupport {

        private final RootBeanDefinition beanDefinition;

        public CglibIdentitySupport(RootBeanDefinition beanDefinition) {
            this.beanDefinition = beanDefinition;
        }

        public RootBeanDefinition getBeanDefinition() {
            return this.beanDefinition;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (other != null && getClass() == other.getClass() && this.beanDefinition.equals(((CglibIdentitySupport) other).beanDefinition));
        }

        @Override
        public int hashCode() {
            return this.beanDefinition.hashCode();
        }
    }

    /**
     * CGLIB 回调用于过滤方法拦截行为。
     */
    private static class MethodOverrideCallbackFilter extends CglibIdentitySupport implements CallbackFilter {

        private static final Log logger = LogFactory.getLog(MethodOverrideCallbackFilter.class);

        public MethodOverrideCallbackFilter(RootBeanDefinition beanDefinition) {
            super(beanDefinition);
        }

        @Override
        public int accept(Method method) {
            MethodOverride methodOverride = getBeanDefinition().getMethodOverrides().getOverride(method);
            if (logger.isTraceEnabled()) {
                logger.trace("MethodOverride for " + method + ": " + methodOverride);
            }
            if (methodOverride == null) {
                return PASSTHROUGH;
            } else if (methodOverride instanceof LookupOverride) {
                return LOOKUP_OVERRIDE;
            } else if (methodOverride instanceof ReplaceOverride) {
                return METHOD_REPLACER;
            }
            throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " + methodOverride.getClass().getName());
        }
    }

    /**
     * CGLIB 方法拦截器，用于覆盖方法，用从容器中查找的 bean 的实现来替换它们。
     */
    private static class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

        private final BeanFactory owner;

        public LookupOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
            super(beanDefinition);
            this.owner = owner;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
            // 转换是安全的，因为CallbackFilter过滤器是选择性使用的。
            LookupOverride lo = (LookupOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
            Assert.state(lo != null, "LookupOverride not found");
            // 如果没有参数，就根本不坚持使用参数
            Object[] argsToUse = (args.length > 0 ? args : null);
            if (StringUtils.hasText(lo.getBeanName())) {
                Object bean = (argsToUse != null ? this.owner.getBean(lo.getBeanName(), argsToUse) : this.owner.getBean(lo.getBeanName()));
                // 通过equals(null)检查检测包保护的NullBean实例
                return (bean.equals(null) ? null : bean);
            } else {
                // 查找匹配（可能为泛型）方法返回类型的目标Bean
                ResolvableType genericReturnType = ResolvableType.forMethodReturnType(method);
                return (argsToUse != null ? this.owner.getBeanProvider(genericReturnType).getObject(argsToUse) : this.owner.getBeanProvider(genericReturnType).getObject());
            }
        }
    }

    /**
     * CGLIB 方法拦截器，用于覆盖方法，用对通用 MethodReplacer 的调用替换它们。
     */
    private static class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

        private final BeanFactory owner;

        public ReplaceOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
            super(beanDefinition);
            this.owner = owner;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
            ReplaceOverride ro = (ReplaceOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
            Assert.state(ro != null, "ReplaceOverride not found");
            // 待办事项：可以考虑缓存单例以进行轻微的性能优化
            MethodReplacer mr = this.owner.getBean(ro.getMethodReplacerBeanName(), MethodReplacer.class);
            return mr.reimplement(obj, method, args);
        }
    }
}
