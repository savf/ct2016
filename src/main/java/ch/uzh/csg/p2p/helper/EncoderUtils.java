package ch.uzh.csg.p2p.helper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.ptr.PointerByReference;

import net.tomp2p.audiovideowrapper.Opus;

public class EncoderUtils {

	public static ArrayList<byte[]> byteBufferToByteArray(List<ByteBuffer> byteBufferList) {
		List<byte[]> result = new ArrayList<byte[]>();
		for (ByteBuffer buf : byteBufferList) {
			result.add(buf.array());
		}
		return (ArrayList<byte[]>) result;
	}

	public static ArrayList<ByteBuffer> byteArrayToByteBuffer(List<byte[]> byteArrayList) {
		List<ByteBuffer> result = new ArrayList<ByteBuffer>();
		for (byte[] byteArray : byteArrayList) {
			ByteBuffer temp = ByteBuffer.allocate(byteArray.length);
			temp.put(byteArray);
			temp.flip();
			result.add(temp);
		}
		return (ArrayList<ByteBuffer>) result;
	}

	public static List<ByteBuffer> getByteBufferList(ShortBuffer shortBuffer) {
		IntBuffer error = IntBuffer.allocate(4);
		PointerByReference opusEncoder =
				Opus.INSTANCE.opus_encoder_create(8000, 1, Opus.OPUS_APPLICATION_VOIP, error);
		int read = 0;
		List<ByteBuffer> list = new ArrayList<ByteBuffer>();
		while (shortBuffer.hasRemaining()) {
			ByteBuffer dataBuffer = ByteBuffer.allocate(1024);
			int toRead = Math.min(shortBuffer.remaining(), dataBuffer.remaining());

			read = Opus.INSTANCE.opus_encode(opusEncoder, shortBuffer, 80, dataBuffer, toRead);
			dataBuffer.position(dataBuffer.position() + read);
			dataBuffer.flip();
			list.add(dataBuffer);
			shortBuffer.position(shortBuffer.position() + 80);
		}
		Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
		shortBuffer.flip();
		return list;
	}

	public static ShortBuffer getShortBuffer(List<ByteBuffer> byteBufferList) {
		IntBuffer error = IntBuffer.allocate(4);
		PointerByReference opusDecoder = Opus.INSTANCE.opus_decoder_create(8000, 1, error);

		ShortBuffer shortBuffer = ShortBuffer.allocate(1024 * 1024);
		for (ByteBuffer dataBuffer : byteBufferList) {
			byte[] transferedBytes = new byte[dataBuffer.remaining()];
			dataBuffer.get(transferedBytes);
			int decoded = Opus.INSTANCE.opus_decode(opusDecoder, transferedBytes,
					transferedBytes.length, shortBuffer, 80, 0);
			shortBuffer.position(shortBuffer.position() + decoded);
		}
		shortBuffer.flip();

		Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
		return shortBuffer;
	}

	public static byte[] shortToByte(final short[] input) {
		final int len = input.length;
		final byte[] buffer = new byte[len * 2];
		for (int i = 0; i < len; i++) {
			buffer[(i * 2) + 1] = (byte) (input[i]);
			buffer[(i * 2)] = (byte) (input[i] >> 8);
		}
		return buffer;
	}
}
