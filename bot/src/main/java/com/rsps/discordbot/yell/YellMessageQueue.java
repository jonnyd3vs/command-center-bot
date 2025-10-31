package com.rsps.discordbot.yell;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Queue for managing yell messages with Discord rate-limit handling
 */
public class YellMessageQueue {

    private final JDA jda;
    private final BlockingQueue<QueuedMessage> messageQueue;
    private final AtomicBoolean running;
    private Thread processorThread;
    private volatile long rateLimitUntil = 0;

    public YellMessageQueue(JDA jda) {
        this.jda = jda;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(false);
    }

    /**
     * Start the message processor thread
     */
    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        processorThread = new Thread(this::processMessages, "YellMessageQueue-Processor");
        processorThread.setDaemon(true);
        processorThread.start();

        System.out.println("[Yell Queue] Message processor started");
    }

    /**
     * Stop the message processor
     */
    public void stop() {
        running.set(false);
        if (processorThread != null) {
            processorThread.interrupt();
        }
        System.out.println("[Yell Queue] Message processor stopped");
    }

    /**
     * Queue a message to be sent
     */
    public void queueMessage(TextChannel channel, String message) {
        messageQueue.offer(new QueuedMessage(channel, message));
        System.out.println("[Yell Queue] Queued message. Queue size: " + messageQueue.size());
    }

    /**
     * Clear all pending messages
     */
    public int clearMessages() {
        int clearedCount = messageQueue.size();
        messageQueue.clear();
        System.out.println("[Yell Queue] Cleared " + clearedCount + " pending messages");
        return clearedCount;
    }

    /**
     * Get current queue size
     */
    public int getQueueSize() {
        return messageQueue.size();
    }

    /**
     * Process messages from the queue
     */
    private void processMessages() {
        while (running.get()) {
            try {
                // Check if we're rate limited
                long now = System.currentTimeMillis();
                if (now < rateLimitUntil) {
                    long sleepTime = rateLimitUntil - now;
                    System.out.println("[Yell Queue] Rate limited. Sleeping for " + sleepTime + "ms");
                    Thread.sleep(sleepTime);
                    continue;
                }

                // Take message from queue (blocking)
                QueuedMessage queuedMessage = messageQueue.poll();
                if (queuedMessage == null) {
                    Thread.sleep(100); // Sleep briefly if queue is empty
                    continue;
                }

                // Send the message
                try {
                    queuedMessage.channel.sendMessage(queuedMessage.message).queue(
                        success -> {
                            // Message sent successfully
                            System.out.println("[Yell Queue] Message sent successfully. Remaining: " + messageQueue.size());
                        },
                        failure -> {
                            // Handle failure
                            System.err.println("[Yell Queue] Failed to send message: " + failure.getMessage());

                            // Check if it's a rate limit error
                            if (failure instanceof RateLimitedException) {
                                RateLimitedException rateLimitEx = (RateLimitedException) failure;
                                long retryAfter = rateLimitEx.getRetryAfter();

                                System.err.println("[Yell Queue] Rate limited! Retry after: " + retryAfter + "ms");
                                rateLimitUntil = System.currentTimeMillis() + retryAfter;

                                // Re-queue the failed message
                                messageQueue.offer(queuedMessage);
                            } else if (failure.getMessage() != null && failure.getMessage().contains("rate limit")) {
                                // Fallback rate limit detection
                                System.err.println("[Yell Queue] Rate limit detected. Delaying 5 seconds.");
                                rateLimitUntil = System.currentTimeMillis() + 5000;

                                // Re-queue the failed message
                                messageQueue.offer(queuedMessage);
                            }
                        }
                    );

                    // Small delay between messages to avoid hitting rate limits
                    Thread.sleep(1000); // 1 second between messages

                } catch (Exception e) {
                    System.err.println("[Yell Queue] Error sending message: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                if (!running.get()) {
                    break; // Thread was interrupted to stop
                }
            } catch (Exception e) {
                System.err.println("[Yell Queue] Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Represents a queued message
     */
    private static class QueuedMessage {
        final TextChannel channel;
        final String message;

        QueuedMessage(TextChannel channel, String message) {
            this.channel = channel;
            this.message = message;
        }
    }
}
