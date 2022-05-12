package org.phoebus.channelfinder;

import static org.phoebus.channelfinder.CFResourceDescriptors.SCROLL_RESOURCE_URI;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@CrossOrigin
@RestController
@RequestMapping(SCROLL_RESOURCE_URI)
@EnableAutoConfiguration
public class ChannelScroll {
    static Logger log = Logger.getLogger(ChannelRepository.class.getName());

    @Value("${elasticsearch.channel.index:channelfinder}")
    private String ES_CHANNEL_INDEX;
    @Value("${elasticsearch.channel.type:cf_channel}")
    private String ES_CHANNEL_TYPE;

    @Autowired
    @Qualifier("indexClient")
    ElasticsearchClient client;

    /**
     * GET method for retrieving a collection of Channel instances, based on a
     * multi-parameter query specifying patterns for tags, property values, and
     * channel names to match against.
     *
     * @param allRequestParams search parameters
     * @return list of all channels
     */
    @GetMapping
    public XmlScroll query(@RequestParam MultiValueMap<String, String> allRequestParams) {
        return search(null, allRequestParams);
    }

    /**
     * GET method for retrieving a collection of Channel instances, based on a
     * multi-parameter query specifying patterns for tags, property values, and
     * channel names to match against.
     *
     * @param scrollId scroll Id
     * @return list of all channels
     */
    @GetMapping("/{scrollId}")
    public XmlScroll query(@PathVariable("scrollId") String scrollId) {
        return search(scrollId, null);
    }

    /**
     * Search for a list of channels based on their name, tags, and/or properties.
     * Search parameters ~name - The name of the channel ~tags - A list of comma
     * separated values ${propertyName}:${propertyValue} -
     * 
     * The query result is sorted based on the channel name ~size - The number of
     * channels to be returned ~from - The starting index of the channel list
     * 
     * @param scrollId scroll ID
     * @param searchParameters - search parameters for scrolling searches
     * @return search scroll
     */
    public XmlScroll search(String scrollId, MultiValueMap<String, String> searchParameters) {
        return null;
//
//        StringBuffer performance = new StringBuffer();
//        long start = System.currentTimeMillis();
//        long totalStart = System.currentTimeMillis();
//
//        RestHighLevelClient client = esService.getSearchClient();
//        start = System.currentTimeMillis();
//        try {
//            SearchRequest searchRequest = new SearchRequest(ES_CHANNEL_INDEX);
//            SearchResponse searchResponse;
//            int size = 100;
//            final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(30L));
//            
//            if(scrollId == null) {
//                BoolQueryBuilder qb = boolQuery();
//                for (Entry<String, List<String>> parameter : searchParameters.entrySet()) {
//                    switch (parameter.getKey()) {
//                    case "~name":
//                        for (String value : parameter.getValue()) {
//                            DisMaxQueryBuilder nameQuery = disMaxQuery();
//                            for (String pattern : value.split("[\\|,;]")) {
//                                nameQuery.add(wildcardQuery("name", pattern.trim()));
//                            }
//                            qb.must(nameQuery);
//                        }
//                        break;
//                    case "~tag":
//                        for (String value : parameter.getValue()) {
//                            DisMaxQueryBuilder tagQuery = disMaxQuery();
//                            for (String pattern : value.split("[\\|,;]")) {
//                                tagQuery.add(wildcardQuery("tags.name", pattern.trim()));
//                            }
//                            qb.must(nestedQuery("tags", tagQuery, ScoreMode.None));
//                        }
//                        break;
//                    case "~size":
//                        Optional<String> maxSize = parameter.getValue().stream().max((o1, o2) -> {
//                            return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
//                        });
//                        if (maxSize.isPresent()) {
//                            size = Integer.valueOf(maxSize.get());
//                        }
//                        break;
//                    default:
//                        DisMaxQueryBuilder propertyQuery = disMaxQuery();
//                        for (String value : parameter.getValue()) {
//                            for (String pattern : value.split("[\\|,;]")) {
//                                propertyQuery
//                                .add(nestedQuery("properties",
//                                        boolQuery().must(matchQuery("properties.name", parameter.getKey().trim()))
//                                        .must(wildcardQuery("properties.value", pattern.trim())),
//                                        ScoreMode.None));
//                            }
//                        }
//                        qb.must(propertyQuery);
//                        break;
//                    }
//                }
//
//                searchRequest.scroll(scroll);
//                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//                searchSourceBuilder.query(qb);
//                searchSourceBuilder.size(size);
//                searchRequest.source(searchSourceBuilder);
//                searchResponse = client.search(searchRequest, RequestOptions.DEFAULT); 
//
//            } else {
//                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
//                scrollRequest.scroll(scroll);
//                searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
//                }
//
//            scrollId = searchResponse.getScrollId();
//            SearchHit[] searchHits = searchResponse.getHits().getHits();
//
//            final ObjectMapper mapper = new ObjectMapper();
//            mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
//            mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
//            List<XmlChannel> result = new ArrayList<XmlChannel>();
//
//            searchResponse.getHits().forEach(hit -> {
//                try {
//                    result.add(mapper.readValue(hit.getSourceAsString(), XmlChannel.class));
//                } catch (Exception e) {
//                    log.log(Level.SEVERE, "Failed to parse result for search : " + searchParameters, e);
//                    //                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    //                      "Failed to parse result for search : " + searchParameters + ", CAUSE: " + e.getMessage(), e);
//                }
//            });   
//
//            if(searchHits.length < size) {
//                ClearScrollRequest clearScrollRequest = new ClearScrollRequest(); 
//                clearScrollRequest.addScrollId(scrollId);
//                ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
//                boolean succeeded = clearScrollResponse.isSucceeded();
//            }
//            
//            XmlScroll scrollResult = new XmlScroll(scrollId, result);
//            return scrollResult;
//        } catch (Exception e) {
//            log.log(Level.SEVERE, "Search failed for: " + searchParameters, e);
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Search failed for: " + searchParameters + ", CAUSE: " + e.getMessage(), e);
//        }
    }
}
