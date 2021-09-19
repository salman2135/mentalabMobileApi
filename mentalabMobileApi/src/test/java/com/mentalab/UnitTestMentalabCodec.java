package com.mentalab;

import android.util.Log;
import com.mentalab.MentalabCodec;
import static org.junit.Assert.assertEquals;

import com.mentalab.exception.InvalidDataException;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class UnitTestMentalabCodec {

  @Test(expected=InvalidDataException.class)
  public void nullCheckDecodeIsCorrect() throws InvalidDataException {
      MentalabCodec.decode(null);
  }
}
