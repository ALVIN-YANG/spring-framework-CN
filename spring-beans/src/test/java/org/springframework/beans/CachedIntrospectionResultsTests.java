// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.OverridingClassLoader;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Juergen Hoeller
 * @作者 Chris Beams
 * @作者 Arjen Poutsma
 */
public class CachedIntrospectionResultsTests {

    @Test
    public void acceptAndClearClassLoader() throws Exception {
        BeanWrapper bw = new BeanWrapperImpl(TestBean.class);
        assertThat(bw.isWritableProperty("name")).isTrue();
        assertThat(bw.isWritableProperty("age")).isTrue();
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(TestBean.class)).isTrue();
        ClassLoader child = new OverridingClassLoader(getClass().getClassLoader());
        Class<?> tbClass = child.loadClass("org.springframework.beans.testfixture.beans.TestBean");
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(tbClass)).isFalse();
        CachedIntrospectionResults.acceptClassLoader(child);
        bw = new BeanWrapperImpl(tbClass);
        assertThat(bw.isWritableProperty("name")).isTrue();
        assertThat(bw.isWritableProperty("age")).isTrue();
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(tbClass)).isTrue();
        CachedIntrospectionResults.clearClassLoader(child);
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(tbClass)).isFalse();
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(TestBean.class)).isTrue();
    }

    @Test
    public void clearClassLoaderForSystemClassLoader() throws Exception {
        BeanUtils.getPropertyDescriptors(ArrayList.class);
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(ArrayList.class)).isTrue();
        CachedIntrospectionResults.clearClassLoader(ArrayList.class.getClassLoader());
        assertThat(CachedIntrospectionResults.strongClassCache.containsKey(ArrayList.class)).isFalse();
    }

    @Test
    public void shouldUseExtendedBeanInfoWhenApplicable() throws NoSuchMethodException, SecurityException {
        // 给定一个具有非void返回值的setter方法类
        @SuppressWarnings("unused")
        class C {

            public Object setFoo(String s) {
                return this;
            }

            public String getFoo() {
                return null;
            }
        }
        // 缓存的反射结果应该委托给扩展的BeanInfo
        CachedIntrospectionResults results = CachedIntrospectionResults.forClass(C.class);
        BeanInfo info = results.getBeanInfo();
        PropertyDescriptor pd = null;
        for (PropertyDescriptor candidate : info.getPropertyDescriptors()) {
            if (candidate.getName().equals("foo")) {
                pd = candidate;
            }
        }
        // 导致一个属性描述符包括了非标准的setFoo方法
        assertThat(pd).isNotNull();
        assertThat(pd.getReadMethod()).isEqualTo(C.class.getMethod("getFoo"));
        // 未找到非void返回值的'setFoo'方法的写入方法。
        // 检查 CachedIntrospectionResults 是否像预期的那样委托给 ExtendedBeanInfo
        assertThat(pd.getWriteMethod()).isEqualTo(C.class.getMethod("setFoo", String.class));
    }
}
