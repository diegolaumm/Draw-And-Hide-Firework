import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.canvas.CanvasData;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.Arrays;

/** Offline product-method comparison, not a Minecraft/GPU benchmark. */
public class Phase05B1QualityCheck {
    private static FreehandCanvas pattern(int kind) {
        var c=new FreehandCanvas(null);
        c.setDiameter(kind==0 ? 1 : kind==1 ? 3 : 8);
        if(kind==0) { c.paint(12.5,100.5,0xFFFF4040); c.paint(243.5,100.5,0xFFFF4040); }
        if(kind==1) { c.paint(12,12,0xFF40CF70); c.paint(244,244,0xFF40CF70); }
        if(kind==2) for(int d=0;d<=360;d+=3) {
            double a=Math.toRadians(d); c.paint(128+80*Math.cos(a),128+80*Math.sin(a),0xFF4080FF);
        }
        if(kind==3) {
            c.setDiameter(32); c.paint(30,128,0xFFFF4040); c.paint(128,128,0xFFFF4040);
            c.endStroke(); c.paint(128,128,0x804080FF); c.paint(226,128,0x804080FF);
        }
        return c;
    }
    private static BufferedImage image(FreehandCanvas c) {
        var b=new BufferedImage(c.width(),c.height(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<c.height();y++) for(int x=0;x<c.width();x++) b.setRGB(x,y,c.pixel(x,y)); return b;
    }
    private static BufferedImage image(CanvasData c) {
        var b=new BufferedImage(c.width(),c.height(),BufferedImage.TYPE_INT_ARGB);
        b.setRGB(0,0,c.width(),c.height(),c.pixels(),0,c.width()); return b;
    }
    public static void main(String[] args) throws Exception {
        var sheet=new BufferedImage(820,1180,BufferedImage.TYPE_INT_RGB); var g=sheet.createGraphics();
        g.setColor(new Color(28,31,38)); g.fillRect(0,0,820,1180); g.setFont(new Font("SansSerif",Font.PLAIN,16));
        String[] names={"1 px thin line","3 px diagonal","8 px circle","32 px color / alpha boundary"};
        g.setColor(Color.WHITE); g.drawString("256 working raster",16,24); g.drawString("32 area / premultiplied",288,24); g.drawString("32 nearest (comparison)",560,24);
        for(int row=0;row<4;row++) {
            var c=pattern(row); var out=c.snapshot(); var nearest=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<32;y++) for(int x=0;x<32;x++) nearest.setRGB(x,y,c.pixel(x*8+4,y*8+4));
            int y0=60+row*280; g.setColor(Color.WHITE); g.drawString(names[row],16,y0-8);
            for(int col=0;col<3;col++) {
                int x0=16+col*272; g.setColor(new Color(112,112,112)); g.fillRect(x0,y0,256,256);
                g.drawImage(col==0?image(c):col==1?image(out):nearest,x0,y0,256,256,null);
            }
            long mass=0,area=0,nn=0; int areaPoints=0,nnPoints=0;
            for(int y=0;y<256;y++) for(int x=0;x<256;x++) mass+=c.pixel(x,y)>>>24;
            for(int y=0;y<32;y++) for(int x=0;x<32;x++) {
                int a=out.pixel(x,y)>>>24,n=nearest.getRGB(x,y)>>>24; area+=a; nn+=n;
                if(a>0) areaPoints++; if(n>0) nnPoints++;
            }
            System.out.printf(java.util.Locale.ROOT,"%s: reference alpha mass %.2f; area %d (%d visible); nearest %d (%d visible)%n",names[row],mass/64.0,area,areaPoints,nn,nnPoints);
        }
        g.dispose(); Files.createDirectories(Path.of("build/phase05b1-quality"));
        ImageIO.write(sheet,"png",Path.of("build/phase05b1-quality/comparison.png").toFile());
        var c=new FreehandCanvas(null); c.setDiameter(64);
        long[] stroke=new long[120],resize=new long[120];
        var bean=java.lang.management.ManagementFactory.getThreadMXBean();
        var allocation=bean instanceof com.sun.management.ThreadMXBean b && b.isThreadAllocatedMemoryEnabled() ? b : null;
        long strokeBytes=0,resizeBytes=0;
        for(int i=-30;i<120;i++) {
            c.endStroke(); long beforeBytes=allocation==null?0:allocation.getCurrentThreadAllocatedBytes(); long t=System.nanoTime(); c.paint(1,1,0xFF4080FF); c.paint(254,254,0xFF4080FF);
            long end=System.nanoTime(); long middleBytes=allocation==null?0:allocation.getCurrentThreadAllocatedBytes(); c.snapshot(); long finish=System.nanoTime();
            long afterBytes=allocation==null?0:allocation.getCurrentThreadAllocatedBytes();
            if(i>=0) { strokeBytes+=middleBytes-beforeBytes; resizeBytes+=afterBytes-middleBytes; }
            if(i>=0) {stroke[i]=end-t;resize[i]=finish-end;}
        }
        Arrays.sort(stroke); Arrays.sort(resize);
        System.out.println(allocation==null?"Allocation measurement unavailable":"Allocated bytes/op: stroke="+strokeBytes/120+", conversion="+resizeBytes/120);
        System.out.printf(java.util.Locale.ROOT,"CPU only, 30 warmups / 120 samples: 64px full diagonal p50 %.3f ms p95 %.3f ms; conversion p50 %.3f ms p95 %.3f ms%n",stroke[60]/1e6,stroke[114]/1e6,resize[60]/1e6,resize[114]/1e6);
    }
}
