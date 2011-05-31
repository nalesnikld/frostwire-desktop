package com.limegroup.gnutella;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.io.IPPortCombo;
import org.limewire.io.IpPort;
import org.limewire.io.NetworkUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.limegroup.gnutella.auth.ContentManager;
import com.limegroup.gnutella.connection.RoutedConnection;
import com.limegroup.gnutella.messagehandlers.InspectionRequestHandler;
import com.limegroup.gnutella.messagehandlers.UDPCrawlerPingHandler;
import com.limegroup.gnutella.messages.FeatureSearchData;
import com.limegroup.gnutella.messages.PingReply;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.messages.PingRequestFactory;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.StaticMessages;
import com.limegroup.gnutella.messages.vendor.HeadPongFactory;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessage;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessageFactory;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.QueryHandlerFactory;
import com.limegroup.gnutella.search.SearchResultHandler;
import com.limegroup.gnutella.settings.ChatSettings;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.MessageSettings;
import com.limegroup.gnutella.util.DataUtils;
import com.limegroup.gnutella.xml.LimeXMLDocumentHelper;
import com.limegroup.gnutella.xml.LimeXMLUtils;

/**
 * This class is the message routing implementation for TCP messages.
 */
@Singleton
public class StandardMessageRouter extends MessageRouterImpl {
    
    private static final Log LOG = LogFactory.getLog(StandardMessageRouter.class);
    
    private final Statistics statistics;

    private final ReplyNumberVendorMessageFactory replyNumberVendorMessageFactory;
    
    @Inject
    public StandardMessageRouter(NetworkManager networkManager,
            QueryRequestFactory queryRequestFactory,
            QueryHandlerFactory queryHandlerFactory,
            HeadPongFactory headPongFactory, PingReplyFactory pingReplyFactory,
            QueryUnicaster queryUnicaster,
            FileManager fileManager, ContentManager contentManager,
            DownloadManager downloadManager, UDPService udpService,
            SearchResultHandler searchResultHandler,
            HostCatcher hostCatcher,
            QueryReplyFactory queryReplyFactory, StaticMessages staticMessages,
            Provider<MessageDispatcher> messageDispatcher,
            MulticastService multicastService, QueryDispatcher queryDispatcher,
            Provider<ActivityCallback> activityCallback,
            ApplicationServices applicationServices,
            @Named("backgroundExecutor")
            ScheduledExecutorService backgroundExecutor,
            Provider<PongCacher> pongCacher,
            GuidMapManager guidMapManager, 
            UDPReplyHandlerCache udpReplyHandlerCache,
            Provider<InspectionRequestHandler> inspectionRequestHandlerFactory,
            Provider<UDPCrawlerPingHandler> udpCrawlerPingHandlerFactory,
            Statistics statistics,
            ReplyNumberVendorMessageFactory replyNumberVendorMessageFactory,
            PingRequestFactory pingRequestFactory, MessageHandlerBinder messageHandlerBinder) {
        super(networkManager, queryRequestFactory, queryHandlerFactory,
                headPongFactory, pingReplyFactory,
                queryUnicaster,
                fileManager, contentManager,
                downloadManager, udpService, searchResultHandler,
                hostCatcher, queryReplyFactory, staticMessages,
                messageDispatcher, multicastService, queryDispatcher,
                activityCallback, applicationServices,
                backgroundExecutor, pongCacher,
                guidMapManager, udpReplyHandlerCache, inspectionRequestHandlerFactory, 
                udpCrawlerPingHandlerFactory, 
                pingRequestFactory, messageHandlerBinder);
        this.statistics = statistics;
        this.replyNumberVendorMessageFactory = replyNumberVendorMessageFactory;
    }
    
    /**
     * Responds to a Gnutella ping with cached pongs. This does special handling
     * for both "heartbeat" pings that were sent to ensure that the connection
     * is still live as well as for pings from a crawler.
     * 
     * @param ping the <tt>PingRequest</tt> to respond to
     * @param handler the <tt>ReplyHandler</tt> to send any pongs to
     */
    @Override
    protected void respondToPingRequest(PingRequest ping,
                                        ReplyHandler handler) {
        
    }

	/**
	 * Responds to a ping request received over a UDP port.  This is
	 * handled differently from all other ping requests.  Instead of
	 * responding with cached pongs, we respond with a pong from our node.
	 *
	 * @param request the <tt>PingRequest</tt> to service
     * @param addr the <tt>InetSocketAddress</tt> containing the IP
     *  and port of the client node
     * @param handler the <tt>ReplyHandler</tt> that should handle any
     *  replies
	 */
    @Override
	protected void respondToUDPPingRequest(PingRequest request, 
										   InetSocketAddress addr,
                                           ReplyHandler handler) {
        
        
	}

    /**
     * Handles the crawler ping of Hops=0 & TTL=2, by sending pongs 
     * corresponding to all its leaves
     * @param m The ping request received
     * @param handler the <tt>ReplyHandler</tt> that should handle any
     *  replies
     */
    private void handleCrawlerPing(PingRequest m, ReplyHandler handler) {
           
    }
    
    @Override
    protected boolean respondToQueryRequest(QueryRequest queryRequest,
                                            byte[] clientGUID,
                                            ReplyHandler handler) {
//        //Only respond if we understand the actual feature, if it had a feature.
//        if(!FeatureSearchData.supportsFeature(queryRequest.getFeatureSelector()))
//            return false;
//                                                
//        // Only send results if we're not busy.  Note that this ignores
//        // queue slots -- we're considered busy if all of our "normal"
//        // slots are full.  This allows some spillover into our queue that
//        // is necessary because we're always returning more total hits than
//        // we have slots available.
//        if(!uploadManager.mayBeServiceable() )  {
//            return false;
//        }
//                                                
//                                                
//        // Ensure that we have a valid IP & Port before we send the response.
//        // Otherwise the QueryReply will fail on creation.
//        if( !NetworkUtils.isValidPort(networkManager.getPort()) ||
//            !NetworkUtils.isValidAddress(networkManager.getAddress()))
//            return false;
//                                                     
//        // Run the local query
//        Response[] responses = fileManager.query(queryRequest);
//        return sendResponses(responses, queryRequest, handler);
        return false;
        
    }

    private boolean sendResponses(Response[] responses, QueryRequest query,
                                 ReplyHandler handler) {
//        // if either there are no responses or, the
//        // response array came back null for some reason,
//        // exit this method
//        if ( (responses == null) || ((responses.length < 1)) )
//            return false;
//
//        // if we cannot service a regular query, only send back results for
//        // application-shared metafiles, if any.
//        if (!uploadManager.isServiceable()) {
//        	
//        	List<Response> filtered = new ArrayList<Response>(responses.length);
//        	for(Response r : responses) {
//        		if (r.isMetaFile() && 
//        				fileManager.isFileApplicationShared(r.getName()))
//        			filtered.add(r);
//        	}
//        	
//        	if (filtered.isEmpty()) // nothing to send..
//        		return false;
//        	
//        	if (filtered.size() != responses.length)
//        		responses = filtered.toArray(new Response[filtered.size()]);
//        }
//        
//        // Here we can do a couple of things - if the query wants
//        // out-of-band replies we should do things differently.  else just
//        // send it off as usual.  only send out-of-band if you can
//        // receive solicited udp AND not servicing too many
//        // uploads AND not connected to the originator of the query
//        if (query.desiresOutOfBandReplies() &&
//            !isConnectedTo(query, handler) && 
//			networkManager.canReceiveSolicited() &&
//            NetworkUtils.isValidAddressAndPort(query.getReplyAddress(), query.getReplyPort())) {
//            
//            // send the replies out-of-band - we need to
//            // 1) buffer the responses
//            // 2) send a ReplyNumberVM with the number of responses
//            if (bufferResponsesForLaterDelivery(query, responses)) {
//                // special out of band handling....
//                InetAddress addr = null;
//                try {
//                    addr = InetAddress.getByName(query.getReplyAddress());
//                } catch (UnknownHostException uhe) {}
//                final int port = query.getReplyPort();
//                
//                if(addr != null) { 
//                    // send a ReplyNumberVM to the host - he'll ACK you if he
//                    // wants the whole shebang
//                    int resultCount = 
//                        (responses.length > 255) ? 255 : responses.length;
//                    final ReplyNumberVendorMessage vm = query.desiresOutOfBandRepliesV3() ?
//                            replyNumberVendorMessageFactory.createV3ReplyNumberVendorMessage(new GUID(query.getGUID()), resultCount) :
//                                replyNumberVendorMessageFactory.createV2ReplyNumberVendorMessage(new GUID(query.getGUID()), resultCount);
//                    udpService.send(vm, addr, port);
//                    if (MessageSettings.OOB_REDUNDANCY.getValue() && 
//                            query.desiresOutOfBandRepliesV3()) {
//                        final InetAddress addrf = addr;
//                        backgroundExecutor.schedule(new Runnable() {
//                            public void run () {
//                                udpService.send(vm, addrf, port);
//                            }
//                        }, 100, TimeUnit.MILLISECONDS);
//                    }
//                    return true;
//                }
//            } else {
//                // else i couldn't buffer the responses due to busy-ness, oh, scrap
//                // them.....
//                return false;                
//            }
//        }
//
//        // send the replies in-band
//        // -----------------------------
//
//        //convert responses to QueryReplies
//        Iterable<QueryReply> iterable = responsesToQueryReplies(responses,
//                                                                  query);
//        //send the query replies
//        try {
//            for(QueryReply queryReply : iterable)
//                sendQueryReply(queryReply);
//        }  catch (IOException e) {
//            // if there is an error, do nothing..
//        }
//        // -----------------------------
//        
//        return true;
        return false;

    }

    /** Returns whether or not we are connected to the originator of this query.
     *  PRE: assumes query.desiresOutOfBandReplies == true
     */
    private final boolean isConnectedTo(QueryRequest query, 
                                        ReplyHandler handler) {
        return query.matchesReplyAddress(handler.getInetAddress().getAddress());
    }

    

    
    /** @return Simply splits the input array into two (almost) equally sized
     *  arrays.
     */
    private Response[][] splitResponses(Response[] in) {
        int middle = in.length/2;
        Response[][] retResps = new Response[2][];
        retResps[0] = new Response[middle];
        retResps[1] = new Response[in.length-middle];
        for (int i = 0; i < middle; i++)
            retResps[0][i] = in[i];
        for (int i = 0; i < (in.length-middle); i++)
            retResps[1][i] = in[i+middle];
        return retResps;
    }

    private void splitAndAddResponses(List<Response[]> addTo, Response[] toSplit) {
        Response[][] splits = splitResponses(toSplit);
        addTo.add(splits[0]);
        addTo.add(splits[1]);
    }

    
}
