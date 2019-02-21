package com.wavesplatform.matcher.util

import java.nio.ByteBuffer

import com.wavesplatform.common.state.ByteStr
import com.wavesplatform.matcher.model.OrderStatus
import com.wavesplatform.transaction.AssetId
import com.wavesplatform.transaction.AssetId.{Asset, Waves}

object Codecs {
  def len(assetId: AssetId): Int = assetId.fold(1)(1 + _.id.arr.length)

  implicit class ByteBufferExt(val b: ByteBuffer) extends AnyVal {
    def putAssetId(assetId: AssetId): ByteBuffer = assetId match {
      case Waves => b.put(0.toByte)
      case Asset(aid) =>
        require(aid.arr.length < Byte.MaxValue, "Asset ID is too long")
        b.put(aid.arr.length.toByte).put(aid.arr)
    }

    def getAssetId: AssetId = b.get() match {
      case 0 => Waves
      case len =>
        val arr = new Array[Byte](len)
        b.get(arr)
        Asset(ByteStr(arr))
    }

    def putFinalOrderStatus(st: OrderStatus): ByteBuffer = st match {
      case OrderStatus.Filled(filled)    => b.put(0.toByte).putLong(filled)
      case OrderStatus.Cancelled(filled) => b.put(1.toByte).putLong(filled)
      case other                         => throw new IllegalArgumentException(s"Can't encode order status $other")
    }

    def getFinalOrderStatus: OrderStatus.Final =
      if (b.get() == 1) OrderStatus.Cancelled(b.getLong) else OrderStatus.Filled(b.getLong)
  }
}
