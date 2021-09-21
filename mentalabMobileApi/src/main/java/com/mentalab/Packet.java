package com.mentalab;

import com.mentalab.exception.InvalidDataException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/** Root packet interface */
abstract class Packet {
  private static final String TAG = "Explore";
  private byte[] byteBuffer = null;
  private int dataCount;

  /** String representation of attributes */
  static double[] bytesToDouble(byte[] bytes, int numOfbytesPerNumber) throws InvalidDataException {
    if (bytes.length % numOfbytesPerNumber != 0) {
      throw new InvalidDataException("Illegal length", null);
    }
    int arraySize = bytes.length / numOfbytesPerNumber;
    double[] values = new double[arraySize];
    for (int index = 0; index < bytes.length; index += numOfbytesPerNumber) {
      int signBit = bytes[index + numOfbytesPerNumber - 1] >> 7;
      double value;

      value =
          ByteBuffer.wrap(new byte[] {bytes[index], bytes[index + 1]})
              .order(ByteOrder.LITTLE_ENDIAN)
              .getShort();
      if (signBit == 1) {
        value = -1 * (Math.pow(2, 8 * numOfbytesPerNumber) - value);
      }

      values[index / numOfbytesPerNumber] = value;
    }
    return values;
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  public abstract void convertData(byte[] byteBuffer) throws InvalidDataException;

  /** String representation of attributes */
  public abstract String toString();

  /** Number of element in each packet */
  public abstract int getDataCount();

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
        return new Environment();
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
        return new Eeg94();
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
      double value;
      if (signBit == 0)
        value =
            ByteBuffer.wrap(
                new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
      else {
        int twosComplimentValue =
            ByteBuffer.wrap(
                new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        value = -1 * (Math.pow(2, 24) - twosComplimentValue);
      }
      values[index / 3] = value;
    }

    return values;
  }

  public static byte getChannelMask() {
    return channelMask;
  }

  public static void setChannelMask(byte channelMask) {
    DataPacket.channelMask = channelMask;
  }

  ArrayList<Float> getVoltageValues() {
    return convertedSamples;
  }
}

/** Interface for packets related to device information */
abstract class InfoPacket extends Packet {
  List<Float> convertedSamples = null;
  ArrayList<String> attributes;
}

/** Interface for packets related to device synchronization */
abstract class UtilPacket extends Packet {

  protected ArrayList<Float> convertedSamples;
}

// class Eeg implements DataPacket {}
class Eeg98 extends DataPacket {
  private static int channelNumber = 8;

  @Override
  public void convertData(byte[] byteBuffer) {
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
  }

  @Override
  public String toString() {

    String data = "ExG 8 channel: [";

    for (Float convertedSample : this.convertedSamples) {
      data += convertedSample + " ,";
    }
    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 8;
  }
}

class Eeg94 extends DataPacket {

  private final int channelNumber = 4;
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {
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
  }

  @Override
  public String toString() {

    String data = "ExG 4 channel: [";
    ListIterator<Float> it = this.convertedSamples.listIterator();

    while (it.hasNext()) {
      data += it.next() + " ,";
    }
    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return this.channelNumber;
  }
}

class Eeg99 extends DataPacket {
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

class Eeg99s extends DataPacket {
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer byte array with input data
   */
  @Override
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99s";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class Orientation extends InfoPacket {

  public Orientation() {
    attributes =
        new ArrayList<String>(
            Arrays.asList(
                "Acc_X", "Acc_Y", "Acc_Z", "Mag_X", "Mag_Y", "Mag_Z", "Gyro_X", "Gyro_Y", "Gyro_Z"));
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    List<Float> listValues = new ArrayList<Float>();
    double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);

    for (int index = 0; index < convertedRawValues.length; index++) {
      if (index < 3) {
        listValues.add((float) (convertedRawValues[index] * 0.061));
      } else if (index < 6) {
        listValues.add((float) (convertedRawValues[index] * 8.750));
      } else {
        if (index == 6) {
          listValues.add((float) (convertedRawValues[index] * 1.52 * -1));
        } else {
          listValues.add((float) (convertedRawValues[index] * 1.52));
        }
      }
    }
    this.convertedSamples = new ArrayList<>(listValues);
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

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 9;
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class DeviceInfoPacket extends InfoPacket {
  @Override
  public void convertData(byte[] byteBuffer) {}

  @Override
  public String toString() {
    return "DeviceInfoPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

/**
 * Acknowledgement packet is sent when a configuration command is successfully executed on the
 * device
 */
class AckPacket extends InfoPacket {
  @Override
  public void convertData(byte[] byteBuffer) {}

  @Override
  public String toString() {
    return "AckPacket";
  }

  @Override
  public int getDataCount() {
    return 0;
  }
}

/** Packet sent from the device to sync clocks */
class TimeStampPacket extends UtilPacket {
  @Override
  public void convertData(byte[] byteBuffer) {}

  @Override
  public String toString() {
    return "TimeStampPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
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
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "DisconnectionPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

class Environment extends InfoPacket {
  float temperature, light, battery;

  public Environment(){
    super.attributes = new ArrayList(Arrays.asList("Temperature ", "Light ", "Battery "));
  }
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    List<Float> listValues = new ArrayList<Float>();

    listValues.add((float) ByteBuffer.wrap(new byte[] {byteBuffer[0], 0, 0, 0}).order(ByteOrder.LITTLE_ENDIAN).getInt());
    listValues.add((float) (ByteBuffer.wrap(new byte[] {byteBuffer[1], byteBuffer[2], 0, 0}).order(
        ByteOrder.LITTLE_ENDIAN).getInt()) * (1000 / 4095) );
    float batteryLevelRaw = (float) ((ByteBuffer.wrap(new byte[]{byteBuffer[3], byteBuffer[4], 0, 0})
            .order(ByteOrder.LITTLE_ENDIAN).getInt() * 16.8 / 6.8) * (1.8 / 2457));

    listValues.add(getBatteryParcentage(batteryLevelRaw));
    this.convertedSamples = new ArrayList<>(listValues);
  }

  /** String representation of attributes */
  @Override
  public String toString(){
    String data = "Environment packets: [";

    for (int index = 0; index < convertedSamples.size(); index += 1) {
      if (index % 9 < 3) {
        data += " Temperature: " + convertedSamples.get(index);
      } else if (index % 9 < 6) {
        data += " Light: " + convertedSamples.get(index);
      } else {
        data += "Battery: " + convertedSamples.get(index);
      }

      data += ",";
    }

    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 3;
  }

  float getBatteryParcentage(float voltage){
    double parcentage = 0;
    if (voltage < 3.1){
      parcentage = 1;
    }
    else if (voltage < 3.5){
      parcentage = (1 + (voltage - 3.1) / .4 * 10);
    }

    else if (voltage < 3.8){
      parcentage = 10 + (voltage - 3.5) / .3 * 40;
    }
    else if (voltage < 3.9){
      parcentage = 40 + (voltage - 3.8) / .1 * 20;
    }
    else if (voltage < 4){
      parcentage = 60 + (voltage - 3.9) / .1 * 15;
    }
    else if (voltage < 4.1){
      parcentage = 75 + (voltage - 4.) / .1 * 15;
    }
    else if (voltage < 4.2){
      parcentage = 90 + (voltage - 4.1) / .1 * 10;
    }
    else {
      parcentage = 100;
    }

    return (float) parcentage;
  }
}
