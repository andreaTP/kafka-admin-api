package org.bf2.admin.kafka.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.Future;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue.ValueType;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Types {

    @Schema(description = "Identifier for a Kafka server / broker.")
    public static class Node {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    @Schema(description = "Kafka topic partition")
    public static class Partition {

        @Schema(name = "partition", description = "The partition id, unique among partitions of the same topic")
        @NotNull
        private Integer partitionId;

        @Schema(description = "List of replicas for the partition")
        private List<Node> replicas;

        // InSyncReplicas
        @Schema(description = "List in-sync replicas for this partition.")
        private List<Node> isr;

        @Schema(description = "Node that is the leader for this partition.")
        private Node leader;

        public Integer getPartition() {
            return partitionId;
        }

        public void setPartition(Integer partition) {
            this.partitionId = partition;
        }

        @Deprecated(forRemoval = true)
        /**
         * @return the unique id for the partition among partitions of the same
         *         topic
         * @deprecated use {@link #getPartition()} instead
         */
        @Schema(name = "id", description = "Unique id for the partition (deprecated, use `partition` instead)", deprecated = true)
        public Integer getId() {
            return partitionId;
        }

        @Deprecated(forRemoval = true)
        /**
         * @param id
         *            the unique id for the partition among partitions of the
         *            same topic
         * @deprecated use {@link #setPartition(Integer)} instead
         */
        public void setId(Integer id) {
            this.partitionId = id;
        }

        public List<Node> getReplicas() {
            return replicas;
        }

        public void setReplicas(List<Node> replicas) {
            this.replicas = replicas;
        }

        public List<Node> getIsr() {
            return isr;
        }

        public void setIsr(List<Node> isr) {
            this.isr = isr;
        }

        public Node getLeader() {
            return leader;
        }

        public void setLeader(Node leader) {
            this.leader = leader;
        }
    }

    @Schema(description = "Key value pair indicating possible configuration options for a topic.")
    public static class ConfigEntry {
        @NotBlank
        @Schema(description = "The key indicating what configuration entry you would like to set for the topic.")
        private String key;
        @NotBlank
        @Schema(description = "Value to indicate the setting on the topic configuration entry.")
        private String value;

        public ConfigEntry() {
        }

        public ConfigEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Schema(title = "Root Type for NewTopicInput", description = "Kafka Topic (A feed where records are stored and published)")
    public static class Topic implements Comparable<Topic> {
        // ID
        @Schema(description = "The name of the topic.")
        private String name;

        private Boolean isInternal;

        @Schema(description = "Partitions for this topic.")
        private List<Partition> partitions;

        @Schema(description = "Topic configuration entry.")
        private List<ConfigEntry> config;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(Boolean internal) {
            isInternal = internal;
        }

        public List<Partition> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partition> partitions) {
            this.partitions = partitions;
        }

        public List<ConfigEntry> getConfig() {
            return config;
        }

        public void setConfig(List<ConfigEntry> config) {
            this.config = config;
        }

        @Override
        public int compareTo(Topic topic) {
            return getName().compareTo(topic.getName());
        }
    }

    @Schema(
        name = "TopicSettings",
        title = "Topic Settings",
        description = "The settings that are applicable to this topic. This includes partitions, configuration information, and number of replicas.")
    public static class TopicSettings {

        @Positive
        @Schema(
            description = "Number of partitions for this topic. "
                    + "If not specified, the default for new topics is `1`. "
                    + "Number of partitions may not be reduced when updating existing topics")
        private Integer numPartitions;

        @Schema(description = "Topic configuration entries.")
        private List<@Valid ConfigEntry> config;

        public TopicSettings() {
        }

        public TopicSettings(Integer numPartitions, List<ConfigEntry> config) {
            this.numPartitions = numPartitions;
            this.config = config;
        }

        public Integer getNumPartitions() {
            return numPartitions;
        }

        public void setNumPartitions(Integer numPartitions) {
            this.numPartitions = numPartitions;
        }

        public List<ConfigEntry> getConfig() {
            return config;
        }

        public void setConfig(List<ConfigEntry> config) {
            this.config = config;
        }
    }

    @Schema(name = "NewTopicInput", description = "Input object to create a new topic.")
    public static class NewTopic {
        @NotBlank
        @Schema(description = "The topic name, this value must be unique.")
        private String name;

        @NotNull
        @Valid
        private TopicSettings settings;

        public NewTopic() {
        }

        public NewTopic(String name, TopicSettings settings) {
            this.name = name;
            this.settings = settings;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TopicSettings getSettings() {
            return settings;
        }

        public void setSettings(TopicSettings settings) {
            this.settings = settings;
        }
    }

    @Schema(title = "Root Type for TopicsToResetOffset")
    public static class TopicsToResetOffset {
        @NotNull
        private String topic;

        private List<Integer> partitions;

        public TopicsToResetOffset() {
        }

        public TopicsToResetOffset(String topic, List<Integer> partitions) {
            this.topic = topic;
            this.partitions = partitions;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public List<Integer> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Integer> partitions) {
            this.partitions = partitions;
        }
    }

    @Schema(name = "ConsumerGroupResetOffsetResultItem")
    public static class TopicPartitionResetResult {

        private String topic;
        private Integer partition;
        private Long offset;

        public TopicPartitionResetResult() {
        }

        public TopicPartitionResetResult(String topic, Integer partition, Long offset) {
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public Long getOffset() {
            return offset;
        }

        public void setOffset(Long offset) {
            this.offset = offset;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TopicPartitionResetResult)) {
                return false;
            }
            TopicPartitionResetResult other = (TopicPartitionResetResult) obj;

            return Objects.equals(topic, other.topic)
                    && Objects.equals(partition, other.partition)
                    && Objects.equals(offset, other.offset);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topic, partition, offset);
        }
    }

    @Schema(name = "ConsumerGroupResetOffsetParameters", title = "Root Type for ConsumerGroupResetOffsetParameters")
    public static class ConsumerGroupOffsetResetParameters {

        @Schema(name = "OffsetType")
        public enum OffsetType {
            TIMESTAMP("timestamp"),
            ABSOLUTE("absolute"),
            LATEST("latest"),
            EARLIEST("earliest");

            String value;

            private OffsetType(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }

            @JsonCreator
            public static OffsetType fromString(String value) {
                return Arrays.stream(values())
                             .filter(v -> v.getValue().equals(value))
                             .findFirst()
                             .orElse(null);
            }
        }

        @JsonIgnore
        private String groupId;

        @NotNull
        private OffsetType offset;

        @Schema(
            description = "Value associated with the given `offset`. "
                    + "Not used for `offset` values `earliest` and `latest`. "
                    + "When `offset` is `timestamp` then `value` must be a valid timestamp representing the point in time to reset the consumer group. "
                    + "When `offset` is `absolute` then `value` must be the integer offset to which the consumer group will be reset.")
        private String value;

        private List<@Valid TopicsToResetOffset> topics;

        public ConsumerGroupOffsetResetParameters() {
        }

        public ConsumerGroupOffsetResetParameters(OffsetType offset, String value, List<TopicsToResetOffset> topics) {
            this.offset = offset;
            this.value = value;
            this.topics = topics;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public List<TopicsToResetOffset> getTopics() {
            return topics;
        }

        public void setTopics(List<TopicsToResetOffset> topics) {
            this.topics = topics;
        }

        public OffsetType getOffset() {
            return offset;
        }

        public void setOffset(OffsetType offset) {
            this.offset = offset;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class PageRequest {
        @QueryParam("page")
        @DefaultValue("1")
        @Positive
        @Parameter(description = "Page number")
        private Integer page;

        @QueryParam("size")
        @DefaultValue("10")
        @Positive
        @Parameter(description = "Number of records per page")
        private Integer size;

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

    }

    public static class DeprecatedPageRequest extends PageRequest {
        @Deprecated(forRemoval = true)
        @QueryParam("offset")
        @Min(0)
        @Parameter(description = "Offset of the first record to return, zero-based")
        private Integer offset;

        @Deprecated(forRemoval = true)
        @QueryParam("limit")
        @Parameter(description = "Maximum number of records to return")
        @Positive
        private Integer limit;

        public boolean isDeprecatedFormat() {
            return offset != null || limit != null;
        }

        public Integer getOffset() {
            return offset != null ? offset : 0;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit != null ? limit : 10;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }
    }

    @Schema(name = "SortDirection")
    public enum SortDirectionEnum {
        ASC, DESC;

        @JsonValue
        public String getValue() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static SortDirectionEnum fromString(String input) {
            if (input == null) {
                return ASC;
            } else if ("desc".equalsIgnoreCase(input)) {
                return DESC;
            } else {
                return ASC;
            }
        }
    }

    public static class ConsumerGroupSortParams extends OrderByInput {
        @QueryParam("orderKey")
        @DefaultValue("name")
        private ConsumerGroupOrderKey field;

        public ConsumerGroupSortParams() {
            super(SortDirectionEnum.ASC);
        }

        public ConsumerGroupOrderKey getField() {
            return field;
        }

        public void setField(ConsumerGroupOrderKey field) {
            this.field = field;
        }

        @Override
        protected Enum<?> getDefaultField() {
            return ConsumerGroupOrderKey.NAME;
        }

        @Override
        protected void setField(Enum<?> field) {
            this.setField((ConsumerGroupOrderKey) field);
        }
    }

    public static class ConsumerGroupDescriptionSortParams extends OrderByInput {
        @QueryParam("orderKey")
        @DefaultValue("partition")
        private ConsumerGroupDescriptionOrderKey field;

        public ConsumerGroupDescriptionSortParams() {
            super(SortDirectionEnum.ASC);
        }

        public ConsumerGroupDescriptionSortParams(ConsumerGroupDescriptionOrderKey field, SortDirectionEnum order) {
            super(order, SortDirectionEnum.ASC);
            this.field = field;
        }

        public ConsumerGroupDescriptionOrderKey getField() {
            return field;
        }

        public void setField(ConsumerGroupDescriptionOrderKey field) {
            this.field = field;
        }

        @Override
        protected Enum<?> getDefaultField() {
            return ConsumerGroupDescriptionOrderKey.PARTITION;
        }

        @Override
        protected void setField(Enum<?> field) {
            this.setField((ConsumerGroupDescriptionOrderKey) field);
        }
    }

    public static class TopicSortParams extends OrderByInput {
        @QueryParam("orderKey")
        @DefaultValue("name")
        @Parameter(description = "Order key to sort the topics by.", required = false)
        private TopicOrderKey field;

        public TopicSortParams() {
            super(SortDirectionEnum.ASC);
        }

        public TopicOrderKey getField() {
            return field;
        }

        public void setField(TopicOrderKey field) {
            this.field = field;
        }

        @Override
        protected Enum<?> getDefaultField() {
            return TopicOrderKey.NAME;
        }

        @Override
        protected void setField(Enum<?> field) {
            this.setField((TopicOrderKey) field);
        }
    }

    public static class AclBindingSortParams extends OrderByInput {
        @QueryParam("orderKey")
        @DefaultValue(AclBinding.PROP_PERMISSION)
        private AclBindingOrderKey field;

        public AclBindingSortParams() {
            super(SortDirectionEnum.DESC);
        }

        public AclBindingOrderKey getField() {
            return field;
        }

        public void setField(AclBindingOrderKey field) {
            this.field = field;
        }

        @Override
        protected Enum<?> getDefaultField() {
            return AclBindingOrderKey.PERMISSION;
        }

        @Override
        protected void setField(Enum<?> field) {
            this.setField((AclBindingOrderKey) field);
        }
    }

    public abstract static class OrderByInput {

        private final SortDirectionEnum defaultOrder;

        @QueryParam("order")
        @Parameter(description = "Order items are sorted", required = false)
        private SortDirectionEnum order;

        protected OrderByInput(SortDirectionEnum defaultOrder) {
            this.defaultOrder = defaultOrder;
        }

        protected OrderByInput(SortDirectionEnum order, SortDirectionEnum defaultOrder) {
            this(defaultOrder);
            this.order = order;
        }

        public SortDirectionEnum getOrder() {
            return order;
        }

        public void setOrder(SortDirectionEnum order) {
            this.order = order;
        }

        protected abstract Enum<?> getField();

        protected abstract void setField(Enum<?> field);

        protected abstract Enum<?> getDefaultField();

        public void setDefaultsIfNecessary() {
            if (getField() == null) {
                setField(getDefaultField());
            }

            if (getOrder() == null) {
                setOrder(this.defaultOrder);
            }
        }
    }

    @Schema(description = "A group of Kafka consumers")
    public static class ConsumerGroup {

        @NotBlank
        @Schema(description = "Unique identifier for the consumer group")
        private String groupId;

        private ConsumerGroupState state;

        @NotNull
        @Schema(description = "The list of consumers associated with this consumer group")
        private List<Consumer> consumers;

        private ConsumerGroupMetrics metrics;

        public ConsumerGroup() {
        }

        public ConsumerGroup(String groupId, ConsumerGroupState state, List<Consumer> consumers, ConsumerGroupMetrics metrics) {
            this.groupId = groupId;
            this.state = state;
            this.consumers = consumers;
            this.metrics = metrics;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public ConsumerGroupState getState() {
            return state;
        }

        public void setState(ConsumerGroupState state) {
            this.state = state;
        }

        public List<Consumer> getConsumers() {
            return consumers;
        }

        public void setConsumers(List<Consumer> consumers) {
            this.consumers = consumers;
        }

        public ConsumerGroupMetrics getMetrics() {
            return metrics;
        }

        public void setMetrics(ConsumerGroupMetrics metrics) {
            this.metrics = metrics;
        }
    }

    public static class ConsumerGroupMetrics {
        private Integer laggingPartitions;
        private Integer activeConsumers;
        private Integer unassignedPartitions;

        public ConsumerGroupMetrics() {
        }

        public ConsumerGroupMetrics(Integer laggingPartitions, Integer activeConsumers, Integer unassignedPartitions) {
            this.laggingPartitions = laggingPartitions;
            this.activeConsumers = activeConsumers;
            this.unassignedPartitions = unassignedPartitions;
        }

        public Integer getLaggingPartitions() {
            return laggingPartitions;
        }

        public void setLaggingPartitions(Integer laggingPartitions) {
            this.laggingPartitions = laggingPartitions;
        }

        public Integer getActiveConsumers() {
            return activeConsumers;
        }

        public void setActiveConsumers(Integer activeConsumers) {
            this.activeConsumers = activeConsumers;
        }

        public Integer getUnassignedPartitions() {
            return unassignedPartitions;
        }

        public void setUnassignedPartitions(Integer unassignedPartitions) {
            this.unassignedPartitions = unassignedPartitions;
        }
    }

    @JsonInclude(Include.NON_NULL)
    public static class PagedResponse<T> {
        @NotNull
        private List<T> items;

        @NotNull
        @Schema(description = "Total number of entries in the full result set")
        private Integer total;

        @Schema(description = "Number of entries per page (returned for fetch requests)")
        private Integer size;

        @Schema(description = "Current page number (returned for fetch requests)")
        private Integer page;

        public static <I> Future<PagedResponse<I>> forItems(List<I> items) {
            PageRequest allResults = new PageRequest();
            allResults.setPage(1);
            allResults.setSize(items.size());

            return forPage(allResults, items).map(response -> {
                // Remove paging information when returning a full result set
                response.setPage(null);
                response.setSize(null);
                return response;
            });
        }

        public static <I> Future<PagedResponse<I>> forPage(PageRequest pageRequest, List<I> items) {
            final int offset = (pageRequest.getPage() - 1) * pageRequest.getSize();
            final int total = items.size();

            if (total > 0 && offset >= total) {
                return Future.failedFuture(new InvalidRequestException("Requested pagination incorrect. Beginning of list greater than full list size ("
                        + items.size() + ")"));
            }

            final int pageSize = pageRequest.getSize();
            final int pageNumber = pageRequest.getPage();
            final int offsetEnd = Math.min(pageSize * pageNumber, total);

            PagedResponse<I> response = new PagedResponse<>();
            response.setSize(pageSize);
            response.setPage(pageNumber);
            response.setItems(items.subList(offset, offsetEnd));
            response.setTotal(total);

            return Future.succeededFuture(response);
        }

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

    }

    @JsonInclude(Include.NON_NULL)
    public static class PagedResponseDeprecated<T> extends PagedResponse<T> {
        /**
         * @deprecated
         */
        @Deprecated(forRemoval = true)
        @Schema(deprecated = true, description = "Offset of the first record returned, zero-based")
        private Integer offset;

        /**
         * @deprecated
         */
        @Deprecated(forRemoval = true)
        @Schema(deprecated = true, description = "Maximum number of records to return, from request")
        private Integer limit;

        /**
         * @deprecated
         */
        @Deprecated(forRemoval = true)
        @Schema(deprecated = true, description = "Total number of entries in the full result set")
        private Integer count;

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    @Schema(description = "A list of consumer groups")
    public static class ConsumerGroupList extends PagedResponseDeprecated<ConsumerGroup> {
    }

    @Schema(name = "ConsumerGroupOrderKey")
    public enum ConsumerGroupOrderKey {
        NAME("name");

        String value;

        private ConsumerGroupOrderKey(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static ConsumerGroupOrderKey fromString(String value) {
            return Arrays.stream(values())
                         .filter(v -> v.getValue().equals(value))
                         .findFirst()
                         .orElse(null);
        }
    }

    @Schema(name = "ConsumerGroupDescriptionOrderKey")
    public enum ConsumerGroupDescriptionOrderKey {
        OFFSET("offset"),
        END_OFFSET("endOffset"),
        LAG("lag"),
        PARTITION("partition");

        String value;

        private ConsumerGroupDescriptionOrderKey(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static ConsumerGroupDescriptionOrderKey fromString(String value) {
            return Arrays.stream(values())
                         .filter(v -> v.getValue().equals(value))
                         .findFirst()
                         .orElse(null);
        }
    }

    @Schema(name = "TopicsList", description = "A list of topics.")
    public static class TopicList extends PagedResponseDeprecated<Topic> {
    }

    @Schema(name = "TopicOrderKey")
    public enum TopicOrderKey {
        NAME("name"),
        PARTITIONS("partitions"),
        RETENTION_MS("retention.ms"),
        RETENTION_BYTES("retention.bytes");

        String value;

        private TopicOrderKey(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static TopicOrderKey fromString(String value) {
            return Arrays.stream(values())
                         .filter(v -> v.getValue().equals(value))
                         .findFirst()
                         .orElse(null);
        }
    }

    @Schema(title = "Root Type for ConsumerGroupResetOffsetResult")
    public static class ConsumerGroupResetOffsetResult extends PagedResponse<TopicPartitionResetResult> {
    }

    @Schema(
        name = "AclBindingListPage",
        title = "ACL Binding List Page",
        description = "A page of ACL binding entries")
    public static class AclBindingList extends PagedResponse<AclBinding> {
    }

    @Schema(
        description = "A Kafka consumer is responsible for reading records from one or more topics and one or more partitions of a topic.")
    public static class Consumer {

        @NotNull
        @Schema(description = "Unique identifier for the consumer group to which this consumer belongs.")
        private String groupId;

        @NotNull
        @Schema(description = "The unique topic name to which this consumer belongs")
        private String topic;

        @NotNull
        @Schema(description = "The partition number to which this consumer group is assigned to.")
        private Integer partition;

        @Schema(
            description = "Offset denotes the position of the consumer in a partition.",
            required = true)
        private long offset;

        @Schema(description = "The log end offset is the offset of the last message written to a log.")
        private long logEndOffset;

        @Schema(
            description = "Offset Lag is the delta between the last produced message and the last consumer's committed offset.",
            required = true)
        private long lag;

        @Schema(description = "The member ID is a unique identifier given to a consumer by the coordinator upon initially joining the group.")
        private String memberId;

        public Consumer() {
        }

        public Consumer(String memberId,
                String groupId,
                String topic,
                Integer partition,
                long offset,
                long lag,
                long logEndOffset) {
            this.memberId = memberId;
            this.groupId = groupId;
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.lag = lag;
            this.logEndOffset = logEndOffset;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public long getLag() {
            return lag;
        }

        public void setLag(long lag) {
            this.lag = lag;
        }

        public long getLogEndOffset() {
            return logEndOffset;
        }

        public void setLogEndOffset(long logEndOffset) {
            this.logEndOffset = logEndOffset;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Consumer consumer = (Consumer) o;
            return getOffset() == consumer.getOffset() &&
                    getLag() == consumer.getLag() &&
                    getLogEndOffset() == consumer.getLogEndOffset() &&
                    getGroupId().equals(consumer.getGroupId()) &&
                    // topic can be null in the case if number of consumers is greater than number of partitions
                    Objects.equals(getTopic(), consumer.getTopic()) &&
                    getPartition().equals(consumer.getPartition());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getGroupId(), getTopic(), getPartition(), getOffset(), getLag(), getLogEndOffset());
        }
    }

    @Schema
    public enum AclBindingOrderKey {
        RESOURCE_TYPE(AclBinding.PROP_RESOURCE_TYPE),
        RESOURCE_NAME(AclBinding.PROP_RESOURCE_NAME),
        PATTERN_TYPE(AclBinding.PROP_PATTERN_TYPE),
        PRINCIPAL(AclBinding.PROP_PRINCIPAL),
        OPERATION(AclBinding.PROP_OPERATION),
        PERMISSION(AclBinding.PROP_PERMISSION);

        String value;

        private AclBindingOrderKey(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static AclBindingOrderKey fromString(String value) {
            return Arrays.stream(values())
                         .filter(v -> v.getValue().equals(value))
                         .findFirst()
                         .orElse(null);
        }
    }

    @Schema(title = "ACL Resource Type", nullable = false)
    public enum AclResourceType {
        GROUP,
        TOPIC,
        CLUSTER,
        TRANSACTIONAL_ID
    }

    @Schema(title = "ACL Resource Type Filter")
    public enum AclResourceTypeFilter {
        ANY,
        GROUP,
        TOPIC,
        CLUSTER,
        TRANSACTIONAL_ID
    }

    @Schema(title = "ACL Pattern Type", nullable = false)
    public enum AclPatternType {
        LITERAL,
        PREFIXED
    }

    @Schema(title = "ACL Pattern Type Filter", description = "Use value 'MATCH' to perform pattern matching.")
    public enum AclPatternTypeFilter {
        LITERAL,
        PREFIXED,
        ANY,
        MATCH
    }

    @Schema(title = "ACL Operation", nullable = false)
    public enum AclOperation {
        ALL,
        READ,
        WRITE,
        CREATE,
        DELETE,
        ALTER,
        DESCRIBE,
        DESCRIBE_CONFIGS,
        ALTER_CONFIGS
    }

    @Schema(title = "ACL Operation Filter")
    public enum AclOperationFilter {
        ALL,
        READ,
        WRITE,
        CREATE,
        DELETE,
        ALTER,
        DESCRIBE,
        DESCRIBE_CONFIGS,
        ALTER_CONFIGS,
        ANY
    }

    @Schema(title = "ACL Permission Type", nullable = false)
    public enum AclPermissionType {
        ALLOW,
        DENY
    }

    @Schema(title = "ACL Permission Type Filter")
    public enum AclPermissionTypeFilter {
        ALLOW,
        DENY,
        ANY
    }

    public static class AclBindingFilterParams {
        @QueryParam(AclBinding.PROP_RESOURCE_TYPE)
        @DefaultValue("ANY")
        @Parameter(
            description = "ACL Resource Type Filter",
            examples = {
                @ExampleObject(
                    name = "anything",
                    summary = "Match any ACL binding resource type",
                    value = "ANY"),
                @ExampleObject(
                    name = "group",
                    summary = "Match ACL bindings for consumer groups",
                    value = "GROUP"),
                @ExampleObject(
                    name = "topic",
                    summary = "Match ACL bindings for topics",
                    value = "TOPIC"),
                @ExampleObject(
                    name = "cluster",
                    summary = "Match ACL bindings for the cluster",
                    value = "CLUSTER"),
                @ExampleObject(
                    name = "transactional_id",
                    summary = "Match ACL bindings for transactional IDs",
                    value = "TRANSACTIONAL_ID"),
            })
        private AclResourceTypeFilter resourceType;

        @QueryParam(AclBinding.PROP_RESOURCE_NAME)
        @Parameter(description = "ACL Resource Name Filter")
        private String resourceName;

        @QueryParam(AclBinding.PROP_PATTERN_TYPE)
        @DefaultValue("ANY")
        @Parameter(
            description = "ACL Pattern Type Filter",
            examples = {
                @ExampleObject(
                    name = "anything",
                    summary = "Match any ACL binding pattern type",
                    value = "ANY"),
                @ExampleObject(
                    name = "literal",
                    summary = "Match a literal resource name or `*`",
                    value = "LITERAL"),
                @ExampleObject(
                    name = "prefixed",
                    summary = "Match a prefixed resource name",
                    value = "PREFIXED"),
            })
        private AclPatternTypeFilter patternType;

        @QueryParam(AclBinding.PROP_PRINCIPAL)
        @DefaultValue("")
        @Pattern(regexp = "^User:(\\*|[a-zA-Z0-9_@.-]+)$")
        @Parameter(
            description = "ACL Principal Filter. Either a specific user or the wildcard user `User:*` may be provided.\n"
                    + "- When fetching by a specific user, the results will also include ACL bindings that apply to all users.\n"
                    + "- When deleting, ACL bindings to be delete must match the provided `principal` exactly.",
            examples = {
                @ExampleObject(
                    name = "wildcard",
                    summary = "Match ACL entries that apply to all users",
                    value = "User:*"),
                @ExampleObject(
                    name = "specific",
                    summary = "Match ACL entries for a specific user",
                    value = "User:admin-5a1-0c1"),
            })
        private String principal;

        @QueryParam(AclBinding.PROP_OPERATION)
        @DefaultValue("ANY")
        @Parameter(
            description = "ACL Operation Filter. The ACL binding operation provided should be valid for the resource type in the request, if not `ANY`.",
            examples = {
                @ExampleObject(
                    name = "anything",
                    summary = "Match any ACL binding operation",
                    value = "ANY"),
            })
        private AclOperationFilter operation;

        @QueryParam(AclBinding.PROP_PERMISSION)
        @DefaultValue("ANY")
        @Parameter(
            description = "ACL Permission Type Filter",
            examples = {
                @ExampleObject(
                    name = "anything",
                    summary = "Match any ACL binding permission type",
                    value = "ANY"),
                @ExampleObject(
                    name = "allow",
                    summary = "Match only ACL bindings allowing access",
                    value = "ALLOW"),
                @ExampleObject(
                    name = "deny",
                    summary = "Match only ACL bindings denying access",
                    value = "DENY"),
            })
        private AclPermissionTypeFilter permission;

        public AclResourceTypeFilter getResourceType() {
            return resourceType;
        }

        public void setResourceType(AclResourceTypeFilter resourceType) {
            this.resourceType = resourceType;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public AclPatternTypeFilter getPatternType() {
            return patternType;
        }

        public void setPatternType(AclPatternTypeFilter patternType) {
            this.patternType = patternType;
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public AclOperationFilter getOperation() {
            return operation;
        }

        public void setOperation(AclOperationFilter operation) {
            this.operation = operation;
        }

        public AclPermissionTypeFilter getPermission() {
            return permission;
        }

        public void setPermission(AclPermissionTypeFilter permission) {
            this.permission = permission;
        }

        static <T extends Enum<T>, K extends Enum<K>> K map(T source, Function<String, K> mapper) {
            return mapper.apply(source == null ? "" : source.name());
        }

        @JsonIgnore
        public org.apache.kafka.common.resource.ResourceType getKafkaResourceType() {
            return map(resourceType, org.apache.kafka.common.resource.ResourceType::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.resource.PatternType getKafkaPatternType() {
            return map(patternType, org.apache.kafka.common.resource.PatternType::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.acl.AclOperation getKafkaOperation() {
            return map(operation, org.apache.kafka.common.acl.AclOperation::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.acl.AclPermissionType getKafkaPermissionType() {
            return map(permission, org.apache.kafka.common.acl.AclPermissionType::fromString);
        }

        public org.apache.kafka.common.acl.AclBindingFilter toKafkaBindingFilter() {
            var patternFilter = new ResourcePatternFilter(getKafkaResourceType(), getResourceName(), getKafkaPatternType());
            var principalFilter = this.principal.isBlank() ? null : this.principal;
            var entryFilter = new AccessControlEntryFilter(principalFilter, null, getKafkaOperation(), getKafkaPermissionType());
            return new org.apache.kafka.common.acl.AclBindingFilter(patternFilter, entryFilter);
        }

    }

    @Schema(
        title = "ACL Binding",
        description = "Represents a binding between a resource pattern and an access control entry")
    public static class AclBinding {
        public static final String PROP_RESOURCE_TYPE = "resourceType";
        public static final String PROP_RESOURCE_NAME = "resourceName";
        public static final String PROP_PATTERN_TYPE = "patternType";
        public static final String PROP_PRINCIPAL = "principal";
        public static final String PROP_OPERATION = "operation";
        public static final String PROP_PERMISSION = "permission";

        @NotNull
        private AclResourceType resourceType;

        @NotBlank
        private String resourceName;

        @NotNull
        private AclPatternType patternType;

        @NotBlank
        @Pattern(regexp = "^User:(\\*|[a-zA-Z0-9_@.-]+)$")
        @Schema(
            title = "ACL Principal",
            description = "Identifies the user or service account to which an ACL entry is bound. "
                    + "The literal prefix value of `User:` is required. "
                    + "May be used to specify all users with value `User:*`.",
            example = "User:user-123-abc")
        private String principal;

        @NotNull
        private AclOperation operation;

        @NotNull
        private AclPermissionType permission;

        private static AclBinding fromSource(UnaryOperator<String> source) {
            var binding = new AclBinding();
            binding.setResourceType(Objects.requireNonNullElse(source.apply(PROP_RESOURCE_TYPE), "ANY"));
            binding.setResourceName(source.apply(PROP_RESOURCE_NAME));
            binding.setPatternType(Objects.requireNonNullElse(source.apply(PROP_PATTERN_TYPE), "ANY"));
            binding.setPrincipal(Objects.requireNonNullElse(source.apply(PROP_PRINCIPAL), ""));
            binding.setOperation(Objects.requireNonNullElse(source.apply(PROP_OPERATION), "ANY"));
            binding.setPermission(Objects.requireNonNullElse(source.apply(PROP_PERMISSION), "ANY"));

            return binding;
        }

        @JsonIgnore
        public URI buildUri(UriBuilder builder) {
            builder.queryParam(PROP_RESOURCE_TYPE, resourceType);
            builder.queryParam(PROP_RESOURCE_NAME, resourceName);
            builder.queryParam(PROP_PATTERN_TYPE, patternType);
            builder.queryParam(PROP_PRINCIPAL, principal);
            builder.queryParam(PROP_OPERATION, operation);
            builder.queryParam(PROP_PERMISSION, permission);
            return builder.build();
        }

        public static AclBinding fromKafkaBinding(org.apache.kafka.common.acl.AclBinding kafkaBinding) {
            var binding = new AclBinding();
            binding.setResourceType(kafkaBinding.pattern().resourceType().toString());
            binding.setResourceName(kafkaBinding.pattern().name());
            binding.setPatternType(kafkaBinding.pattern().patternType().toString());
            binding.setPrincipal(kafkaBinding.entry().principal());
            binding.setOperation(kafkaBinding.entry().operation().toString());
            binding.setPermission(kafkaBinding.entry().permissionType().toString());

            return binding;
        }

        @JsonCreator
        public static AclBinding fromJsonObject(JsonNode jsonBinding) {
            return fromSource(fieldName -> Optional.ofNullable(jsonBinding.get(fieldName))
                                                   .filter(Objects::nonNull)
                                                   .filter(JsonNode::isTextual)
                                                   .map(JsonNode::asText)
                                                   .orElse(null));
        }

        public static AclBinding fromJsonObject(JsonObject jsonBinding) {
            return fromSource(fieldName -> Optional.ofNullable(jsonBinding.get(fieldName))
                                                   .filter(Objects::nonNull)
                                                   .filter(value -> value.getValueType() == ValueType.STRING)
                                                   .map(JsonString.class::cast)
                                                   .map(JsonString::getString)
                                                   .orElse(null));
        }

        public org.apache.kafka.common.acl.AclBinding toKafkaBinding() {
            var pattern = new ResourcePattern(getKafkaResourceType(), getResourceName(), getKafkaPatternType());
            var entry = new AccessControlEntry(getPrincipal(), "*", getKafkaOperation(), getKafkaPermissionType());
            return new org.apache.kafka.common.acl.AclBinding(pattern, entry);
        }

        static <T extends Enum<T>, K extends Enum<K>> K map(T source, Function<String, K> mapper) {
            return mapper.apply(source == null ? "" : source.name());
        }

        @JsonIgnore
        public org.apache.kafka.common.resource.ResourceType getKafkaResourceType() {
            return map(resourceType, org.apache.kafka.common.resource.ResourceType::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.resource.PatternType getKafkaPatternType() {
            return map(patternType, org.apache.kafka.common.resource.PatternType::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.acl.AclOperation getKafkaOperation() {
            return map(operation, org.apache.kafka.common.acl.AclOperation::fromString);
        }

        @JsonIgnore
        public org.apache.kafka.common.acl.AclPermissionType getKafkaPermissionType() {
            return map(permission, org.apache.kafka.common.acl.AclPermissionType::fromString);
        }

        public AclResourceType getResourceType() {
            return resourceType;
        }

        public void setResourceType(AclResourceType resourceType) {
            this.resourceType = resourceType;
        }

        private void setResourceType(String resourceType) {
            this.resourceType = AclResourceType.valueOf(resourceType);
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public AclPatternType getPatternType() {
            return patternType;
        }

        public void setPatternType(AclPatternType patternType) {
            this.patternType = patternType;
        }

        private void setPatternType(String patternType) {
            this.patternType = AclPatternType.valueOf(patternType);
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public AclOperation getOperation() {
            return operation;
        }

        public void setOperation(AclOperation operation) {
            this.operation = operation;
        }

        private void setOperation(String operation) {
            this.operation = AclOperation.valueOf(operation);
        }

        public AclPermissionType getPermission() {
            return permission;
        }

        public void setPermission(AclPermissionType permission) {
            this.permission = permission;
        }

        private void setPermission(String permission) {
            this.permission = AclPermissionType.valueOf(permission);
        }
    }

    @JsonInclude(Include.NON_NULL)
    @Schema(name = "Error", description = "General error response")
    public static class Error {
        int code;
        @JsonProperty("error_message")
        String errorMessage;
        @JsonProperty("class")
        @Schema(deprecated = true)
        String className;

        public Error(int code, String errorMessage) {
            this.code = code;
            this.errorMessage = errorMessage;
        }

        public Error() {
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

    }
}
