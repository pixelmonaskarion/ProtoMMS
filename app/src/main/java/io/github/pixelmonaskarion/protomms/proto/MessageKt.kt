// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: io/github/pixelmonaskarion/protomms/proto/proto_mms.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package io.github.pixelmonaskarion.protomms.proto;

@kotlin.jvm.JvmName("-initializemessage")
public inline fun message(block: io.github.pixelmonaskarion.protomms.proto.MessageKt.Dsl.() -> kotlin.Unit): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message =
  io.github.pixelmonaskarion.protomms.proto.MessageKt.Dsl._create(io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `io.github.pixelmonaskarion.protomms.proto.Message`
 */
public object MessageKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message = _builder.build()

    /**
     * `optional string text = 1;`
     */
    public var text: kotlin.String
      @JvmName("getText")
      get() = _builder.getText()
      @JvmName("setText")
      set(value) {
        _builder.setText(value)
      }
    /**
     * `optional string text = 1;`
     */
    public fun clearText() {
      _builder.clearText()
    }
    /**
     * `optional string text = 1;`
     * @return Whether the text field is set.
     */
    public fun hasText(): kotlin.Boolean {
      return _builder.hasText()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class AttachmentsProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     */
     public val attachments: com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getAttachmentsList()
      )
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     * @param value The attachments to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAttachments")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.add(value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment) {
      _builder.addAttachments(value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     * @param value The attachments to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAttachments")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.plusAssign(value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment) {
      add(value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     * @param values The attachments to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllAttachments")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.addAll(values: kotlin.collections.Iterable<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment>) {
      _builder.addAllAttachments(values)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     * @param values The attachments to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllAttachments")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.plusAssign(values: kotlin.collections.Iterable<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment>) {
      addAll(values)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     * @param index The index to set the value at.
     * @param value The attachments to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setAttachments")
    public operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.set(index: kotlin.Int, value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment) {
      _builder.setAttachments(index, value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Attachment attachments = 2;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearAttachments")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment, AttachmentsProxy>.clear() {
      _builder.clearAttachments()
    }


    /**
     * `string message_id = 3;`
     */
    public var messageId: kotlin.String
      @JvmName("getMessageId")
      get() = _builder.getMessageId()
      @JvmName("setMessageId")
      set(value) {
        _builder.setMessageId(value)
      }
    /**
     * `string message_id = 3;`
     */
    public fun clearMessageId() {
      _builder.clearMessageId()
    }

    /**
     * `.io.github.pixelmonaskarion.protomms.proto.Address sender = 4;`
     */
    public var sender: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address
      @JvmName("getSender")
      get() = _builder.getSender()
      @JvmName("setSender")
      set(value) {
        _builder.setSender(value)
      }
    /**
     * `.io.github.pixelmonaskarion.protomms.proto.Address sender = 4;`
     */
    public fun clearSender() {
      _builder.clearSender()
    }
    /**
     * `.io.github.pixelmonaskarion.protomms.proto.Address sender = 4;`
     * @return Whether the sender field is set.
     */
    public fun hasSender(): kotlin.Boolean {
      return _builder.hasSender()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class RecipientsProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     */
     public val recipients: com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getRecipientsList()
      )
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     * @param value The recipients to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addRecipients")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.add(value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address) {
      _builder.addRecipients(value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     * @param value The recipients to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignRecipients")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.plusAssign(value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address) {
      add(value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     * @param values The recipients to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllRecipients")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.addAll(values: kotlin.collections.Iterable<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address>) {
      _builder.addAllRecipients(values)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     * @param values The recipients to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllRecipients")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.plusAssign(values: kotlin.collections.Iterable<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address>) {
      addAll(values)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     * @param index The index to set the value at.
     * @param value The recipients to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setRecipients")
    public operator fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.set(index: kotlin.Int, value: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address) {
      _builder.setRecipients(index, value)
    }
    /**
     * `repeated .io.github.pixelmonaskarion.protomms.proto.Address recipients = 5;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearRecipients")
    public fun com.google.protobuf.kotlin.DslList<io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address, RecipientsProxy>.clear() {
      _builder.clearRecipients()
    }

  }
}
@kotlin.jvm.JvmSynthetic
public inline fun io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message.copy(block: io.github.pixelmonaskarion.protomms.proto.MessageKt.Dsl.() -> kotlin.Unit): io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message =
  io.github.pixelmonaskarion.protomms.proto.MessageKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val io.github.pixelmonaskarion.protomms.proto.ProtoMms.MessageOrBuilder.senderOrNull: io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address?
  get() = if (hasSender()) getSender() else null

