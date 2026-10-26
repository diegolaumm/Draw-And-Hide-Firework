package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class EditorPayloadTest {
    private RegistryFriendlyByteBuf buffer() { return new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY); }
    @Test public void maximumSaveAndServerSnapshotUseExistingComponentCodec() {
        var b=buffer();
        try {
            int[] pixels=new int[1024]; for(int i=0;i<1024;i++) pixels[i]=i*1234567;
            var p=new EditorCanvasPayload(UUID.randomUUID(),new CanvasData(32,32,pixels));
            EditorCanvasPayload.CODEC.encode(b,p); assertEquals(4116,b.readableBytes());
            assertEquals(p,EditorCanvasPayload.CODEC.decode(b));
            var opened=new EditorCanvasPayload.Opened(p.token(),p.canvas());
            EditorCanvasPayload.Opened.CODEC.encode(b,opened);
            assertEquals(opened,EditorCanvasPayload.Opened.CODEC.decode(b));
            assertEquals(EditorCanvasPayload.OPENED_TYPE,opened.type());
            assertEquals(EditorCanvasPayload.SAVE,p.type());
        } finally { b.release(); }
    }
    @Test public void invalidHeaderVersionSizeDimensionsTruncationAndTrailingRejected() {
        var b=buffer();
        try {
            for(int[] header:new int[][]{{2,1,1},{1,0,1},{1,33,1},{1,1,33},{1,32,32}}) {
                b.clear(); b.writeByte(1); b.writeLong(0); b.writeLong(0);
                for(int x:header) b.writeByte(x);
                assertThrows(RuntimeException.class,()->EditorCanvasPayload.CODEC.decode(b));
            }
            b.clear(); b.writeZero(4117);
            assertThrows(IllegalArgumentException.class,()->EditorCanvasPayload.CODEC.decode(b));
            b.clear(); b.writeByte(2); b.writeZero(20);
            assertThrows(IllegalArgumentException.class,()->EditorCanvasPayload.CODEC.decode(b));
            b.clear(); b.writeByte(1);
            assertThrows(IllegalArgumentException.class,()->EditorCanvasPayload.CODEC.decode(b));
            b.clear(); EditorCanvasPayload.CODEC.encode(b,new EditorCanvasPayload(UUID.randomUUID(),new CanvasData(1,1,new int[]{-1})));
            b.writeByte(0);
            assertThrows(IllegalArgumentException.class,()->EditorCanvasPayload.CODEC.decode(b));
        } finally { b.release(); }
        assertThrows(IllegalArgumentException.class,()->new EditorCanvasPayload(UUID.randomUUID(),new CanvasData(33,1,new int[33])));
        assertThrows(IllegalArgumentException.class,()->new EditorCanvasPayload(UUID.randomUUID(),null));
    }
    @Test public void openRequestIsExactlyOneVersionByte() {
        var b=buffer();
        try {
            OpenEditorPayload.CODEC.encode(b,new OpenEditorPayload()); assertEquals(1,b.readableBytes());
            assertEquals(new OpenEditorPayload(),OpenEditorPayload.CODEC.decode(b));
            for(int n:new int[]{0,2,100}) {
                b.clear(); b.writeZero(n);
                assertThrows(IllegalArgumentException.class,()->OpenEditorPayload.CODEC.decode(b));
            }
            b.clear(); b.writeByte(2);
            assertThrows(IllegalArgumentException.class,()->OpenEditorPayload.CODEC.decode(b));
        } finally { b.release(); }
    }
    @Test public void minimumAndRectangularDimensionsKeepEveryPixel() {
        var b=buffer();
        try {
            for(int[] size:new int[][]{{1,1},{1,32},{32,1},{7,19},{16,16}}) {
                int[] pixels=new int[size[0]*size[1]];
                for(int i=0;i<pixels.length;i++) pixels[i]=i*1234567;
                var p=new EditorCanvasPayload(UUID.randomUUID(),new CanvasData(size[0],size[1],pixels));
                b.clear(); EditorCanvasPayload.CODEC.encode(b,p);
                assertEquals(p,EditorCanvasPayload.CODEC.decode(b));
            }
        } finally { b.release(); }
    }
}
