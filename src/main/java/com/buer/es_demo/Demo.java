package com.buer.es_demo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.util.ObjectBuilder;
import jakarta.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Demo {

    String serverUrl = "https://localhost:9200";
    String apiKey = "VnVhQ2ZHY0JDZGJrU...";

    // Create the low-level client
    RestClient restClient = RestClient
            .builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[]{
                    new BasicHeader("Authorization", "ApiKey " + apiKey)
            })
            .build();

    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());

    // And create the API client
    ElasticsearchClient esClient = new ElasticsearchClient(transport);


    public void save() throws IOException {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("c1");
        customer1.setTags(new ArrayList<>(){{add("tagA");}});

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("c2");
        customer2.setTags(new ArrayList<>(){{add("tagA"); add("tagB");}});

        Customer customer3 = new Customer();
        customer3.setId(3L);
        customer3.setName("c3");
        customer3.setTags(new ArrayList<>(){{add("tagC"); add("tagB");}});

        List<Customer> customers = new ArrayList<>();
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);

        for (Customer customer : customers) {
            IndexResponse response = esClient.index(i -> i
                    .index("customer")
                    .id(customer.getId().toString())
                    .document(customer)
            );
            System.out.println(response);
        }

        /**
         * 查看customer索引的数据
         * */
        SearchResponse<Customer> response = esClient.search(s -> s
                        .index("customer"),
                Customer.class
        );
        System.out.println(response);

    }

    /**
     * ES的查询是和评分有关的，评分越高，查询结果越靠前
     * 如果查出不需要的值需要关闭评分查询
     * query.constantScore()
     * 这个使用方法在8.9版本我没搞懂
     * */
    public void query() throws IOException {

        //查询 tagA && tagB
        SearchResponse<Customer> search = esClient.search(s -> s
                        .index("customer")
                        .query(q -> q
                                .bool(builder -> builder
                                        .must(must -> must
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagA")
                                                )
                                        ).must(must -> must
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagB")
                                                ))
                                )
                        ),
                Customer.class);
        System.out.println(search);

        //查询 tagA || tagB
        SearchResponse<Customer> search2 = esClient.search(s -> s
                        .index("customer")
                        .query(q -> q
                                .bool(builder -> builder
                                        .should(must -> must
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagA")
                                                )
                                        ).should(must -> must
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagB")
                                                )
                                        ).minimumShouldMatch("1")   //至少满足一个
                                )
                        ),
                Customer.class);
        System.out.println(search2);

        //查询 !tagA && tagB
        SearchResponse<Customer> search3 = esClient.search(s -> s
                        .index("customer")
                        .query(q -> q
                                .bool(builder -> builder
                                        .mustNot(must -> must       //!tagA
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagA")
                                                )
                                        ).must(must -> must
                                                .match(match -> match
                                                        .field("tags")
                                                        .query("tagB")
                                                ))
                                )
                        ),
                Customer.class);
        System.out.println(search3);
    }
}
