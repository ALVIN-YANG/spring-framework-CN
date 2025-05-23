// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可证了解具体管理权限和限制。*/
package org.springframework.beans.factory.parsing;

/**
 * 该接口的空实现，为所有回调方法提供了无操作(no-op)的实现。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class EmptyReaderEventListener implements ReaderEventListener {

    @Override
    public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
        // 无操作（No Operation）
    }

    @Override
    public void componentRegistered(ComponentDefinition componentDefinition) {
        // 这是一个缩写，通常代表 "No Operation"。在 Java 编程中，这通常用于表示一个没有实际操作（即什么也不做）的方法或代码块。以下是几种可能的中文翻译：1. 空操作2. 无操作3. 空指令4. 不操作
    }

    @Override
    public void aliasRegistered(AliasDefinition aliasDefinition) {
        // 无操作（No Operation）
    }

    @Override
    public void importProcessed(ImportDefinition importDefinition) {
        // 无操作（No Operation）
    }
}
