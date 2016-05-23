package ch.uzh.csg.p2p.helper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;

public class AudioUtils {
  public static AudioFormat FORMAT = new AudioFormat(8000.0f, 16, 1, true, true);

  private boolean running;
  private boolean mute;
  private Node node;
  private User sender;
  private List<Friend> receiverList;

  public AudioUtils(Node node, User sender, Friend receiver) {
    this.node = node;
    this.sender = sender;
    receiverList = new ArrayList<Friend>();
    receiverList.add(receiver);
  }

  public AudioUtils(Node node, User sender) {
    this.node = node;
    this.sender = sender;
    receiverList = new ArrayList<Friend>();
  }

  public void startAudio() throws LineUnavailableException {
    running = true;
    mute = false;

    DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);

    final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
    line.open(FORMAT);
    line.start();
    Runnable runner = new Runnable() {
      int bufferSize = (int) FORMAT.getSampleRate() * FORMAT.getFrameSize();
      byte buffer[] = new byte[bufferSize];

      public void run() {
        while (running) {
          final ShortBuffer output = ShortBuffer.allocate(1024 * 1024);
          int count = line.read(buffer, 0, buffer.length);
          for (int i = 0; i < count; i += 2) {
            int b1 = buffer[i + 1] & 0xff;
            int b2 = buffer[i] << 8;
            output.put((short) (b1 | b2));
          }
          output.flip();
          Runnable sendRunnable = new Runnable() {

            public void run() {
              List<ByteBuffer> byteBufferList = EncoderUtils.getByteBufferList(output);
              if (!mute) {
                Date date = new Date();
                for (Friend receiver : receiverList) {
                  AudioMessage audioMessage =
                      new AudioMessage(sender.getUsername(), receiver.getName(),
                          receiver.getPeerAddress(), date,
                          EncoderUtils.byteBufferToByteArray(byteBufferList));
                  MessageRequest request = new MessageRequest(audioMessage, RequestType.SEND);
                  RequestHandler.handleRequest(request, node);
                }
              }
            }
          };
          Thread sendThread = new Thread(sendRunnable);
          sendThread.start();
        }
        line.drain();
        line.flush();
        line.stop();
        line.close();
      }
    };
    Thread captureThread = new Thread(runner);
    captureThread.start();
  }

  public static void playAudio(List<ByteBuffer> byteBufferList) throws LineUnavailableException {

    SourceDataLine speaker = AudioSystem.getSourceDataLine(FORMAT);
    ShortBuffer shortBuffer = EncoderUtils.getShortBuffer(byteBufferList);

    if (!speaker.isOpen()) {
      speaker.open(FORMAT);
    }

    speaker.start();

    short[] shortAudioBuffer = new short[shortBuffer.remaining()];
    shortBuffer.get(shortAudioBuffer);
    byte[] audio = EncoderUtils.shortToByte(shortAudioBuffer);
    speaker.write(audio, 0, audio.length);

    speaker.drain();
    speaker.flush();
    speaker.stop();
    speaker.close();
  }

  public void endAudio() throws ClassNotFoundException, IOException, LineUnavailableException {
    if (running) {
      running = false;
      for (Friend receiver : receiverList) {
        AudioRequest request =
            new AudioRequest(RequestType.SEND, RequestStatus.ABORTED, receiver.getPeerAddress(),
                receiver.getName(), sender.getUsername());
        RequestHandler.handleRequest(request, node);
      }
      receiverList.clear();
    }
  }

  public void mute() {
    mute = true;
  }

  public void unmute() {
    mute = false;
  }

  public void addReceiver(Friend receiver) {
    receiverList.add(receiver);
  }

  public void removeReceiver(Friend receiver) {
    receiverList.remove(receiver);
  }

}
