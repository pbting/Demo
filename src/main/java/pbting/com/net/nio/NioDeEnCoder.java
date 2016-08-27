package pbting.com.net.nio;

import java.nio.ByteBuffer;

public class NioDeEnCoder {

	public static String getVarStrFromBuf(ByteBuffer buf, int start, int MaxLen) {
		byte[] btemp = new byte[MaxLen - start + 1];
		int avaibleBytes = buf.capacity() - buf.position();
		if (!(avaibleBytes > 0)) {
			return "";
		}
		int len = 0;
		for (int i = start; i < MaxLen; i++) {
			btemp[len] = buf.get();
			if (btemp[len] == 0) {
				break;
			}
			len++;
		}
		return new String(btemp, 0, len);
	}
}
