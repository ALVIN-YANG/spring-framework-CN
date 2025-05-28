// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证以了解管理许可权限和限制的特定语言。*/
package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author 阿德里安·科利尔
 * @since 2.0
 */
public class ClassWithPrivateAnnotatedMember {

    public void doSomething() {
        doInTransaction();
    }

    @Transactional
    private void doInTransaction() {
    }
}
