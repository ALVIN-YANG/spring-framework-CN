// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发的，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.testfixture.beans;

import java.io.IOException;

/**
 * 用于 {@link org.springframework.beans.testfixture.beans.TestBean} 的接口。
 *
 * <p>有两个方法与 Person 上的方法相同，但如果这个接口继承自 Person，则会破坏很多测试。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface ITestBean extends AgeHolder {

    String getName();

    void setName(String name);

    default void applyName(Object name) {
        setName(String.valueOf(name));
    }

    ITestBean getSpouse();

    void setSpouse(ITestBean spouse);

    ITestBean[] getSpouses();

    String[] getStringArray();

    void setStringArray(String[] stringArray);

    Integer[][] getNestedIntegerArray();

    Integer[] getSomeIntegerArray();

    void setSomeIntegerArray(Integer[] someIntegerArray);

    void setNestedIntegerArray(Integer[][] nestedIntegerArray);

    int[] getSomeIntArray();

    void setSomeIntArray(int[] someIntArray);

    int[][] getNestedIntArray();

    void setNestedIntArray(int[][] someNestedArray);

    /**
     * 抛出一个给定的（非空）异常。
     */
    void exceptional(Throwable t) throws Throwable;

    Object returnsThis();

    INestedTestBean getDoctor();

    INestedTestBean getLawyer();

    IndexedTestBean getNestedIndexedBean();

    /**
     * 将年龄增加一。
     * @return 返回之前的年龄
     */
    int haveBirthday();

    void unreliableFileOperation() throws IOException;
}
