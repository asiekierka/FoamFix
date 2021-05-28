package pl.asie.foamfix.coremod.injections.network;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.commons.lang3.Validate;

import java.nio.charset.StandardCharsets;

public class ByteBufUtilsInject {
	public static void writeUTF8String(ByteBuf to, String string) {
		int length = Utf8.encodedLength(string);
		Validate.isTrue(ByteBufUtils.varIntByteCount(length) < 3, "The string is too long for this encoding.");
		ByteBufUtils.writeVarInt(to, length, 2);
		to.writeCharSequence(string, StandardCharsets.UTF_8);
	}
}
