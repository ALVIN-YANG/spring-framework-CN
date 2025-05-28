// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 *
 * 作者：Chris Beams
 * 作者：Juergen Hoeller
 * 作者：Sam Brannen
 * 自3.1版本以来
 */
class ExtendedBeanInfoTests {

    @Test
    void standardReadMethodOnly() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public String getFoo() {
                return null;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isFalse();
    }

    @Test
    void standardWriteMethodOnly() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public void setFoo(String f) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void standardReadAndWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public void setFoo(String f) {
            }

            public String getFoo() {
                return null;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void nonStandardWriteMethodOnly() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public C setFoo(String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void standardReadAndNonStandardWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public String getFoo() {
                return null;
            }

            public C setFoo(String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void standardReadAndNonStandardIndexedWriteMethod() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public String[] getFoo() {
                return null;
            }

            public C setFoo(int i, String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foo")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void standardReadMethodsAndOverloadedNonStandardWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public String getFoo() {
                return null;
            }

            public C setFoo(String foo) {
                return this;
            }

            public C setFoo(Number foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
        for (PropertyDescriptor pd : ebi.getPropertyDescriptors()) {
            if (pd.getName().equals("foo")) {
                assertThat(pd.getWriteMethod()).isEqualTo(C.class.getMethod("setFoo", String.class));
                return;
            }
        }
        throw new AssertionError("never matched write method");
    }

    @Test
    void cornerSpr9414() throws Exception {
        @SuppressWarnings("unused")
        class Parent {

            public Number getProperty1() {
                return 1;
            }
        }
        class Child extends Parent {

            @Override
            public Integer getProperty1() {
                return 2;
            }
        }
        {
            // 始终通过
            ExtendedBeanInfo bi = new ExtendedBeanInfo(Introspector.getBeanInfo(Parent.class));
            assertThat(hasReadMethodForProperty(bi, "property1")).isTrue();
        }
        {
            // 在修复SPR-9414之前失败
            ExtendedBeanInfo bi = new ExtendedBeanInfo(Introspector.getBeanInfo(Child.class));
            assertThat(hasReadMethodForProperty(bi, "property1")).isTrue();
        }
    }

    @Test
    void cornerSpr9453() throws Exception {
        class Bean implements Spr9453<Class<?>> {

            @Override
            public Class<?> getProp() {
                return null;
            }
        }
        {
            // 总是传递
            BeanInfo info = Introspector.getBeanInfo(Bean.class);
            assertThat(info.getPropertyDescriptors()).hasSize(2);
        }
        {
            // 在修复 SPR-9453 问题之前失败
            BeanInfo info = new ExtendedBeanInfo(Introspector.getBeanInfo(Bean.class));
            assertThat(info.getPropertyDescriptors()).hasSize(2);
        }
    }

    @Test
    void standardReadMethodInSuperclassAndNonStandardWriteMethodInSubclass() throws Exception {
        @SuppressWarnings("unused")
        class B {

            public String getFoo() {
                return null;
            }
        }
        @SuppressWarnings("unused")
        class C extends B {

            public C setFoo(String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    @Test
    void standardReadMethodInSuperAndSubclassesAndGenericBuilderStyleNonStandardWriteMethodInSuperAndSubclasses() throws Exception {
        abstract class B<This extends B<This>> {

            @SuppressWarnings("unchecked")
            protected final This instance = (This) this;

            private String foo;

            public String getFoo() {
                return foo;
            }

            public This setFoo(String foo) {
                this.foo = foo;
                return this.instance;
            }
        }
        class C extends B<C> {

            private int bar = -1;

            public int getBar() {
                return bar;
            }

            public C setBar(int bar) {
                this.bar = bar;
                return this.instance;
            }
        }
        C c = new C().setFoo("blue").setBar(42);
        assertThat(c.getFoo()).isEqualTo("blue");
        assertThat(c.getBar()).isEqualTo(42);
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasReadMethodForProperty(bi, "bar")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "bar")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "bar")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "bar")).isTrue();
    }

    @Test
    void nonPublicStandardReadAndWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            String getFoo() {
                return null;
            }

            C setFoo(String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isFalse();
    }

    /**
     * `@link ExtendedBeanInfo` 应该在奇怪的边缘情况下表现得与 `@link BeanInfo` 完全一样。
     */
    @Test
    void readMethodReturnsSupertypeOfWriteMethodParameter() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Number getFoo() {
                return null;
            }

            public void setFoo(Integer foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isEqualTo(hasWriteMethodForProperty(bi, "foo"));
    }

    @Test
    void indexedReadMethodReturnsSupertypeOfIndexedWriteMethodParameter() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Number getFoos(int index) {
                return null;
            }

            public void setFoos(int index, Integer foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isEqualTo(hasIndexedWriteMethodForProperty(bi, "foos"));
    }

    /**
     * `@link ExtendedBeanInfo` 应该在奇怪的边缘情况下表现得与 `@link BeanInfo` 完全一样。
     */
    @Test
    void readMethodReturnsSubtypeOfWriteMethodParameter() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Integer getFoo() {
                return null;
            }

            public void setFoo(Number foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isFalse();
    }

    @Test
    void indexedReadMethodReturnsSubtypeOfIndexedWriteMethodParameter() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Integer getFoos(int index) {
                return null;
            }

            public void setFoo(int index, Number foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isFalse();
    }

    @Test
    void indexedReadMethodOnly() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 索引读取方法
            public String getFoos(int i) {
                return null;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasReadMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void indexedWriteMethodOnly() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 索引写入方法
            public void setFoos(int i, String foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasWriteMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foos")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void indexedReadAndIndexedWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 索引读取方法
            public String getFoos(int i) {
                return null;
            }

            // 索引写入方法
            public void setFoos(int i, String foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasReadMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foos")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void readAndWriteAndIndexedReadAndIndexedWriteMethods() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 读取方法
            public String[] getFoos() {
                return null;
            }

            // 索引读取方法
            public String getFoos(int i) {
                return null;
            }

            // 编写方法
            public void setFoos(String[] foos) {
            }

            // 索引写入方法
            public void setFoos(int i, String foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void indexedReadAndNonStandardIndexedWrite() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 索引读取方法
            public String getFoos(int i) {
                return null;
            }

            // 非标准索引写入方法
            public C setFoos(int i, String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        // 有趣！默认情况下，标准检查器会检测到索引写入方法上的非void返回类型。
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void indexedReadAndNonStandardWriteAndNonStandardIndexedWrite() throws Exception {
        @SuppressWarnings("unused")
        class C {

            // 非标准写入方法
            public C setFoos(String[] foos) {
                return this;
            }

            // 索引读取方法
            public String getFoos(int i) {
                return null;
            }

            // 非标准索引写入方法
            public C setFoos(int i, String foo) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foos")).isFalse();
        // 就像上面一样，默认情况下，标准的Inspector会捕获索引写入方法上的非void返回类型
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
        assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foos")).isTrue();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isTrue();
    }

    @Test
    void cornerSpr9702() throws Exception {
        {
            // 基准与标准写入方法
            @SuppressWarnings("unused")
            class C {

                // 无返回值、非索引的写入方法
                public void setFoos(String[] foos) {
                }

                // 索引读取方法
                public String getFoos(int i) {
                    return null;
                }
            }
            BeanInfo bi = Introspector.getBeanInfo(C.class);
            assertThat(hasReadMethodForProperty(bi, "foos")).isFalse();
            assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
            assertThat(hasWriteMethodForProperty(bi, "foos")).isTrue();
            assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
            BeanInfo ebi = Introspector.getBeanInfo(C.class);
            assertThat(hasReadMethodForProperty(ebi, "foos")).isFalse();
            assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
            assertThat(hasWriteMethodForProperty(ebi, "foos")).isTrue();
            assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isFalse();
        }
        {
            // 具有非标准写入方法的变体
            @SuppressWarnings("unused")
            class C {

                // 非返回值、非索引的写入方法
                public C setFoos(String[] foos) {
                    return this;
                }

                // 索引读取方法
                public String getFoos(int i) {
                    return null;
                }
            }
            BeanInfo bi = Introspector.getBeanInfo(C.class);
            assertThat(hasReadMethodForProperty(bi, "foos")).isFalse();
            assertThat(hasIndexedReadMethodForProperty(bi, "foos")).isTrue();
            assertThat(hasWriteMethodForProperty(bi, "foos")).isFalse();
            assertThat(hasIndexedWriteMethodForProperty(bi, "foos")).isFalse();
            BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));
            assertThat(hasReadMethodForProperty(ebi, "foos")).isFalse();
            assertThat(hasIndexedReadMethodForProperty(ebi, "foos")).isTrue();
            assertThat(hasWriteMethodForProperty(ebi, "foos")).isTrue();
            assertThat(hasIndexedWriteMethodForProperty(ebi, "foos")).isFalse();
        }
    }

    /**
     * 在SPR-10111（SPR-9702的后续修复）之前，此方法会因`Class.getDeclareMethods()`的非确定性结果而在JDK 7下间歇性地抛出关于“索引和非索引方法之间类型不匹配”的`IntrospectionException`（大约每四次发生一次）。
     * 请参阅 https://bugs.java.com/bugdatabase/view_bug.do?bug_id=7023180
     * @see #cornerSpr9702()
     */
    @Test
    void cornerSpr10111() throws Exception {
        assertThatNoException().isThrownBy(() -> new ExtendedBeanInfo(Introspector.getBeanInfo(BigDecimal.class)));
    }

    @Test
    void subclassWriteMethodWithCovariantReturnType() throws Exception {
        @SuppressWarnings("unused")
        class B {

            public String getFoo() {
                return null;
            }

            public Number setFoo(String foo) {
                return null;
            }
        }
        class C extends B {

            @Override
            public String getFoo() {
                return null;
            }

            @Override
            public Integer setFoo(String foo) {
                return null;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
        assertThat(ebi.getPropertyDescriptors()).hasSameSizeAs(bi.getPropertyDescriptors());
    }

    @Test
    void nonStandardReadMethodAndStandardWriteMethod() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public void getFoo() {
            }

            public void setFoo(String foo) {
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isTrue();
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
    }

    /**
     * 确保不会将空字符串传递给 PropertyDescriptor 构造函数。这可能在处理 ArrayList.set(int,Object) 时发生
     */
    @Test
    void emptyPropertiesIgnored() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Object set(Object o) {
                return null;
            }

            public Object set(int i, Object o) {
                return null;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(ebi.getPropertyDescriptors()).isEqualTo(bi.getPropertyDescriptors());
    }

    @Test
    void overloadedNonStandardWriteMethodsOnly_orderA() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Object setFoo(String p) {
                return new Object();
            }

            public Object setFoo(int p) {
                return new Object();
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
        for (PropertyDescriptor pd : ebi.getPropertyDescriptors()) {
            if (pd.getName().equals("foo")) {
                assertThat(pd.getWriteMethod()).isEqualTo(C.class.getMethod("setFoo", String.class));
                return;
            }
        }
        throw new AssertionError("never matched write method");
    }

    @Test
    void overloadedNonStandardWriteMethodsOnly_orderB() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Object setFoo(int p) {
                return new Object();
            }

            public Object setFoo(String p) {
                return new Object();
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "foo")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "foo")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "foo")).isTrue();
        for (PropertyDescriptor pd : ebi.getPropertyDescriptors()) {
            if (pd.getName().equals("foo")) {
                assertThat(pd.getWriteMethod()).isEqualTo(C.class.getMethod("setFoo", String.class));
                return;
            }
        }
        throw new AssertionError("never matched write method");
    }

    /**
     * 封堵了由 SPR-8522 揭示的bug，该bug中一个（表面上）带索引的写方法没有对应的带索引的读方法，导致 ExtendedBeanInfo 无法正确处理。下面的本地类C代表了来自Google的 GsonBuilder 类的相关方法。有趣的是，setDateFormat(int, int) 方法实际上并不是作为带索引的写方法设计的；只是看起来是这样。
     */
    @Test
    void reproSpr8522() throws Exception {
        @SuppressWarnings("unused")
        class C {

            public Object setDateFormat(String pattern) {
                return new Object();
            }

            public Object setDateFormat(int style) {
                return new Object();
            }

            public Object setDateFormat(int dateStyle, int timeStyle) {
                return new Object();
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(C.class);
        assertThat(hasReadMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "dateFormat")).isFalse();
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasWriteMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasIndexedReadMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "dateFormat")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "dateFormat")).isFalse();
        assertThat(hasWriteMethodForProperty(ebi, "dateFormat")).isTrue();
        assertThat(hasIndexedReadMethodForProperty(ebi, "dateFormat")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(ebi, "dateFormat")).isFalse();
    }

    @Test
    void propertyCountsMatch() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(TestBean.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(ebi.getPropertyDescriptors()).hasSameSizeAs(bi.getPropertyDescriptors());
    }

    @Test
    void propertyCountsWithNonStandardWriteMethod() throws Exception {
        class ExtendedTestBean extends TestBean {

            @SuppressWarnings("unused")
            public ExtendedTestBean setFoo(String s) {
                return this;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(ExtendedTestBean.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        boolean found = false;
        for (PropertyDescriptor pd : ebi.getPropertyDescriptors()) {
            if (pd.getName().equals("foo")) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
        assertThat(ebi.getPropertyDescriptors()).hasSize(bi.getPropertyDescriptors().length + 1);
    }

    /**
     * `@link BeanInfo#getPropertyDescriptors()` 返回按字母数字排序。
     * 测试 `@link ExtendedBeanInfo#getPropertyDescriptors()` 是否也进行相同的排序。
     */
    @Test
    void propertyDescriptorOrderIsEqual() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(TestBean.class);
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        for (int i = 0; i < bi.getPropertyDescriptors().length; i++) {
            assertThat(ebi.getPropertyDescriptors()[i].getName()).isEqualTo(bi.getPropertyDescriptors()[i].getName());
        }
    }

    @Test
    void propertyDescriptorComparator() throws Exception {
        ExtendedBeanInfo.PropertyDescriptorComparator c = new ExtendedBeanInfo.PropertyDescriptorComparator();
        assertThat(c.compare(new PropertyDescriptor("a", null, null), new PropertyDescriptor("a", null, null))).isEqualTo(0);
        assertThat(c.compare(new PropertyDescriptor("abc", null, null), new PropertyDescriptor("abc", null, null))).isEqualTo(0);
        assertThat(c.compare(new PropertyDescriptor("a", null, null), new PropertyDescriptor("b", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor("b", null, null), new PropertyDescriptor("a", null, null))).isGreaterThan(0);
        assertThat(c.compare(new PropertyDescriptor("abc", null, null), new PropertyDescriptor("abd", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor("xyz", null, null), new PropertyDescriptor("123", null, null))).isGreaterThan(0);
        assertThat(c.compare(new PropertyDescriptor("a", null, null), new PropertyDescriptor("abc", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor("abc", null, null), new PropertyDescriptor("a", null, null))).isGreaterThan(0);
        assertThat(c.compare(new PropertyDescriptor("abc", null, null), new PropertyDescriptor("b", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor(" ", null, null), new PropertyDescriptor("a", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor("1", null, null), new PropertyDescriptor("a", null, null))).isLessThan(0);
        assertThat(c.compare(new PropertyDescriptor("a", null, null), new PropertyDescriptor("A", null, null))).isGreaterThan(0);
    }

    @Test
    void reproSpr8806() throws Exception {
        // 不会抛出异常
        Introspector.getBeanInfo(LawLibrary.class);
        // 在SPR-8806引入的更改之后不会抛出异常
        new ExtendedBeanInfo(Introspector.getBeanInfo(LawLibrary.class));
    }

    @Test
    void cornerSpr8949() throws Exception {
        class A {

            @SuppressWarnings("unused")
            public boolean isTargetMethod() {
                return false;
            }
        }
        class B extends A {

            @Override
            public boolean isTargetMethod() {
                return false;
            }
        }
        BeanInfo bi = Introspector.getBeanInfo(B.class);
        // java.beans.Introspector 返回了被重写 read 方法的“错误”声明类
        // 方法，这反过来违反了在 {@link ExtendedBeanInfo} 中的期望
        // 方法等价性。Spring 的 {@link ClassUtils#getMostSpecificMethod(Method, Class)}
        // 在这里提供帮助，现在也被用在 ExtendedBeanInfo 中。
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(bi, "targetMethod")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "targetMethod")).isFalse();
        assertThat(hasReadMethodForProperty(ebi, "targetMethod")).isTrue();
        assertThat(hasWriteMethodForProperty(ebi, "targetMethod")).isFalse();
    }

    @Test
    void cornerSpr8937AndSpr12582() throws Exception {
        @SuppressWarnings("unused")
        class A {

            public void setAddress(String addr) {
            }

            public void setAddress(int index, String addr) {
            }

            public String getAddress(int index) {
                return null;
            }
        }
        // 基准：
        BeanInfo bi = Introspector.getBeanInfo(A.class);
        boolean hasReadMethod = hasReadMethodForProperty(bi, "address");
        boolean hasWriteMethod = hasWriteMethodForProperty(bi, "address");
        boolean hasIndexedReadMethod = hasIndexedReadMethodForProperty(bi, "address");
        boolean hasIndexedWriteMethod = hasIndexedWriteMethodForProperty(bi, "address");
        // ExtendedBeanInfo 需要表现得与 BeanInfo 完全一样...
        BeanInfo ebi = new ExtendedBeanInfo(bi);
        assertThat(hasReadMethodForProperty(ebi, "address")).isEqualTo(hasReadMethod);
        assertThat(hasWriteMethodForProperty(ebi, "address")).isEqualTo(hasWriteMethod);
        assertThat(hasIndexedReadMethodForProperty(ebi, "address")).isEqualTo(hasIndexedReadMethod);
        assertThat(hasIndexedWriteMethodForProperty(ebi, "address")).isEqualTo(hasIndexedWriteMethod);
    }

    @Test
    void shouldSupportStaticWriteMethod() throws Exception {
        {
            BeanInfo bi = Introspector.getBeanInfo(WithStaticWriteMethod.class);
            assertThat(hasReadMethodForProperty(bi, "prop1")).isFalse();
            assertThat(hasWriteMethodForProperty(bi, "prop1")).isFalse();
            assertThat(hasIndexedReadMethodForProperty(bi, "prop1")).isFalse();
            assertThat(hasIndexedWriteMethodForProperty(bi, "prop1")).isFalse();
        }
        {
            BeanInfo bi = new ExtendedBeanInfo(Introspector.getBeanInfo(WithStaticWriteMethod.class));
            assertThat(hasReadMethodForProperty(bi, "prop1")).isFalse();
            assertThat(hasWriteMethodForProperty(bi, "prop1")).isTrue();
            assertThat(hasIndexedReadMethodForProperty(bi, "prop1")).isFalse();
            assertThat(hasIndexedWriteMethodForProperty(bi, "prop1")).isFalse();
        }
    }

    // SPR-12434 是一个标识符，通常用于表示一个特定的 Jira 问题跟踪项编号（Jira issue ID）。在 Java 代码注释中，它不会被翻译，因为它是一个专有名词。所以，翻译成中文仍然是：SPR-12434
    @Test
    void shouldDetectValidPropertiesAndIgnoreInvalidProperties() throws Exception {
        BeanInfo bi = new ExtendedBeanInfo(Introspector.getBeanInfo(java.awt.Window.class));
        assertThat(hasReadMethodForProperty(bi, "locationByPlatform")).isTrue();
        assertThat(hasWriteMethodForProperty(bi, "locationByPlatform")).isTrue();
        assertThat(hasIndexedReadMethodForProperty(bi, "locationByPlatform")).isFalse();
        assertThat(hasIndexedWriteMethodForProperty(bi, "locationByPlatform")).isFalse();
    }

    private boolean hasWriteMethodForProperty(BeanInfo beanInfo, String propertyName) {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getName().equals(propertyName)) {
                return pd.getWriteMethod() != null;
            }
        }
        return false;
    }

    private boolean hasReadMethodForProperty(BeanInfo beanInfo, String propertyName) {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getName().equals(propertyName)) {
                return pd.getReadMethod() != null;
            }
        }
        return false;
    }

    private boolean hasIndexedWriteMethodForProperty(BeanInfo beanInfo, String propertyName) {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getName().equals(propertyName)) {
                if (!(pd instanceof IndexedPropertyDescriptor)) {
                    return false;
                }
                return ((IndexedPropertyDescriptor) pd).getIndexedWriteMethod() != null;
            }
        }
        return false;
    }

    private boolean hasIndexedReadMethodForProperty(BeanInfo beanInfo, String propertyName) {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getName().equals(propertyName)) {
                if (!(pd instanceof IndexedPropertyDescriptor)) {
                    return false;
                }
                return ((IndexedPropertyDescriptor) pd).getIndexedReadMethod() != null;
            }
        }
        return false;
    }

    interface Spr9453<T> {

        T getProp();
    }

    interface Book {
    }

    interface TextBook extends Book {
    }

    interface LawBook extends TextBook {
    }

    interface BookOperations {

        Book getBook();

        void setBook(Book book);
    }

    interface TextBookOperations extends BookOperations {

        @Override
        TextBook getBook();
    }

    abstract class Library {

        public Book getBook() {
            return null;
        }

        public void setBook(Book book) {
        }
    }

    class LawLibrary extends Library implements TextBookOperations {

        @Override
        public LawBook getBook() {
            return null;
        }
    }

    static class WithStaticWriteMethod {

        public static void setProp1(String prop1) {
        }
    }
}
