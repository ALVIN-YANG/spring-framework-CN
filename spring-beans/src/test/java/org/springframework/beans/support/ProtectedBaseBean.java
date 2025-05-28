// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可证”）；除非符合许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途适用性。
* 请参阅许可证以了解管理许可权限和限制的具体语言。*/
package org.springframework.beans.support;

/**
 * @author Juergen Hoeller
 * @since 2004年7月29日
 */
class ProtectedBaseBean {

    private String someProperty;

    public void setSomeProperty(String someProperty) {
        this.someProperty = someProperty;
    }

    public String getSomeProperty() {
        return someProperty;
    }
}
