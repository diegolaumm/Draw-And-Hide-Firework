package com.drawfirework.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenEditorPayload() implements CustomPacketPayload {
    public static final Type<OpenEditorPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("drawfirework", "open_editor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEditorPayload> CODEC = StreamCodec.of(
            (b,p) -> b.writeByte(1), b -> {
                if (b.readableBytes() != 1 || b.readUnsignedByte() != 1)
                    throw new IllegalArgumentException("Invalid editor open request");
                return new OpenEditorPayload();
            });
    @Override public Type<OpenEditorPayload> type() { return TYPE; }
}
