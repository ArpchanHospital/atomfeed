package org.ict4h.atomfeed.client.api;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import org.ict4h.atomfeed.client.api.data.Event;
import org.ict4h.atomfeed.client.domain.Marker;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AtomFeedClientTest {

    @Test
    public void findsNoUnprocessedEventsWhenTheMostRecentIsProcessed() throws Exception {
        URI feedUri = new URI("http://www.example.com/feed/working");

        Entry latest = new Entry();
        latest.setId("latest");
        Feed feed = getFeed(latest);

        FeedClient client = new AtomFeedClient(getAllFeeds(feedUri, feed), getSingleMarker(feedUri, latest.getId()));

        EventWorker worker = mock(EventWorker.class);
        client.processEvents(feedUri, worker);
        verifyZeroInteractions(worker);
    }

    @Test
    public void findsASingleUnprocessedEvent() throws Exception {
        URI feedUri = new URI("http://www.example.com/feed/working");

        Entry lastProcessed = new Entry();
        lastProcessed.setId("lastProcessed");

        final Entry latest = new Entry();
        latest.setId("latest");

        Feed feed = getFeed(lastProcessed, latest);
        FeedClient client = new AtomFeedClient(getAllFeeds(feedUri, feed), getSingleMarker(feedUri, lastProcessed.getId()));

        EventWorker worker = mock(EventWorker.class);
        client.processEvents(feedUri,worker);
        verify(worker).process(argThat(new ArgumentMatcher<Event>() {
            @Override
            public boolean matches(Object o) {
                return ((Event)o).getId().equals(latest.getId());
            }
        }));
    }

    private Feed getFeed(Entry... entries) {
        ArrayList mutableEntries = new ArrayList();
        mutableEntries.addAll(Arrays.asList(entries));
        Feed feed = new Feed();
        feed.setEntries(mutableEntries);
        return feed;
    }

    private AllFeeds getAllFeeds(URI feedUri, Feed feed) {
        AllFeeds feeds = mock(AllFeeds.class);
        when(feeds.getFor(feedUri)).thenReturn(feed);
        return feeds;
    }

    private AllMarkers getSingleMarker(URI feedUri, String entryId) throws Exception {
        AllMarkers markers = mock(AllMarkers.class);
        when(markers.get(feedUri)).thenReturn(new Marker(feedUri, entryId));
        return markers;
    }
}
