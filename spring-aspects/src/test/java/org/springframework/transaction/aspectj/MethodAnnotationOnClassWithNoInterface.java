// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按照“原样”基础分发的，不提供任何形式（明示或暗示）的保证。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.Transactional;

public class MethodAnnotationOnClassWithNoInterface {

    @Transactional(rollbackFor = InterruptedException.class)
    public Object echo(Throwable t) throws Throwable {
        if (t != null) {
            throw t;
        }
        return t;
    }

    public void noTransactionAttribute() {
    }
}
