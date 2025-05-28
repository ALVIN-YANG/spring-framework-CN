// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache 许可协议版本 2.0（以下简称“许可证”）授权；
* 您只能在不违反许可证的情况下使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解管理许可权和限制的具体语言。*/
package org.springframework.beans.testfixture.beans.subpkg;

/**
 * 用于测试切点匹配。
 *
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcutTests#testWithinRootAndSubpackages()
 *
 * @author Chris Beams
 */
public class DeepBean {

    public void aMethod(String foo) {
        // 无操作（No Operation）
    }
}
