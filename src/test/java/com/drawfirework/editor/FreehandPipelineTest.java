package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.network.EditorCanvasPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.Test;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class FreehandPipelineTest {
    @Test public void highResolutionNeverEntersSavePayload() {
        var c=new FreehandCanvas(null); c.paint(0,0,0xFFFF0000); c.paint(255,255,0xFFFF0000);
        var p=new EditorCanvasPayload(UUID.randomUUID(),c.snapshot());
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            EditorCanvasPayload.CODEC.encode(b,p); assertEquals(4116,b.readableBytes());
            assertEquals(p,EditorCanvasPayload.CODEC.decode(b));
        } finally { b.release(); }
        assertEquals(1024,p.canvas().pixelCount());
    }
    @Test public void editedLegacyRectanglesUseTheSameOneShotGate() {
        for(int[] size:new int[][]{{1,32},{32,1},{7,19},{16,16}}) {
            var original=new CanvasData(size[0],size[1],new int[size[0]*size[1]]);
            var c=new FreehandCanvas(original); c.paint(1,1,-1);
            var token=UUID.randomUUID(); var stack=new Object(); var count=new AtomicInteger();
            var s=new EditSession(token,stack,0,original,0);
            assertTrue(s.save(token,1,true,stack,0,original,true,c.snapshot(),count::incrementAndGet));
            assertFalse(s.save(token,2,true,stack,0,original,true,c.snapshot(),count::incrementAndGet));
            assertEquals(1,count.get());
        }
    }
    @Test public void newItemDefaultIs32AndOldDefaultCannotResizeIt() {
        var token=UUID.randomUUID(); var stack=new Object(); var count=new AtomicInteger();
        var oldSize=new CanvasData(16,16,new int[256]);
        assertFalse(new EditSession(token,stack,0,null,0).save(token,1,true,stack,0,null,true,oldSize,count::incrementAndGet));
        assertTrue(new EditSession(token,stack,0,null,0).save(token,1,true,stack,0,null,true,new FreehandCanvas(null).snapshot(),count::incrementAndGet));
        assertEquals(1,count.get());
    }
}
