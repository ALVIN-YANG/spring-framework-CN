// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache 许可协议版本 2.0（"许可证"）授权；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按"原样"分发的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.xml.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.UtilNamespaceHandler;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * 对 {@link DefaultNamespaceHandlerResolver} 类的单元和集成测试。
 *
 * @author Rob Harrop
 * @author Rick Evans
 */
public class DefaultNamespaceHandlerResolverTests {

    @Test
    public void testResolvedMappedHandler() {
        DefaultNamespaceHandlerResolver resolver = new DefaultNamespaceHandlerResolver(getClass().getClassLoader());
        NamespaceHandler handler = resolver.resolve("http://www.springframework.org/schema/util");
        assertThat(handler).as("Handler should not be null.").isNotNull();
        assertThat(handler.getClass()).as("Incorrect handler loaded").isEqualTo(UtilNamespaceHandler.class);
    }

    @Test
    public void testResolvedMappedHandlerWithNoArgCtor() {
        DefaultNamespaceHandlerResolver resolver = new DefaultNamespaceHandlerResolver();
        NamespaceHandler handler = resolver.resolve("http://www.springframework.org/schema/util");
        assertThat(handler).as("Handler should not be null.").isNotNull();
        assertThat(handler.getClass()).as("Incorrect handler loaded").isEqualTo(UtilNamespaceHandler.class);
    }

    @Test
    public void testNonExistentHandlerClass() {
        String mappingPath = "org/springframework/beans/factory/xml/support/nonExistent.properties";
        new DefaultNamespaceHandlerResolver(getClass().getClassLoader(), mappingPath);
    }

    @Test
    public void testCtorWithNullClassLoaderArgument() {
        // 绝对不能退出...
        new DefaultNamespaceHandlerResolver(null);
    }

    @Test
    public void testCtorWithNullClassLoaderArgumentAndNullMappingLocationArgument() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultNamespaceHandlerResolver(null, null));
    }

    @Test
    public void testCtorWithNonExistentMappingLocationArgument() {
        // 绝对不能退出；我们不希望不存在的资源导致异常
        new DefaultNamespaceHandlerResolver(null, "738trbc bobabloobop871");
    }
}
