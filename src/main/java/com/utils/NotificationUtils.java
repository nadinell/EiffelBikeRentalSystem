package com.utils;

    import com.eiffelbikerental.model.User;
    import com.sendgrid.*;
    import java.io.IOException;

    public class NotificationUtils {
        // SendGrid API key for email, should be stored securely
        private static final String SENDGRID_API_KEY = "";  

        // Send an email using SendGrid
        public static void sendEmail(String to, String subject, String body) throws IOException {
            Email from = new Email(""); 
            Email recipient = new Email(to); // Email of the user
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, recipient, content);

            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send"); // This is the endpoint for sending emails
                request.setBody(mail.build());
                sg.api(request); // Send the email via SendGrid API
                System.out.println("Email sent successfully to: " + to); // Log success
            } catch (IOException e) {
                System.err.println("Error sending email: " + e.getMessage());
                throw e; // Propagate the exception
            }
        }

        // General notification method (send both SMS and email)
        public static void sendNotification(User user, String message) {
            boolean emailSent = false;

            // Send email if the email is available
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    sendEmail(user.getEmail(), "Bike Availability Notification", message);
                    emailSent = true;
                } catch (IOException e) {
                    System.err.println("Error sending email to " + user.getEmail());
                }
            }

            // Log if no email could be sent
            if (!emailSent) {
                if (user.getEmail() == null || user.getEmail().isEmpty()) {
                    System.err.println("User has no valid email address.");
                } else {
                    System.err.println("Failed to send email to: " + user.getEmail());
                }
            }
        }
    }
