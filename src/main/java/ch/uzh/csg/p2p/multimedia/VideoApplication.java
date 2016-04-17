package ch.uzh.csg.p2p.multimedia;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.audiovideowrapper.H264Wrapper;
import net.tomp2p.audiovideowrapper.VideoData;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

public class VideoApplication extends Application {

	private Webcam webcam;
	private final static ImageView IMG = new ImageView();
	private final static List<BufferedImage> bufferedImages = new ArrayList<BufferedImage>();

	public void initialize() throws LineUnavailableException {
		webcam = Webcam.getDefault();
		Dimension[] d = webcam.getViewSizes();
		// webcam.setViewSize(d[d.length-1]);

		VideoData frameVideo = H264Wrapper.decodeAndPlay(IMG);
		H264Wrapper.recordAndEncode(webcam, frameVideo);
		VideoApplication.launch(VideoApplication.class);
	}

	/*
	 * return imagelist and clear this list after
	 */
	public List<BufferedImage> getVideoData() {
		List<BufferedImage> bis = new ArrayList<BufferedImage>();
		bis.addAll(bufferedImages);
		bufferedImages.clear();
		return bis;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Audio Video Test");
		StackPane root = new StackPane();
		root.getChildren().add(IMG);

		while (H264Wrapper.getH() == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		primaryStage.setScene(new Scene(root, H264Wrapper.getW(), H264Wrapper.getH()));
		primaryStage.show();
	}

	@Override
	public void stop() {
		H264Wrapper.stopRecordeAndEncode();
	}
}
