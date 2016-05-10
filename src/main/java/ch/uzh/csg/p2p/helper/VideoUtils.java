package ch.uzh.csg.p2p.helper;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.VideoMessage;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.VideoRequest;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import net.tomp2p.audiovideowrapper.H264Wrapper;
import net.tomp2p.audiovideowrapper.VideoData;
import net.tomp2p.futures.FutureDirect;

public class VideoUtils {
	private static Logger log = LoggerFactory.getLogger(VideoUtils.class);

	private boolean running;
	private boolean mute;
	private Node node;
	private User sender;
	private List<Friend> receiverList;

	private Webcam webcam;
	private VideoData frameVideo;
	private ImageView IMG;

	public VideoUtils(Node node, User sender, Friend receiver) {
		this.node = node;
		this.sender = sender;
		receiverList = new ArrayList<Friend>();
		receiverList.add(receiver);
	}
	
	public VideoUtils(Node node, User sender) {
		this.node = node;
		this.sender = sender;
		receiverList = new ArrayList<Friend>();
	}

	public void startVideo(ImageView imageView) throws LineUnavailableException {
		running = true;
		mute = false;
		IMG = imageView;

		webcam = Webcam.getDefault();
		Dimension[] d = webcam.getViewSizes();
		webcam.setViewSize(d[d.length - 1]);

		frameVideo = H264Wrapper.decodeAndPlay(IMG);
		H264Wrapper.recordAndEncode(webcam, frameVideo);

		H264Encoder encoder = new H264Encoder();
		BufferedImage rgb = webcam.getImage();
		Picture yuv = Picture.create(rgb.getWidth(), rgb.getHeight(), ColorSpace.YUV420);
		ByteBuffer buffer =
				encoder.encodeFrame(yuv, ByteBuffer.allocate(rgb.getWidth() * rgb.getHeight() * 3));
		List<ByteBuffer> byteBufferList = new ArrayList<ByteBuffer>();

		while (running) {
			frameVideo.created(buffer, 1000, H264Wrapper.getW(), H264Wrapper.getH());
			byteBufferList.add(buffer);
			if (byteBufferList.size() > 1) {
				sendVideoData(byteBufferList);
				byteBufferList.clear();
			}
		}
	}

	private void sendVideoData(List<ByteBuffer> byteBufferList) {
		if (!mute) {
			Date date = new Date();
			for (Friend receiver : receiverList) {
				VideoMessage videoMessage =
						new VideoMessage(sender.getUsername(), receiver.getName(), receiver.getPeerAddress(), date,
								EncoderUtils.byteBufferToByteArray(byteBufferList));
				node.getPeer().peer().sendDirect(receiver.getPeerAddress()).object(videoMessage)
						.start();
			}
		}
	}

	public static void playVideo(List<byte[]> byteArray) throws IOException {
		log.info("Play Video");
		log.info(byteArray.size()+"");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray.get(0));
        BufferedImage bf = ImageIO.read(bais);
        
        WritableImage wr = null;
        if (bf != null) {
            wr = new WritableImage(bf.getWidth(), bf.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < bf.getWidth(); x++) {
                for (int y = 0; y < bf.getHeight(); y++) {
                    pw.setArgb(x, y, bf.getRGB(x, y));
                }
            }
        }
 
        //ImageView imView = new ImageView(wr);

		// TODO
	}

	public void endVideo() throws ClassNotFoundException, IOException, LineUnavailableException {
		if(running) {
			running = false;
			for (Friend receiver : receiverList) {
				VideoRequest request = new VideoRequest(RequestType.SEND, RequestStatus.ABORTED,
						receiver.getPeerAddress(), receiver.getName(), sender.getUsername());
				RequestHandler.handleRequest(request, node);
			}
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
