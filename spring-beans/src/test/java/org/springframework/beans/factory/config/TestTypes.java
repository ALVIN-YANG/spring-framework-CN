// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者们。
*
* 根据 Apache 许可证版本 2.0 ("许可证") 进行许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ObjectFactory;

/**
 * 该包中共享的测试类型。
 *
 * @author Chris Beams
 */
final class TestTypes {
}

/**
 * @作者 Juergen Hoeller
 */
class NoOpScope implements Scope {

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
