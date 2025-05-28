// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.testfixture.beans;

import java.io.Serializable;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 实现 Person 接口的可序列化版本。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class SerializablePerson implements Person, Serializable {

    private String name;

    private int age;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public Object echo(Object o) throws Throwable {
        if (o instanceof Throwable) {
            throw (Throwable) o;
        }
        return o;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof SerializablePerson that && ObjectUtils.nullSafeEquals(this.name, that.name) && this.age == that.age));
    }

    @Override
    public int hashCode() {
        return SerializablePerson.class.hashCode();
    }
}
