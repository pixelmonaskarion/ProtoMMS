// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: io/github/pixelmonaskarion/protomms/proto/proto_mms.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package io.github.pixelmonaskarion.protomms.proto;

@kotlin.jvm.JvmName("-initializeaddress")
public inline fun address(block: io.github.pixelmonaskarion.protomms.proto.AddressKt.Dsl.() -> kotlin.Unit): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address =
  io.github.pixelmonaskarion.protomms.proto.AddressKt.Dsl._create(io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `io.github.pixelmonaskarion.protomms.proto.Address`
 */
public object AddressKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address = _builder.build()

    /**
     * `string address = 1;`
     */
    public var address: kotlin.String
      @JvmName("getAddress")
      get() = _builder.getAddress()
      @JvmName("setAddress")
      set(value) {
        _builder.setAddress(value)
      }
    /**
     * `string address = 1;`
     */
    public fun clearAddress() {
      _builder.clearAddress()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address.copy(block: io.github.pixelmonaskarion.protomms.proto.AddressKt.Dsl.() -> kotlin.Unit): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address =
  io.github.pixelmonaskarion.protomms.proto.AddressKt.Dsl._create(this.toBuilder()).apply { block() }._build()

