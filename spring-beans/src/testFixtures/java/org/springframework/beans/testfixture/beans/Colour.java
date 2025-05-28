// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证以了解具体管理许可和限制的条款。*/
package org.springframework.beans.testfixture.beans;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 *
 * 作者：Rob Harrop
 * 作者：Juergen Hoeller
 */
public class Colour {

    public static final Colour RED = new Colour("RED");

    public static final Colour BLUE = new Colour("BLUE");

    public static final Colour GREEN = new Colour("GREEN");

    public static final Colour PURPLE = new Colour("PURPLE");

    private final String name;

    public Colour(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
