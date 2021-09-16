package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/** Root packet interface */
abstract class Packet {
  private static final String TAG = "Explore";
  private byte[] byteBuffer = null;

  /** String representation of attributes */
  static float[] bytesToFloats(byte[] bytes) {
    if (bytes.length % Float.BYTES != 0) throw new RuntimeException("Illegal length");
    float floats[] = new float[bytes.length / Float.BYTES];
    ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
    return floats;
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  public abstract List<Float> convertData(byte[] byteBuffer);

  /** String representation of attributes */
  public abstract String toString();

  enum PacketId {
    ORIENTATION(13) {
      @Override
      public Packet createInstance() {
        return new Orientation();
      }
    },
    ENVIRONMENT(19) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    TIMESTAMP(27) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    DISCONNECT(25) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    INFO(99) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    EEG94(144) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    EEG98(146) {
      @Override
      public Packet createInstance() {
        return new Eeg98();
      }
    },
    EEG99S(30) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    EEG99(62) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    EEG94R(208) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    EEG98R(210) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    CMDRCV(192) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    CMDSTAT(193) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    MARKER(194) {
      @Override
      public Packet createInstance() {
        return null;
      }
    },
    CALIBINFO(195) {
      @Override
      public Packet createInstance() {
        return null;
      }
    };

    private int value;

    PacketId(int value) {
      this.value = value;
    }

    public int getNumVal() {
      return value;
    }

    public abstract Packet createInstance();
  }
}
;

/** Interface for different EEG packets */
abstract class DataPacket extends Packet {
  private static final String TAG = "Explore";
  private static byte channelMask;
  protected ArrayList<Float> convertedSamples;

  static double[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
    if (byteArray.length % 3 != 0)
      throw new InvalidDataException("Byte buffer is not read properly", null);
    int arraySize = byteArray.length / 3;
    double[] values = new double[arraySize];

    for (int index = 0; index < byteArray.length; index += 3) {
      if (index == 0) {
        channelMask = byteArray[index];
      }
      int signBit = byteArray[index + 2] >> 7;
      double value = 0;
      if (signBit == 0)
        value =
            ByteBuffer.wrap(
                    new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                .getInt();
      else {
        int twosComplimentValue =
            ByteBuffer.wrap(
                    new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                .getInt();
        value = -1 * (Math.pow(2, 24) - twosComplimentValue);
      }
      values[index / 3] = value;
    }

    return values;
  }

  ArrayList<Float> getVoltageValues() {
    return convertedSamples;
  }

  int getChannelCount() {
    return 8;
  }
}

/** Interface for packets related to device information */
abstract class InfoPacket extends Packet {}

/** Interface for packets related to device synchronization */
abstract class UtilPacket extends Packet {}

// class Eeg implements DataPacket {}
class Eeg98 extends DataPacket {
  private static int channelNumber = 8;

  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    List<Float> values = new ArrayList<Float>();
    try {
      double[] data = DataPacket.toInt32(byteBuffer);

      for (int index = 0; index < data.length; index++) {
        // skip int representation for status bit
        if (index % 9 == 0) continue;
        // calculation for gain adjustment
        double exgUnit = Math.pow(10, -6);
        double vRef = 2.4;
        double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
        values.add((float) (data[index] * (vRef / gain)));
      }
    } catch (InvalidDataException | IOException e) {
      e.printStackTrace();
    }
    this.convertedSamples = new ArrayList<>(values);
    return values;
  }

  @Override
  public String toString() {

    String data = "ExG 8 channel: [";
    ListIterator<Float> it = this.convertedSamples.listIterator();

    while (it.hasNext()) {
      data += it.next() + " ,";
    }
    return data + "]";
  }
}

class Eeg94 extends DataPacket {

  private static List<Float> convertedSamples = null;
  private static int channelNumber = 8;
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   * @return
   */
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    List<Float> values = new ArrayList<Float>();
    try {
      double[] data = DataPacket.toInt32(byteBuffer);

      for (int index = 0; index < data.length; index++) {
        // skip int representation for status bit
        if (index % 5 == 0) continue;
        // calculation for gain adjustment
        double exgUnit = Math.pow(10, -6);
        double vRef = 2.4;
        double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
        values.add((float) (data[index] * (vRef / gain)));
      }
    } catch (InvalidDataException | IOException e) {
      e.printStackTrace();
    }
    this.convertedSamples = new ArrayList<>(values);
    return values;
  }

  @Override
  public String toString() {

    String data = "ExG 8 channel: [";
    ListIterator<Float> it = this.convertedSamples.listIterator();

    while (it.hasNext()) {
      data += it.next() + " ,";
    }
    return data + "]";
  }
}

class Eeg99 extends DataPacket {
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99";
  }
}

class Eeg99s extends DataPacket {
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99s";
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class Orientation extends InfoPacket {
  List<Float> convertedSamples = null;

  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    List<Float> listValues = new ArrayList<Float>();
    Log.d("Explore", "length is ........" + byteBuffer.length);
    for (int index = 0; index < byteBuffer.length; index += 2) {

      int decodedInteger =
          ByteBuffer.wrap(new byte[] {byteBuffer[index], byteBuffer[index + 1], 0, 0})
              .order(java.nio.ByteOrder.LITTLE_ENDIAN)
              .getInt();
      if (index < 6) {
        listValues.add((float) (decodedInteger * 0.061));
      } else if (index < 12) {
        listValues.add((float) (decodedInteger * 8.750));
      } else {
        if (index == 12) {
          listValues.add((float) (decodedInteger * 1.52 * -1));
        } else {
          listValues.add((float) (decodedInteger * 1.52));
        }
      }
    }
    convertedSamples = new ArrayList<Float>(listValues);
    return listValues;
  }

  @Override
  public String toString() {
    String data = "Orientation packets: [";

    for (int index = 0; index < convertedSamples.size(); index += 1) {
      if (index % 9 < 3) {
        data += " accelerometer: " + convertedSamples.get(index);
      } else if (index % 9 < 6) {
        data += " magnetometer: " + convertedSamples.get(index);
      } else {
        data += "gyroscope: " + convertedSamples.get(index);
      }

      data += ",";
    }

    return data + "]";
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class DeviceInfoPacket extends InfoPacket {
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  @Override
  public String toString() {
    return "DeviceInfoPacket";
  }
}

/**
 * Acknowledgement packet is sent when a configuration command is successfully executed on the
 * device
 */
class AckPacket extends InfoPacket {
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  @Override
  public String toString() {
    return "AckPacket";
  }
}

/** Packet sent from the device to sync clocks */
class TimeStampPacket extends UtilPacket {
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  @Override
  public String toString() {
    return "TimeStampPacket";
  }
}

/** Disconnection packet is sent when the host machine is disconnected from the device */
class DisconnectionPacket extends UtilPacket {
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public List<Float> convertData(byte[] byteBuffer) {
    return new ArrayList<Float>();
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "DisconnectionPacket";
  }
}
