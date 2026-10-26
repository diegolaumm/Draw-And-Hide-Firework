package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import org.junit.Test;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class EditSessionTest {
    private final Object stack = new Object();
    private final UUID token = UUID.randomUUID();
    private final CanvasData original = new CanvasData(2,1,new int[]{0x12345678,0xFF0000FF});
    private EditSession session() { return new EditSession(token,stack,3,original,100); }
    @Test public void actualProductGateComparesPixelContentsAndWritesOnce() {
        var s=session(); var writes=new AtomicInteger();
        var equal=new CanvasData(2,1,original.pixels());
        assertTrue(s.save(token,101,true,stack,3,equal,true,equal,()->{
            writes.incrementAndGet();
            assertFalse(s.save(token,101,true,stack,3,equal,true,equal,writes::incrementAndGet));
        }));
        assertFalse(s.save(token,112,true,stack,3,equal,true,equal,writes::incrementAndGet));
        assertEquals(1,writes.get());
    }
    @Test public void refusesSurvivalDeadWrongItemChangedSlotAndStackAndComponents() {
        var writes=new AtomicInteger();
        assertFalse(session().save(token,101,false,stack,3,original,true,original,writes::incrementAndGet));
        assertFalse(session().save(token,101,true,new Object(),3,original,true,original,writes::incrementAndGet));
        assertFalse(session().save(token,101,true,stack,4,original,true,original,writes::incrementAndGet));
        assertFalse(session().save(token,101,true,stack,3,original,false,original,writes::incrementAndGet));
        var changed=new CanvasData(2,1,new int[]{0x12345679,0xFF0000FF});
        assertFalse(session().save(token,101,true,stack,3,changed,true,original,writes::incrementAndGet));
        assertFalse(session().save(token,101,true,stack,3,null,true,original,writes::incrementAndGet));
        assertEquals(0,writes.get());
    }
    @Test public void refusesExpiredUnknownTokenInvalidCanvasAndResizing() {
        var writes=new AtomicInteger();
        assertFalse(session().save(UUID.randomUUID(),101,true,stack,3,original,true,original,writes::incrementAndGet));
        for(long tick:new long[]{99,12100})
            assertFalse(session().save(token,tick,true,stack,3,original,true,original,writes::incrementAndGet));
        for(CanvasData c:new CanvasData[]{null,new CanvasData(33,1,new int[33]),new CanvasData(1,2,new int[2])})
            assertFalse(session().save(token,101,true,stack,3,original,true,c,writes::incrementAndGet));
        assertEquals(0,writes.get());
    }
    @Test public void absentComponentGetsOnlyExplicit32By32SaveAndCancelDoesNotWrite() {
        var s=new EditSession(token,stack,3,null,100); var writes=new AtomicInteger();
        var editor=new FreehandCanvas(null); editor.paint(0,0,-1);
        assertTrue(s.current(101,true,stack,3,null,true)); assertEquals(0,writes.get());
        assertTrue(s.save(token,101,true,stack,3,null,true,editor.snapshot(),writes::incrementAndGet));
        assertEquals(1,writes.get());
    }
    @Test public void actualRequestThrottleAllowsImmediateSaveButLimitsEachChannel() {
        var gate = new EditorRequestGate();
        assertTrue(gate.admit(100,true));
        assertTrue(gate.admit(100,false));
        for (long tick : new long[]{100,101,109}) {
            assertFalse(gate.admit(tick,true)); assertFalse(gate.admit(tick,false));
        }
        assertTrue(gate.admit(110,true)); assertTrue(gate.admit(110,false));
    }
}
