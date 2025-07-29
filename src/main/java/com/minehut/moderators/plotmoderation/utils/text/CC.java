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

    public static final TextColor MH_BLUE_COLOR = TextColor.fromHexString("#34c0eb");

    private Component rawComponent(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public Component component(String text) {
        return MiniMessage.miniMessage().deserialize(
                text,
                TagResolver.resolver(
                        "mh_blue",
                        Tag.styling(Objects.requireNonNull(MH_BLUE_COLOR))
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
