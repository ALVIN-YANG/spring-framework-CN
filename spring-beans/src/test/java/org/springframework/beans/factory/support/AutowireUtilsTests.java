// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按"原样"分发，
* 不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对 {@link AutowireUtils} 的单元测试。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Loïc Ledoyen
 */
public class AutowireUtilsTests {

    @Test
    public void genericMethodReturnTypes() {
        Method notParameterized = ReflectionUtils.findMethod(MyTypeWithMethods.class, "notParameterized");
        Object actual = AutowireUtils.resolveReturnTypeForFactoryMethod(notParameterized, new Object[0], getClass().getClassLoader());
        assertThat(actual).isEqualTo(String.class);
        Method notParameterizedWithArguments = ReflectionUtils.findMethod(MyTypeWithMethods.class, "notParameterizedWithArguments", Integer.class, Boolean.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(notParameterizedWithArguments, new Object[] { 99, true }, getClass().getClassLoader())).isEqualTo(String.class);
        Method createProxy = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createProxy", Object.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createProxy, new Object[] { "foo" }, getClass().getClassLoader())).isEqualTo(String.class);
        Method createNamedProxyWithDifferentTypes = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createNamedProxy", String.class, Object.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createNamedProxyWithDifferentTypes, new Object[] { "enigma", 99L }, getClass().getClassLoader())).isEqualTo(Long.class);
        Method createNamedProxyWithDuplicateTypes = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createNamedProxy", String.class, Object.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createNamedProxyWithDuplicateTypes, new Object[] { "enigma", "foo" }, getClass().getClassLoader())).isEqualTo(String.class);
        Method createMock = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createMock", Class.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createMock, new Object[] { Runnable.class }, getClass().getClassLoader())).isEqualTo(Runnable.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createMock, new Object[] { Runnable.class.getName() }, getClass().getClassLoader())).isEqualTo(Runnable.class);
        Method createNamedMock = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createNamedMock", String.class, Class.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createNamedMock, new Object[] { "foo", Runnable.class }, getClass().getClassLoader())).isEqualTo(Runnable.class);
        Method createVMock = ReflectionUtils.findMethod(MyTypeWithMethods.class, "createVMock", Object.class, Class.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(createVMock, new Object[] { "foo", Runnable.class }, getClass().getClassLoader())).isEqualTo(Runnable.class);
        // 理想情况下，我们期望看到 String.class 而不是 Object.class，但
        // resolveReturnTypeForFactoryMethod() 目前不支持这种形式的
        // 查找。
        Method extractValueFrom = ReflectionUtils.findMethod(MyTypeWithMethods.class, "extractValueFrom", MyInterfaceType.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(extractValueFrom, new Object[] { new MySimpleInterfaceType() }, getClass().getClassLoader())).isEqualTo(Object.class);
        // 理想情况下，我们期望得到 Boolean.class 而不是 Object.class，但这个
        // 由于类型擦除，在运行时无法获取信息。
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, false);
        map.put(1, true);
        Method extractMagicValue = ReflectionUtils.findMethod(MyTypeWithMethods.class, "extractMagicValue", Map.class);
        assertThat(AutowireUtils.resolveReturnTypeForFactoryMethod(extractMagicValue, new Object[] { map }, getClass().getClassLoader())).isEqualTo(Object.class);
    }

    public interface MyInterfaceType<T> {
    }

    public class MySimpleInterfaceType implements MyInterfaceType<String> {
    }

    public static class MyTypeWithMethods<T> {

        /**
         * 模拟一个工厂方法，该方法将提供的对象包装为相同类型的代理。
         */
        public static <T> T createProxy(T object) {
            return null;
        }

        /**
         * 与 {@link #createProxy(Object)} 类似，但在类型为 {@code T} 的参数之前添加了一个额外的参数。注意，它们在调用时可能具有相同的时间！
         */
        public static <T> T createNamedProxy(String name, T object) {
            return null;
        }

        /**
         * 模拟类似于Mockito和EasyMock库中发现的工厂方法。
         */
        public static <MOCK> MOCK createMock(Class<MOCK> toMock) {
            return null;
        }

        /**
         * 与{@link #createMock(Class)}类似，但在参数化参数之前添加了一个额外的方法参数。
         */
        public static <T> T createNamedMock(String name, Class<T> toMock) {
            return null;
        }

        /**
         * 与 {@link #createNamedMock(String, Class)} 类似，但增加了一个参数化类型。
         */
        public static <V extends Object, T> T createVMock(V name, Class<T> toMock) {
            return null;
        }

        /**
         * 从接口支持的类型中提取一些值（即，通过接口的具体系实现，非泛型实现）。
         */
        public static <T> T extractValueFrom(MyInterfaceType<T> myInterfaceType) {
            return null;
        }

        /**
         * 从提供的映射中提取一些魔法值。
         */
        public static <K, V> V extractMagicValue(Map<K, V> map) {
            return null;
        }

        public MyInterfaceType<Integer> integer() {
            return null;
        }

        public MySimpleInterfaceType string() {
            return null;
        }

        public Object object() {
            return null;
        }

        @SuppressWarnings("rawtypes")
        public MyInterfaceType raw() {
            return null;
        }

        public String notParameterized() {
            return null;
        }

        public String notParameterizedWithArguments(Integer x, Boolean b) {
            return null;
        }

        public void readIntegerInputMessage(MyInterfaceType<Integer> message) {
        }

        public void readIntegerArrayInputMessage(MyInterfaceType<Integer>[] message) {
        }

        public void readGenericArrayInputMessage(T[] message) {
        }
    }
}
