// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（"许可证"），您可以在遵守许可证的前提下使用此文件；
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照"现状"提供，
* 不提供任何形式的明示或暗示保证，包括但不限于对适销性、适用性或非侵权的保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.aot;

import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 测试使用 {@code @Configuration} 风格的类以创建 {@link TestBean}。
 *
 * @author Phillip Webb
 */
public class TestBeanConfiguration {

    public TestBean testBean() {
        return new TestBean();
    }
}
