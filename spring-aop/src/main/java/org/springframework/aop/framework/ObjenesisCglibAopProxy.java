// 翻译完成 glm-4-flash
/** 版权所有 2002-2022，原作者或作者。
*
* 根据Apache许可证第2版（"许可证"）进行许可；
* 您不得使用此文件，除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证，了解具体规定许可权和限制。*/
package org.springframework.aop.framework;

import java.lang.reflect.Constructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.util.ReflectionUtils;

/**
 * 基于 Objenesis 扩展的 {@link CglibAopProxy}，用于创建不调用类构造函数的代理实例。
 * 自 Spring 4 开始默认使用。
 *
 * @author Oliver Gierke
 * @author Juergen Hoeller
 * @since 4.0
 */
@SuppressWarnings("serial")
class ObjenesisCglibAopProxy extends CglibAopProxy {

    private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

    private static final SpringObjenesis objenesis = new SpringObjenesis();

    /**
     * 为给定的AOP配置创建一个新的ObjenesisCglibAopProxy。
     * @param config 以AdvisedSupport对象形式提供的AOP配置
     */
    public ObjenesisCglibAopProxy(AdvisedSupport config) {
        super(config);
    }

    @Override
    protected Class<?> createProxyClass(Enhancer enhancer) {
        return enhancer.createClass();
    }

    @Override
    protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        Class<?> proxyClass = enhancer.createClass();
        Object proxyInstance = null;
        if (objenesis.isWorthTrying()) {
            try {
                proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
            } catch (Throwable ex) {
                logger.debug("Unable to instantiate proxy using Objenesis, " + "falling back to regular proxy construction", ex);
            }
        }
        if (proxyInstance == null) {
            // 通过默认构造函数进行常规实例化...
            try {
                Constructor<?> ctor = (this.constructorArgs != null ? proxyClass.getDeclaredConstructor(this.constructorArgTypes) : proxyClass.getDeclaredConstructor());
                ReflectionUtils.makeAccessible(ctor);
                proxyInstance = (this.constructorArgs != null ? ctor.newInstance(this.constructorArgs) : ctor.newInstance());
            } catch (Throwable ex) {
                throw new AopConfigException("Unable to instantiate proxy using Objenesis, " + "and regular proxy instantiation via default constructor fails as well", ex);
            }
        }
        ((Factory) proxyInstance).setCallbacks(callbacks);
        return proxyInstance;
    }
}
