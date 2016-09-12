import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import geomerative.*; 

import org.apache.batik.svggen.font.*; 
import org.apache.batik.svggen.font.table.*; 
import controlP5.*; 
import processing.core.*; 
import processing.xml.*; 
import geomerative.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class dynamicType extends PApplet {

//Credits FoxliderAtom



ImgProc imgProc = new ImgProc();
RFont font;

//    ___[ Settings ]___

String inp = "", lastText;
String zin = "";

int maxSeekerSpeed = 35;
int minSeekerSpeed = 21;
float maxSeekerForce = 1000;
float minSeekerForce = 1;

int WindowSizeH = 325;
int WindowSizeV = 1500;

int fontSize = 100;
int currWord = 0;

int[] currFrame;
int[] prevFrame;
int[] tempFrame;
String[] words;

boolean newtext =        false,
        auto =           false,
        looping =        false, 
        pauze =          false,
        fullscreenMode = false;

ArrayList seekers, coords;

public void setup() 
{
  String[] lines = loadStrings("..\\settings.txt");
  for (int i = 0; i < lines.length; i++) 
  {
    String[] setting = split(lines[i], '=');
    println(setting[0]+" "+setting[1]);
    switch (i)
    {
      case 0:
        newtext=PApplet.parseBoolean(setting[1]);
        break;
      case 1:
        auto=PApplet.parseBoolean(setting[1]);
        break;
      case 2:
        looping=PApplet.parseBoolean(setting[1]);
        break;
      case 3:
        pauze=PApplet.parseBoolean(setting[1]);
        break;
      case 4:
        fullscreenMode=PApplet.parseBoolean(setting[1]);
        break;
      case 5:
        maxSeekerSpeed=PApplet.parseInt(setting[1]);
        break;
      case 6:
        minSeekerSpeed=PApplet.parseInt(setting[1]);
        break;
      case 7:
        maxSeekerForce=PApplet.parseInt(setting[1]);
        break;
      case 8:
        minSeekerForce=PApplet.parseInt(setting[1]);
        break;
      case 9:
        WindowSizeH=PApplet.parseInt(setting[1]);
        break;
      case 10:
        WindowSizeV=PApplet.parseInt(setting[1]);
        break;
      case 11:
        fontSize=PApplet.parseInt(setting[1]);
        break;
      case 12:
        zin=setting[1];
        break;
    }
    words = split(zin, " ");
  }
    
    
  
  
  
  if (fullscreenMode)
  {
    size(screen.width, screen.height);
    frame.removeNotify();
    frame.setUndecorated(true);
    frame.addNotify();
  }
  else
  {
    size(WindowSizeV, WindowSizeH);
  }
  RG.init(this);
  font = new RFont("haas.ttf", fontSize, RFont.CENTER);
  seekers = new ArrayList();
  coords = new ArrayList();
  noStroke();
  noCursor();
  fill(0);
  frameRate(60);

  //imgProc
  currFrame = new int[width*height];
  prevFrame = new int[width*height];
  tempFrame = new int[width*height];
  for(int i=0; i<width*height; i++) 
  {
    currFrame[i] = color(0);
    prevFrame[i] = color(0);
    tempFrame[i] = color(0);
  }
}

public void draw() 
{
  imgProc.blur(prevFrame, tempFrame, width, height);
  arraycopy(tempFrame, currFrame);

  if((frameCount == 1) && auto) 
  {
    inp = words[currWord];
  }

  RGroup grp = font.toGroup(inp);
  RCommand.setSegmentLength(1);
  RCommand.setSegmentator(RCommand.UNIFORMLENGTH);
  RPoint[] pnts = grp.getPoints();

  if(pnts != null) 
  {
    if(newtext) 
    {
      coords = new ArrayList();
    }
    if(pnts.length > 0) 
    {
      update(pnts.length, pnts, 4);
    }
    if(newtext) 
    {
      newtext = false;
    }

    //add seekers if there are more points than seekers
    checkSeekerCount(pnts.length);

    if(auto) 
    {
      if(arrived() == 100) 
      {
        pauze = true;
      }
      if(arrived() == 0 && pauze && seekers.size() == 0)
      {
        println("nieuw");
        if (!looping)
        {
          delay(5000);
          exit();
        }
        if(currWord < words.length-1) 
        {
          currWord++;
          inp = words[currWord];
        }
        else 
        {
          currWord = 0;
          inp = words[currWord];
        }
        pauze = false;
      }
      
    }
  }
  else 
  {
    seekers = new ArrayList();
  }
  if(inp != lastText) 
  {
    newtext = true;
  }
  lastText = inp;
  imgProc.drawPixelArray(currFrame, 0, 0, width, height);  
  arraycopy(currFrame, prevFrame);
}
class Boid {

  PVector loc, vel, acc;
  float r, maxforce, maxspeed;
  boolean arrived;
  int c;

  Boid(PVector l, float ms, float mf, int c, boolean arrived) {
    acc = new PVector(0,0);
    vel = new PVector(0,0);
    loc = l.get();
    r = 3.0f;
    maxspeed = ms;
    maxforce = mf;
    this.c=c;
    this.arrived = arrived;
  }

  // Method to update location
  public void update() {
    // Update velocity
    vel.add(acc);
    // Limit speed
    vel.limit(maxspeed);
    loc.add(vel);
    // Reset accelertion to 0 each cycle
    acc.mult(0);
  }

  public void arrive(PVector target) {
    acc.add(steer(target,true));
  }

  // A method that calculates a steering vector towards a target
  // Takes a second argument, if true, it slows down as it approaches the target
  public PVector steer(PVector target, boolean slowdown) {
    PVector steer;  // The steering vector
    PVector desired = PVector.sub(target,loc);  // A vector pointing from the location to the target
    float d = desired.mag(); // Distance from the target is the magnitude of the vector
    // If the distance is greater than 0, calc steering (otherwise return zero vector)
    if (d > 0) {
      // Normalize desired
      desired.normalize();
      // Two options for desired vector magnitude (1 -- based on distance, 2 -- maxspeed)
      if ((slowdown) && (d < 100.0f)) desired.mult(maxspeed*(d/100.0f)); // This damping is somewhat arbitrary
      else desired.mult(maxspeed);
      // Steering = Desired minus Velocity
      steer = PVector.sub(desired,vel);
      steer.limit(maxforce);  // Limit to maximum steering force
    } 
    else {
      steer = new PVector(0,0);
    }
    return steer;
  }
}

class Point {
  float x, y, z;
  boolean arrived;

  Point(float x, float y, boolean arrived) {
    this.arrived = arrived;
    this.x = x;
    this.y = y;
  }
}

// ImgProc by Marcin Ignac
// http://www.marcinignac.com/

public class ImgProc {

  public void ImgProc() {
  }

  public void drawPixelArray(int[] src, int dx, int dy, int w, int h) {  
    loadPixels();
    int x;
    int y;
    for(int i=0; i<w*h; i++) {
      x = dx + i % w;
      y = dy + i / w;
      pixels[x  + y * w] = src[i];
    }
    updatePixels();
  }

  public void blur(int[] src, int[] dst, int w, int h) 
  {
    int c;
    int r;
    int g;
    int b;
    for(int y=1; y<h-1; y++) 
    {
      for(int x=1; x<w-1; x++) 
      {      
        r = 0;
        g = 0;
        b = 0;
        for(int yb=-1; yb<=1; yb++) 
        {
          for(int xb=-1; xb<=1; xb++) 
          {  
            c = src[(x+xb)+(y-yb)*w];      
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += (c) & 0xFF;
          }
        }      
        r /= 9;
        g /= 9;
        b /= 9;
        dst[x + y*w] = 0xFF000000 | (r << 16) | (g << 8) | b;
      }
    }
  }
}
public float arrived() 
{
  float arrived = 0;
  if(coords != null) 
  {
    for(int i = 0; i < seekers.size(); i++) 
    {
      Boid seeker = (Boid) seekers.get(i);
      if(seeker.arrived == true) 
      {
        arrived++;
      }
    }
    return (arrived/coords.size())*100;
  }
  else 
  {
    return 0;
  }
}

public void checkSeekerCount(int count) 
{
  if(count > 1) {
    if((seekers.size() < count) && (!pauze)) 
    {
      for(int y = 0; y < 15; y++) 
      {
        int px = (int) random(width);
        int py = (int) random(height);
        int c = color(19, 134, 214);
        newSeeker(px,py,c);
      }
    }
    if((seekers.size() > count)) 
    {
      for(int z = 0; z < (seekers.size() - count); z++) 
      {
        seekers.remove(seekers.size()-1);
      }
    }
    if(pauze && seekers.size() > 15) 
    {
      for(int z = 0; z < 15; z++) 
      {
        seekers.remove(0);
      }
    }
    else if(pauze && seekers.size() > 0) 
    {
      if((frameCount % 10) == 0) 
      {
        seekers.remove(0);
      }
    }
  }
}

public void newSeeker(float x, float y, int c) 
{
  float maxspeed = random(minSeekerSpeed, maxSeekerSpeed);
  float maxforce = random(minSeekerForce, maxSeekerForce);
  seekers.add(new Boid(new PVector(x,y),maxspeed,maxforce,c,false));
}

public void update(int count, RPoint[] pnts, int baseR) 
{
  for ( int i = 0; i < count; i++ )
  {
    float mx = (pnts[i].x)+width/2;
    float my = (pnts[i].y)+height/2;

    if(newtext) {
      coords.add(new Point(mx, my, false));
    }

    if((i < seekers.size()) && (i < coords.size())) 
    {
      Boid seeker = (Boid) seekers.get(i);
      Point coord = (Point) coords.get(i);
      if(!pauze) {
        seeker.arrive(new PVector(coord.x,coord.y));
        seeker.update();
      }
      if((seeker.loc.x >= 0) && (seeker.loc.x < width-1) && (seeker.loc.y >= 0) && (seeker.loc.y < height-1)) 
      {
        int currC = currFrame[(int)seeker.loc.x + ((int)seeker.loc.y)*width];
        currFrame[(int)seeker.loc.x + ((int)seeker.loc.y)*width] = blendColor(seeker.c, currC, ADD);
      }

      if(((seeker.loc.x > mx-1) && (seeker.loc.x < mx+1)) && ((seeker.loc.y > my-1) && (seeker.loc.y < my+1)) && (coord.arrived == false)) 
      {
        seeker.arrived = true;
      }
      else 
      {
        seeker.arrived = false;
      }
    }
  }
}

public void keyPressed () 
{
  if(auto == false) 
  {
    if (  keyCode == DELETE || keyCode == BACKSPACE ) 
    {
      if ( inp.length() > 0 ) 
      {
        inp = inp.substring(0,inp.length()-1);
      }
    }
    else if (key != CODED) 
    {
      inp = inp + key;
      print(key);
    }
  }
}

public void mouseReleased() 
{
  //skip word
  pauze =! pauze;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "dynamicType" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
