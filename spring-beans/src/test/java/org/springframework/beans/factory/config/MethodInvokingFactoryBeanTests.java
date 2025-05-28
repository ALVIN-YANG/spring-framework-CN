// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.MethodInvoker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link MethodInvokingFactoryBean} 和 {@link MethodInvokingBean} 的单元测试。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2003年11月21日
 */
public class MethodInvokingFactoryBeanTests {

    @Test
    public void testParameterValidation() throws Exception {
        // 断言只有静态或非静态被设置，但不能同时设置或都不设置
        MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
        assertThatIllegalArgumentException().isThrownBy(mcfb::afterPropertiesSet);
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(this);
        mcfb.setTargetMethod("whatever");
        assertThatExceptionOfType(NoSuchMethodException.class).isThrownBy(mcfb::afterPropertiesSet);
        // 虚假的静态方法
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("some.bogus.Method.name");
        assertThatExceptionOfType(NoSuchMethodException.class).isThrownBy(mcfb::afterPropertiesSet);
        // 虚假的静态方法
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("method1");
        assertThatIllegalArgumentException().isThrownBy(mcfb::afterPropertiesSet);
        // 缺少方法
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(this);
        assertThatIllegalArgumentException().isThrownBy(mcfb::afterPropertiesSet);
        // 虚假方法
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(this);
        mcfb.setTargetMethod("bogus");
        assertThatExceptionOfType(NoSuchMethodException.class).isThrownBy(mcfb::afterPropertiesSet);
        // 静态方法
        TestClass1._staticField1 = 0;
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("staticMethod1");
        mcfb.afterPropertiesSet();
        // 非静态方法
        TestClass1 tc1 = new TestClass1();
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(tc1);
        mcfb.setTargetMethod("method1");
        mcfb.afterPropertiesSet();
    }

    @Test
    public void testGetObjectType() throws Exception {
        TestClass1 tc1 = new TestClass1();
        MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(tc1);
        mcfb.setTargetMethod("method1");
        mcfb.afterPropertiesSet();
        assertThat(int.class.equals(mcfb.getObjectType())).isTrue();
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("voidRetvalMethod");
        mcfb.afterPropertiesSet();
        Class<?> objType = mcfb.getObjectType();
        assertThat(void.class).isSameAs(objType);
        // 验证我们是否可以调用一个方法，其参数是子类型
        // 目标方法参数类型
        TestClass1._staticField1 = 0;
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes");
        mcfb.setArguments(new ArrayList<>(), new ArrayList<>(), "hello");
        mcfb.afterPropertiesSet();
        mcfb.getObjectType();
        // 在 afterPropertiesSet 方法中，对不正确的参数类型进行失败处理
        mcfb = new MethodInvokingFactoryBean();
        mcfb.registerCustomEditor(String.class, new StringTrimmerEditor(false));
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes");
        mcfb.setArguments("1", new Object());
        assertThatExceptionOfType(NoSuchMethodException.class).isThrownBy(mcfb::afterPropertiesSet);
    }

    @Test
    public void testGetObject() throws Exception {
        // 单例，非静态
        TestClass1 tc1 = new TestClass1();
        MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(tc1);
        mcfb.setTargetMethod("method1");
        mcfb.afterPropertiesSet();
        Integer i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        // 非单例，非静态
        tc1 = new TestClass1();
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetObject(tc1);
        mcfb.setTargetMethod("method1");
        mcfb.setSingleton(false);
        mcfb.afterPropertiesSet();
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(2);
        // 单例，静态
        TestClass1._staticField1 = 0;
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("staticMethod1");
        mcfb.afterPropertiesSet();
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        // 非单例，静态
        TestClass1._staticField1 = 0;
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setStaticMethod("org.springframework.beans.factory.config.MethodInvokingFactoryBeanTests$TestClass1.staticMethod1");
        mcfb.setSingleton(false);
        mcfb.afterPropertiesSet();
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(1);
        i = (Integer) mcfb.getObject();
        assertThat(i).isEqualTo(2);
        // 返回值类型为 void
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("voidRetvalMethod");
        mcfb.afterPropertiesSet();
        assertThat(mcfb.getObject()).isNull();
        // 现在看看我们是否可以匹配具有超类型参数的方法和参数
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes");
        mcfb.setArguments(new ArrayList<>(), new ArrayList<>(), "hello");
        // 应该通过
        mcfb.afterPropertiesSet();
    }

    @Test
    public void testArgumentConversion() throws Exception {
        MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes");
        mcfb.setArguments(new ArrayList<>(), new ArrayList<>(), "hello", "bogus");
        assertThatExceptionOfType(NoSuchMethodException.class).as("Matched method with wrong number of args").isThrownBy(mcfb::afterPropertiesSet);
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes");
        mcfb.setArguments(1, new Object());
        assertThatExceptionOfType(NoSuchMethodException.class).as("Should have failed on getObject with mismatched argument types").isThrownBy(mcfb::afterPropertiesSet);
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes2");
        mcfb.setArguments(new ArrayList<>(), new ArrayList<>(), "hello", "bogus");
        mcfb.afterPropertiesSet();
        assertThat(mcfb.getObject()).isEqualTo("hello");
        mcfb = new MethodInvokingFactoryBean();
        mcfb.setTargetClass(TestClass1.class);
        mcfb.setTargetMethod("supertypes2");
        mcfb.setArguments(new ArrayList<>(), new ArrayList<>(), new Object());
        assertThatExceptionOfType(NoSuchMethodException.class).as("Matched method when shouldn't have matched").isThrownBy(mcfb::afterPropertiesSet);
    }

    @Test
    public void testInvokeWithNullArgument() throws Exception {
        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("nullArgument");
        methodInvoker.setArguments(new Object[] { null });
        methodInvoker.prepare();
        methodInvoker.invoke();
    }

    @Test
    public void testInvokeWithIntArgument() throws Exception {
        ArgumentConvertingMethodInvoker methodInvoker = new ArgumentConvertingMethodInvoker();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArgument");
        methodInvoker.setArguments(5);
        methodInvoker.prepare();
        methodInvoker.invoke();
        methodInvoker = new ArgumentConvertingMethodInvoker();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArgument");
        methodInvoker.setArguments(5);
        methodInvoker.prepare();
        methodInvoker.invoke();
    }

    @Test
    public void testInvokeWithIntArguments() throws Exception {
        MethodInvokingBean methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments(new Object[] { new Integer[] { 5, 10 } });
        methodInvoker.afterPropertiesSet();
        methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments(new Object[] { new String[] { "5", "10" } });
        methodInvoker.afterPropertiesSet();
        methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments(new Object[] { new Integer[] { 5, 10 } });
        methodInvoker.afterPropertiesSet();
        methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments("5", "10");
        methodInvoker.afterPropertiesSet();
        methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments(new Object[] { new Integer[] { 5, 10 } });
        methodInvoker.afterPropertiesSet();
        methodInvoker = new MethodInvokingBean();
        methodInvoker.setTargetClass(TestClass1.class);
        methodInvoker.setTargetMethod("intArguments");
        methodInvoker.setArguments("5", "10");
        methodInvoker.afterPropertiesSet();
    }

    public static class TestClass1 {

        public static int _staticField1;

        public int _field1 = 0;

        public int method1() {
            return ++_field1;
        }

        public static int staticMethod1() {
            return ++TestClass1._staticField1;
        }

        public static void voidRetvalMethod() {
        }

        public static void nullArgument(Object arg) {
        }

        public static void intArgument(int arg) {
        }

        public static void intArguments(int[] arg) {
        }

        public static String supertypes(Collection<?> c, Integer i) {
            return i.toString();
        }

        public static String supertypes(Collection<?> c, List<?> l, String s) {
            return s;
        }

        public static String supertypes2(Collection<?> c, List<?> l, Integer i) {
            return i.toString();
        }

        public static String supertypes2(Collection<?> c, List<?> l, String s, Integer i) {
            return s;
        }

        public static String supertypes2(Collection<?> c, List<?> l, String s, String s2) {
            return s;
        }
    }
}
