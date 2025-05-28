// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class TransactionalAnnotationOnlyOnClassWithNoInterface {

    public Object echo(Throwable t) throws Throwable {
        if (t != null) {
            throw t;
        }
        return t;
    }

    void nonTransactionalMethod() {
        // 无操作（No Operation）
    }
}
