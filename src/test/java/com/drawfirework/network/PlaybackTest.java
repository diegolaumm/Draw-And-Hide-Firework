package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.Test;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class PlaybackTest {
    private PlaybackPayload event(CanvasData c) { return new PlaybackPayload(UUID.randomUUID(),"minecraft:overworld",1,80,-2,c); }
    @Test public void payloadRoundtripAndLimit() {
        var p=event(CanvasData.blank());
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try { PlaybackPayload.CODEC.encode(b,p); assertTrue(b.readableBytes()<=4654);
            assertEquals(p,PlaybackPayload.CODEC.decode(b)); } finally { b.release(); }
        assertThrows(IllegalArgumentException.class,()->event(new CanvasData(33,1,new int[33])));
    }
    @Test public void invalidMetadataAndCanvasRejected() {
        for(double n:new double[]{Double.NaN,Double.POSITIVE_INFINITY,30_000_001})
            assertThrows(IllegalArgumentException.class,()->new PlaybackPayload(UUID.randomUUID(),"minecraft:overworld",n,0,0,CanvasData.blank()));
        assertThrows(IllegalArgumentException.class,()->new PlaybackPayload(UUID.randomUUID(),"bad space",0,0,0,CanvasData.blank()));
        assertThrows(IllegalArgumentException.class,()->event(null));
    }
    @Test public void decoderRejectsTruncationTrailingAndInvalidCoordinates() {
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            PlaybackPayload.CODEC.encode(b,event(CanvasData.blank())); b.writeByte(0);
            assertThrows(IllegalArgumentException.class,()->PlaybackPayload.CODEC.decode(b));
            b.clear(); b.writeByte(1); b.writeLong(0); b.writeLong(0); b.writeUtf("minecraft:overworld",128);
            b.writeDouble(Double.NaN);b.writeDouble(0);b.writeDouble(0);
            assertThrows(IllegalArgumentException.class,()->PlaybackPayload.CODEC.decode(b));
            b.clear(); b.writeByte(1);
            assertThrows(IndexOutOfBoundsException.class,()->PlaybackPayload.CODEC.decode(b));
        } finally { b.release(); }
    }
    @Test public void actualSendGateRejectsMissingInvalidAndRepeatedUse() {
        var gate=new UseGate(); var count=new AtomicInteger();
        assertFalse(gate.send(1,false,true,CanvasData.blank(),count::incrementAndGet));
        assertFalse(gate.send(1,true,false,CanvasData.blank(),count::incrementAndGet));
        assertFalse(gate.send(1,true,true,null,count::incrementAndGet));
        assertFalse(gate.send(1,true,true,new CanvasData(33,1,new int[33]),count::incrementAndGet));
        assertTrue(gate.send(1,true,true,CanvasData.blank(),()->{
            count.incrementAndGet(); assertFalse(gate.send(1,true,true,CanvasData.blank(),count::incrementAndGet)); }));
        assertFalse(gate.send(10,true,true,CanvasData.blank(),count::incrementAndGet));
        assertTrue(gate.send(11,true,true,CanvasData.blank(),count::incrementAndGet)); assertEquals(2,count.get());
    }
}
