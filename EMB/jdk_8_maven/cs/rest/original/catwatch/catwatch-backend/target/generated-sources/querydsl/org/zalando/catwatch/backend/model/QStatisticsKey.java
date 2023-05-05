package org.zalando.catwatch.backend.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QStatisticsKey is a Querydsl query type for StatisticsKey
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QStatisticsKey extends BeanPath<StatisticsKey> {

    private static final long serialVersionUID = -1139369725L;

    public static final QStatisticsKey statisticsKey = new QStatisticsKey("statisticsKey");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.util.Date> snapshotDate = createDateTime("snapshotDate", java.util.Date.class);

    public QStatisticsKey(String variable) {
        super(StatisticsKey.class, forVariable(variable));
    }

    public QStatisticsKey(Path<? extends StatisticsKey> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatisticsKey(PathMetadata<?> metadata) {
        super(StatisticsKey.class, metadata);
    }

}

