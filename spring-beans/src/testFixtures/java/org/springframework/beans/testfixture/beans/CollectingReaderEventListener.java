// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证了解具体语言管理权限和限制。*/
package org.springframework.beans.testfixture.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.ReaderEventListener;

/**
 * @作者 Rob Harrop
 * @作者 Juergen Hoeller
 */
public class CollectingReaderEventListener implements ReaderEventListener {

    private final List<DefaultsDefinition> defaults = new ArrayList<>();

    private final Map<String, ComponentDefinition> componentDefinitions = new LinkedHashMap<>(8);

    private final Map<String, List<AliasDefinition>> aliasMap = new LinkedHashMap<>(8);

    private final List<ImportDefinition> imports = new ArrayList<>();

    @Override
    public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
        this.defaults.add(defaultsDefinition);
    }

    public List<DefaultsDefinition> getDefaults() {
        return Collections.unmodifiableList(this.defaults);
    }

    @Override
    public void componentRegistered(ComponentDefinition componentDefinition) {
        this.componentDefinitions.put(componentDefinition.getName(), componentDefinition);
    }

    public ComponentDefinition getComponentDefinition(String name) {
        return this.componentDefinitions.get(name);
    }

    public ComponentDefinition[] getComponentDefinitions() {
        Collection<ComponentDefinition> collection = this.componentDefinitions.values();
        return collection.toArray(new ComponentDefinition[0]);
    }

    @Override
    public void aliasRegistered(AliasDefinition aliasDefinition) {
        List<AliasDefinition> aliases = this.aliasMap.computeIfAbsent(aliasDefinition.getBeanName(), k -> new ArrayList<>());
        aliases.add(aliasDefinition);
    }

    public List<AliasDefinition> getAliases(String beanName) {
        List<AliasDefinition> aliases = this.aliasMap.get(beanName);
        return (aliases != null ? Collections.unmodifiableList(aliases) : null);
    }

    @Override
    public void importProcessed(ImportDefinition importDefinition) {
        this.imports.add(importDefinition);
    }

    public List<ImportDefinition> getImports() {
        return Collections.unmodifiableList(this.imports);
    }
}
