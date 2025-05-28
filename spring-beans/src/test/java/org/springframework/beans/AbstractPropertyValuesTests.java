// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rod Johnson
 * @author Chris Beams
 *
 * 作者：Rod Johnson
 * 作者：Chris Beams
 */
public abstract class AbstractPropertyValuesTests {

    /**
     * 必须包含：forname=Tony surname=Blair age=50
     */
    protected void doTestTony(PropertyValues pvs) {
        assertThat(pvs.getPropertyValues()).as("Contains 3").hasSize(3);
        assertThat(pvs.contains("forname")).as("Contains forname").isTrue();
        assertThat(pvs.contains("surname")).as("Contains surname").isTrue();
        assertThat(pvs.contains("age")).as("Contains age").isTrue();
        assertThat(!pvs.contains("tory")).as("Doesn't contain tory").isTrue();
        PropertyValue[] ps = pvs.getPropertyValues();
        Map<String, String> m = new HashMap<>();
        m.put("forname", "Tony");
        m.put("surname", "Blair");
        m.put("age", "50");
        for (PropertyValue element : ps) {
            Object val = m.get(element.getName());
            assertThat(val).as("Can't have unexpected value").isNotNull();
            assertThat(val instanceof String).as("Val i string").isTrue();
            assertThat(val.equals(element.getValue())).as("val matches expected").isTrue();
            m.remove(element.getName());
        }
        assertThat(m).as("Map size is 0").isEmpty();
    }
}
