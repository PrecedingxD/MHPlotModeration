package com.minehut.moderators.plotmoderation.utils.config;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

@UtilityClass
public class ConfigUtils {

    public ConfigurationSection getOrCreateConfigurationSection(MemorySection parent, String id) {
        final ConfigurationSection section = parent.getConfigurationSection(id);

        if (section != null) {
            return section;
        }

        return parent.createSection(id);
    }

}
