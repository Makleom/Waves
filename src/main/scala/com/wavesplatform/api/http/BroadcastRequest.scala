package com.wavesplatform.api.http
import com.wavesplatform.common.state.ByteStr
import com.wavesplatform.transaction.AssetId.{Asset, Waves}
import com.wavesplatform.transaction.ValidationError.Validation
import com.wavesplatform.transaction.{AssetId, AssetIdStringLength, ValidationError}

trait BroadcastRequest {
  protected def parseBase58(v: String, error: String, maxLength: Int): Validation[ByteStr] =
    if (v.length > maxLength) Left(ValidationError.GenericError(error))
    else ByteStr.decodeBase58(v).toOption.toRight(ValidationError.GenericError(error))

  protected def parseBase58(v: Option[String], error: String, maxLength: Int): Validation[ByteStr] =
    v.fold[Either[ValidationError, ByteStr]](Right(ByteStr(Array.emptyByteArray)))(_v => parseBase58(_v, error, maxLength))

  protected def parseBase58ToOption(v: Option[String], error: String, maxLength: Int): Validation[Option[ByteStr]] =
    v.fold[Either[ValidationError, Option[ByteStr]]](Right(None)) { s =>
      parseBase58(s, error, maxLength).map(b => Option(b))
    }

  protected def parseBase58ToAsset(v: String): Validation[Asset] =
    parseBase58(v, "invalid.assetId", AssetIdStringLength)
      .map(Asset)

  protected def parseBase58ToAssetId(v: Option[String], err: String): Validation[AssetId] =
    parseBase58ToOption(v.filter(_.length > 0), err, AssetIdStringLength)
      .map {
        case Some(str) => Asset(str)
        case None      => Waves
      }
}
