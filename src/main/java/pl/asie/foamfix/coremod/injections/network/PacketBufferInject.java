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
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.nio.charset.StandardCharsets;

public class PacketBufferInject extends PacketBuffer {
	public PacketBufferInject(ByteBuf wrapped) {
		super(wrapped);
	}

	// Idea from Velocity project
	@Override
	public PacketBuffer writeString(String string) {
		int length = Utf8.encodedLength(string);
		if (length > 32767) {
			throw new EncoderException("String too big (was " + length + " bytes encoded, max " + 32767 + ")");
		} else {
			this.writeVarInt(length);
			this.writeCharSequence(string, StandardCharsets.UTF_8);
			return this;
		}
	}
}
