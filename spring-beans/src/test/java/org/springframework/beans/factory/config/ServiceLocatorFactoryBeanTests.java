// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的。
* 请参阅许可证了解具体规定权限和限制。*/
package org.springframework.beans.factory.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 对 {@link ServiceLocatorFactoryBean} 的单元测试。
 *
 * @author Colin Sampaleanu
 * @author Rick Evans
 * @author Chris Beams
 */
public class ServiceLocatorFactoryBeanTests {

    private DefaultListableBeanFactory bf;

    @BeforeEach
    public void setUp() {
        bf = new DefaultListableBeanFactory();
    }

    @Test
    public void testNoArgGetter() {
        bf.registerBeanDefinition("testService", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator.class).getBeanDefinition());
        TestServiceLocator factory = (TestServiceLocator) bf.getBean("factory");
        TestService testService = factory.getTestService();
        assertThat(testService).isNotNull();
    }

    @Test
    public void testErrorOnTooManyOrTooFew() throws Exception {
        bf.registerBeanDefinition("testService", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("testServiceInstance2", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator.class).getBeanDefinition());
        bf.registerBeanDefinition("factory2", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator2.class).getBeanDefinition());
        bf.registerBeanDefinition("factory3", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestService2Locator.class).getBeanDefinition());
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).as("more than one matching type").isThrownBy(() -> ((TestServiceLocator) bf.getBean("factory")).getTestService());
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).as("more than one matching type").isThrownBy(() -> ((TestServiceLocator2) bf.getBean("factory2")).getTestService(null));
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).as("no matching types").isThrownBy(() -> ((TestService2Locator) bf.getBean("factory3")).getTestService());
    }

    @Test
    public void testErrorOnTooManyOrTooFewWithCustomServiceLocatorException() {
        bf.registerBeanDefinition("testService", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("testServiceInstance2", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator.class).addPropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException1.class).getBeanDefinition());
        bf.registerBeanDefinition("factory2", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator2.class).addPropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException2.class).getBeanDefinition());
        bf.registerBeanDefinition("factory3", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestService2Locator.class).addPropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException3.class).getBeanDefinition());
        assertThatExceptionOfType(CustomServiceLocatorException1.class).as("more than one matching type").isThrownBy(() -> ((TestServiceLocator) bf.getBean("factory")).getTestService()).withCauseInstanceOf(NoSuchBeanDefinitionException.class);
        assertThatExceptionOfType(CustomServiceLocatorException2.class).as("more than one matching type").isThrownBy(() -> ((TestServiceLocator2) bf.getBean("factory2")).getTestService(null)).withCauseInstanceOf(NoSuchBeanDefinitionException.class);
        assertThatExceptionOfType(CustomServiceLocatorException3.class).as("no matching type").isThrownBy(() -> ((TestService2Locator) bf.getBean("factory3")).getTestService());
    }

    @Test
    public void testStringArgGetter() throws Exception {
        bf.registerBeanDefinition("testService", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator2.class).getBeanDefinition());
        // 测试 null id 的字符串参数获取器
        TestServiceLocator2 factory = (TestServiceLocator2) bf.getBean("factory");
        @SuppressWarnings("unused")
        TestService testBean = factory.getTestService(null);
        // 现在使用显式ID进行测试
        testBean = factory.getTestService("testService");
        // 现在验证失败，因为ID不正确
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).isThrownBy(() -> factory.getTestService("bogusTestService"));
    }

    // When using ApplicationContext (see comments) it works, but fails when using BeanFactory.
    @Disabled
    // When using ApplicationContext (see comments) it works, but fails when using BeanFactory.
    @Test
    public void testCombinedLocatorInterface() {
        bf.registerBeanDefinition("testService", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerAlias("testService", "1");
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class).getBeanDefinition());
        // 静态上下文 ctx = new StaticApplicationContext();
        // ctx.registerPrototype("testService", TestService.class, new MutablePropertyValues()); 这段代码的注释翻译成中文是：注册名为 "testService" 的原型，类型为 TestService.class，并使用新的 MutablePropertyValues 实例作为其属性值。
        // ctx.registerAlias("testService", "1");注释翻译：注册别名，将"testService"映射到"1"。
        // MutablePropertyValues mpv = new MutablePropertyValues(); 翻译成中文为：可变属性值对象 mpv = new 可变属性值对象();
        // mpv.addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class); 这段代码的注释翻译成中文为：mpv 添加属性值，将 "serviceLocatorInterface" 属性设置为 TestServiceLocator3 类。
        // ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);```javactx注册一个单例Bean，Bean名为"factory"，Bean类型为ServiceLocatorFactoryBean.class，并传递了参数mpv。```
        // ctx.refresh(); // 刷新ctx对象
        TestServiceLocator3 factory = (TestServiceLocator3) bf.getBean("factory");
        TestService testBean1 = factory.getTestService();
        TestService testBean2 = factory.getTestService("testService");
        TestService testBean3 = factory.getTestService(1);
        TestService testBean4 = factory.someFactoryMethod();
        assertThat(testBean2).isNotSameAs(testBean1);
        assertThat(testBean3).isNotSameAs(testBean1);
        assertThat(testBean4).isNotSameAs(testBean1);
        assertThat(testBean3).isNotSameAs(testBean2);
        assertThat(testBean4).isNotSameAs(testBean2);
        assertThat(testBean4).isNotSameAs(testBean3);
        assertThat(factory.toString()).contains("TestServiceLocator3");
    }

    // When using ApplicationContext (see comments), it works normally, but it fails when using BeanFactory.
    @Disabled
    // When using ApplicationContext (see comments), it works normally, but it fails when using BeanFactory.
    @Test
    public void testServiceMappings() {
        bf.registerBeanDefinition("testService1", genericBeanDefinition(TestService.class).getBeanDefinition());
        bf.registerBeanDefinition("testService2", genericBeanDefinition(ExtendedTestService.class).getBeanDefinition());
        bf.registerBeanDefinition("factory", genericBeanDefinition(ServiceLocatorFactoryBean.class).addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class).addPropertyValue("serviceMappings", "=testService1\n1=testService1\n2=testService2").getBeanDefinition());
        // 静态应用程序上下文 ctx = new StaticApplicationContext();
        // ctx.registerPrototype("testService1", TestService.class, new MutablePropertyValues()); 代码注释翻译成中文为：注册名为 "testService1" 的原型，原型类为 TestService，并使用新的 MutablePropertyValues 实例作为属性值。
        // ctx.registerPrototype("testService2", ExtendedTestService.class, new MutablePropertyValues()); 这段代码的注释翻译成中文为：注册一个原型为 "testService2" 的服务，其类类型为 ExtendedTestService，并使用一个新的 MutablePropertyValues 对象。
        // MutablePropertyValues mpv = new MutablePropertyValues();  // 创建一个可变属性值对象实例
        // mpv.addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class); 这段代码的注释内容翻译成中文是：mpv 添加了一个属性值，属性名为 "serviceLocatorInterface"，其值为 TestServiceLocator3 类。
        // mpv.addPropertyValue("serviceMappings", "=testService1\n1=testService1\n2=testService2"); 这段代码注释的中文翻译可能是：mpv添加属性值“serviceMappings”，值为“=testService1\n1=testService1\n2=testService2”；这里假设`mpv`是一个对象，其`addPropertyValue`方法用于向该对象添加或更新属性值。属性名为"serviceMappings"，对应的值是一系列的映射，其中包含服务和对应的编号。这里的换行符`\n`表明了这是一行文本，包含了多行数据。
        // ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);```javactx注册一个名为"factory"的单例，类型为ServiceLocatorFactoryBean.class，并传递参数mpv。```
        // ctx.refresh(); // 调用 ctx 对象的 refresh 方法以刷新或更新其状态或内容
        TestServiceLocator3 factory = (TestServiceLocator3) bf.getBean("factory");
        TestService testBean1 = factory.getTestService();
        TestService testBean2 = factory.getTestService("testService1");
        TestService testBean3 = factory.getTestService(1);
        TestService testBean4 = factory.getTestService(2);
        assertThat(testBean2).isNotSameAs(testBean1);
        assertThat(testBean3).isNotSameAs(testBean1);
        assertThat(testBean4).isNotSameAs(testBean1);
        assertThat(testBean3).isNotSameAs(testBean2);
        assertThat(testBean4).isNotSameAs(testBean2);
        assertThat(testBean4).isNotSameAs(testBean3);
        assertThat(testBean1 instanceof ExtendedTestService).isFalse();
        assertThat(testBean2 instanceof ExtendedTestService).isFalse();
        assertThat(testBean3 instanceof ExtendedTestService).isFalse();
        assertThat(testBean4 instanceof ExtendedTestService).isTrue();
    }

    @Test
    public void testNoServiceLocatorInterfaceSupplied() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(new ServiceLocatorFactoryBean()::afterPropertiesSet);
    }

    @Test
    public void testWhenServiceLocatorInterfaceIsNotAnInterfaceType() throws Exception {
        ServiceLocatorFactoryBean factory = new ServiceLocatorFactoryBean();
        factory.setServiceLocatorInterface(getClass());
        assertThatIllegalArgumentException().isThrownBy(factory::afterPropertiesSet);
        // 应该抛出异常，提供的服务定位器接口类型不正确（非接口类型）
    }

    @Test
    public void testWhenServiceLocatorExceptionClassToExceptionTypeWithOnlyNoArgCtor() throws Exception {
        ServiceLocatorFactoryBean factory = new ServiceLocatorFactoryBean();
        assertThatIllegalArgumentException().isThrownBy(() -> factory.setServiceLocatorExceptionClass(ExceptionClassWithOnlyZeroArgCtor.class));
        // 应该抛出，糟糕（无效异常类型）的服务定位器异常类提供
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testWhenServiceLocatorExceptionClassIsNotAnExceptionSubclass() throws Exception {
        ServiceLocatorFactoryBean factory = new ServiceLocatorFactoryBean();
        assertThatIllegalArgumentException().isThrownBy(() -> factory.setServiceLocatorExceptionClass((Class) getClass()));
        // 应该抛出异常，不良（非异常类型）的serviceLocatorException类被提供
    }

    @Test
    public void testWhenServiceLocatorMethodCalledWithTooManyParameters() throws Exception {
        ServiceLocatorFactoryBean factory = new ServiceLocatorFactoryBean();
        factory.setServiceLocatorInterface(ServiceLocatorInterfaceWithExtraNonCompliantMethod.class);
        factory.afterPropertiesSet();
        ServiceLocatorInterfaceWithExtraNonCompliantMethod locator = (ServiceLocatorInterfaceWithExtraNonCompliantMethod) factory.getObject();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> locator.getTestService("not", "allowed"));
    }

    @Test
    public void testRequiresListableBeanFactoryAndChokesOnAnythingElse() throws Exception {
        BeanFactory beanFactory = mock();
        try {
            ServiceLocatorFactoryBean factory = new ServiceLocatorFactoryBean();
            factory.setBeanFactory(beanFactory);
        } catch (FatalBeanException ex) {
            // 预期
        }
    }

    public static class TestService {
    }

    public static class ExtendedTestService extends TestService {
    }

    public static class TestService2 {
    }

    public interface TestServiceLocator {

        TestService getTestService();
    }

    public interface TestServiceLocator2 {

        TestService getTestService(String id) throws CustomServiceLocatorException2;
    }

    public interface TestServiceLocator3 {

        TestService getTestService();

        TestService getTestService(String id);

        TestService getTestService(int id);

        TestService someFactoryMethod();
    }

    public interface TestService2Locator {

        TestService2 getTestService() throws CustomServiceLocatorException3;
    }

    public interface ServiceLocatorInterfaceWithExtraNonCompliantMethod {

        TestService2 getTestService();

        TestService2 getTestService(String serviceName, String defaultNotAllowedParameter);
    }

    @SuppressWarnings("serial")
    public static class CustomServiceLocatorException1 extends NestedRuntimeException {

        public CustomServiceLocatorException1(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class CustomServiceLocatorException2 extends NestedCheckedException {

        public CustomServiceLocatorException2(Throwable cause) {
            super("", cause);
        }
    }

    @SuppressWarnings("serial")
    public static class CustomServiceLocatorException3 extends NestedCheckedException {

        public CustomServiceLocatorException3(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
    public static class ExceptionClassWithOnlyZeroArgCtor extends Exception {
    }
}
