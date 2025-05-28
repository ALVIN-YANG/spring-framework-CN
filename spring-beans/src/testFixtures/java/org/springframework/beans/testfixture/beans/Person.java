// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.testfixture.beans;

/**
 * @author Rod Johnson
 */
public interface Person {

    String getName();

    void setName(String name);

    int getAge();

    void setAge(int i);

    /**
     * 测试非属性方法匹配。如果参数是 Throwable 类型，则会抛出异常而不是返回。
     */
    Object echo(Object o) throws Throwable;
}
