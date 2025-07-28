package com.minehut.moderators.plotmoderation.utils.text;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;

@UtilityClass
public class CC {

    private Component rawComponent(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public Component component(String text) {
        return MiniMessage.miniMessage().deserialize(
                text,
                TagResolver.resolver(
                        "mh_blue",
                        Tag.styling(Objects.requireNonNull(TextColor.fromHexString("#34c0eb")))
                ),
                TagResolver.resolver(
                        "prefix",
                        Tag.inserting(
                                rawComponent("<#34c0eb><bold>Moderation</bold></#34c0eb> <gray>|</gray> ")
                        )
                )
        );
    }

}
