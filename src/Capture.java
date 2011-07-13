package org.nlogo.extensions.qtj;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.Argument;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

import quicktime.std.sg.SequenceGrabber;
import quicktime.QTSession;
import quicktime.std.sg.SGVideoChannel;
import quicktime.qd.*;
import quicktime.std.sg.SGDeviceList;
import quicktime.util.RawEncodedImage;
import quicktime.io.QTFile;
import quicktime.util.RawEncodedImage;

public strictfp class Capture {
  private static SequenceGrabber capture;
  private static QDGraphics graphics;
  private static float height;

  public static void unload()
      throws ExtensionException {
    try {
      if (capture != null) {
        capture.stop();
        capture = null;
        graphics = null;
        QTSession.close();
      }
    } catch (quicktime.std.StdQTException e) {
      throw new ExtensionException(e.getMessage());
    }
  }

  public static class StartCamera
      extends DefaultCommand {
    public Syntax getSyntax() {
      return Syntax.commandSyntax(new int[]{Syntax.NumberType(), Syntax.NumberType()});
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      double patchSize = context.getAgent().world().patchSize();
      float width = (float) (args[0].getDoubleValue() * patchSize);
      float height = (float) (args[1].getDoubleValue() * patchSize);

      try {
        QDRect rect = new QDRect(width, height);
        SGVideoChannel channel = getVideoChannel(rect);
        initializeChannel(channel, rect);
      } catch (quicktime.std.StdQTException e) {
        String msg = "Failed to open a session.  QuickTime may not be installed properly. Or your camera may not be connected or on.";
        if (System.getProperty("os.name").startsWith("Windows")) {
          msg += " Perhaps WinVDig is not installed.";
        }
        throw new ExtensionException(msg);
      } catch (quicktime.QTException e) {
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  private static SGVideoChannel getVideoChannel(QDRect rect)
      throws quicktime.QTException, ExtensionException {
    unload();

    QTSession.open();

    // workaround for intel macs (from imagej)
    graphics = quicktime.util.EndianOrder.isNativeLittleEndian()
        ? new QDGraphics(QDConstants.k32BGRAPixelFormat, rect)
        : new QDGraphics(QDGraphics.kDefaultPixelFormat, rect);

    capture = new SequenceGrabber();
    capture.setGWorld(graphics, null);

    return new SGVideoChannel(capture);
  }

  private static void initializeChannel(SGVideoChannel channel, QDRect rect)
      throws quicktime.QTException {
    channel.setBounds(rect);
    channel.setUsage(quicktime.std.StdQTConstants.seqGrabRecord |
        quicktime.std.StdQTConstants.seqGrabPreview |
        quicktime.std.StdQTConstants.seqGrabPlayDuringRecord);

    channel.setFrameRate(0);
    channel.setCompressorType(quicktime.std.StdQTConstants.kComponentVideoCodecType);

    QTFile movieFile = new QTFile(new java.io.File("NoFile"));
    capture.setDataOutput(null, quicktime.std.StdQTConstants.seqGrabDontMakeMovie);
    capture.prepare(true, true);
    capture.startRecord();
    capture.idle();
    capture.update(null);
  }

  public static class SelectCamera
      extends DefaultCommand {
    public Syntax getSyntax() {
      return Syntax.commandSyntax(new int[]{Syntax.NumberType(), Syntax.NumberType()});
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      double patchSize = context.getAgent().world().patchSize();
      float width = (float) (args[0].getDoubleValue() * patchSize);
      float height = (float) (args[1].getDoubleValue() * patchSize);

      try {
        QDRect rect = new QDRect(width, height);
        SGVideoChannel channel = getVideoChannel(rect);
        quicktime.std.sg.SGDeviceList list = channel.getDeviceList(0);
        String[] options = new String[list.getCount()];
        int numDevices = 0;
        for (int i = 0; i < list.getCount(); i++) {
          quicktime.std.sg.SGDeviceName device = list.getDeviceName(i);
          if ((device.getFlags() & quicktime.std.StdQTConstants.sgDeviceNameFlagDeviceUnavailable) == 0) {
            options[numDevices] = device.getName();
            numDevices++;
          }
        }

        if (numDevices == 0) {
          throw new ExtensionException("There are no cameras connected to your computer");
        }

        if (numDevices != options.length) {
          String[] tmp = new String[numDevices];
          System.arraycopy(options, 0, tmp, 0, numDevices);
          options = tmp;
        }

        Object selected = javax.swing.JOptionPane.showInputDialog
            (null, "Select an input device: ", "QTJ Extension",
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (selected != null) {
          channel.setDevice(selected.toString());
          initializeChannel(channel, rect);
        }
      } catch (quicktime.std.StdQTException e) {
        String msg = "Failed to open a session.  QuickTime may not be installed properly.";
        if (System.getProperty("os.name").startsWith("Windows")) {
          msg += " Perhaps WinVDig is not installed.";
        }
        throw new ExtensionException(msg);
      } catch (quicktime.QTException e) {
        throw new ExtensionException("Your QuickTime installation is not configured properly");
      }
    }
  }

  public static class StopCamera
      extends DefaultCommand {
    public Syntax getSyntax() {
      return Syntax.commandSyntax(new int[]{});
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      if (capture == null) {
        throw new ExtensionException("There is no open camera session");
      }
      try {
        capture.stop();
        capture = null;
        graphics = null;
        QTSession.close();
      } catch (Exception e) {
        e.printStackTrace();
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  public static class Image
      extends DefaultReporter {
    public Syntax getSyntax() {
      return Syntax.reporterSyntax
          (new int[]{}, Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      if (capture == null) {
        throw new ExtensionException("There is no open camera session");
      }
      try {
        capture.idle();
        capture.update(null);
        PixMap map = graphics.getPixMap();
        RawEncodedImage image = map.getPixelData();
        int intsPerRow = image.getRowBytes() / 4;
        int height = graphics.getBounds().getHeight();

        int[] data = new int[intsPerRow * height];
        image.copyToArray(0, data, 0, data.length);

        return QTJExtension.getBufferedImage(data, intsPerRow, height);
      } catch (Exception e) {
        e.printStackTrace();
        throw new ExtensionException(e.getMessage());
      }
    }
  }
}
