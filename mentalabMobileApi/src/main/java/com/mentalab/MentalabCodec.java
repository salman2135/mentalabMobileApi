package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MentalabCodec {

  private static final String TAG = "Explore";

  /**
   * Decodes a device raw data stream
   *
   * <p>Incoming bytes from Bluetooth are converted to an immutable Map of Queues of Numbers. ExG
   * channels are saved as single precision floating point numbers (Float) in the unit of mVolt.
   * Launches one worker thread on first invocation.
   *
   * @throws InvalidDataException throws when invalid data is received
   * @parameter InputStream of device bytes
   * @return Immutable Map of Queues of Numbers
   */
  public static Map<String, Queue<Float>> decode(InputStream stream) throws InvalidDataException {

    int loop_Count = 0;
    while (loop_Count < 100) {
      int pId = 0;
      try {
        byte[] buffer = new byte[1024];
        // reading PID
        stream.read(buffer, 0, 1);
        pId = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        Log.d(TAG, "pid .." + pId);
        buffer = new byte[1024];

        // reading count
        stream.read(buffer, 0, 1);
        int count = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        buffer = new byte[1024];

        // reading payload
        stream.read(buffer, 0, 2);
        int payload = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        buffer = new byte[1024];

        // reading timestamp
        stream.read(buffer, 0, 4);
        int timeStamp = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

        Log.d(TAG, "pid .." + pId + " payload is : " + payload);

        // reading payload data
        buffer = new byte[payload];
        stream.read(buffer, 0, payload - 4);
        // parsing payload data

        List<Float> voltageValueArray =
            parsePayloadData(pId, Arrays.copyOfRange(buffer, 0, buffer.length - 5));
      } catch (IOException exception) {
        throw new InvalidDataException("Byte data conversion failed", null);
      }
    }
    return null;
  }

  /**
   * Encodes a command
   *
   * @throws InvalidCommandException when the command is not recognized
   * @return byte[] encoded commands that can be sent to the device
   */
  static byte[] encodeCommand(final String command) throws InvalidCommandException {
    return new byte[10]; // Some example while stub
  }

  static List<Float> parsePayloadData(int pId, byte[] byteBuffer) {

    for (Packet.PacketId packetId : Packet.PacketId.values()) {
      if (packetId.getNumVal() == pId && pId == 146) {
        Log.d(TAG, "Converting data for Explore");
        Packet packet = packetId.createInstance();
        if (packet != null) {
          return packet.convertData(byteBuffer);
        }
      }
    }
    return null;
  }
}
