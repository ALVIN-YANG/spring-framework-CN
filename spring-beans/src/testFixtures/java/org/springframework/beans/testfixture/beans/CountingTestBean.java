// 翻译完成 glm-4-flash
/** 版权所有 2002-2005 原作者或原作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.testfixture.beans;

/**
 * @author Juergen Hoeller
 * @since 2005年3月15日
 */
public class CountingTestBean extends TestBean {

    public static int count = 0;

    public CountingTestBean() {
        count++;
    }
}
