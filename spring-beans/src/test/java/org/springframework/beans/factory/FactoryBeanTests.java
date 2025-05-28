// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件，除非遵守许可证。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的的保证。
* 请参阅许可证，了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.testfixture.stereotype.Component;
import org.springframework.util.Assert;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * @作者 Rob Harrop
 * @作者 Juergen Hoeller
 * @作者 Chris Beams
 */
public class FactoryBeanTests {

    private static final Class<?> CLASS = FactoryBeanTests.class;

    private static final Resource RETURNS_NULL_CONTEXT = qualifiedResource(CLASS, "returnsNull.xml");

    private static final Resource WITH_AUTOWIRING_CONTEXT = qualifiedResource(CLASS, "withAutowiring.xml");

    private static final Resource ABSTRACT_CONTEXT = qualifiedResource(CLASS, "abstract.xml");

    private static final Resource CIRCULAR_CONTEXT = qualifiedResource(CLASS, "circular.xml");

    @Test
    public void testFactoryBeanReturnsNull() throws Exception {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(RETURNS_NULL_CONTEXT);
        assertThat(factory.getBean("factoryBean").toString()).isEqualTo("null");
    }

    @Test
    public void testFactoryBeansWithAutowiring() throws Exception {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(WITH_AUTOWIRING_CONTEXT);
        BeanFactoryPostProcessor ppc = (BeanFactoryPostProcessor) factory.getBean("propertyPlaceholderConfigurer");
        ppc.postProcessBeanFactory(factory);
        assertThat(factory.getType("betaFactory")).isNull();
        Alpha alpha = (Alpha) factory.getBean("alpha");
        Beta beta = (Beta) factory.getBean("beta");
        Gamma gamma = (Gamma) factory.getBean("gamma");
        Gamma gamma2 = (Gamma) factory.getBean("gammaFactory");
        assertThat(alpha.getBeta()).isSameAs(beta);
        assertThat(beta.getGamma()).isSameAs(gamma);
        assertThat(beta.getGamma()).isSameAs(gamma2);
        assertThat(beta.getName()).isEqualTo("yourName");
    }

    @Test
    public void testFactoryBeansWithIntermediateFactoryBeanAutowiringFailure() throws Exception {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(WITH_AUTOWIRING_CONTEXT);
        BeanFactoryPostProcessor ppc = (BeanFactoryPostProcessor) factory.getBean("propertyPlaceholderConfigurer");
        ppc.postProcessBeanFactory(factory);
        Beta beta = (Beta) factory.getBean("beta");
        Alpha alpha = (Alpha) factory.getBean("alpha");
        Gamma gamma = (Gamma) factory.getBean("gamma");
        assertThat(alpha.getBeta()).isSameAs(beta);
        assertThat(beta.getGamma()).isSameAs(gamma);
    }

    @Test
    public void testAbstractFactoryBeanViaAnnotation() throws Exception {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(ABSTRACT_CONTEXT);
        factory.getBeansWithAnnotation(Component.class);
    }

    @Test
    public void testAbstractFactoryBeanViaType() throws Exception {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(ABSTRACT_CONTEXT);
        factory.getBeansOfType(AbstractFactoryBean.class);
    }

    @Test
    public void testCircularReferenceWithPostProcessor() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(CIRCULAR_CONTEXT);
        CountingPostProcessor counter = new CountingPostProcessor();
        factory.addBeanPostProcessor(counter);
        BeanImpl1 impl1 = factory.getBean(BeanImpl1.class);
        assertThat(impl1).isNotNull();
        assertThat(impl1.getImpl2()).isNotNull();
        assertThat(impl1.getImpl2()).isNotNull();
        assertThat(impl1.getImpl2().getImpl1()).isSameAs(impl1);
        assertThat(counter.getCount("bean1")).isEqualTo(1);
        assertThat(counter.getCount("bean2")).isEqualTo(1);
    }

    public static class NullReturningFactoryBean implements FactoryBean<Object> {

        @Override
        public Object getObject() {
            return null;
        }

        @Override
        public Class<?> getObjectType() {
            return null;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    public static class Alpha implements InitializingBean {

        private Beta beta;

        public void setBeta(Beta beta) {
            this.beta = beta;
        }

        public Beta getBeta() {
            return beta;
        }

        @Override
        public void afterPropertiesSet() {
            Assert.notNull(beta, "'beta' property is required");
        }
    }

    public static class Beta implements InitializingBean {

        private Gamma gamma;

        private String name;

        public void setGamma(Gamma gamma) {
            this.gamma = gamma;
        }

        public Gamma getGamma() {
            return gamma;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public void afterPropertiesSet() {
            Assert.notNull(gamma, "'gamma' property is required");
        }
    }

    public static class Gamma {
    }

    @Component
    public static class BetaFactoryBean implements FactoryBean<Object> {

        public BetaFactoryBean(Alpha alpha) {
        }

        private Beta beta;

        public void setBeta(Beta beta) {
            this.beta = beta;
        }

        @Override
        public Object getObject() {
            return this.beta;
        }

        @Override
        public Class<?> getObjectType() {
            return null;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    public abstract static class AbstractFactoryBean implements FactoryBean<Object> {
    }

    public static class PassThroughFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

        private Class<T> type;

        private String instanceName;

        private BeanFactory beanFactory;

        private T instance;

        public PassThroughFactoryBean(Class<T> type) {
            this.type = type;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public T getObject() {
            if (instance == null) {
                instance = beanFactory.getBean(instanceName, type);
            }
            return instance;
        }

        @Override
        public Class<?> getObjectType() {
            return type;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    public static class CountingPostProcessor implements BeanPostProcessor {

        private final Map<String, AtomicInteger> count = new HashMap<>();

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof FactoryBean) {
                return bean;
            }
            AtomicInteger c = count.get(beanName);
            if (c == null) {
                c = new AtomicInteger();
                count.put(beanName, c);
            }
            c.incrementAndGet();
            return bean;
        }

        public int getCount(String beanName) {
            AtomicInteger c = count.get(beanName);
            if (c != null) {
                return c.intValue();
            } else {
                return 0;
            }
        }
    }

    public static class BeanImpl1 {

        private BeanImpl2 impl2;

        public BeanImpl2 getImpl2() {
            return impl2;
        }

        public void setImpl2(BeanImpl2 impl2) {
            this.impl2 = impl2;
        }
    }

    public static class BeanImpl2 {

        private BeanImpl1 impl1;

        public BeanImpl1 getImpl1() {
            return impl1;
        }

        public void setImpl1(BeanImpl1 impl1) {
            this.impl1 = impl1;
        }
    }
}
