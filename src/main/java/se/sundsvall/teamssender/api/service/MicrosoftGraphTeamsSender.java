package se.sundsvall.teamssender.api.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;


public class MicrosoftGraphTeamsSender {

    private final GraphServiceClient graphServiceClient;

    public MicrosoftGraphTeamsSender(final GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    @Override
    public void sendEmail(final SendEmailRequest request) {
        try {
            final var sender = request.sender();

            final var message = createMessage();
            message.setFrom(createRecipient(sender.name(), sender.address()));
            message.setSender(createRecipient(sender.name(), sender.address()));
            message.setSubject(request.subject());
            message.setBody(createItemBody(request));

            // Recipient
            message.setToRecipients(List.of(createRecipient(request.emailAddress())));

            // Reply-to
            final var replyTo = ofNullable(sender.replyTo())
                    .filter(StringUtils::isNotBlank)
                    .orElse(sender.address());
            message.setReplyTo(List.of(createRecipient(replyTo)));

            // Attachments
            final var attachments = ofNullable(request.attachments()).orElse(emptyList()).stream()
                    .map(this::createAttachment)
                    .filter(Objects::nonNull)
                    .toList();
            if (!attachments.isEmpty()) {
                message.setAttachments(attachments);
            }

            // Headers
            final var headers = ofNullable(request.headers()).orElse(emptyMap()).entrySet().stream()
                    .map(this::createHeader)
                    .toList();
            if (!headers.isEmpty()) {
                message.setInternetMessageHeaders(headers);
            }

            // Request
            final var requestBody = createSendMailPostRequestBody();
            requestBody.setMessage(message);
            requestBody.setSaveToSentItems(false);

            // Send the e-mail
            graphServiceClient.users()
                    .byUserId(sender.address())
                    .sendMail()
                    .post(requestBody);
        } catch (final Exception e) {
            throw Problem.builder()
                    .withStatus(Status.INTERNAL_SERVER_ERROR)
                    .withDetail("Unable to send e-mail")
                    .build();
        }
    }

    Message createMessage() {
        return new Message();
    }

    SendMailPostRequestBody createSendMailPostRequestBody() {
        return new SendMailPostRequestBody();
    }

    ItemBody createItemBody(final SendEmailRequest request) {
        final var itemBody = new ItemBody();

        // Prioritize/use HTML, if it's set
        if (isNotBlank(request.htmlMessage())) {
            itemBody.setContentType(BodyType.Html);
            itemBody.setContent(new String(Base64.getDecoder().decode(request.htmlMessage()), UTF_8));
        } else {
            itemBody.setContentType(BodyType.Text);
            itemBody.setContent(request.message());
        }

        return itemBody;
    }

    Recipient createRecipient(final String emailAddress) {
        return createRecipient(null, emailAddress);
    }

    Recipient createRecipient(final String name, final String emailAddress) {
        final var address = new EmailAddress();
        address.setAddress(emailAddress);

        // Set the name, if present
        ofNullable(name).ifPresent(address::setName);

        final var recipient = new Recipient();
        recipient.setEmailAddress(address);
        return recipient;
    }

    Attachment createAttachment(final SendEmailRequest.Attachment attachment) {
        if (!BASE64_VALIDATOR.isValid(attachment.content())) {
            return null;
        }
        final var content = Base64.getDecoder().decode(attachment.content());

        final var fileAttachment = new FileAttachment();
        fileAttachment.setName(attachment.name());
        fileAttachment.setContentType(attachment.contentType());
        fileAttachment.setContentBytes(content);
        return fileAttachment;
    }

    InternetMessageHeader createHeader(final Map.Entry<String, List<String>> headerEntry) {
        final var header = new InternetMessageHeader();
        header.setName("X-" + Header.fromString(headerEntry.getKey()).getKey());
        header.setValue(formatHeader(headerEntry.getValue()));
        return header;
    }



}
