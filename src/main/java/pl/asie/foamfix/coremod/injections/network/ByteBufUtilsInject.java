/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
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
