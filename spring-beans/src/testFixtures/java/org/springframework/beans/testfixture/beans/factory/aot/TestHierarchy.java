// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.testfixture.beans.factory.aot;

/**
 * 一个层次结构，其中bean的暴露类型是部分签名。
 *
 * @author Stephane Nicoll
 */
public class TestHierarchy {

    public interface One {
    }

    public interface Two {
    }

    public static class Implementation implements One, Two {
    }

    public static One oneBean() {
        return new Implementation();
    }
}
