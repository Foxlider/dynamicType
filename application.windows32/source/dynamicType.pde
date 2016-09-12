//Credits FoxliderAtom
import geomerative.*;


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

void setup() 
{
  String[] lines = loadStrings("..\\settings.txt");
  for (int i = 0; i < lines.length; i++) 
  {
    String[] setting = split(lines[i], '=');
    println(setting[0]+" "+setting[1]);
    switch (i)
    {
      case 0:
        newtext=boolean(setting[1]);
        break;
      case 1:
        auto=boolean(setting[1]);
        break;
      case 2:
        looping=boolean(setting[1]);
        break;
      case 3:
        pauze=boolean(setting[1]);
        break;
      case 4:
        fullscreenMode=boolean(setting[1]);
        break;
      case 5:
        maxSeekerSpeed=int(setting[1]);
        break;
      case 6:
        minSeekerSpeed=int(setting[1]);
        break;
      case 7:
        maxSeekerForce=int(setting[1]);
        break;
      case 8:
        minSeekerForce=int(setting[1]);
        break;
      case 9:
        WindowSizeH=int(setting[1]);
        break;
      case 10:
        WindowSizeV=int(setting[1]);
        break;
      case 11:
        fontSize=int(setting[1]);
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

void draw() 
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