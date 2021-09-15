package com.mentalab;

import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Root packet interface
 */
abstract class Packet {
    private static final String TAG = "Explore";
    private byte[] byteBuffer = null;
    enum PacketId {
        ORIENTATION(13){
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
                return new DisconnectionPacket();
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
            this.value =  value;
        }
        public int getNumVal() {
            return value;
        }

        public abstract Packet createInstance();
    }

    /**
     * Converts binary data stream to human readable voltage values
     * @param byteBuffer
     */
    public abstract List<Float> convertData(byte[] byteBuffer);

    /**
     * String representation of attributes
     */
    public abstract String toString();

    /**
     *String representation of attributes
     */
    static float[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
        if (byteArray.length %3 != 0)
            throw new InvalidDataException("Byte buffer is not read properly", null);
        int arraySize = byteArray.length / 3;
        float[] values = new float[arraySize];

        for(int index = 0; index < byteArray.length; index += 3){
            //byte[] buffer = new byte[3];
            //inputStream.read(buffer, index, 3);
            //int value = ByteBuffer.wrap(new byte[]{byteArray[index], byteArray[index + 1], byteArray[index + 2], 0}).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            int value = (byteArray[index] & 0xff) << 16 | (byteArray[index + 1] & 0xff) << 8 | (byteArray[index + 2] & 0xff);
            values[index / 3] = value;
            Log.d(TAG, "Value from Int2_32 is: " + value);
        }


        return values;
    }

    static float[] bytesToFloats(byte[] bytes) {
        if (bytes.length % Float.BYTES != 0)
            throw new RuntimeException("Illegal length");
        float floats[] = new float[bytes.length / Float.BYTES];
        ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        return floats;
    }
};

/**
 * Interface for different EEG packets
 */
abstract class DataPacket extends Packet {}

/**
 * Interface for packets related to device information
 */
abstract class InfoPacket extends Packet {}

/**
 * Interface for packets related to device synchronization
 */
abstract class  UtilPacket extends Packet {}

//class Eeg implements DataPacket {}
class Eeg98 extends DataPacket {
    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        Log.d("Explore", "calling convert");
        List<Float> values = new ArrayList<Float>();
        try {
            float[] data = Packet.toInt32(byteBuffer);
            for(int index = 0; index < data.length; index++){
                values.add(data[index]);
            }
        } catch (InvalidDataException | IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public String toString() {
        return null;
    }
}

class Eeg94 extends DataPacket {
    /**
     * Converts binary data stream to human readable voltage values
     * @param byteBuffer
     * @return
     */
    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        return new ArrayList<Float>();
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Eeg94";
    }
}

class Eeg99 extends DataPacket {
    /**
     * Converts binary data stream to human readable voltage values
     * @param byteBuffer
     */
    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        return new ArrayList<Float>();
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Eeg99";
    }
}

class Eeg99s extends DataPacket {
    /**
     * Converts binary data stream to human readable voltage values
     * @param byteBuffer
     */
    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        return new ArrayList<Float>();
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Eeg99s";
    }
}

/**
 * Device related information packet to transmit
 * firmware version, ADC mask and sampling rate
 */
class Orientation extends InfoPacket {

    Orientation(){
    }

    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        List<Float> listValues = new ArrayList<Float>();
        float[] values = Packet.bytesToFloats(byteBuffer);
        for(float number: values){
            listValues.add(new Float(number));
        }
        return listValues;
    }

    @Override
    public String toString() {
        return "Orientation";
    }
}

/**
 * Device related information packet to transmit
 * firmware version, ADC mask and sampling rate
 */
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
 * Acknowledgement packet is sent when a configuration
 * command is successfully executed on the device
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

/**
 * Packet sent from the device to sync clocks
 */
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

/**
 * Disconnection packet is sent when the host machine is disconnected
 * from the device
 */
class DisconnectionPacket extends UtilPacket {
    /**
     * Converts binary data stream to human readable voltage values
     * @param byteBuffer
     */
    @Override
    public List<Float> convertData(byte[] byteBuffer) {
        return new ArrayList<Float>();
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "DisconnectionPacket";
    }
}
