/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;

import com.rabbitmq.client.Channel;

/**
 * Used to manually perform a basic reject of the message in flow, allowing fine control of message
 * throttling. It looks for a delivery-tag inbound message property and an amqp.channel session
 * property. If the former is missing, it logs a warning. If the former is present but not the
 * latter, it throws an exception.
 */
public class AmqpMessageRejecter extends AbstractChannelMessageProcessor
{
    private static final Log LOG = LogFactory.getLog(AmqpMessageRejecter.class);
    private static final String CHANNEL_ACTION = "reject";

    protected boolean requeue = false;

    public MuleEvent process(final MuleEvent event) throws MuleException
    {
        reject(event, requeue);
        return event;
    }

    public void setRequeue(final boolean requeue)
    {
        this.requeue = requeue;
    }

    public static void reject(final MuleEvent event, final boolean multiple) throws MuleException
    {
        reject(event.getMessage(), multiple);
    }

    public static void reject(final MuleMessage message, final boolean requeue) throws MuleException
    {
        final Long deliveryTag = getDeliveryTagOrFail(message, CHANNEL_ACTION);
        final Channel channel = getChannelOrFail(message, CHANNEL_ACTION);

        try
        {
            channel.basicReject(deliveryTag, requeue);
        }
        catch (final IOException ioe)
        {
            throw new DefaultMuleException("Failed to reject message w/deliveryTag: " + deliveryTag
                                           + " on channel: " + channel, ioe);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Manually rejected message w/deliveryTag: " + deliveryTag + " on channel: " + channel);
        }
    }
}
