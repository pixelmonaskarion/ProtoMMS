package io.github.pixelmonaskarion.xmlsms;

public class XMLSMS {
    public static final String MESSAGE_PREFIX = "\uD83C\uDDFD\u200B\uD83C\uDDF2\u200B\uD83C\uDDF1\u200B\uD83C\uDDF8\u200B\uD83C\uDDF2\u200B\uD83C\uDDF8";
    public static String createBody(String messageText) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(MESSAGE_PREFIX);
        messageBuilder.append("<Body>");
            messageBuilder.append("<MessageText>");
                messageBuilder.append(messageText);
            messageBuilder.append("</MessageText>");
        messageBuilder.append("</Body>");
        return messageBuilder.toString();
    }
}
