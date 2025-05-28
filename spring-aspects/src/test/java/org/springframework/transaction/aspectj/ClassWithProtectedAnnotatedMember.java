// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License 2.0 版本（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式的质量保证或条件，
* 无论是明示的还是隐含的。有关许可的具体语言、权限和限制，
* 请参阅许可证。*/
package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.Transactional;

/**
 * @作者 Adrian Colyer
 * @自 2.0
 */
public class ClassWithProtectedAnnotatedMember {

    public void doSomething() {
        doInTransaction();
    }

    @Transactional
    protected void doInTransaction() {
    }
}
