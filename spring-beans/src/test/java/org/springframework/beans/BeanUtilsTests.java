// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.testfixture.beans.DerivedTestBean;
import org.springframework.beans.testfixture.beans.ITestBean;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.lang.Nullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * 测试用例对于 {@link BeanUtils}。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 19.05.2003
 */
class BeanUtilsTests {

    @Test
    void instantiateClassGivenInterface() {
        assertThatExceptionOfType(FatalBeanException.class).isThrownBy(() -> BeanUtils.instantiateClass(List.class));
    }

    @Test
    void instantiateClassGivenClassWithoutDefaultConstructor() {
        assertThatExceptionOfType(FatalBeanException.class).isThrownBy(() -> BeanUtils.instantiateClass(CustomDateEditor.class));
    }

    // 很抱歉，您没有提供具体的Java代码注释内容。请提供需要翻译的注释内容，我才能为您进行准确的翻译。
    @Test
    void instantiateClassWithOptionalNullableType() throws NoSuchMethodException {
        Constructor<BeanWithNullableTypes> ctor = BeanWithNullableTypes.class.getDeclaredConstructor(Integer.class, Boolean.class, String.class);
        BeanWithNullableTypes bean = BeanUtils.instantiateClass(ctor, null, null, "foo");
        assertThat(bean.getCounter()).isNull();
        assertThat(bean.isFlag()).isNull();
        assertThat(bean.getValue()).isEqualTo("foo");
    }

    // 很抱歉，您只提供了一个代码片段的标识符 "gh-22531"。没有提供具体的代码注释内容，因此我无法进行翻译。请提供完整的代码注释内容，以便我能够为您翻译成中文。
    @Test
    void instantiateClassWithFewerArgsThanParameters() throws NoSuchMethodException {
        Constructor<BeanWithPrimitiveTypes> constructor = getBeanWithPrimitiveTypesConstructor();
        assertThatExceptionOfType(BeanInstantiationException.class).isThrownBy(() -> BeanUtils.instantiateClass(constructor, null, null, "foo"));
    }

    // 很抱歉，您只提供了一个代码片段的标识符“gh-22531”。没有提供具体的代码注释内容，我无法进行翻译。请提供完整的代码注释内容，以便我能够为您翻译成中文。
    @Test
    void instantiateClassWithMoreArgsThanParameters() throws NoSuchMethodException {
        Constructor<BeanWithPrimitiveTypes> constructor = getBeanWithPrimitiveTypesConstructor();
        assertThatExceptionOfType(BeanInstantiationException.class).isThrownBy(() -> BeanUtils.instantiateClass(constructor, null, null, null, null, null, null, null, null, "foo", null));
    }

    // gh-22531, gh-27390这两个代码注释看起来像是 GitHub pull request (PR) 的编号。在 GitHub 中，每个 pull request 都有一个唯一的编号，通常以 "gh-" 开头。以下是翻译：gh-22531, gh-27390GitHub号22531, GitHub号27390
    @Test
    void instantiateClassWithOptionalPrimitiveTypes() throws NoSuchMethodException {
        Constructor<BeanWithPrimitiveTypes> constructor = getBeanWithPrimitiveTypesConstructor();
        BeanWithPrimitiveTypes bean = BeanUtils.instantiateClass(constructor, null, null, null, null, null, null, null, null, "foo");
        assertSoftly(softly -> {
            softly.assertThat(bean.isFlag()).isFalse();
            softly.assertThat(bean.getByteCount()).isEqualTo((byte) 0);
            softly.assertThat(bean.getShortCount()).isEqualTo((short) 0);
            softly.assertThat(bean.getIntCount()).isEqualTo(0);
            softly.assertThat(bean.getLongCount()).isEqualTo(0L);
            softly.assertThat(bean.getFloatCount()).isEqualTo(0F);
            softly.assertThat(bean.getDoubleCount()).isEqualTo(0D);
            softly.assertThat(bean.getCharacter()).isEqualTo('\0');
            softly.assertThat(bean.getText()).isEqualTo("foo");
        });
    }

    private Constructor<BeanWithPrimitiveTypes> getBeanWithPrimitiveTypesConstructor() throws NoSuchMethodException {
        return BeanWithPrimitiveTypes.class.getConstructor(boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class, String.class);
    }

    @Test
    void instantiatePrivateClassWithPrivateConstructor() throws NoSuchMethodException {
        Constructor<PrivateBeanWithPrivateConstructor> ctor = PrivateBeanWithPrivateConstructor.class.getDeclaredConstructor();
        BeanUtils.instantiateClass(ctor);
    }

    @Test
    void getPropertyDescriptors() throws Exception {
        PropertyDescriptor[] actual = Introspector.getBeanInfo(TestBean.class).getPropertyDescriptors();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(TestBean.class);
        assertThat(descriptors).as("Descriptors should not be null").isNotNull();
        assertThat(descriptors).as("Invalid number of descriptors returned").hasSameSizeAs(actual);
    }

    @Test
    void beanPropertyIsArray() {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(ContainerBean.class);
        for (PropertyDescriptor descriptor : descriptors) {
            if ("containedBeans".equals(descriptor.getName())) {
                assertThat(descriptor.getPropertyType().isArray()).as("Property should be an array").isTrue();
                assertThat(ContainedBean.class).isEqualTo(descriptor.getPropertyType().getComponentType());
            }
        }
    }

    @Test
    void findEditorByConvention() {
        assertThat(BeanUtils.findEditorByConvention(Resource.class).getClass()).isEqualTo(ResourceEditor.class);
    }

    @Test
    void copyProperties() throws Exception {
        TestBean tb = new TestBean();
        tb.setName("rod");
        tb.setAge(32);
        tb.setTouchy("touchy");
        TestBean tb2 = new TestBean();
        assertThat(tb2.getName()).as("Name empty").isNull();
        assertThat(tb2.getAge()).as("Age empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy empty").isNull();
        BeanUtils.copyProperties(tb, tb2);
        assertThat(tb2.getName()).as("Name copied").isEqualTo(tb.getName());
        assertThat(tb2.getAge()).as("Age copied").isEqualTo(tb.getAge());
        assertThat(tb2.getTouchy()).as("Touchy copied").isEqualTo(tb.getTouchy());
    }

    @Test
    void copyPropertiesWithDifferentTypes1() throws Exception {
        DerivedTestBean tb = new DerivedTestBean();
        tb.setName("rod");
        tb.setAge(32);
        tb.setTouchy("touchy");
        TestBean tb2 = new TestBean();
        assertThat(tb2.getName()).as("Name empty").isNull();
        assertThat(tb2.getAge()).as("Age empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy empty").isNull();
        BeanUtils.copyProperties(tb, tb2);
        assertThat(tb2.getName()).as("Name copied").isEqualTo(tb.getName());
        assertThat(tb2.getAge()).as("Age copied").isEqualTo(tb.getAge());
        assertThat(tb2.getTouchy()).as("Touchy copied").isEqualTo(tb.getTouchy());
    }

    @Test
    void copyPropertiesWithDifferentTypes2() throws Exception {
        TestBean tb = new TestBean();
        tb.setName("rod");
        tb.setAge(32);
        tb.setTouchy("touchy");
        DerivedTestBean tb2 = new DerivedTestBean();
        assertThat(tb2.getName()).as("Name empty").isNull();
        assertThat(tb2.getAge()).as("Age empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy empty").isNull();
        BeanUtils.copyProperties(tb, tb2);
        assertThat(tb2.getName()).as("Name copied").isEqualTo(tb.getName());
        assertThat(tb2.getAge()).as("Age copied").isEqualTo(tb.getAge());
        assertThat(tb2.getTouchy()).as("Touchy copied").isEqualTo(tb.getTouchy());
    }

    /**
     * 将 {@code Integer} 可以复制到 {@code Number}。
     */
    @Test
    void copyPropertiesFromSubTypeToSuperType() {
        IntegerHolder integerHolder = new IntegerHolder();
        integerHolder.setNumber(42);
        NumberHolder numberHolder = new NumberHolder();
        BeanUtils.copyProperties(integerHolder, numberHolder);
        assertThat(integerHolder.getNumber()).isEqualTo(42);
        assertThat(numberHolder.getNumber()).isEqualTo(42);
    }

    /**
     * 可以将 {@code List<Integer>} 复制到 {@code List<Integer>}。
     */
    @Test
    void copyPropertiesHonorsGenericTypeMatchesFromIntegerToInteger() {
        IntegerListHolder1 integerListHolder1 = new IntegerListHolder1();
        integerListHolder1.getList().add(42);
        IntegerListHolder2 integerListHolder2 = new IntegerListHolder2();
        BeanUtils.copyProperties(integerListHolder1, integerListHolder2);
        assertThat(integerListHolder1.getList()).containsExactly(42);
        assertThat(integerListHolder2.getList()).containsExactly(42);
    }

    /**
     * 可以将 {@code List<?>} 复制到 {@code List<?>}。
     */
    @Test
    void copyPropertiesHonorsGenericTypeMatchesFromWildcardToWildcard() {
        List<?> list = List.of("foo", 42);
        WildcardListHolder1 wildcardListHolder1 = new WildcardListHolder1();
        wildcardListHolder1.setList(list);
        WildcardListHolder2 wildcardListHolder2 = new WildcardListHolder2();
        assertThat(wildcardListHolder2.getList()).isEmpty();
        BeanUtils.copyProperties(wildcardListHolder1, wildcardListHolder2);
        assertThat(wildcardListHolder1.getList()).isEqualTo(list);
        assertThat(wildcardListHolder2.getList()).isEqualTo(list);
    }

    /**
     * 可以将 {@code List<Integer>} 复制到 {@code List<?>}。
     */
    @Test
    void copyPropertiesHonorsGenericTypeMatchesFromIntegerToWildcard() {
        IntegerListHolder1 integerListHolder1 = new IntegerListHolder1();
        integerListHolder1.getList().add(42);
        WildcardListHolder2 wildcardListHolder2 = new WildcardListHolder2();
        BeanUtils.copyProperties(integerListHolder1, wildcardListHolder2);
        assertThat(integerListHolder1.getList()).containsExactly(42);
        assertThat(wildcardListHolder2.getList()).isEqualTo(List.of(42));
    }

    /**
     * 可以将 {@code List<Integer>} 复制到 {@code List<? extends Number>}。
     */
    @Test
    void copyPropertiesHonorsGenericTypeMatchesForUpperBoundedWildcard() {
        IntegerListHolder1 integerListHolder1 = new IntegerListHolder1();
        integerListHolder1.getList().add(42);
        NumberUpperBoundedWildcardListHolder numberListHolder = new NumberUpperBoundedWildcardListHolder();
        BeanUtils.copyProperties(integerListHolder1, numberListHolder);
        assertThat(integerListHolder1.getList()).containsExactly(42);
        assertThat(numberListHolder.getList()).isEqualTo(List.of(42));
    }

    /**
     * `Number` 不能复制到 `Integer`。
     */
    @Test
    void copyPropertiesDoesNotCopyFromSuperTypeToSubType() {
        NumberHolder numberHolder = new NumberHolder();
        numberHolder.setNumber(42);
        IntegerHolder integerHolder = new IntegerHolder();
        BeanUtils.copyProperties(numberHolder, integerHolder);
        assertThat(numberHolder.getNumber()).isEqualTo(42);
        assertThat(integerHolder.getNumber()).isNull();
    }

    /**
     * 不能将 {@code List<Integer>} 复制到 {@code List<Long>}。
     */
    @Test
    void copyPropertiesDoesNotHonorGenericTypeMismatches() {
        IntegerListHolder1 integerListHolder = new IntegerListHolder1();
        integerListHolder.getList().add(42);
        LongListHolder longListHolder = new LongListHolder();
        BeanUtils.copyProperties(integerListHolder, longListHolder);
        assertThat(integerListHolder.getList()).containsExactly(42);
        assertThat(longListHolder.getList()).isEmpty();
    }

    /**
     * `List<Integer>` 不能复制到 `List<Number>`。
     */
    @Test
    void copyPropertiesDoesNotHonorGenericTypeMismatchesFromSubTypeToSuperType() {
        IntegerListHolder1 integerListHolder = new IntegerListHolder1();
        integerListHolder.getList().add(42);
        NumberListHolder numberListHolder = new NumberListHolder();
        BeanUtils.copyProperties(integerListHolder, numberListHolder);
        assertThat(integerListHolder.getList()).containsExactly(42);
        assertThat(numberListHolder.getList()).isEmpty();
    }

    // 很抱歉，您提供的 "gh-26531" 并不是一个有效的 Java 代码注释内容。它看起来像是一个 GitHub pull request 或 issue 的编号。如果您能提供具体的 Java 代码注释内容，我将很乐意将其翻译成中文。
    @Test
    void copyPropertiesIgnoresGenericsIfSourceOrTargetHasUnresolvableGenerics() throws Exception {
        Order original = new Order("test", List.of("foo", "bar"));
        // 创建一个代理，使其getLineItems()方法的泛型类型信息丢失。
        OrderSummary proxy = (OrderSummary) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { OrderSummary.class }, new OrderInvocationHandler(original));
        assertThat(OrderSummary.class.getDeclaredMethod("getLineItems").toGenericString()).contains("java.util.List<java.lang.String>");
        assertThat(proxy.getClass().getDeclaredMethod("getLineItems").toGenericString()).contains("java.util.List").doesNotContain("<java.lang.String>");
        // 确保我们的自定义代理按预期工作。
        assertThat(proxy.getId()).isEqualTo("test");
        assertThat(proxy.getLineItems()).containsExactly("foo", "bar");
        // 从代理复制到目标。
        Order target = new Order();
        BeanUtils.copyProperties(proxy, target);
        assertThat(target.getId()).isEqualTo("test");
        assertThat(target.getLineItems()).containsExactly("foo", "bar");
    }

    // 很抱歉，您提供的 "gh-32888" 并不是一个 Java 代码注释的内容，它看起来更像是 GitHub 上某个 pull request 或 issue 的编号。由于没有提供实际的代码注释内容，我无法进行翻译。如果您能提供具体的 Java 代码注释内容，我会很乐意帮助您将其翻译成中文。
    @Test
    public void copyPropertiesWithGenericCglibClass() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(User.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> proxy.invokeSuper(obj, args));
        User user = (User) enhancer.create();
        user.setId(1);
        user.setName("proxy");
        user.setAddress("addr");
        User target = new User();
        BeanUtils.copyProperties(user, target);
        assertThat(target.getId()).isEqualTo(user.getId());
        assertThat(target.getName()).isEqualTo(user.getName());
        assertThat(target.getAddress()).isEqualTo(user.getAddress());
    }

    @Test
    void copyPropertiesWithEditable() throws Exception {
        TestBean tb = new TestBean();
        assertThat(tb.getName()).as("Name empty").isNull();
        tb.setAge(32);
        tb.setTouchy("bla");
        TestBean tb2 = new TestBean();
        tb2.setName("rod");
        assertThat(tb2.getAge()).as("Age empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy empty").isNull();
        // "touchy" 不应被复制：它未在 ITestBean 中定义
        BeanUtils.copyProperties(tb, tb2, ITestBean.class);
        assertThat(tb2.getName()).as("Name copied").isNull();
        assertThat(tb2.getAge()).as("Age copied").isEqualTo(32);
        assertThat(tb2.getTouchy()).as("Touchy still empty").isNull();
    }

    @Test
    void copyPropertiesWithIgnore() throws Exception {
        TestBean tb = new TestBean();
        assertThat(tb.getName()).as("Name empty").isNull();
        tb.setAge(32);
        tb.setTouchy("bla");
        TestBean tb2 = new TestBean();
        tb2.setName("rod");
        assertThat(tb2.getAge()).as("Age empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy empty").isNull();
        // "配偶"、"敏感"、"年龄"不应被复制
        BeanUtils.copyProperties(tb, tb2, "spouse", "touchy", "age");
        assertThat(tb2.getName()).as("Name copied").isNull();
        assertThat(tb2.getAge()).as("Age still empty").isEqualTo(0);
        assertThat(tb2.getTouchy()).as("Touchy still empty").isNull();
    }

    @Test
    void copyPropertiesWithIgnoredNonExistingProperty() {
        NameAndSpecialProperty source = new NameAndSpecialProperty();
        source.setName("name");
        TestBean target = new TestBean();
        BeanUtils.copyProperties(source, target, "specialProperty");
        assertThat(target.getName()).isEqualTo("name");
    }

    @Test
    void copyPropertiesWithInvalidProperty() {
        InvalidProperty source = new InvalidProperty();
        source.setName("name");
        source.setFlag1(true);
        source.setFlag2(true);
        InvalidProperty target = new InvalidProperty();
        BeanUtils.copyProperties(source, target);
        assertThat(target.getName()).isEqualTo("name");
        assertThat((boolean) target.getFlag1()).isTrue();
        assertThat(target.getFlag2()).isTrue();
    }

    @Test
    void resolveSimpleSignature() throws Exception {
        Method desiredMethod = MethodSignatureBean.class.getMethod("doSomething");
        assertSignatureEquals(desiredMethod, "doSomething");
        assertSignatureEquals(desiredMethod, "doSomething()");
    }

    @Test
    void resolveInvalidSignatureEndParen() {
        assertThatIllegalArgumentException().isThrownBy(() -> BeanUtils.resolveSignature("doSomething(", MethodSignatureBean.class));
    }

    @Test
    void resolveInvalidSignatureStartParen() {
        assertThatIllegalArgumentException().isThrownBy(() -> BeanUtils.resolveSignature("doSomething)", MethodSignatureBean.class));
    }

    @Test
    void resolveWithAndWithoutArgList() throws Exception {
        Method desiredMethod = MethodSignatureBean.class.getMethod("doSomethingElse", String.class, int.class);
        assertSignatureEquals(desiredMethod, "doSomethingElse");
        assertThat(BeanUtils.resolveSignature("doSomethingElse()", MethodSignatureBean.class)).isNull();
    }

    @Test
    void resolveTypedSignature() throws Exception {
        Method desiredMethod = MethodSignatureBean.class.getMethod("doSomethingElse", String.class, int.class);
        assertSignatureEquals(desiredMethod, "doSomethingElse(java.lang.String, int)");
    }

    @Test
    void resolveOverloadedSignature() throws Exception {
        // 测试无参数的解析
        Method desiredMethod = MethodSignatureBean.class.getMethod("overloaded");
        assertSignatureEquals(desiredMethod, "overloaded()");
        // 使用单个参数解析
        desiredMethod = MethodSignatureBean.class.getMethod("overloaded", String.class);
        assertSignatureEquals(desiredMethod, "overloaded(java.lang.String)");
        // 使用两个参数解决
        desiredMethod = MethodSignatureBean.class.getMethod("overloaded", String.class, BeanFactory.class);
        assertSignatureEquals(desiredMethod, "overloaded(java.lang.String, org.springframework.beans.factory.BeanFactory)");
    }

    @Test
    void resolveSignatureWithArray() throws Exception {
        Method desiredMethod = MethodSignatureBean.class.getMethod("doSomethingWithAnArray", String[].class);
        assertSignatureEquals(desiredMethod, "doSomethingWithAnArray(java.lang.String[])");
        desiredMethod = MethodSignatureBean.class.getMethod("doSomethingWithAMultiDimensionalArray", String[][].class);
        assertSignatureEquals(desiredMethod, "doSomethingWithAMultiDimensionalArray(java.lang.String[][])");
    }

    @Test
    void spr6063() {
        PropertyDescriptor[] descrs = BeanUtils.getPropertyDescriptors(Bean.class);
        PropertyDescriptor keyDescr = BeanUtils.getPropertyDescriptor(Bean.class, "value");
        assertThat(keyDescr.getPropertyType()).isEqualTo(String.class);
        for (PropertyDescriptor propertyDescriptor : descrs) {
            if (propertyDescriptor.getName().equals(keyDescr.getName())) {
                assertThat(propertyDescriptor.getPropertyType()).as(propertyDescriptor.getName() + " has unexpected type").isEqualTo(keyDescr.getPropertyType());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(classes = { boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, DayOfWeek.class, String.class, LocalDateTime.class, Date.class, URI.class, URL.class, Locale.class, Class.class })
    void isSimpleValueType(Class<?> type) {
        assertThat(BeanUtils.isSimpleValueType(type)).as("Type [" + type.getName() + "] should be a simple value type").isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = { int[].class, Object.class, List.class, void.class, Void.class })
    void isNotSimpleValueType(Class<?> type) {
        assertThat(BeanUtils.isSimpleValueType(type)).as("Type [" + type.getName() + "] should not be a simple value type").isFalse();
    }

    @ParameterizedTest
    @ValueSource(classes = { boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, DayOfWeek.class, String.class, LocalDateTime.class, Date.class, URI.class, URL.class, Locale.class, Class.class, boolean[].class, Boolean[].class, LocalDateTime[].class, Date[].class })
    void isSimpleProperty(Class<?> type) {
        assertThat(BeanUtils.isSimpleProperty(type)).as("Type [" + type.getName() + "] should be a simple property").isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = { Object.class, List.class, void.class, Void.class })
    void isNotSimpleProperty(Class<?> type) {
        assertThat(BeanUtils.isSimpleProperty(type)).as("Type [" + type.getName() + "] should not be a simple property").isFalse();
    }

    private void assertSignatureEquals(Method desiredMethod, String signature) {
        assertThat(BeanUtils.resolveSignature(signature, MethodSignatureBean.class)).isEqualTo(desiredMethod);
    }

    @SuppressWarnings("unused")
    private static class NumberHolder {

        private Number number;

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }
    }

    @SuppressWarnings("unused")
    private static class IntegerHolder {

        private Integer number;

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }

    @SuppressWarnings("unused")
    private static class WildcardListHolder1 {

        private List<?> list = new ArrayList<>();

        public List<?> getList() {
            return list;
        }

        public void setList(List<?> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class WildcardListHolder2 {

        private List<?> list = new ArrayList<>();

        public List<?> getList() {
            return list;
        }

        public void setList(List<?> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class NumberUpperBoundedWildcardListHolder {

        private List<? extends Number> list = new ArrayList<>();

        public List<? extends Number> getList() {
            return list;
        }

        public void setList(List<? extends Number> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class NumberListHolder {

        private List<Number> list = new ArrayList<>();

        public List<Number> getList() {
            return list;
        }

        public void setList(List<Number> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class IntegerListHolder1 {

        private List<Integer> list = new ArrayList<>();

        public List<Integer> getList() {
            return list;
        }

        public void setList(List<Integer> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class IntegerListHolder2 {

        private List<Integer> list = new ArrayList<>();

        public List<Integer> getList() {
            return list;
        }

        public void setList(List<Integer> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class LongListHolder {

        private List<Long> list = new ArrayList<>();

        public List<Long> getList() {
            return list;
        }

        public void setList(List<Long> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("unused")
    private static class NameAndSpecialProperty {

        private String name;

        private int specialProperty;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setSpecialProperty(int specialProperty) {
            this.specialProperty = specialProperty;
        }

        public int getSpecialProperty() {
            return specialProperty;
        }
    }

    @SuppressWarnings("unused")
    private static class InvalidProperty {

        private String name;

        private String value;

        private boolean flag1;

        private boolean flag2;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setValue(int value) {
            this.value = Integer.toString(value);
        }

        public String getValue() {
            return this.value;
        }

        public void setFlag1(boolean flag1) {
            this.flag1 = flag1;
        }

        public Boolean getFlag1() {
            return this.flag1;
        }

        public void setFlag2(Boolean flag2) {
            this.flag2 = flag2;
        }

        public boolean getFlag2() {
            return this.flag2;
        }
    }

    @SuppressWarnings("unused")
    private static class ContainerBean {

        private ContainedBean[] containedBeans;

        public ContainedBean[] getContainedBeans() {
            return containedBeans;
        }

        public void setContainedBeans(ContainedBean[] containedBeans) {
            this.containedBeans = containedBeans;
        }
    }

    @SuppressWarnings("unused")
    private static class ContainedBean {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    private static class MethodSignatureBean {

        public void doSomething() {
        }

        public void doSomethingElse(String s, int x) {
        }

        public void overloaded() {
        }

        public void overloaded(String s) {
        }

        public void overloaded(String s, BeanFactory beanFactory) {
        }

        public void doSomethingWithAnArray(String[] strings) {
        }

        public void doSomethingWithAMultiDimensionalArray(String[][] strings) {
        }
    }

    private interface MapEntry<K, V> {

        K getKey();

        void setKey(V value);

        V getValue();

        void setValue(V value);
    }

    private static class Bean implements MapEntry<String, String> {

        private String key;

        private String value;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void setKey(String aKey) {
            key = aKey;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String aValue) {
            value = aValue;
        }
    }

    private static class BeanWithNullableTypes {

        private Integer counter;

        private Boolean flag;

        private String value;

        @SuppressWarnings("unused")
        public BeanWithNullableTypes(@Nullable Integer counter, @Nullable Boolean flag, String value) {
            this.counter = counter;
            this.flag = flag;
            this.value = value;
        }

        @Nullable
        public Integer getCounter() {
            return counter;
        }

        @Nullable
        public Boolean isFlag() {
            return flag;
        }

        public String getValue() {
            return value;
        }
    }

    private static class BeanWithPrimitiveTypes {

        private boolean flag;

        private byte byteCount;

        private short shortCount;

        private int intCount;

        private long longCount;

        private float floatCount;

        private double doubleCount;

        private char character;

        private String text;

        @SuppressWarnings("unused")
        public BeanWithPrimitiveTypes(boolean flag, byte byteCount, short shortCount, int intCount, long longCount, float floatCount, double doubleCount, char character, String text) {
            this.flag = flag;
            this.byteCount = byteCount;
            this.shortCount = shortCount;
            this.intCount = intCount;
            this.longCount = longCount;
            this.floatCount = floatCount;
            this.doubleCount = doubleCount;
            this.character = character;
            this.text = text;
        }

        public boolean isFlag() {
            return flag;
        }

        public byte getByteCount() {
            return byteCount;
        }

        public short getShortCount() {
            return shortCount;
        }

        public int getIntCount() {
            return intCount;
        }

        public long getLongCount() {
            return longCount;
        }

        public float getFloatCount() {
            return floatCount;
        }

        public double getDoubleCount() {
            return doubleCount;
        }

        public char getCharacter() {
            return character;
        }

        public String getText() {
            return text;
        }
    }

    private static class PrivateBeanWithPrivateConstructor {

        private PrivateBeanWithPrivateConstructor() {
        }
    }

    @SuppressWarnings("unused")
    private static class Order {

        private String id;

        private List<String> lineItems;

        Order() {
        }

        Order(String id, List<String> lineItems) {
            this.id = id;
            this.lineItems = lineItems;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getLineItems() {
            return this.lineItems;
        }

        public void setLineItems(List<String> lineItems) {
            this.lineItems = lineItems;
        }

        @Override
        public String toString() {
            return "Order [id=" + this.id + ", lineItems=" + this.lineItems + "]";
        }
    }

    private interface OrderSummary {

        String getId();

        List<String> getLineItems();
    }

    private static class OrderInvocationHandler implements InvocationHandler {

        private final Order order;

        OrderInvocationHandler(Order order) {
            this.order = order;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                // 忽略参数，因为 OrderSummary 没有声明任何带参数的方法。
                // 我们不支持equals(Object)等。
                return Order.class.getDeclaredMethod(method.getName()).invoke(this.order);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

    private static class GenericBaseModel<T> {

        private T id;

        private String name;

        public T getId() {
            return id;
        }

        public void setId(T id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class User extends GenericBaseModel<Integer> {

        private String address;

        public User() {
            super();
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
